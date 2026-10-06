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

package de.gematik.ti.erp.app.messages.domain.model

import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// TODO CommResV3 Rework to OrderErpModel using  Communication And CommunicationPayload ErpModels
@Deprecated("Rework this whole class. Use ErpModel from commResV3")
object OrderUseCaseData {
    @Serializable
    data class Pharmacy(
        val id: String,
        val name: String
    )

    @Serializable
    data class Order(
        val orderId: String,
        val prescriptions: List<TaskErpModel?>,
        val sentOn: Instant,
        val pharmacy: Pharmacy,
        val hasUnreadMessages: Boolean,
        val latestCommunicationMessage: LastMessage?,
        val invoiceInfo: InvoiceInfo = InvoiceInfo(),
        val supplyOption: CommunicationSupplyOptionTypeErpModel? = null
    )

    @Serializable
    data class OrderDetail(
        val orderId: String,
        val taskDetailedBundles: List<TaskDetailedBundle>,
        val sentOn: Instant,
        val pharmacy: Pharmacy,
        val hasUnreadMessages: Boolean = false,
        val requestPayload: CommunicationPayloadErpModel? = null
    )

    @Serializable
    data class InvoiceInfo(
        val hasInvoice: Boolean = false,
        val invoiceSentOn: Instant? = null,
        val medicationName: String? = null,
        val consumed: Boolean = true
    )

    @Serializable
    data class TaskDetailedBundle(
        val invoiceInfo: InvoiceInfo = InvoiceInfo(),
        val prescription: TaskErpModel?
    )

    @Serializable
    sealed interface OrderCardErpModel {
        val sentOn: Instant
        val hasSeen: Boolean

        // wenn Replymessages kommt wie kann ich es  in PharmacyReplyCard karte packen,
        // DispenseRequestCard
        @Serializable
        data class DispenseRequestCard(
            override val sentOn: Instant,
            val pharmacy: Pharmacy,
            val tasks: List<TaskErpModel>,
            val payload: CommunicationPayloadErpModel?,
            override val hasSeen: Boolean
        ) : OrderCardErpModel

        @Serializable
        data class PharmacyReplyCard(
            val message: Message,
            val payloads: List<CommunicationPayloadErpModel>,
            override val sentOn: Instant,
            override val hasSeen: Boolean
        ) : OrderCardErpModel

        @Serializable
        data class UserReplyCard(
            override val sentOn: Instant,
            val payload: CommunicationPayloadErpModel,
            override val hasSeen: Boolean
        ) : OrderCardErpModel

        @Serializable
        data class InvoiceCard(
            override val sentOn: Instant,
            val invoiceInfo: InvoiceInfo,
            val tasks: List<TaskErpModel>,
            override val hasSeen: Boolean
        ) : OrderCardErpModel
    }

    @Serializable
    data class OrderErpModel(
        val orderId: String,
        val tasks: List<TaskErpModel>,
        val pharmacy: Pharmacy,
        val cards: List<OrderCardErpModel>
    )

    @Serializable
    data class Message(
        val communicationId: String,
        val sentOn: Instant,
        val content: String?,
        val additionalInfo: String = "",
        val pickUpCodeDMC: String?,
        val pickUpCodeHR: String?,
        val link: String?,
        val consumed: Boolean,
        val prescriptions: List<TaskErpModel?>,
        val taskIds: List<String> = emptyList(),
        val isTaskIdCountMatching: Boolean = false,
        val payloads: List<CommunicationPayloadErpModel> = emptyList()
    ) {
        enum class Type {
            All,
            Link,
            PickUpCodeDMC,
            PickUpCodeHR,
            Text,
            Empty
        }

        val type: Type = determineMessageType()

        private fun determineMessageType(): Type {
            val filledFieldsCount = listOfNotNull(link, pickUpCodeDMC, pickUpCodeHR, content).size

            return when {
                filledFieldsCount == 0 -> Type.Empty
                filledFieldsCount > 1 -> Type.All
                link != null -> Type.Link
                pickUpCodeDMC != null -> Type.PickUpCodeDMC
                pickUpCodeHR != null -> Type.PickUpCodeHR
                content != null -> Type.Text
                else -> Type.Empty
            }
        }
    }
}
