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

package de.gematik.ti.erp.app.demomode.repository.consent

import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConsentInfo.JSON_RESPONSE_CONSENT
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
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
}
