/*
 * Copyright (c) 2021 gematik GmbH
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
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Communication
import java.time.OffsetDateTime
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val service: ErpService
) {

    suspend fun loadCapabilityStatement() = safeApiCall(
        errorMessage = "error while loading capability statement"
    ) { service.capability() }

    suspend fun fetchTasks(lastKnownUpdate: OffsetDateTime?): Result<Bundle> = safeApiCall(
        "error while loading tasks"
    ) {
        val dateTimeString: String? =
            lastKnownUpdate?.let { "ge${DateConverter().fromOffsetDateTime(it)}" }
        service.allTasks(dateTimeString)
    }

    suspend fun fetchCommunications(): Result<Bundle> = safeApiCall(
        errorMessage = "error getting communications"
    ) {
        service.communication()
    }

    suspend fun taskWithKBVBundle(taskID: String) = safeApiCall(
        errorMessage = "error while downloading KBV Bundle $taskID"
    ) { service.taskWithKBVBundle(taskID) }

    suspend fun allAuditEvents(lastKnownUpdate: OffsetDateTime?) = safeApiCall(
        errorMessage = "Error getting all Audit Events"
    ) {
        val dateTimeString: String? =
            lastKnownUpdate?.let { "ge${DateConverter().fromOffsetDateTime(it)}" }
        service.allAuditEvents(dateTimeString)
    }

    suspend fun medicationDispense(taskId: String) = safeApiCall(
        errorMessage = "Error getting medication dispenses"
    ) {
        service.medicationDispense(taskId)
    }

    suspend fun deleteTask(taskId: String) = safeApiCall(
        "error deleting task $taskId"
    ) {
        service.deleteTask(taskId)
    }

    suspend fun communicate(com: Communication) = safeApiCall(
        errorMessage = "error while posting communication"
    ) { service.communication(com) }
}
