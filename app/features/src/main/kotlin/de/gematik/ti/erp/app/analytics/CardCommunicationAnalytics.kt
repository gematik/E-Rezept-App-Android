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

package de.gematik.ti.erp.app.analytics

import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CardCommunicationAnalytics(
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
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
    fun trackCardState(screenName: String) {
        if (analyticsAllowed.value) {
            Contentsquare.send(screenName)
            Napier.d("Analytics send $screenName")
        } else {
            Napier.d("Analytics not allowed")
        }
    }

    @Requirement(
        "A_19093-01#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = " ...track user is authenticated."
    )
    fun trackIdentifiedWithIDP() {
        trackCardState("idp_authenticated")
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
        trackCardState("auth_error_${kind.event}")
    }
}

fun CardCommunicationAnalytics.trackCardCommunication(state: AuthenticationState) {
    if (analyticsAllowed.value) {
        when (state) {
            AuthenticationState.HealthCardBlocked ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.CardBlocked)

            AuthenticationState.HealthCardCardAccessNumberWrong ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.CardAccessNumberWrong)

            AuthenticationState.HealthCardCommunicationInterrupted ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.CardCommunicationInterrupted)

            AuthenticationState.HealthCardPin1RetryLeft,
            AuthenticationState.HealthCardPin2RetriesLeft ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.CardPinWrong)

            AuthenticationState.IDPCommunicationFailed ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.IDPCommunicationFailed)

            AuthenticationState.IDPCommunicationInvalidCertificate ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.IDPCommunicationInvalidCertificate)

            AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.IDPCommunicationInvalidOCSPOfCard)

            AuthenticationState.SecureElementCryptographyFailed ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.SecureElementCryptographyFailed)

            AuthenticationState.UserNotAuthenticated ->
                trackAuthenticationProblem(CardCommunicationAnalytics.AuthenticationProblem.UserNotAuthenticated)

            else -> {}
        }
    }
}
