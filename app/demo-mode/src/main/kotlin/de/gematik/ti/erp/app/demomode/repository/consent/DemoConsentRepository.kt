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

package de.gematik.ti.erp.app.demomode.repository.consent

import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConsentInfo.JSON_RESPONSE_CONSENT
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class DemoConsentRepository : ConsentRepository {
    override suspend fun getConsent(
        profileId: ProfileIdentifier
    ): Result<JsonElement> = Result.success(Json.parseToJsonElement(JSON_RESPONSE_CONSENT))

    override suspend fun grantConsent(
        profileId: ProfileIdentifier,
        consent: JsonElement
    ): Result<Unit> = Result.success(Unit)

    override suspend fun revokeChargeConsent(
        profileId: ProfileIdentifier
    ): Result<Unit> = Result.success(Unit)

    override suspend fun saveGrantConsentDrawerShown(profileId: ProfileIdentifier) {
        // nothing happens
    }

    override fun isConsentDrawerShown(profileId: ProfileIdentifier): Boolean = true

    override fun isConsentGranted(it: JsonElement): Boolean = true

    override fun getInsuranceId(profileId: ProfileIdentifier): String = "X1234567890"
}
