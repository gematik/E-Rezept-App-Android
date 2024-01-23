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

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Removes the profile from the [profileRepository] and
 * invalidates it in the [idpRepository]
 * adds a new profile with the new name to the [profileRepository].
 */
class ResetProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val idpRepository: IdpRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(profile: Profile, newProfileName: String) {
        withContext(dispatcher) {
            // Not sure if the order needs to be changed here.
            profileRepository.saveProfile(newProfileName, activate = true)
            idpRepository.invalidateDecryptedAccessToken(profile.name)
            profileRepository.removeProfile(profile.id)
        }
    }
}
