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

package de.gematik.ti.erp.app.pharmacy.model

import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import kotlinx.datetime.Instant

data class PrescriptionInOrderErpModel(
    val taskId: String,
    val accessCode: String,
    val title: String?,
    val isSelfPayerPrescription: Boolean,
    val index: Int?,
    val timestamp: Instant,
    val substitutionsAllowed: Boolean,
    val isTeratogenicPrescription: Boolean,
    val isScanned: Boolean
)

data class OrderStateErpModel(
    val prescriptionsInOrder: List<PrescriptionInOrderErpModel>,
    val selfPayerPrescriptionIds: List<String>,
    val contact: ShippingInfoErpModel,
    val isLoading: Boolean = false
) {
    val selfPayerPrescriptionNames = prescriptionsInOrder
        .filter { it.taskId in this.selfPayerPrescriptionIds }
        .mapNotNull { it.title }

    companion object {
        val Empty = OrderStateErpModel(
            prescriptionsInOrder = emptyList(),
            selfPayerPrescriptionIds = emptyList(),
            contact = ShippingInfoErpModel.EmptyShippingInfoErpModel,
            isLoading = true
        )
    }
}
