/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.invoice.model

import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant

object InvoiceData {

    enum class SpecialPZN(val value: String) {
        EmergencyServiceFee("02567018"),
        BTMFee("02567001"),
        TPrescriptionFee("06460688"),
        ProvisioningCosts("09999637"),
        DeliveryServiceCosts("06461110");

        companion object {
            fun isAnyOf(pzn: String): Boolean = values().any { it.value == pzn }

            fun valueOfPZN(pzn: String) = SpecialPZN.values().find { it.value == pzn }
        }
    }

    data class PKVInvoice(
        val profileId: String,
        val taskId: String,
        val timestamp: Instant,
        val pharmacyOrganization: SyncedTaskData.Organization,
        val practitionerOrganization: SyncedTaskData.Organization,
        val practitioner: SyncedTaskData.Practitioner,
        var patient: SyncedTaskData.Patient,
        val medicationRequest: SyncedTaskData.MedicationRequest,
        val whenHandedOver: FhirTemporal?,
        val invoice: Invoice
    )

    data class Invoice(
        val totalAdditionalFee: Double,
        val totalBruttoAmount: Double,
        val currency: String,
        val chargeableItems: List<ChargeableItem> = listOf(),
        val additionalDispenseItems: List<ChargeableItem> = listOf(),
        val additionalInformation: List<String> = listOf()
    )

    data class ChargeableItem(
        val description: Description,
        val text: String,
        val factor: Double,
        val price: PriceComponent
    ) {

        sealed interface Description {
            data class PZN(val pzn: String) : Description {
                fun isSpecialPZN() = SpecialPZN.isAnyOf(pzn)
            }

            data class TA1(val ta1: String) : Description {
                fun isSpecialPZN() = SpecialPZN.isAnyOf(ta1)
            }

            data class HMNR(val hmnr: String) : Description {
                fun isSpecialPZN() = SpecialPZN.isAnyOf(hmnr)
            }
        }
    }

    data class PriceComponent(val value: Double, val tax: Double)
}
