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

import de.gematik.ti.erp.app.database.settings.EuVersion
import de.gematik.ti.erp.app.database.settings.EuVersionDataStore
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants.FhirEuRedeemAccessCodeRequestMeta
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeResponseConstants.FhirEuRedeemAccessCodeResponseMeta
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirTaskEuPatchInputModelConstants.FhirTaskEuPatchMeta
import kotlinx.coroutines.flow.firstOrNull

interface EuVersionRepository {
    suspend fun getEuPatchMeta(): FhirTaskEuPatchMeta
    suspend fun getEuRedeemAccessCodeRequestMeta(): FhirEuRedeemAccessCodeRequestMeta
    suspend fun getEuRedeemAccessCodeResponseMeta(): FhirEuRedeemAccessCodeResponseMeta
}

class DefaultEuVersionRepository(
    private val dataStore: EuVersionDataStore?,
    private val isDebugMode: Boolean
) : EuVersionRepository {

    private suspend fun getEuVersion(): EuVersion {
        return if (isDebugMode && dataStore != null) {
            dataStore.euVersion.firstOrNull() ?: EuVersion.V_1_1
        } else {
            EuVersion.V_1_1
        }
    }

    override suspend fun getEuPatchMeta(): FhirTaskEuPatchMeta = when (getEuVersion()) {
        EuVersion.V_1_0 -> FhirTaskEuPatchMeta.V_1_0
        EuVersion.V_1_1 -> FhirTaskEuPatchMeta.V_1_1
    }

    override suspend fun getEuRedeemAccessCodeRequestMeta(): FhirEuRedeemAccessCodeRequestMeta = when (getEuVersion()) {
        EuVersion.V_1_0 -> FhirEuRedeemAccessCodeRequestMeta.V_1_0
        EuVersion.V_1_1 -> FhirEuRedeemAccessCodeRequestMeta.V_1_1
    }

    override suspend fun getEuRedeemAccessCodeResponseMeta(): FhirEuRedeemAccessCodeResponseMeta = when (getEuVersion()) {
        EuVersion.V_1_0 -> FhirEuRedeemAccessCodeResponseMeta.V_1_0
        EuVersion.V_1_1 -> FhirEuRedeemAccessCodeResponseMeta.V_1_1
    }
}
