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

package de.gematik.ti.erp.app.prescription.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

object PrescriptionScreenData {
    enum class EmptyActiveScreenState {
        LoggedIn,
        LoggedOutWithoutTokenBiometrics,
        LoggedOutWithoutToken,
        LoggedOut,
        NeverConnected,
        NotEmpty
    }

    enum class EmptyArchiveScreenState {
        NeverConnected,
        NothingArchived,
        NotEmpty
    }

    private fun ProfilesUseCaseData.Profile.neverConnected() = ssoTokenScope == null && lastAuthenticated == null

    private fun ProfilesUseCaseData.Profile.ssoTokenSetAndConnected() =
        ssoTokenScope?.token != null && ssoTokenScope.token?.isValid() == true

    private fun ProfilesUseCaseData.Profile.ssoTokenSetAndDisconnected() =
        ssoTokenScope != null && ssoTokenScope.token?.isValid() == false ||
            lastAuthenticated != null

    private fun ProfilesUseCaseData.Profile.ssoTokenNotSet() =
        when (ssoTokenScope) {
            is IdpData.ExternalAuthenticationToken,
            is IdpData.AlternateAuthenticationToken,
            is IdpData.AlternateAuthenticationWithoutToken,
            is IdpData.DefaultToken -> ssoTokenScope.token == null
            null -> true
        }

    private fun ProfilesUseCaseData.Profile.ssoTokenWithoutScope() =
        when (ssoTokenScope) {
            is IdpData.AlternateAuthenticationWithoutToken -> true
            else -> false
        }

    @Immutable
    data class State(
        val prescriptions: List<PrescriptionUseCaseData.Prescription>,
        val redeemedPrescriptions: List<PrescriptionUseCaseData.Prescription>
    ) {
        @Stable
        fun emptyActiveScreen(profile: ProfilesUseCaseData.Profile): EmptyActiveScreenState {
            val noPrescriptions = prescriptions.isEmpty()
            return if (noPrescriptions) {
                when {
                    profile.neverConnected() ->
                        EmptyActiveScreenState.NeverConnected
                    profile.ssoTokenWithoutScope() ->
                        EmptyActiveScreenState.LoggedOutWithoutTokenBiometrics
                    profile.ssoTokenNotSet() ->
                        EmptyActiveScreenState.LoggedOutWithoutToken
                    profile.ssoTokenSetAndConnected() ->
                        EmptyActiveScreenState.LoggedIn
                    profile.ssoTokenSetAndDisconnected() ->
                        EmptyActiveScreenState.LoggedOut
                    else ->
                        EmptyActiveScreenState.NotEmpty
                }
            } else {
                EmptyActiveScreenState.NotEmpty
            }
        }

        @Stable
        fun emptyArchiveScreen(profile: ProfilesUseCaseData.Profile): EmptyArchiveScreenState =
            when {
                redeemedPrescriptions.isEmpty() && profile.neverConnected() ->
                    EmptyArchiveScreenState.NeverConnected
                redeemedPrescriptions.isEmpty() ->
                    EmptyArchiveScreenState.NothingArchived
                else ->
                    EmptyArchiveScreenState.NotEmpty
            }
    }
}
