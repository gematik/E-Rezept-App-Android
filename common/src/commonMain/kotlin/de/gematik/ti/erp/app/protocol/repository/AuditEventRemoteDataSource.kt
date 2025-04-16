/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.protocol.repository

import androidx.compose.ui.text.intl.Locale
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.serialization.json.JsonElement

class AuditEventRemoteDataSource(
    private val service: ErpService
) {
    suspend fun getAuditEvents(
        count: Int? = null,
        offset: Int? = null,
        profileId: ProfileIdentifier
    ): Result<JsonElement> = safeApiCall(
        errorMessage = "Error getting all audit events"
    ) {
        service.getAuditEvents(
            profileId = profileId,
            language = Locale.current.language,
            count = count,
            offset = offset
        )
    }
}
