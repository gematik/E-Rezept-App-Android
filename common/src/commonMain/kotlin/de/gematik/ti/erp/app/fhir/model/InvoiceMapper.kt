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
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedDouble
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.or
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.fhir.parser.toFhirTemporal
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonElement

typealias PkvDispenseFn<R> = (
    whenHandedOver: FhirTemporal
) -> R

typealias InvoiceFn<R> = (
    totalAdditionalFee: Double,
    totalBruttoAmount: Double,
    currency: String,
    items: List<InvoiceData.ChargeableItem>
) -> R

fun extractInvoiceKBVAndErpPrBundle(
    bundle: JsonElement,
    process: (
        taskId: String,
        invoiceBundle: JsonElement,
        kbvBundle: JsonElement,
        erpPrBundle: JsonElement
    ) -> Unit
) {
    val resources = bundle
        .findAll("entry.resource")

    lateinit var invoiceBundle: JsonElement
    lateinit var kbvBundle: JsonElement
    lateinit var erpPrBundle: JsonElement
    var taskId = ""

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle",
                "1.1"
            ) -> {
                taskId = resource
                    .findAll("identifier")
                    .filterWith(
                        "system",
                        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")
                    )
                    .firstOrNull()
                    ?.containedString("value") ?: ""

                invoiceBundle = resource
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
                "1.1.0"
            ) -> {
                kbvBundle = resource
            }

            profileString.isProfileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Bundle",
                "1.2"
            ) -> {
                erpPrBundle = resource
            }
        }
    }
    process(taskId, invoiceBundle, kbvBundle, erpPrBundle)
}
fun extractBinary(erpPrBundle: JsonElement): ByteArray? {
    return erpPrBundle.contained("signature").containedString("data").toByteArray()
}

fun <Dispense, Pharmacy, PharmacyAddress, Invoice> extractInvoiceBundle(
    bundle: JsonElement,
    processDispense: PkvDispenseFn<Dispense>,
    processPharmacyAddress: AddressFn<PharmacyAddress>,
    processPharmacy: OrganizationFn<Pharmacy, PharmacyAddress>,
    processInvoice: InvoiceFn<Invoice>,
    save: (
        taskId: String,
        timestamp: Instant,
        pharmacy: Pharmacy,
        invoice: Invoice,
        dispense: Dispense
    ) -> Unit
) {
    val profileString = bundle.contained("meta").contained("profile").contained()

    when {
        profileString.isProfileValue(
            "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle",
            "1.1"
        ) -> extractInvoiceBundleVersion11(
            bundle,
            processDispense,
            processPharmacyAddress,
            processPharmacy,
            processInvoice,
            save
        )
    }
}

fun <Dispense, Pharmacy, PharmacyAddress, Invoice, R> extractInvoiceBundleVersion11(
    bundle: JsonElement,
    processDispense: PkvDispenseFn<Dispense>,
    processPharmacyAddress: AddressFn<PharmacyAddress>,
    processPharmacy: OrganizationFn<Pharmacy, PharmacyAddress>,
    processInvoice: InvoiceFn<Invoice>,
    save: (
        taskId: String,
        timestamp: Instant,
        pharmacy: Pharmacy,
        invoice: Invoice,
        dispense: Dispense
    ) -> R
): R {
    val taskId = bundle.findAll("identifier").filterWith(
        "system",
        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")
    )
        .firstOrNull()
        ?.containedString("value")

    val timestamp = bundle.containedString("timestamp").toInstant()

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
        requireNotNull(taskId) { "TaskId missing" },
        timestamp,
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
                    val price = InvoiceData.PriceComponent(value, tax)
                    when (it.containedString("system")) {
                        "http://fhir.de/CodeSystem/ifa/pzn" ->
                            InvoiceData.ChargeableItem(InvoiceData.ChargeableItem.Description.PZN(code), factor, price)

                        "http://TA1.abda.de" ->
                            InvoiceData.ChargeableItem(InvoiceData.ChargeableItem.Description.TA1(code), factor, price)

                        "http://fhir.de/sid/gkv/hmnr" ->
                            InvoiceData.ChargeableItem(InvoiceData.ChargeableItem.Description.HMNR(code), factor, price)

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

fun extractTaskIdsFromChargeItemBundle(
    bundle: JsonElement
): Pair<Int, List<String>> {
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
    val resources = bundle
        .findAll("entry.resource")

    val taskIds = resources.mapNotNull { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue(
                "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_ChargeItem",
                "1.0"
            ) ->
                resource
                    .findAll("identifier")
                    .filterWith(
                        "system",
                        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")
                    )
                    .first()
                    .containedString("value")

            else -> null
        }
    }

    return bundleTotal to taskIds.toList()
}
