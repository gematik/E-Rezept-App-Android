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

package de.gematik.ti.erp.app.consent.repository

import de.gematik.ti.erp.app.consent.model.ConsentType
import de.gematik.ti.erp.app.consent.model.extractConsentBundle
import de.gematik.ti.erp.app.fhir.FhirConsentErpModelCollection
import de.gematik.ti.erp.app.fhir.consent.FhirConsentParser
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

class DefaultConsentRepository(
    private val remoteDataSource: ConsentRemoteDataSource,
    private val localDataSource: ConsentLocalDataSource,
    private val parsers: FhirConsentParser
) : ConsentRepository {
    override suspend fun getPkvConsent(
        profileId: ProfileIdentifier,
        category: String
    ): Result<JsonElement> = remoteDataSource.getPkvConsent(profileId = profileId, category)

    override suspend fun getEuConsent(
        profileId: ProfileIdentifier,
        category: String
    ): Result<FhirConsentErpModelCollection> {
        return remoteDataSource.getEuConsent(profileId = profileId, category).mapCatching { consent ->
            parsers.extract(consent)
        }
    }

    override suspend fun grantPkvConsent(
        profileId: ProfileIdentifier,
        consent: JsonElement
    ): Result<Unit> = remoteDataSource.grantPkvConsent(profileId = profileId, consent = consent)

    override suspend fun grantEuConsent(
        profileId: ProfileIdentifier,
        consent: JsonElement
    ): Result<Unit> = remoteDataSource.grantEuConsent(profileId = profileId, consent = consent)

    override suspend fun revokeConsent(
        profileId: ProfileIdentifier,
        category: String
    ): Result<Unit> = remoteDataSource.deleteConsent(profileId = profileId, category = category)

    override suspend fun saveGrantConsentDrawerShown(profileId: ProfileIdentifier) =
        localDataSource.saveGiveConsentDrawerShown(
            profileId
        )

    override fun isConsentDrawerShown(profileId: ProfileIdentifier): Boolean = localDataSource.getConsentDrawerShown(profileId)

    override fun isPkvConsentGranted(it: JsonElement): Boolean {
        var granted = false
        extractConsentBundle(it) { consentTypes ->
            granted = consentTypes.any { consentType ->
                consentType == ConsentType.Charge
            }
        }
        return granted
    }
    override fun isEuConsentGranted(it: JsonElement): Boolean {
        var granted = false
        try {
            extractConsentBundle(it) { consentTypes ->
                granted = consentTypes.any { consentType ->
                    consentType == ConsentType.Charge
                }
            }
            return granted
        } catch (e: Exception) {
            Napier.e { "Consent parsing error ${e.stackTraceToString()}" }
            return false
        }
    }

    override fun getInsuranceId(profileId: ProfileIdentifier): String? =
        localDataSource.getInsuranceId(profileId)
}
