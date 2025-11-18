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

package de.gematik.ti.erp.app.eurezept.repository

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.serialization.json.JsonElement

class EuTaskRemoteDataSource(
    private val service: ErpService
) {
    suspend fun toggleIsEuRedeemableByPatientAuthorization(
        profileIdentifier: ProfileIdentifier,
        taskId: String,
        payload: JsonElement
    ): Result<JsonElement> =
        safeApiCall("Error changing isEuRedeemableByPatientAuthorization for $taskId.") {
            service.toggleIsEuRedeemableByPatientAuthorization(
                profileId = profileIdentifier,
                taskId = taskId,
                payload = payload
            )
        }

    suspend fun createEuRedeemAccessCode(
        profileIdentifier: ProfileIdentifier,
        authorizationRequest: JsonElement
    ): Result<JsonElement> =
        safeApiCall("Error creating EuRedeemAccessCode for $profileIdentifier.") {
            service.createEuRedeemAccessCode(
                profileId = profileIdentifier,
                authorizationRequest = authorizationRequest
            )
        }

    suspend fun getEuRedeemAccessCode(
        profileIdentifier: ProfileIdentifier
    ): Result<JsonElement> =
        safeApiCall("Error retrieving EuRedeemAccessCode for $profileIdentifier.") {
            service.getEuRedeemAccessCode(
                profileId = profileIdentifier
            )
        }

    suspend fun deleteEuRedeemAccessCode(
        profileIdentifier: ProfileIdentifier
    ): Result<JsonElement> =
        safeApiCall("Error deleting EuRedeemAccessCode for $profileIdentifier.") {
            service.deleteEuRedeemAccessCode(
                profileId = profileIdentifier
            )
        }
}
