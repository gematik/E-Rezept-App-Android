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

    suspend fun getTasksByUrl(
        profileId: ProfileIdentifier,
        url: String
    ) = safeApiCall(
        errorMessage = "Error getting paginated task $url"
    ) {
        service.getTasksByUrl(
            profileId = profileId,
            url = url
        )
    }

    suspend fun taskWithKBVBundle(
        profileId: ProfileIdentifier,
        taskId: String
    ) = safeApiCall(
        errorMessage = "error while downloading KBV Bundle $taskId"
    ) { service.getTaskWithKBVBundle(profileId = profileId, id = taskId) }

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
