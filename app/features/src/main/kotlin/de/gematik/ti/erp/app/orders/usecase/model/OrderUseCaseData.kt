/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.orders.usecase.model

import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.datetime.Instant

object OrderUseCaseData {
    data class Pharmacy(
        val id: String,
        val name: String
    )

    data class Order(
        val orderId: String,
        // TODO: List<Task-id> is a duplicate here, we need to refactor it since it is inside the List<Prescription>
        val taskIds: List<String>,
        val prescriptions: List<Prescription?>,
        val sentOn: Instant,
        val pharmacy: Pharmacy,
        val hasUnreadMessages: Boolean
    )

    data class OrderDetail(
        val orderId: String,
        val taskDetailedBundles: List<TaskDetailedBundle>,
        val sentOn: Instant,
        val pharmacy: Pharmacy,
        val hasUnreadMessages: Boolean
    )

    data class TaskDetailedBundle(
        // TODO: Task-id is a duplicate here, we need to refactor it
        val taskId: String,
        val hasInvoice: Boolean = false,
        val prescription: Prescription?
    )

    data class Message(
        val communicationId: String,
        val sentOn: Instant,
        val message: String?,
        val code: String?,
        val link: String?,
        val consumed: Boolean,
        val hasInvoice: Boolean
    ) {
        enum class Type {
            All,
            Link,
            Code,
            Text,
            Empty
        }

        val type: Type = run {
            var filled = 0
            link?.let { filled++ }
            code?.let { filled++ }
            message?.let { filled++ }

            if (filled == 0) {
                Type.Empty
            } else if (filled > 1) {
                Type.All
            } else {
                when {
                    link != null -> Type.Link
                    code != null -> Type.Code
                    message != null -> Type.Text
                    else -> Type.All
                }
            }
        }
    }
}
