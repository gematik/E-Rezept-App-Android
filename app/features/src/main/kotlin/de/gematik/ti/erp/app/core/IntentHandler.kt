/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.net.toUri
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeEnded
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeStarted
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken.Companion.toUniversalLinkToken
import de.gematik.ti.erp.app.medicationplan.worker.REMINDER_NOTIFICATION_INTENT_ACTION
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URI

const val ExternalAppAuthenticationBaseUri = "https://das-e-rezept-fuer-deutschland.de/extauth"
const val WwwExternalAppAuthenticationBaseUri = "https://www.das-e-rezept-fuer-deutschland.de/extauth"
const val ShareBaseUri = "https://das-e-rezept-fuer-deutschland.de/prescription"

private fun String.isLikelyIosDeeplink(): Boolean =
    startsWith("itms-apps://") || contains("apps.apple.com") || contains("platform=ios", ignoreCase = true)

data class GidResultIntent(
    val uriData: String,
    val resultChannel: Channel<String>
)

@Stable
class IntentHandler(private val context: Context) {
    private val extAuthChannel = Channel<GidResultIntent>(Channel.CONFLATED)
    private val shareChannel = Channel<String>(Channel.CONFLATED)
    private val gidSuccessfulChannel = Channel<String>(Channel.CONFLATED)

    val extAuthIntent = extAuthChannel.receiveAsFlow()
    val shareIntent = shareChannel.receiveAsFlow()
    val gidSuccessfulIntent = gidSuccessfulChannel.receiveAsFlow()

    @Requirement(
        "O.Source_1#7",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Processing of universal link",
        codeLines = 7
    )
    @Suppress("NestedBlockDepth")
    suspend fun propagateIntent(intent: Intent) {
        if (intent.validateForDemoMode()) {
            if (intent.action == DemoModeStarted.name) {
                (context as BaseActivity).setAsDemoMode()
                (context).analytics.init(context)
            } else if (intent.action == DemoModeEnded.name) {
                (context as BaseActivity).cancelDemoMode()
            }
        } else {
            (context as BaseActivity).cancelDemoMode()
            (context).analytics.init(context)
            if (intent.action == REMINDER_NOTIFICATION_INTENT_ACTION) {
                (context).shouldShowMedicationSuccess()
            }
        }

        intent.data?.let {
            val data = it.toString()
            Napier.d("Received new intent: $data")

            if (data.isValidUri()) {
                when {
                    data.startsWith(ExternalAppAuthenticationBaseUri) || data.startsWith(
                        WwwExternalAppAuthenticationBaseUri
                    ) -> {
                        if (URI(data).validateForUniversalLink()) {
                            extAuthChannel.send(
                                GidResultIntent(
                                    uriData = data,
                                    resultChannel = gidSuccessfulChannel
                                )
                            )
                        }
                    }

                    data.startsWith(ShareBaseUri) -> {
                        shareChannel.send(data)
                    }
                }
            }
        }
    }

    @Requirement(
        "O.Auth_4#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Start the external app."
    )
    fun tryStartingExternalHealthInsuranceAuthenticationApp(
        redirect: URI,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        try {
            clear() // clear possible cached values
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirect.toString())))
            onSuccess()
        } catch (e: ActivityNotFoundException) {
            Napier.e { "Activity missing, user needs to install the other app" }
            onFailure()
        }
    }

    @Requirement(
        "O.Auth_4#7",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "App must initiate an external authentication flow using a provided redirect URI (deepLink)."
    )
    fun tryStartingExternalApp(
        deepLink: String,
        onIosDeeplink: () -> Unit
    ) {
        try {
            if (!deepLink.isValidUri()) return

            if (deepLink.isLikelyIosDeeplink()) {
                onIosDeeplink()
                return
            }

            val intent = if (deepLink.startsWith("intent://", ignoreCase = true)) {
                Intent.parseUri(deepLink, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_VIEW, deepLink.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Napier.e(e) { "Activity not found for deepLink: $deepLink" }
        } catch (e: Exception) {
            Napier.e(e) { "Failed to open deepLink: $deepLink" }
        }
    }

    private fun clear() {
        extAuthChannel.tryReceive()
        shareChannel.tryReceive()
        gidSuccessfulChannel.tryReceive()
    }
}

@Requirement(
    "O.Source_1#6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "All parameters are mandatory for the universal link token are checked before it is sent for processing",
    codeLines = 2
)
fun URI.validateForUniversalLink(): Boolean = this.toUniversalLinkToken() != null

@Requirement(
    "O.Source_1#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "External uri is validated to be a valid uri",
    codeLines = 7
)
private fun String.isValidUri(): Boolean = try {
    URI(this)
    true
} catch (e: Exception) {
    false
}

@Requirement(
    "O.Source_1#8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Demo mode is started or ended with an intent and it is also checked that no other data is present in the intent."
)
fun Intent.validateForDemoMode(): Boolean =
    scheme == null && extras == null &&
        (action == DemoModeIntentAction.DemoModeStarted.name || action == DemoModeIntentAction.DemoModeEnded.name)

val LocalIntentHandler =
    staticCompositionLocalOf<IntentHandler> { error("No intent handler provided!") }
