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

package de.gematik.ti.erp.app.consent.usecase

import de.gematik.ti.erp.app.consent.model.ConsentType
import de.gematik.ti.erp.app.consent.model.createConsent
import de.gematik.ti.erp.app.consent.model.extractConsentBundle
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

class ConsentUseCase(private val consentRepository: ConsentRepository) {

    suspend fun getChargeConsent(
        profileId: ProfileIdentifier
    ) = consentRepository.getConsent(
        profileId = profileId
    ).map {
        var granted = false
        extractConsentBundle(it) { consentTypes ->
            granted = consentTypes.any { consentType ->
                consentType == ConsentType.Charge
            }
        }
        granted
    }

    suspend fun grantChargeConsent(
        profileId: ProfileIdentifier,
        insuranceId: String
    ): Result<Unit> {
        val consent = createConsent(insuranceId)
        return consentRepository.grantConsent(
            profileId = profileId,
            consent = consent
        )
    }

    suspend fun deleteChargeConsent(
        profileId: ProfileIdentifier
    ): Result<Unit> =
        consentRepository.deleteChargeConsent(
            profileId = profileId
        )
}
