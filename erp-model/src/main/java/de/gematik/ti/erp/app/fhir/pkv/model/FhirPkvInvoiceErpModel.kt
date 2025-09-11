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

package de.gematik.ti.erp.app.fhir.pkv.model

import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.support.FhirChargeableItemCodeErpModel
import de.gematik.ti.erp.app.fhir.support.FhirCostErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.serialization.Serializable

@Serializable
data class FhirPkvInvoiceErpModel(
    val taskId: String?,
    val timestamp: FhirTemporal?,
    val organization: FhirTaskOrganizationErpModel?,
    val lineItems: List<FhirPkvInvoiceChargeItemErpModel>,
    val whenHandedOver: FhirTemporal?,
    val totalAdditionalFee: FhirCostErpModel?,
    val totalGrossFee: FhirCostErpModel?,
    val additionalInvoiceInformation: List<String> = emptyList(),
    val additionalDispenseItems: List<FhirPkvInvoiceChargeItemErpModel> = emptyList(),
    val binary: ByteArray?
) {
    // due to ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FhirPkvInvoiceErpModel

        if (taskId != other.taskId) return false
        if (timestamp != other.timestamp) return false
        if (organization != other.organization) return false
        if (lineItems != other.lineItems) return false
        if (!binary.contentEquals(other.binary)) return false

        return true
    }

    // due to ByteArray
    override fun hashCode(): Int {
        var result = taskId?.hashCode() ?: 0
        result = 31 * result + (timestamp?.hashCode() ?: 0)
        result = 31 * result + (organization?.hashCode() ?: 0)
        result = 31 * result + lineItems.hashCode()
        result = 31 * result + (binary?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class FhirPkvInvoiceChargeItemErpModel(
    val price: String?,
    val tax: String?,
    val factor: String?,
    val isPartialQuantityDelivery: Boolean,
    val spenderPzn: String?,
    val chargeItemCode: FhirChargeableItemCodeErpModel?
)
