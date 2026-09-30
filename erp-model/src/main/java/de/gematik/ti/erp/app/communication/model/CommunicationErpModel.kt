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

package de.gematik.ti.erp.app.communication.model

import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CommunicationErpModel(
    val communicationId: String,
    val orderId: String,
    val taskId: String,
    val senderTelematikId: String,
    val consumed: Boolean,
    val payload: CommunicationPayloadErpModel? = null,
    val profile: CommunicationProfile,
    var recipient: String = "",
    val profileId: String?,
    val timeStamp: Instant?,
    val pharmacyName: String? = null,
    val taskIds: List<String> = emptyList(),
    val isTaskIdCountMatching: Boolean = false
) {
    companion object {
        fun CommunicationErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }

    enum class CommunicationProfile {
        ErxCommunicationDispReq, ErxCommunicationReply, InApp, EuOrder;

        fun toEntityValue() = when (this) {
            ErxCommunicationDispReq -> CommunicationProfileV1.ErxCommunicationDispReq
            ErxCommunicationReply -> CommunicationProfileV1.ErxCommunicationReply
            InApp -> CommunicationProfileV1.InApp
            EuOrder -> null // Eu-order is a different business flow
        }
    }
}

suspend fun Flow<List<CommunicationErpModel>>.getLatestTimestamp(
    requestCommunication: CommunicationErpModel
): Instant? {
    return firstOrNull()
        ?.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
        ?.timeStamp
        ?.coerceAtLeast(requestCommunication.timeStamp ?: Instant.DISTANT_PAST) // Ensure we always use the latest date
}
