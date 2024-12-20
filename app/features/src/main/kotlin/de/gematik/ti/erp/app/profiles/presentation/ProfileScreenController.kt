/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.presentation.GetProfileByIdController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeleteProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("LongParameterList")
class ProfileScreenController(
    profileId: ProfileIdentifier,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val switchActiveProfileUseCase: SwitchActiveProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutProfileUseCase: LogoutProfileUseCase,
    private val addProfileUseCase: AddProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase
) : GetProfileByIdController(
    selectedProfileId = profileId,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase
) {

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
            logoutProfileUseCase = logoutProfileUseCase
        )
    }
}