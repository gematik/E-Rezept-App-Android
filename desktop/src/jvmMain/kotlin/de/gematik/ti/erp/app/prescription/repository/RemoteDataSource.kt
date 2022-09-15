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
import de.gematik.ti.erp.app.core.safeApiCall
import de.gematik.ti.erp.app.core.safeApiCallNullable
import org.hl7.fhir.r4.model.Bundle

class RemoteDataSource(
    private val service: ErpService
) {
    suspend fun getTasks(): Result<Bundle> =
        safeApiCall("Error while loading tasks") { service.getAllTasks() }

    suspend fun getTaskWithKBVBundle(taskId: String) =
        safeApiCall("Error while downloading KBV Bundle $taskId") { service.getTaskWithKBVBundle(taskId) }

    suspend fun getAllAuditEvents() =
        safeApiCall("Error getting all Audit Events") { service.getAllAuditEvents() }

    suspend fun getAllMedicationDispenses() =
        safeApiCall("Error getting all Medication Dispenses") { service.getAllMedicationDispenses() }

    suspend fun getAllCommunications(): Result<Bundle> =
        safeApiCall("Error getting communications") { service.getAllCommunications() }

    suspend fun deleteTask(taskId: String) =
        safeApiCallNullable("Error deleting Task $taskId") { service.deleteTask(taskId) }
}
