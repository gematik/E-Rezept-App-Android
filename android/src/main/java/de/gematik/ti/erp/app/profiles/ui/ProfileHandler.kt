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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

interface ProfileBridge {
    val profiles: Flow<List<ProfilesUseCaseData.Profile>>
    suspend fun switchActiveProfile(profile: ProfilesUseCaseData.Profile)
    suspend fun switchProfileToPKV(profileId: ProfileIdentifier)
}

@Stable
class ProfileHandler(
    private val bridge: ProfileBridge,
    coroutineScope: CoroutineScope,
    activeDefaultProfile: ProfilesUseCaseData.Profile = ProfilesStateData.defaultProfile
) {
    enum class ProfileConnectionState {
        LoggedIn,
        LoggedOutWithoutTokenBiometrics,
        LoggedOutWithoutToken,
        LoggedOut,
        NeverConnected
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

    @Stable
    fun connectionState(profile: ProfilesUseCaseData.Profile): ProfileConnectionState? =
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

    var activeProfile by mutableStateOf(activeDefaultProfile)
        private set

    private var profilesFlow =
        bridge
            .profiles
            .onEach {
                activeProfile = it.find { it.active } ?: ProfilesStateData.defaultProfile
            }
            .shareIn(coroutineScope, SharingStarted.Eagerly, 1)

    val profiles: State<List<ProfilesUseCaseData.Profile>>
        @Composable
        get() = profilesFlow.collectAsState(emptyList())

    suspend fun switchActiveProfile(profile: ProfilesUseCaseData.Profile) {
        bridge.switchActiveProfile(profile)
    }

    suspend fun switchProfileToPKV(profileId: ProfileIdentifier) {
        bridge.switchProfileToPKV(profileId)
    }
}

private fun profileHandlerSaver(
    profilesController: ProfilesController,
    scope: CoroutineScope
): Saver<ProfileHandler, String> = Saver(
    save = { state ->
        state.activeProfile.id
    },
    restore = { savedState ->
        ProfileHandler(profilesController, scope, ProfilesStateData.defaultProfile.copy(id = savedState))
    }
)

@Composable
fun rememberProfileHandler(): ProfileHandler {
    val profilesController = rememberProfilesController()
    val coroutineScope = rememberCoroutineScope()
    return rememberSaveable(
        saver = profileHandlerSaver(
            profilesController,
            coroutineScope
        )
    ) {
        ProfileHandler(profilesController, coroutineScope)
    }
}

val LocalProfileHandler =
    staticCompositionLocalOf<ProfileHandler> { error("No profile state provided!") }
