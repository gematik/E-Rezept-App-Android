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

package de.gematik.ti.erp.app.invoice.model

import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PKVInvoiceErpModel(
    val profileId: String,
    val taskId: String,
    val accessCode: String,
    val timestamp: Instant,
    val pharmacyOrganization: OrganizationErpModel?,
    val practitionerOrganization: OrganizationErpModel?,
    val practitioner: PractitionerErpModel?,
    val patient: PatientErpModel?,
    val medicationRequest: MedicationRequestErpModel?,
    val whenHandedOver: FhirTemporal?,
    val consumed: Boolean,
    val invoice: InvoiceErpModel?,
    val invoiceBinary: ByteArray? = null,
    val kbvBinary: ByteArray? = null,
    val erpPrBinary: ByteArray? = null
) {
    val dmcPayload: String
        get() = "{\"urls\":[\"ChargeItem/$taskId?ac=$accessCode\"]}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PKVInvoiceErpModel) return false

        if (profileId != other.profileId) return false
        if (taskId != other.taskId) return false
        if (accessCode != other.accessCode) return false
        if (timestamp != other.timestamp) return false
        if (pharmacyOrganization != other.pharmacyOrganization) return false
        if (practitionerOrganization != other.practitionerOrganization) return false
        if (practitioner != other.practitioner) return false
        if (patient != other.patient) return false
        if (medicationRequest != other.medicationRequest) return false
        if (whenHandedOver != other.whenHandedOver) return false
        if (consumed != other.consumed) return false
        if (invoice != other.invoice) return false
        if (invoiceBinary != null) {
            if (other.invoiceBinary == null) return false
            if (!invoiceBinary.contentEquals(other.invoiceBinary)) return false
        } else if (other.invoiceBinary != null) return false
        if (kbvBinary != null) {
            if (other.kbvBinary == null) return false
            if (!kbvBinary.contentEquals(other.kbvBinary)) return false
        } else if (other.kbvBinary != null) return false
        if (erpPrBinary != null) {
            if (other.erpPrBinary == null) return false
            if (!erpPrBinary.contentEquals(other.erpPrBinary)) return false
        } else if (other.erpPrBinary != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profileId.hashCode()
        result = 31 * result + taskId.hashCode()
        result = 31 * result + accessCode.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (pharmacyOrganization?.hashCode() ?: 0)
        result = 31 * result + (practitionerOrganization?.hashCode() ?: 0)
        result = 31 * result + (practitioner?.hashCode() ?: 0)
        result = 31 * result + (patient?.hashCode() ?: 0)
        result = 31 * result + (medicationRequest?.hashCode() ?: 0)
        result = 31 * result + (whenHandedOver?.hashCode() ?: 0)
        result = 31 * result + consumed.hashCode()
        result = 31 * result + (invoice?.hashCode() ?: 0)
        result = 31 * result + (invoiceBinary?.contentHashCode() ?: 0)
        result = 31 * result + (kbvBinary?.contentHashCode() ?: 0)
        result = 31 * result + (erpPrBinary?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class InvoiceErpModel(
    val totalAdditionalFee: Double,
    val totalBruttoAmount: Double,
    val currency: String,
    val chargeableItems: List<ChargeableItemErpModel> = listOf(),
    val additionalDispenseItems: List<ChargeableItemErpModel> = listOf(),
    val additionalInformation: List<String> = listOf()
)

@Serializable
data class ChargeableItemErpModel(
    val description: ChargeableItemDescriptionErpModel,
    val text: String,
    val factor: Double,
    val price: PriceComponentErpModel?
)

@Serializable
sealed interface ChargeableItemDescriptionErpModel {
    val value: String

    @Serializable
    data class PZN(override val value: String) : ChargeableItemDescriptionErpModel

    @Serializable
    data class HMNR(override val value: String) : ChargeableItemDescriptionErpModel

    @Serializable
    data class TA1(override val value: String) : ChargeableItemDescriptionErpModel
}

@Serializable
data class PriceComponentErpModel(val value: Double, val tax: Double)

@Serializable
data class InvoiceStatusErpModel(val taskId: String, val consumed: Boolean)
