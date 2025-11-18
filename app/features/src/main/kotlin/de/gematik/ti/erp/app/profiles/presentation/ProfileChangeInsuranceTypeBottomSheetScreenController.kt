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
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileInsuranceTypeUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileChangeInsuranceTypeBottomSheetScreenController(
    private val logOutProfileUseCase: LogoutProfileUseCase,
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val switchProfileInsuranceTypeUseCase: SwitchProfileInsuranceTypeUseCase,
    private val profileId: ProfileIdentifier?
) : Controller() {
    private val _profileState: MutableStateFlow<UiState<ProfilesUseCaseData.Profile>> = MutableStateFlow(UiState.Loading())
    val profileState: StateFlow<UiState<ProfilesUseCaseData.Profile>> = _profileState

    init {
        _profileState.update { UiState.Loading() }
        controllerScope.launch {
            try {
                profileId?.let {
                    getProfileByIdUseCase.invoke(profileId).collect { profile ->
                        _profileState.update { UiState.Data(profile) }
                    }
                } ?: _profileState.update { UiState.Error(error = IllegalArgumentException("ProfileId is null")) }
            } catch (e: Exception) {
                _profileState.update { UiState.Error(error = IllegalArgumentException("ProfileId is null")) }
            }
        }
    }

    internal fun logOutProfile() {
        controllerScope.launch {
            profileId?.let { logOutProfileUseCase.invoke(it) }
        }
    }

    internal fun setProfileInsuranceTypeAsPKV() {
        controllerScope.launch {
            profileId?.let {
                switchProfileInsuranceTypeUseCase.invoke(it, ProfilesData.InsuranceType.PKV)
            }
        }
    }

    internal fun setProfileInsuranceTypeAsGKV() {
        controllerScope.launch {
            profileId?.let {
                switchProfileInsuranceTypeUseCase.invoke(it, ProfilesData.InsuranceType.GKV)
            }
        }
    }

    internal fun setProfileInsuranceTypeAsBUND() {
        controllerScope.launch {
            profileId?.let {
                switchProfileInsuranceTypeUseCase.invoke(it, ProfilesData.InsuranceType.BUND)
            }
        }
    }
}

@Composable
fun rememberProfileChangeInsuranceTypeBottomSheetScreenController(
    profileId: ProfileIdentifier?
): ProfileChangeInsuranceTypeBottomSheetScreenController {
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val logOutProfileUseCase by rememberInstance<LogoutProfileUseCase>()
    val switchProfileInsuranceTypeUseCase by rememberInstance<SwitchProfileInsuranceTypeUseCase>()
    return remember {
        ProfileChangeInsuranceTypeBottomSheetScreenController(
            logOutProfileUseCase = logOutProfileUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            switchProfileInsuranceTypeUseCase = switchProfileInsuranceTypeUseCase,
            profileId = profileId
        )
    }
}
