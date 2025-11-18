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

import de.gematik.ti.erp.app.fhir.FhirConsentErpModelCollection
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.serialization.json.JsonElement

interface ConsentRepository {
    suspend fun getPkvConsent(
        profileId: ProfileIdentifier,
        category: String
    ): Result<JsonElement>

    suspend fun getEuConsent(
        profileId: ProfileIdentifier,
        category: String
    ): Result<FhirConsentErpModelCollection>

    suspend fun grantPkvConsent(
        profileId: ProfileIdentifier,
        consent: JsonElement
    ): Result<Unit>

    suspend fun grantEuConsent(
        profileId: ProfileIdentifier,
        consent: JsonElement
    ): Result<Unit>

    suspend fun revokeConsent(
        profileId: ProfileIdentifier,
        category: String
    ): Result<Unit>

    suspend fun saveGrantConsentDrawerShown(profileId: ProfileIdentifier)

    fun isConsentDrawerShown(profileId: ProfileIdentifier): Boolean

    fun isPkvConsentGranted(it: JsonElement): Boolean

    fun isEuConsentGranted(it: JsonElement): Boolean

    fun getInsuranceId(profileId: ProfileIdentifier): String?
}
