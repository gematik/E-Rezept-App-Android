/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.analytics

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.edit
import androidx.navigation.NavHostController
import de.gematik.ti.erp.app.core.LocalAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.aakira.napier.Napier
import java.security.MessageDigest

private const val TrackerName = "Tracker"

// `gemSpec_eRp_FdV A_20187`
class Analytics constructor(
    private val context: Context
) {
    private val _trackingAllowed = MutableStateFlow(false)
    val trackingAllowed: StateFlow<Boolean>
        get() = _trackingAllowed

    private val prefsName = "pro.piwik.sdk_" +
        MessageDigest.getInstance("MD5").digest(TrackerName.toByteArray())
            .joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }

    init {
        Napier.d("Init tracker")

        _trackingAllowed.value = !context.getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).getBoolean("tracker.optout", true)
    }

    fun allowTracking() {
        _trackingAllowed.value = true

        context.getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).let { prefs ->
            prefs.edit {
                putBoolean("tracker.optout", false)
            }
        }

        Napier.d("Tracking allowed")
    }

    fun disallowTracking() {
        _trackingAllowed.value = false

        context.getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).let { prefs ->
            prefs.edit {
                putBoolean("tracker.optout", true)
            }
        }

        Napier.d("Tracking disallowed")
    }

    @Suppress("UnusedPrivateMember")
    fun trackScreen(path: String) {
        // noop
    }

    fun trackIdentifiedWithIDP() {
        // noop
    }

    enum class AuthenticationProblem {
        CardBlocked,
        CardAccessNumberWrong,
        CardCommunicationInterrupted,
        CardPinWrong,
        IDPCommunicationFailed,
        IDPCommunicationInvalidCertificate,
        IDPCommunicationInvalidOCSPOfCard,
        SecureElementCryptographyFailed,
        UserNotAuthenticated
    }

    @Suppress("UnusedPrivateMember")
    fun trackAuthenticationProblem(kind: AuthenticationProblem) {
        // noop
    }

    fun trackSaveScannedPrescriptions() {
        // noop
    }
}

@Composable
fun TrackNavigationChanges(navController: NavHostController) {
    val tracker = LocalAnalytics.current

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            try {
                tracker.trackScreen(Uri.parse(it.destination.route).buildUpon().clearQuery().build().toString())
            } catch (expected: Exception) {
                Napier.e("Couldn't track navigation screen", expected)
            }
        }
    }
}
