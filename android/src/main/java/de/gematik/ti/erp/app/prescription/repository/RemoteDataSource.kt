/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.db.converter.DateConverter
import java.time.OffsetDateTime
import javax.inject.Inject
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Communication
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RemoteDataSource @Inject constructor(
    private val service: ErpService
) {

    // greater _than_, otherwise we query the same resource again
    private fun gtString(timestamp: Instant) =
        "gt${timestamp.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"

    suspend fun fetchTasks(lastKnownUpdate: Instant?, profileName: String): Result<Bundle> =
        safeApiCall(
            "error while loading tasks"
        ) {
            val dateTimeString =
                lastKnownUpdate?.let { gtString(it) }
            service.allTasks(profileName, dateTimeString)
        }

    suspend fun fetchCommunications(profileName: String): Result<Bundle> = safeApiCall(
        errorMessage = "error getting communications"
    ) {
        service.communication(profileName)
    }

    suspend fun taskWithKBVBundle(profileName: String, taskID: String) = safeApiCall(
        errorMessage = "error while downloading KBV Bundle $taskID"
    ) { service.taskWithKBVBundle(profileName = profileName, id = taskID) }

    suspend fun allAuditEvents(
        profileName: String,
        lastKnownUpdate: OffsetDateTime?,
        count: Int? = null,
        offset: Int? = null
    ) = safeApiCall(
        errorMessage = "Error getting all Audit Events"
    ) {
        val dateTimeString: String? =
            lastKnownUpdate?.let { "gt${DateConverter().fromOffsetDateTime(it)}" }
        service.allAuditEvents(
            profileName = profileName,
            lastKnownDate = dateTimeString,
            count = count,
            offset = offset
        )
    }

    suspend fun medicationDispense(profileName: String, taskId: String) = safeApiCall(
        errorMessage = "Error getting medication dispenses"
    ) {
        service.medicationDispense(profileName, id = taskId)
    }

    suspend fun deleteTask(profileName: String, taskId: String) = safeApiCall(
        "error deleting task $taskId"
    ) {
        service.deleteTask(profileName, id = taskId)
    }

    suspend fun communicate(profileName: String, com: Communication) = safeApiCall(
        errorMessage = "error while posting communication"
    ) {
        service.communication(profileName, communication = com)
    }
}
