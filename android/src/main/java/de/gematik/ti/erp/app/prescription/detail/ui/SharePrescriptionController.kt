/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.detail.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.utils.compose.createToastShort
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import org.kodein.di.LazyDelegate
import org.kodein.di.compose.rememberInstance
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.time.Instant

private const val ShareBaseUri = "https://das-e-rezept-fuer-deutschland.de/prescription/#"

@Stable
class SharePrescriptionController(
    prescriptionUseCase: LazyDelegate<PrescriptionUseCase>,
    private val context: Context,
    private val profileId: ProfileIdentifier? = null
) {
    enum class HandleResult {
        TaskAlreadyExists, TaskSaved, Failure
    }

    private val prescriptionUseCase by prescriptionUseCase

    fun share(taskId: String, accessCode: String) {
        val uri = URI(ShareBaseUri + URLEncoder.encode("[\"$taskId|$accessCode\"]", Charsets.UTF_8.name()))

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, uri.toString())
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    /**
     * Handles an incoming share intent.
     *
     * URI pattern is .../prescription/#["TASK_ID|ACCESS_CODE"]
     */
    suspend fun handle(value: String): HandleResult =
        if (value.startsWith(ShareBaseUri)) {
            try {
                requireNotNull(profileId) { "Profile id not provided" }

                val validUri = URI(value)
                val (taskId, accessCode) = validUri.fragment
                    .removeSurrounding("[\"", "\"]")
                    .split("|", limit = 2)

                require(TwoDCodeValidator.taskIdPattern.matches(taskId)) { "Invalid task id" }
                require(TwoDCodeValidator.accessCodePattern.matches(accessCode)) { "Invalid access code" }

                val allTaskIds = prescriptionUseCase.getAllTasksWithTaskIdOnly().first()
                if (taskId in allTaskIds) {
                    HandleResult.TaskAlreadyExists
                } else {
                    prescriptionUseCase.saveScannedTasks(
                        profileId = profileId,
                        tasks = listOf(
                            ScannedTaskData.ScannedTask(
                                profileId = profileId,
                                taskId = taskId,
                                accessCode = accessCode,
                                scannedOn = Instant.now(),
                                redeemedOn = null
                            )
                        )
                    )

                    HandleResult.TaskSaved
                }
            } catch (e: URISyntaxException) {
                Napier.e(e) { "Invalid uri" }
                HandleResult.Failure
            } catch (e: IllegalArgumentException) {
                Napier.e(e) { "Invalid task id or access code pattern" }
                HandleResult.Failure
            } catch (e: IndexOutOfBoundsException) {
                Napier.e(e) { "Error parsing input parameter" }
                HandleResult.Failure
            }
        } else {
            HandleResult.Failure
        }
}

@Composable
fun rememberSharePrescriptionController(): SharePrescriptionController {
    val context = LocalContext.current
    val prescriptionUseCase = rememberInstance<PrescriptionUseCase>()
    return remember {
        SharePrescriptionController(
            context = context,
            prescriptionUseCase = prescriptionUseCase
        )
    }
}

@Composable
fun rememberSharePrescriptionController(
    profileId: ProfileIdentifier
): SharePrescriptionController {
    val context = LocalContext.current
    val prescriptionUseCase = rememberInstance<PrescriptionUseCase>()
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
    authenticationModeAndMethod: Flow<AuthenticationModeAndMethod>
) {
    val activeProfile = LocalProfileHandler.current.activeProfile
    val controller = rememberSharePrescriptionController(activeProfile.id)
    val intentHandler = LocalIntentHandler.current
    val context = LocalContext.current

    val taskAdded = stringResource(R.string.share_import_prescription_added)
    val taskExists = stringResource(R.string.share_import_prescription_exists)
    val otherFailure = stringResource(R.string.share_import_error)

    LaunchedEffect(controller) {
        // TODO: maybe handle all intents only after authentication
        authenticationModeAndMethod.collectLatest { auth ->
            if (auth is AuthenticationModeAndMethod.Authenticated) {
                intentHandler.shareIntent.collect { intent ->
                    when (controller.handle(intent)) {
                        SharePrescriptionController.HandleResult.TaskAlreadyExists ->
                            createToastShort(context, taskExists)

                        SharePrescriptionController.HandleResult.TaskSaved ->
                            createToastShort(context, taskAdded)

                        SharePrescriptionController.HandleResult.Failure ->
                            createToastShort(context, otherFailure)
                    }
                }
            }
        }
    }
}
