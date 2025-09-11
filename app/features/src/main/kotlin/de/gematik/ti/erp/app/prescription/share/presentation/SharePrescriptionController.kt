/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.prescription.share.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.intent.SharePrescriptionUrls.GematikErp
import de.gematik.ti.erp.app.intent.SharePrescriptionUrls.isSharePrescriptionAllowed
import de.gematik.ti.erp.app.prescription.model.PrescriptionLink
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import org.kodein.di.compose.rememberInstance
import java.net.URISyntaxException

@Stable
class SharePrescriptionController(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val context: Context,
    private val profileId: ProfileIdentifier? = null
) {
    enum class HandleResult {
        TaskAlreadyExists, TaskSaved, Failure
    }

    fun share(taskId: String, accessCode: String, name: String?) {
        val payload = buildString {
            append(taskId)
            append('|')
            append(accessCode)
            if (!name.isNullOrBlank()) {
                append('|')
                append(name)
            }
        }
        // Wrap in quotes and square brackets to match your expected format
        val encoded = Uri.encode("[\"$payload\"]", Charsets.UTF_8.name())

        val uri = GematikErp.toUri().buildUpon().encodedFragment(encoded).build()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, uri.toString())
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    /**
     * Handles an incoming share intent.
     *
     * URI pattern is .../prescription/#["TASK_ID|ACCESS_CODE"|"NAME"]
     */
    @Requirement(
        "O.Source_1#9",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Uri is validated for task-id and access-code pattern before sharing"
    )
    suspend fun handle(value: String): List<HandleResult> =
        if (value.isSharePrescriptionAllowed()) {
            try {
                requireNotNull(profileId) { "Profile id not provided" }
                val existingTasksInDatabase =
                    prescriptionUseCase.getAllTasksWithTaskIdOnly().first()
                val prescriptionLinks = extractPrescriptions(value)

                val result = prescriptionLinks.map { prescriptionLink ->
                    val isValidTaskId =
                        TwoDCodeValidator.taskIdPattern.matches(prescriptionLink.taskId)
                    val isValidAccessCode =
                        TwoDCodeValidator.accessCodePattern.matches(prescriptionLink.accessCode)

                    when {
                        !isValidAccessCode || !isValidTaskId -> HandleResult.Failure
                        prescriptionLink.taskId in existingTasksInDatabase -> HandleResult.TaskAlreadyExists
                        else -> {
                            val medicationString = when {
                                prescriptionLink.name.isNullOrBlank() -> context.getString(R.string.pres_details_scanned_medication)
                                else -> prescriptionLink.name
                            }
                            prescriptionUseCase.saveScannedTasks(
                                profileId = profileId,
                                tasks = listOf(
                                    ScannedTaskData.ScannedTask(
                                        profileId = profileId,
                                        index = 0,
                                        name = prescriptionLink.name ?: "",
                                        taskId = prescriptionLink.taskId,
                                        accessCode = prescriptionLink.accessCode,
                                        scannedOn = Clock.System.now(),
                                        redeemedOn = null
                                    )
                                ),
                                medicationString = medicationString
                            )

                            HandleResult.TaskSaved
                        }
                    }
                }
                result
            } catch (e: URISyntaxException) {
                Napier.e(e) { "Invalid uri" }
                listOf(HandleResult.Failure)
            } catch (e: IllegalArgumentException) {
                Napier.e(e) { "Invalid task id or access code pattern" }
                listOf(HandleResult.Failure)
            } catch (e: IndexOutOfBoundsException) {
                Napier.e(e) { "Error parsing input parameter" }
                listOf(HandleResult.Failure)
            }
        } else {
            listOf(HandleResult.Failure)
        }

    private fun extractPrescriptions(url: String): List<PrescriptionLink> {
        val uri = url.toUri()

        // 1) Get the payload in *encoded* form: prefer fragment; else tail after "/prescription"
        val encodedPayload = uri.encodedFragment?.takeIf { it.isNotEmpty() }
            ?: run {
                val encodedPath = uri.encodedPath ?: return emptyList()
                val marker = "/prescription"
                val idx = encodedPath.indexOf(marker)
                if (idx < 0) return emptyList()
                var start = idx + marker.length
                // tolerate an extra '/' or '#' after "prescription"
                if (start < encodedPath.length && (encodedPath[start] == '/' || encodedPath[start] == '#')) {
                    start++
                }
                if (start >= encodedPath.length) return emptyList()
                encodedPath.substring(start) // e.g. "%5B%22...%22%5D"
            }

        // 2) Decode once to get the raw JSON-like text
        val decoded = Uri.decode(encodedPayload)
        // decoded can be: ["task|code|name", "task|code"]  OR  "task|code|name" (single, no brackets)

        // 3) Pull out quoted items if present; otherwise treat whole decoded as one item
        val quoted = "\"([^\"]+)\"".toRegex().findAll(decoded).map { it.groupValues[1] }.toList()
        val rawItems = if (quoted.isNotEmpty()) quoted else listOf(decoded)

        // 4) Map to PrescriptionLink (2 or 3 fields supported)
        return rawItems.mapNotNull { raw ->
            val parts = raw.split("|", limit = 3)
            when (parts.size) {
                2 -> PrescriptionLink(taskId = parts[0], accessCode = parts[1], name = null)
                3 -> PrescriptionLink(taskId = parts[0], accessCode = parts[1], name = parts[2])
                else -> null // malformed
            }
        }
    }
}

@Composable
fun rememberSharePrescriptionController(
    profileId: ProfileIdentifier
): SharePrescriptionController {
    val context = LocalContext.current
    val prescriptionUseCase by rememberInstance<PrescriptionUseCase>()
    return remember(profileId) {
        SharePrescriptionController(
            context = context,
            prescriptionUseCase = prescriptionUseCase,
            profileId = profileId
        )
    }
}

@Composable
fun SharePrescriptionHandler(
    activeProfile: ProfilesUseCaseData.Profile,
    authenticationModeAndMethod: Flow<AuthenticationModeAndMethod>
) {
    val controller = rememberSharePrescriptionController(activeProfile.id)
    val intentHandler = LocalIntentHandler.current
    val snackbar = LocalSnackbar.current

    val taskAdded = stringResource(R.string.share_import_prescription_added)
    val taskExists = stringResource(R.string.share_import_prescription_exists)
    val otherFailure = stringResource(R.string.share_import_error)

    LaunchedEffect(controller) {
        authenticationModeAndMethod.collectLatest { auth ->
            if (auth is AuthenticationModeAndMethod.Authenticated) {
                intentHandler.shareIntent.collect { intent ->
                    try {
                        val shareResults = controller.handle(intent)
                        val hasFailure =
                            shareResults.contains(SharePrescriptionController.HandleResult.Failure)
                        val existingTask =
                            shareResults.contains(SharePrescriptionController.HandleResult.TaskAlreadyExists)
                        val savedSuccessfully =
                            shareResults.contains(SharePrescriptionController.HandleResult.TaskSaved)
                        val allSavedSuccessfully = savedSuccessfully && !hasFailure && !existingTask
                        when {
                            hasFailure -> snackbar.show(otherFailure)
                            existingTask -> snackbar.show(taskExists)
                            allSavedSuccessfully -> snackbar.show(taskAdded)
                        }
                    } catch (e: Exception) {
                        Napier.e(e) { "Error while handling share intent" }
                        snackbar.show(otherFailure)
                    }
                }
            }
        }
    }
}
