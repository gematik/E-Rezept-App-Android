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

package de.gematik.ti.erp.app.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.net.toUri
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeEnded
import de.gematik.ti.erp.app.demomode.DemoModeIntentAction.DemoModeStarted
import de.gematik.ti.erp.app.demomode.validateForDemoMode
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken.Companion.toUniversalLinkToken
import de.gematik.ti.erp.app.intent.ExternalAuthUrls.isExternalAuthAllowed
import de.gematik.ti.erp.app.intent.GidResultIntent
import de.gematik.ti.erp.app.intent.SharePrescriptionUrls.isSharePrescriptionAllowed
import de.gematik.ti.erp.app.medicationplan.alarm.REMINDER_NOTIFICATION_INTENT_ACTION
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URI

private fun String.isLikelyIosDeeplink(): Boolean =
    startsWith("itms-apps://") || contains("apps.apple.com") || contains("platform=ios", ignoreCase = true)

@Stable
class IntentHandler(private val context: Context) {
    private val extAuthChannel = Channel<GidResultIntent>(Channel.CONFLATED)
    private val shareChannel = Channel<String>(Channel.CONFLATED)
    private val gidSuccessfulShared = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val extAuthIntent = extAuthChannel.receiveAsFlow()
    val shareIntent = shareChannel.receiveAsFlow()
    val gidSuccessfulIntent: SharedFlow<String> = gidSuccessfulShared.asSharedFlow()

    @Requirement(
        "O.Source_1#7",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Processing of universal link",
        codeLines = 33
    )
    suspend fun propagateIntent(intent: Intent) {
        val base = context as BaseActivity
        val action = intent.action
        val isDemoIntent = !action.isNullOrEmpty() && intent.validateForDemoMode()

        // --- Actions (no URI required) ---
        when (action) {
            DemoModeStarted.name -> if (isDemoIntent) base.setAsDemoMode() else base.cancelDemoMode()
            DemoModeEnded.name -> base.cancelDemoMode()
            REMINDER_NOTIFICATION_INTENT_ACTION -> {
                if (!isDemoIntent) base.cancelDemoMode()
                base.setPendingNavigationToMedicationNotificationScreen()
            }

            else -> if (!isDemoIntent) base.cancelDemoMode()
        }

        val uri = intent.data ?: return
        if (uri.scheme != "https") {
            Napier.w { "Ignoring intent with non-https scheme: ${uri.scheme}" }
            return
        }
        val url = uri.toString()

        Napier.d("Received new intent: $url")

        if (!url.isValidUri()) {
            Napier.w { "Ignoring intent with invalid URI: $url" }
            return
        }

        when {
            url.isExternalAuthAllowed() -> {
                if (URI(url).validateForUniversalLink()) {
                    extAuthChannel.send(
                        GidResultIntent(
                            uriData = url,
                            onSuccess = { data -> gidSuccessfulShared.emit(data) }
                        )
                    )
                } else {
                    // Missing mandatory 'code' and/or 'state' query parameters.
                    // This typically means the external health-insurance app returned
                    // an error response or an incomplete redirect URL.
                    Napier.e {
                        "Dropping external-auth intent – mandatory 'code'/'state' parameters " +
                            "missing in URL: $url"
                    }
                }
            }

            url.isSharePrescriptionAllowed() -> {
                shareChannel.send(url)
            }

            else -> Napier.w { "Received https intent that matches no known handler: $url" }
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
            val baseIntent = Intent(Intent.ACTION_VIEW, redirect.toString().toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // Try to find the single non-browser app that handles this redirect URI
            // (e.g. the health-insurance app). Targeting a specific package bypasses
            // the Android app-chooser entirely.
            val targetPackage = resolveNonBrowserPackage(baseIntent)
            if (targetPackage != null) {
                Napier.d { "Targeting specific package for external auth: $targetPackage" }
                baseIntent.setPackage(targetPackage)
            }
            context.startActivity(baseIntent)
            onSuccess()
        } catch (e: ActivityNotFoundException) {
            Napier.e(e) { "Activity missing, user needs to install the other app" }
            onFailure()
        }
    }

    /**
     * Identifies the single non-browser app that can handle [intent], if exactly one exists.
     *
     * Strategy:
     * 1. Query all activities that can handle [intent].
     * 2. Query all activities that can handle a plain `https://example.com` URL — these are browsers.
     * 3. Remove the browser set from the first set.
     * 4. Return the package name only when exactly **one** non-browser remains, so we never
     *    silently pick the wrong app when multiple health-insurance apps are installed.
     */
    private fun resolveNonBrowserPackage(intent: Intent): String? {
        val pm = context.packageManager

        @Suppress("DEPRECATION")
        fun queryActivities(i: Intent) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(i, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
            } else {
                pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY)
            }

        val candidates = queryActivities(intent)
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first().activityInfo.packageName

        // Identify browser packages by what handles a generic https URL
        val browserCheckIntent = Intent(Intent.ACTION_VIEW, "https://example.com".toUri()).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        val browserPackages = queryActivities(browserCheckIntent)
            .map { it.activityInfo.packageName }
            .toSet()

        val nonBrowserApps = candidates.filter {
            it.activityInfo.packageName !in browserPackages
        }

        return if (nonBrowserApps.size == 1) {
            nonBrowserApps.first().activityInfo.packageName
        } else {
            Napier.d {
                "Cannot auto-select target package: ${nonBrowserApps.size} non-browser " +
                    "candidates found for ${intent.data}"
            }
            null
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
        // no-op for shared flow; we keep last success for late subscribers
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

val LocalIntentHandler =
    staticCompositionLocalOf<IntentHandler> { error("No intent handler provided!") }
