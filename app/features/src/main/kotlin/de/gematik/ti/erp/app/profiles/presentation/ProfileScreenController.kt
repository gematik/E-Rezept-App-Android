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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeleteProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("LongParameterList")
class ProfileScreenController(
    profileId: ProfileIdentifier,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    networkStatusTracker: NetworkStatusTracker,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    private val switchActiveProfileUseCase: SwitchActiveProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutProfileUseCase: LogoutProfileUseCase,
    private val addProfileUseCase: AddProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase
) : ChooseAuthenticationController(
    profileId = profileId,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    biometricAuthenticator = biometricAuthenticator,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker
) {
    val deleteDialogEvent = ComposableEvent<Unit>()

    fun switchActiveProfile(id: ProfileIdentifier) {
        controllerScope.launch {
            switchActiveProfileUseCase(id)
        }
    }

    fun logout(profileId: ProfileIdentifier) {
        controllerScope.launch {
            logoutProfileUseCase(profileId)
        }
    }

    fun updateProfileName(name: String) {
        controllerScope.launch {
            combinedProfile.value.data?.selectedProfile?.let {
                updateProfileUseCase(
                    modifier = UpdateProfileUseCase.Companion.ProfileModifier.Name(name),
                    id = it.id
                )
            }
            refreshCombinedProfile()
        }
    }

    fun deleteProfile(profileId: ProfileIdentifier, profileName: String) {
        controllerScope.launch {
            deleteProfileUseCase(profileId, profileName)
        }
    }

    fun addNewProfile(name: String) {
        controllerScope.launch {
            addProfileUseCase(name)
        }
    }

    fun triggerDeleteProfileDialog() {
        deleteDialogEvent.trigger(Unit)
    }
}

@Composable
fun rememberProfileScreenController(profileId: ProfileIdentifier): ProfileScreenController {
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val updateProfileUseCase by rememberInstance<UpdateProfileUseCase>()
    val switchActiveProfileUseCase by rememberInstance<SwitchActiveProfileUseCase>()
    val addProfileUseCase by rememberInstance<AddProfileUseCase>()
    val deleteProfileUseCase by rememberInstance<DeleteProfileUseCase>()
    val logoutProfileUseCase by rememberInstance<LogoutProfileUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current

    return remember(profileId) {
        ProfileScreenController(
            profileId = profileId,
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            updateProfileUseCase = updateProfileUseCase,
            addProfileUseCase = addProfileUseCase,
            deleteProfileUseCase = deleteProfileUseCase,
            logoutProfileUseCase = logoutProfileUseCase,
            biometricAuthenticator = biometricAuthenticator,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker
        )
    }
}
