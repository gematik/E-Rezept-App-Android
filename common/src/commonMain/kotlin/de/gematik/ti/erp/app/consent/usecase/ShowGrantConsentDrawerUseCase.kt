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

package de.gematik.ti.erp.app.consent.usecase

import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShowGrantConsentDrawerUseCase(
    private val consentRepository: ConsentRepository,
    private val profilesRepository: ProfileRepository
) {
    operator fun invoke(): Flow<Boolean> =
        profilesRepository.activeProfile().map { profile ->
            with(profile) {
                isPkv() && isConsentDrawerNotShown() &&
                    isSsoValid() && isConsentNotGranted()
            }
        }

    private fun ProfilesData.Profile.isPkv() = insuranceType == ProfilesData.InsuranceType.PKV

    private fun ProfilesData.Profile.isSsoValid() = singleSignOnTokenScope?.token?.isValid() ?: false

    private fun ProfilesData.Profile.isConsentDrawerNotShown() = !consentRepository.isConsentDrawerShown(id)

    private suspend fun ProfilesData.Profile.isConsentNotGranted() =
        !(consentRepository.getConsent(id).map { consentRepository.isConsentGranted(it) }.getOrNull() ?: false)
}