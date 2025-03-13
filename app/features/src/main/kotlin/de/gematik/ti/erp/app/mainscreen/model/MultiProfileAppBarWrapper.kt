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

package de.gematik.ti.erp.app.mainscreen.model

import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import kotlinx.coroutines.flow.StateFlow

data class ProfileLifecycleState(
    val isProfileRefreshing: StateFlow<Boolean>,
    val networkStatus: StateFlow<Boolean>,
    val isTokenValid: StateFlow<Boolean>,
    val isRegistered: StateFlow<Boolean>
)

data class MultiProfileAppBarWrapper(
    val profileLifecycleState: ProfileLifecycleState,
    val activeProfile: StateFlow<Profile>,
    val existingProfiles: StateFlow<List<Profile>>
) {
    companion object {
        val DEFAULT_EMPTY_PROFILE = Profile(
            id = "",
            name = "",
            insurance = ProfileInsuranceInformation(
                insuranceType = ProfilesUseCaseData.InsuranceType.NONE
            ),
            isActive = false,
            color = ProfilesData.ProfileColorNames.SPRING_GRAY,
            lastAuthenticated = null,
            ssoTokenScope = null,
            avatar = ProfilesData.Avatar.PersonalizedImage
        )
    }
}
