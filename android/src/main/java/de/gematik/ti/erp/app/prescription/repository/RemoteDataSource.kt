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
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.api.safeApiCallNullable
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Communication
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RemoteDataSource(
    private val service: ErpService
) {

    // greater _than_, otherwise we query the same resource again
    private fun gtString(timestamp: Instant) =
        "gt${timestamp.atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"

    suspend fun fetchTasks(lastKnownUpdate: Instant?, profileId: ProfileIdentifier): Result<Bundle> =
        safeApiCall(
            "error while loading tasks"
        ) {
            val dateTimeString =
                lastKnownUpdate?.let { gtString(it) }
            service.allTasks(profileId, dateTimeString)
        }

    suspend fun fetchCommunications(
        profileId: ProfileIdentifier,
        count: Int?,
        lastKnownUpdate: String?
    ): Result<Bundle> = safeApiCall(
        errorMessage = "error getting communications"
    ) {
        service.getCommunications(
            profileId = profileId,
            count = count,
            lastKnownDate = lastKnownUpdate
        )
    }

    suspend fun taskWithKBVBundle(profileId: ProfileIdentifier, taskID: String) = safeApiCall(
        errorMessage = "error while downloading KBV Bundle $taskID"
    ) { service.taskWithKBVBundle(profileId = profileId, id = taskID) }

    suspend fun loadBundleOfMedicationDispenses(profileId: ProfileIdentifier, taskId: String) = safeApiCall(
        errorMessage = "Error getting medication dispenses"
    ) {
        val id = "https://gematik.de/fhir/NamingSystem/PrescriptionID|$taskId"
        service.bundleOfMedicationDispenses(profileId, id = id)
    }

    suspend fun deleteTask(profileId: ProfileIdentifier, taskId: String) = safeApiCallNullable(
        "error deleting task $taskId"
    ) {
        service.deleteTask(profileId, id = taskId)
    }

    suspend fun communicate(profileId: ProfileIdentifier, com: Communication, accessCode: String? = null) = safeApiCall(
        errorMessage = "error while posting communication"
    ) {
        service.communication(profileId, communication = com, accessCode = accessCode)
    }
}
