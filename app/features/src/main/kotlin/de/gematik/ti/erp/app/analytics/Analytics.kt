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

package de.gematik.ti.erp.app.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCase
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCaseData
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.analytics.usecase.StartTrackerUseCase
import de.gematik.ti.erp.app.analytics.usecase.StopTrackerUseCase
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Analytics(
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val startTrackerUseCase: StartTrackerUseCase,
    private val stopTrackerUseCase: StopTrackerUseCase,
    private val analyticsUseCase: AnalyticsUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val scope = CoroutineScope(dispatcher)

    private val isAnalyticsAllowed by lazy {
        isAnalyticsAllowedUseCase.invoke()
    }

    val analyticsAllowed: StateFlow<Boolean>
        get() = isAnalyticsAllowed.stateIn(scope, SharingStarted.Eagerly, false)

    @Requirement(
        "A_19093-01#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = " ...track screens for analytics purposes."
    )
    @Requirement(
        "O.Purp_2#4",
        "O.Purp_4#1",
        "O.Data_6#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "...recording for screens here too after opt-in",
        codeLines = 8
    )
    fun trackScreen(screenName: String) {
        if (analyticsAllowed.value) {
            Contentsquare.send(screenName)
            Napier.d("Analytics send $screenName")
        } else {
            Napier.d("Analytics not allowed")
        }
    }

    /**
     * This init function is called from the [BaseActivity] to initialize the analytics.
     */
    fun init(activity: BaseActivity) {
        Napier.d("Init Analytics")
        scope.launch {
            val isAllowed = isAnalyticsAllowed.first()
            activity.runOnUiThread {
                setAnalyticsPreference(isAllowed)
            }
        }
    }

    private val popUpFlow = MutableStateFlow(AnalyticsData.defaultPopUp)

    private var analyticsScreenFlow = combine(
        analyticsUseCase.screenNamesFlow,
        popUpFlow
    ) { screenNames, popUp ->
        AnalyticsData.AnalyticsScreenState(screenNames, popUp)
    }

    val screenState
        @Composable
        get() = analyticsScreenFlow.collectAsStateWithLifecycle(AnalyticsData.defaultAnalyticsState)

    @Requirement(
        "O.Purp_5#5",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Enable/disable usage analytics."
    )
    fun setAnalyticsPreference(allow: Boolean) {
        when (allow) {
            true -> allowAnalytics()
            else -> disallowAnalytics()
        }
    }

    private fun allowAnalytics() {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(true)
            startTrackerUseCase()
        }
    }

    @Requirement(
        "O.Purp_5#7",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Disable usage analytics."
    )
    private fun disallowAnalytics() {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(false)
            stopTrackerUseCase()
        }
    }

    @Requirement(
        "A_19093-01#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = " ...track user is authenticated."
    )
    fun trackIdentifiedWithIDP() {
        trackScreen("idp_authenticated")
    }

    enum class AuthenticationProblem(val event: String) {
        CardBlocked("card_blocked"),
        CardAccessNumberWrong("card_can_wrong"),
        CardCommunicationInterrupted("card_com_interrupted"),
        CardPinWrong("card_pin_wrong"),
        IDPCommunicationFailed("idp_com_failed"),
        IDPCommunicationInvalidCertificate("idp_com_invalid_certificate"),
        IDPCommunicationInvalidOCSPOfCard("idp_com_invalid_ocsp_of_card"),
        SecureElementCryptographyFailed("secure_element_cryptography_failed"),
        UserNotAuthenticated("user_not_authenticated")
    }

    @Requirement(
        "A_19093-01#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = " ...track user has authentication error."
    )
    fun trackAuthenticationProblem(kind: AuthenticationProblem) {
        trackScreen("auth_error_${kind.event}")
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

object AnalyticsData {
    @Immutable
    data class AnalyticsScreenState(
        val screenNamesList: List<AnalyticsUseCaseData.AnalyticsScreenName>,
        var popUp: PopUp
    )

    data class PopUp(
        var visible: Boolean,
        var name: String
    )

    val defaultPopUp = PopUp(
        visible = false,
        name = ""
    )

    val defaultAnalyticsState = AnalyticsScreenState(
        screenNamesList = emptyList(),
        popUp = defaultPopUp
    )
}
