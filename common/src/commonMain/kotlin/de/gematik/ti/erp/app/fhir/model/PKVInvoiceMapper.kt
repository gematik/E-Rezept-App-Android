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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedDouble
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.or
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.fhir.parser.toFhirTemporal
import kotlinx.serialization.json.JsonElement

enum class SpecialPZN(val pzn: String) {
    EmergencyServiceFee("02567018"),
    BTMFee("02567001"),
    TPrescriptionFee("06460688"),
    ProvisioningCosts("09999637"),
    DeliveryServiceCosts("06461110");

    companion object {
        fun isAnyOf(pzn: String): Boolean = values().any { it.pzn == pzn }

        fun valueOfPZN(pzn: String) = SpecialPZN.values().find { it.pzn == pzn }
    }
}

data class ChargeableItem(val description: Description, val factor: Double, val price: PriceComponent) {
    sealed interface Description {
        data class PZN(val pzn: String) : Description {
            fun isSpecialPZN() = SpecialPZN.isAnyOf(pzn)
        }

        data class TA1(val ta1: String) : Description

        data class HMNR(val hmnr: String) : Description
    }
}

data class PriceComponent(val value: Double, val tax: Double)

typealias PkvDispenseFn<R> = (
    whenHandedOver: FhirTemporal
) -> R

typealias InvoiceFn<R> = (
    totalAdditionalFee: Double,
    totalBruttoAmount: Double,
    currency: String,
    items: List<ChargeableItem>
) -> R

fun <Dispense, Pharmacy, PharmacyAddress, Invoice, R> extractPKVInvoiceBundle(
    bundle: JsonElement,
    processDispense: PkvDispenseFn<Dispense>,
    processPharmacyAddress: AddressFn<PharmacyAddress>,
    processPharmacy: OrganizationFn<Pharmacy, PharmacyAddress>,
    processInvoice: InvoiceFn<Invoice>,
    save: (
        pharmacy: Pharmacy,
        invoice: Invoice,
        dispense: Dispense
    ) -> R
): R? {
    val profileString = bundle.contained("meta").contained("profile").contained()
    return when {
        profileString.isProfileValue(
            "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle",
            "1.1"
        ) -> extractPKVInvoiceBundleVersion11(
            bundle,
            processDispense,
            processPharmacyAddress,
            processPharmacy,
            processInvoice,
            save
        )

        else -> null
    }
}

fun <Dispense, Pharmacy, PharmacyAddress, Invoice, R> extractPKVInvoiceBundleVersion11(
    bundle: JsonElement,
    processDispense: PkvDispenseFn<Dispense>,
    processPharmacyAddress: AddressFn<PharmacyAddress>,
    processPharmacy: OrganizationFn<Pharmacy, PharmacyAddress>,
    processInvoice: InvoiceFn<Invoice>,
    save: (
        pharmacy: Pharmacy,
        invoice: Invoice,
        dispense: Dispense
    ) -> R
): R {
    val resources = bundle
        .findAll("entry.resource")

    var dispense: Dispense? = null
    var pharmacy: Pharmacy? = null
    var invoice: Invoice? = null

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abgabeinformationen",
                "1.1"
            ) -> {
                dispense = extractPkvDispense(
                    resource,
                    processDispense
                )
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Apotheke",
                "1.1"
            ) -> {
                pharmacy = extractOrganization(
                    resource,
                    processPharmacy,
                    processPharmacyAddress
                )
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abrechnungszeilen",
                "1.1"
            ) -> {
                invoice = extractInvoice(
                    resource,
                    processInvoice
                )
            }
        }
    }

    return save(
        requireNotNull(pharmacy) { "Pharmacy missing" },
        requireNotNull(invoice) { "Invoice missing" },
        requireNotNull(dispense) { "Dispense missing" }
    )
}

fun <Dispense> extractPkvDispense(
    dispense: JsonElement,
    processDispense: PkvDispenseFn<Dispense>
): Dispense {
    return processDispense(
        dispense.containedString("whenHandedOver").toFhirTemporal()
    )
}

fun <Invoice> extractInvoice(
    invoice: JsonElement,
    processInvoice: InvoiceFn<Invoice>
): Invoice {
    val totalGross = invoice.contained("totalGross")
    val currency = totalGross.containedString("currency")
    val totalBruttoAmount = totalGross.containedDouble("value")

    val totalAdditionalFee = invoice
        .findAll("totalGross.extension")
        .filterWith(
            "url",
            stringValue("http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Gesamtzuzahlung")
        )
        .first()
        .contained("valueMoney")
        .containedDouble("value")

    val items = invoice
        .findAll("lineItem")
        .mapNotNull { lineItem ->
            val value = lineItem
                .contained("priceComponent")
                .contained("amount")
                .containedDouble("value")

            val tax = lineItem
                .findAll("priceComponent.extension")
                .filterWith(
                    "url",
                    stringValue("http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-MwStSatz")
                )
                .first()
                .containedDouble("valueDecimal")

            val factor = lineItem
                .contained("priceComponent")
                .containedDouble("factor")

            val item = lineItem
                .findAll("chargeItemCodeableConcept.coding")
                .filterWith(
                    "system",
                    or(
                        stringValue("http://fhir.de/CodeSystem/ifa/pzn"),
                        stringValue("http://TA1.abda.de"),
                        stringValue("http://fhir.de/sid/gkv/hmnr")
                    )
                )
                .firstOrNull()
                ?.let {
                    val code = it.containedString("code")
                    val price = PriceComponent(value, tax)
                    when (it.containedString("system")) {
                        "http://fhir.de/CodeSystem/ifa/pzn" ->
                            ChargeableItem(ChargeableItem.Description.PZN(code), factor, price)

                        "http://TA1.abda.de" ->
                            ChargeableItem(ChargeableItem.Description.TA1(code), factor, price)

                        "http://fhir.de/sid/gkv/hmnr" ->
                            ChargeableItem(ChargeableItem.Description.HMNR(code), factor, price)

                        else -> null
                    }
                }

            item
        }.toList()

    return processInvoice(
        totalAdditionalFee,
        totalBruttoAmount,
        currency,
        items
    )
}
