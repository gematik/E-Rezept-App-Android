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

package de.gematik.ti.erp.app.cardwall.usecase

import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MiniCardWallUseCase(
    private val idpRepository: IdpRepository,
    private val profilesUseCase: ProfilesUseCase
) {
    fun authenticationData(profileId: ProfileIdentifier): Flow<IdpData.AuthenticationData> =
        idpRepository.authenticationData(profileId)

    fun profileData(profileId: ProfileIdentifier): Flow<ProfilesUseCaseData.Profile> =
        profilesUseCase.profiles.map { profiles ->
            requireNotNull(profiles.find { it.id == profileId }) { "Profile `$profileId` missing!" }
        }
}
