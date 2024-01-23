/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.profiles.ui

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile

@Deprecated("Not to be used, left here for reference in case of errors found")
class ProfileHandler {

    enum class ProfileConnectionState {
        LoggedIn,
        LoggedOutWithoutTokenBiometrics,
        LoggedOutWithoutToken,
        LoggedOut,
        NeverConnected
    }

    private fun Profile.neverConnected() = ssoTokenScope == null && lastAuthenticated == null

    private fun Profile.ssoTokenSetAndConnected() =
        ssoTokenScope?.token != null && ssoTokenScope?.token?.isValid() == true

    private fun Profile.ssoTokenSetAndDisconnected() =
        ssoTokenScope != null && ssoTokenScope?.token?.isValid() == false ||
            lastAuthenticated != null

    private fun Profile.ssoTokenNotSet() =
        when (ssoTokenScope) {
            is IdpData.ExternalAuthenticationToken,
            is IdpData.AlternateAuthenticationToken,
            is IdpData.AlternateAuthenticationWithoutToken,
            is IdpData.DefaultToken -> ssoTokenScope?.token == null

            null -> true
        }

    private fun Profile.ssoTokenWithoutScope() =
        when (ssoTokenScope) {
            is IdpData.AlternateAuthenticationWithoutToken -> true
            else -> false
        }

    @Stable
    fun connectionState(profile: Profile): ProfileConnectionState? =
        when {
            profile.neverConnected() ->
                ProfileConnectionState.NeverConnected

            profile.ssoTokenWithoutScope() ->
                ProfileConnectionState.LoggedOutWithoutTokenBiometrics

            profile.ssoTokenNotSet() ->
                ProfileConnectionState.LoggedOutWithoutToken

            profile.ssoTokenSetAndConnected() ->
                ProfileConnectionState.LoggedIn

            profile.ssoTokenSetAndDisconnected() ->
                ProfileConnectionState.LoggedOut

            else -> null
        }
}
