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
import de.gematik.ti.erp.app.fhir.parser.containedInt
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

const val Denominator = 1000.0

typealias PkvDispenseFn<R> = (
    whenHandedOver: FhirTemporal
) -> R

typealias InvoiceFn<R> = (
    totalAdditionalFee: Double,
    totalBruttoAmount: Double,
    currency: String,
    items: List<InvoiceData.ChargeableItem>,
    additionalDispenseItem: InvoiceData.ChargeableItem?
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
                "1.2"
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
fun extractBinary(bundle: JsonElement): ByteArray? {
    return bundle.contained("signature").containedString("data").toByteArray()
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
            "1.2"
        ) -> extractInvoiceBundleVersion12(
            bundle,
            processDispense,
            processPharmacyAddress,
            processPharmacy,
            processInvoice,
            save
        )
    }
}

fun <Dispense, Pharmacy, PharmacyAddress, Invoice, R> extractInvoiceBundleVersion12(
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

    val additionalDispenseBundle = bundle.findAll("entry.resource")
        .filterWith(
            "meta.profile",
            stringValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenEinheit|1.2"
            )
        ).firstOrNull()

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
                "1.2"
            ) -> {
                dispense = extractPkvDispense(
                    resource,
                    processDispense
                )
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Apotheke",
                "1.2"
            ) -> {
                pharmacy = extractOrganization(
                    resource,
                    processPharmacy,
                    processPharmacyAddress
                )
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abrechnungszeilen",
                "1.2"
            ) -> {
                invoice = extractInvoice(
                    resource,
                    additionalDispenseBundle,
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
    billingLines: JsonElement,
    additionalDispenseJson: JsonElement?,
    processInvoice: InvoiceFn<Invoice>
): Invoice {
    val totalGross = billingLines.contained("totalGross")
    val currency = totalGross.containedString("currency")
    val totalBruttoAmount = totalGross.containedDouble("value")

    val totalAdditionalFee = billingLines
        .findAll("totalGross.extension")
        .filterWith(
            "url",
            stringValue("http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Gesamtzuzahlung")
        )
        .first()
        .contained("valueMoney")
        .containedDouble("value")

    val items = billingLines
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
                    val text = lineItem
                        .contained("chargeItemCodeableConcept").containedString("text")
                    val code = it.containedString("code")
                    val price = InvoiceData.PriceComponent(value, tax)
                    when (it.containedString("system")) {
                        "http://fhir.de/CodeSystem/ifa/pzn" ->
                            InvoiceData.ChargeableItem(
                                InvoiceData.ChargeableItem.Description.PZN(code),
                                text,
                                factor,
                                price
                            )

                        "http://TA1.abda.de" ->
                            InvoiceData.ChargeableItem(
                                InvoiceData.ChargeableItem.Description.TA1(code),
                                text,
                                factor,
                                price
                            )

                        "http://fhir.de/sid/gkv/hmnr" ->
                            InvoiceData.ChargeableItem(
                                InvoiceData.ChargeableItem.Description.HMNR(code),
                                text,
                                factor,
                                price
                            )

                        else -> null
                    }
                }

            item
        }.toList()

    val additionalDispenseItem = extractAdditionalDispenseItem(additionalDispenseJson)

    return processInvoice(
        totalAdditionalFee,
        totalBruttoAmount,
        currency,
        items,
        additionalDispenseItem
    )
}

fun extractAdditionalDispenseItem(
    additionalDispenseJson: JsonElement?
): InvoiceData.ChargeableItem? {
    return additionalDispenseJson?.let { additionalDispense ->
        val lineItem = additionalDispense.contained("lineItem")
        val text = "" // only Special indicator and its designation from the billing line are available
        val code = lineItem
            .findAll("chargeItemCodeableConcept.coding")
            .filterWith(
                "system",
                or(
                    stringValue("http://fhir.de/CodeSystem/ifa/pzn"),
                    stringValue("http://TA1.abda.de"),
                    stringValue("http://fhir.de/sid/gkv/hmnr")
                )
            )
            .first().containedString("code")

        val price = InvoiceData.PriceComponent(0.0, 0.0) // unused property TA_DAV_PKV 6.2.8.2

        val factor = lineItem.contained("priceComponent")
            .containedInt("factor") / Denominator // TA_DAV_PKV 6.2.8.2

        val item = when (
            lineItem.contained("chargeItemCodeableConcept")
                .contained("coding").containedString("system")
        ) {
            "http://fhir.de/CodeSystem/ifa/pzn" ->
                InvoiceData.ChargeableItem(
                    InvoiceData.ChargeableItem.Description.PZN(code),
                    text,
                    factor,
                    price
                )

            "http://TA1.abda.de" ->
                InvoiceData.ChargeableItem(
                    InvoiceData.ChargeableItem.Description.TA1(code),
                    text,
                    factor,
                    price
                )

            "http://fhir.de/sid/gkv/hmnr" ->
                InvoiceData.ChargeableItem(
                    InvoiceData.ChargeableItem.Description.HMNR(code),
                    text,
                    factor,
                    price
                )

            else -> null
        }
        item
    }
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
