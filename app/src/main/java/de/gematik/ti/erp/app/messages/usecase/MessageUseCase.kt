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

package de.gematik.ti.erp.app.messages.usecase

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.messages.repository.MessageRepository
import de.gematik.ti.erp.app.messages.ui.models.CommunicationReply
import de.gematik.ti.erp.app.messages.ui.models.ErrorUIMessage
import de.gematik.ti.erp.app.messages.ui.models.UIMessage
import de.gematik.ti.erp.app.pharmacy.repository.model.CommunicationPayloadInbox
import javax.inject.Inject
import kotlinx.coroutines.flow.map

const val SHIPMENT = "shipment"
const val LOCAL = "onPremise"
const val DELIVERY = "delivery"
const val ERROR = "none"

class MessageUseCase @Inject constructor(
    private val repository: MessageRepository,
    private val moshi: Moshi
) {

    private val adapter by lazy {
        moshi.adapter(CommunicationPayloadInbox::class.java)
    }

    fun loadCommunicationsLocally(profile: CommunicationProfile) =
        repository.loadCommunications(profile)
            .map {
                it.map { communication ->
                    mapToUIMessage(communication)
                }
            }

    fun unreadCommunicationsAvailable(profile: CommunicationProfile) =
        repository.loadUnreadCommunications(profile).map { it.isNotEmpty() }

    suspend fun updateCommunicationResource(communicationId: String, consumed: Boolean) {
        repository.setCommunicationAcknowledgedStatus(communicationId, consumed)
    }

    private fun mapToUIMessage(communication: Communication): CommunicationReply {
        communication.payload?.let { contentString ->
            var payload: CommunicationPayloadInbox? = null
            try {
                payload = adapter.fromJson(contentString)
            } catch (e: Exception) {
                return errorMessage(
                    contentString,
                    communication.communicationId,
                    communication.consumed,
                    communication.time
                )
            }
            return when (payload?.supplyOptionsType) {
                SHIPMENT ->
                    shipmentMessage(
                        payload,
                        communication.communicationId,
                        communication.consumed
                    )
                LOCAL ->
                    localMessage(
                        payload,
                        communication.communicationId,
                        communication.consumed
                    )
                DELIVERY ->
                    deliveryMessage(
                        payload,
                        communication.communicationId,
                        communication.consumed
                    )
                else ->
                    errorMessage(
                        contentString,
                        communication.communicationId,
                        communication.consumed,
                        communication.time
                    )
            }
        }
        return errorMessage(
            "empty content string",
            communication.communicationId,
            communication.consumed,
            communication.time
        )
    }

    private fun shipmentMessage(
        payload: CommunicationPayloadInbox,
        communicationId: String,
        consumed: Boolean
    ) =
        UIMessage(
            communicationId = communicationId,
            supplyOptionsType = payload.supplyOptionsType,
            header = R.string.communication_shipment_inbox_header,
            message = if (payload.infoText.isEmpty()) null else payload.infoText,
            url = payload.url,
            actionText = if (payload.url.isNullOrEmpty()) -1 else R.string.communication_shipment_action_text,
            consumed = consumed
        )

    private fun localMessage(
        payload: CommunicationPayloadInbox,
        communicationId: String,
        consumed: Boolean
    ) =
        UIMessage(
            communicationId = communicationId,
            supplyOptionsType = payload.supplyOptionsType,
            header = R.string.communication_local_inbox_header,
            message = if (payload.infoText.isEmpty()) null else payload.infoText,
            pickUpCodeHR = payload.pickUpCodeHR,
            pickUpCodeDMC = payload.pickUpCodeDMC,
            actionText = R.string.communication_local_action_text,
            consumed = consumed
        )

    private fun deliveryMessage(
        payload: CommunicationPayloadInbox,
        communicationId: String,
        consumed: Boolean
    ) =
        UIMessage(
            communicationId = communicationId,
            supplyOptionsType = payload.supplyOptionsType,
            header = R.string.communication_delivery_inbox_header,
            message = if (payload.infoText.isEmpty()) null else payload.infoText,
            consumed = consumed
        )

    private fun errorMessage(
        message: String,
        communicationId: String,
        consumed: Boolean,
        timestamp: String
    ) =
        ErrorUIMessage(
            communicationId = communicationId,
            supplyOptionsType = ERROR,
            header = R.string.communication_error_inbox_header,
            message = message,
            displayText = R.string.communication_error_inbox_display_text,
            actionText = R.string.communication_error_action_text,
            consumed = consumed,
            timeStamp = timestamp
        )
}
