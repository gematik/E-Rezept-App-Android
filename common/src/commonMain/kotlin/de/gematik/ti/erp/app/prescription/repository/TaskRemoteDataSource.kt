/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

class TaskRemoteDataSource(
    private val service: ErpService
) {
    suspend fun getTasks(
        profileId: ProfileIdentifier,
        lastUpdated: String?,
        count: Int? = null,
        offset: Int? = null
    ) = safeApiCall(
        errorMessage = "Error getting all tasks"
    ) {
        service.getTasks(
            profileId = profileId,
            lastUpdated = lastUpdated,
            count = count,
            offset = offset
        )
    }

    suspend fun taskWithKBVBundle(
        profileId: ProfileIdentifier,
        taskID: String
    ) = safeApiCall(
        errorMessage = "error while downloading KBV Bundle $taskID"
    ) { service.getTaskWithKBVBundle(profileId = profileId, id = taskID) }

    suspend fun loadBundleOfMedicationDispenses(
        profileId: ProfileIdentifier,
        taskId: String
    ) = safeApiCall(
        errorMessage = "Error getting medication dispenses"
    ) {
        val id = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId|$taskId"
        service.bundleOfMedicationDispenses(profileId, id = id)
    }
}
