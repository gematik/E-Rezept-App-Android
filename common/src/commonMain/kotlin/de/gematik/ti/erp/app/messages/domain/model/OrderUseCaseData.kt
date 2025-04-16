/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.messages.domain.model

import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

object OrderUseCaseData {
    @Serializable
    data class Pharmacy(
        val id: String,
        val name: String
    )

    @Serializable
    data class Order(
        val orderId: String,
        val prescriptions: List<Prescription?>,
        val sentOn: Instant,
        val pharmacy: Pharmacy,
        val hasUnreadMessages: Boolean,
        val latestCommunicationMessage: LastMessage?,
        val invoiceInfo: InvoiceInfo = InvoiceInfo()
    )

    @Serializable
    data class OrderDetail(
        val orderId: String,
        val taskDetailedBundles: List<TaskDetailedBundle>,
        val sentOn: Instant,
        val pharmacy: Pharmacy,
        val hasUnreadMessages: Boolean = false
    )

    @Serializable
    data class InvoiceInfo(
        val hasInvoice: Boolean = false,
        val invoiceSentOn: Instant? = null
    )

    @Serializable
    data class TaskDetailedBundle(
        val invoiceInfo: InvoiceInfo = InvoiceInfo(),
        val prescription: Prescription?
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
        val prescriptions: List<Prescription?>,
        val taskIds: List<String> = emptyList(),
        val isTaskIdCountMatching: Boolean = false
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
