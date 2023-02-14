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
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.LocalAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.aakira.napier.Napier
import java.security.MessageDigest

private const val PrefsName = "analyticsAllowed"

// `gemSpec_eRp_FdV A_20187`
class Analytics constructor(
    private val context: Context,
    private val prefs: SharedPreferences
) {
    private val _analyticsAllowed = MutableStateFlow(false)
    val analyticsAllowed: StateFlow<Boolean>
        get() = _analyticsAllowed

    // TODO remove in future versions
    private val piwikPrefsName = "pro.piwik.sdk_" +
        MessageDigest.getInstance("MD5").digest("Tracker".toByteArray())
            .joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }

    init {
        Napier.d("Init Analytics")

        Contentsquare.forgetMe()

        // TODO remove in future versions
        val piwikOptOut = !context.getSharedPreferences(
            piwikPrefsName,
            Context.MODE_PRIVATE
        ).getBoolean("tracker.optout", true)

        _analyticsAllowed.value = prefs.getBoolean(PrefsName, !piwikOptOut)
        if (_analyticsAllowed.value) {
            allowTracking()
        } else {
            disallowTracking()
        }
    }

    fun tagScreen(screenName: String) {
        if (analyticsAllowed.value) {
            Contentsquare.send(screenName)
            Napier.d("Analytics send $screenName")
        }
    }

    fun allowTracking() {
        _analyticsAllowed.value = true

        Contentsquare.optIn(context)

        prefs.edit {
            putBoolean(PrefsName, true)
        }

        Napier.d("Analytics allowed")
    }

    fun disallowTracking() {
        _analyticsAllowed.value = false

        Contentsquare.optOut(context)

        prefs.edit {
            putBoolean(PrefsName, false)
        }

        Napier.d("Analytics disallowed")
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
    val analytics = LocalAnalytics.current

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            try {
                analytics.tagScreen(Uri.parse(it.destination.route).buildUpon().clearQuery().build().toString())
            } catch (expected: Exception) {
                Napier.e("Couldn't track navigation screen", expected)
            }
        }
    }
}
fun Analytics.trackAuth(state: AuthenticationState) {
    if (analyticsAllowed.value) {
        when (state) {
            AuthenticationState.HealthCardBlocked ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardBlocked)
            AuthenticationState.HealthCardCardAccessNumberWrong ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardAccessNumberWrong)
            AuthenticationState.HealthCardCommunicationInterrupted ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardCommunicationInterrupted)
            AuthenticationState.HealthCardPin1RetryLeft,
            AuthenticationState.HealthCardPin2RetriesLeft ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.CardPinWrong)
            AuthenticationState.IDPCommunicationFailed ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.IDPCommunicationFailed)
            AuthenticationState.IDPCommunicationInvalidCertificate ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.IDPCommunicationInvalidCertificate)
            AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.IDPCommunicationInvalidOCSPOfCard)
            AuthenticationState.SecureElementCryptographyFailed ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.SecureElementCryptographyFailed)
            AuthenticationState.UserNotAuthenticated ->
                trackAuthenticationProblem(Analytics.AuthenticationProblem.UserNotAuthenticated)
            else -> {}
        }
    }
}
