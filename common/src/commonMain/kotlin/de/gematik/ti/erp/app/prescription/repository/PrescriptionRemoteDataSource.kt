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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.api.safeApiCallNullable
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.serialization.json.JsonElement

class PrescriptionRemoteDataSource(
    private val service: ErpService
) {
    suspend fun fetchCommunications(
        profileId: ProfileIdentifier,
        count: Int?,
        lastKnownUpdate: String?
    ): Result<JsonElement> = safeApiCall(
        errorMessage = "error getting communications"
    ) {
        service.getCommunications(
            profileId = profileId,
            count = count,
            lastKnownDate = lastKnownUpdate
        )
    }

    suspend fun deleteTask(profileId: ProfileIdentifier, taskId: String) = safeApiCallNullable(
        "error deleting task $taskId"
    ) {
        service.deleteTask(profileId, id = taskId)
    }

    suspend fun communicate(profileId: ProfileIdentifier, communication: JsonElement, accessCode: String? = null) =
        safeApiCall(errorMessage = "error while posting communication") {
            service.postCommunication(profileId = profileId, communication = communication, accessCode = accessCode)
        }
}
