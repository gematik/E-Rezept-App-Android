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

package de.gematik.ti.erp.app.consent.repository

import de.gematik.ti.erp.app.consent.model.ConsentType
import de.gematik.ti.erp.app.consent.model.extractConsentBundle
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.serialization.json.JsonElement

class DefaultConsentRepository(
    private val remoteDataSource: ConsentRemoteDataSource,
    private val localDataSource: ConsentLocalDataSource
) : ConsentRepository {
    override suspend fun getConsent(
        profileId: ProfileIdentifier
    ): Result<JsonElement> = remoteDataSource.getConsent(profileId = profileId)

    override suspend fun grantConsent(
        profileId: ProfileIdentifier,
        consent: JsonElement
    ): Result<Unit> = remoteDataSource.grantConsent(profileId = profileId, consent = consent)

    override suspend fun revokeChargeConsent(
        profileId: ProfileIdentifier
    ): Result<Unit> = remoteDataSource.deleteChargeConsent(profileId = profileId)

    override suspend fun saveGrantConsentDrawerShown(profileId: ProfileIdentifier) =
        localDataSource.saveGiveConsentDrawerShown(
            profileId
        )

    override fun isConsentDrawerShown(profileId: ProfileIdentifier): Boolean = localDataSource.getConsentDrawerShown(
        profileId
    )

    override fun isConsentGranted(it: JsonElement): Boolean {
        var granted = false
        extractConsentBundle(it) { consentTypes ->
            granted = consentTypes.any { consentType ->
                consentType == ConsentType.Charge
            }
        }
        return granted
    }
}