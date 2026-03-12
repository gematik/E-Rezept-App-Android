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

package de.gematik.ti.erp.app.settings.repository

import de.gematik.ti.erp.app.database.settings.CommunicationVersion
import de.gematik.ti.erp.app.database.settings.CommunicationVersionDataStore
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions
import kotlinx.coroutines.flow.first

interface CommunicationVersionRepository {
    suspend fun getCommunicationVersion(): FhirCommunicationVersions.CommunicationVersion
}

class DefaultCommunicationVersionRepository(
    private val dataStore: CommunicationVersionDataStore?,
    private val isDebugMode: Boolean
) : CommunicationVersionRepository {
    override suspend fun getCommunicationVersion(): FhirCommunicationVersions.CommunicationVersion {
        return if (isDebugMode && dataStore != null) {
            when (dataStore.communicationVersion.first()) {
                CommunicationVersion.V_1_2 -> FhirCommunicationVersions.CommunicationVersion.V_1_2
                CommunicationVersion.V_1_3 -> FhirCommunicationVersions.CommunicationVersion.V_1_3
                CommunicationVersion.V_1_4 -> FhirCommunicationVersions.CommunicationVersion.V_1_4
                CommunicationVersion.V_1_5 -> FhirCommunicationVersions.CommunicationVersion.V_1_5
                CommunicationVersion.V_1_6 -> FhirCommunicationVersions.CommunicationVersion.V_1_6
            }
        } else {
            FhirCommunicationVersions.CommunicationVersion.V_1_5
        }
    }
}
