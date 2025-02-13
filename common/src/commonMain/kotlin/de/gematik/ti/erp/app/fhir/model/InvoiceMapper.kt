/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedBooleanOrNull
import de.gematik.ti.erp.app.fhir.parser.containedDouble
import de.gematik.ti.erp.app.fhir.parser.containedDoubleOrNull
import de.gematik.ti.erp.app.fhir.parser.containedInt
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.or
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.toFhirTemporal
import de.gematik.ti.erp.app.utils.toFormattedDateTime
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

const val Denominator = 1000.0
const val IndicatorUrl =
    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-ZusatzdatenFaktorkennzeichen"

const val AdditionalProductionUrl12 =
    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenHerstellung|1.2"

const val AdditionalProductionUrl13 =
    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenHerstellung|1.3"

typealias PkvDispenseFn<R> = (
    whenHandedOver: FhirTemporal
) -> R

typealias InvoiceFn<R> = (
    totalAdditionalFee: Double,
    totalBruttoAmount: Double,
    currency: String,
    items: List<InvoiceData.ChargeableItem>,
    additionalDispenseItems: List<InvoiceData.ChargeableItem>,
    additionalInformation: List<String>
) -> R

@Requirement(
    "O.Source_2#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Sanitization is also done for all FHIR mapping."
)
fun extractInvoiceKBVAndErpPrBundle(
    bundle: JsonElement,
    process: (
        taskId: String,
        accessCode: String,
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
    var accessCode = ""

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue(
                "https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_ChargeItem",
                "1.0"
            ) -> {
                taskId = resource
                    .findAll("identifier")
                    .filterWith(
                        "system",
                        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")
                    )
                    .firstOrNull()
                    ?.containedString("value") ?: ""

                accessCode = resource
                    .findAll("identifier")
                    .filterWith(
                        "system",
                        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode")
                    )
                    .firstOrNull()
                    ?.containedString("value") ?: ""
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle",
                "1.2",
                "1.3"
            ) -> {
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
                "1.2",
                "1.3",
                "1.4"
            ) -> {
                erpPrBundle = resource
            }
        }
    }
    process(taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle)
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
            "1.2",
            "1.3"
        ) -> extractInvoiceBundleVersion12And13(
            bundle,
            processDispense,
            processPharmacyAddress,
            processPharmacy,
            processInvoice,
            save
        )
    }
}

fun <Dispense, Pharmacy, PharmacyAddress, Invoice, R> extractInvoiceBundleVersion12And13(
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

    val timestamp = Instant.parse(bundle.containedString("timestamp"))

    val additionalDispenses = bundle.findAll("entry.resource")
        .filterWith(
            "meta.profile",
            or(
                stringValue(
                    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenEinheit|1.2"
                ),
                stringValue(
                    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-ZusatzdatenEinheit|1.3"
                )
            )
        )

    val additionalDispenseData = bundle.findAll("entry.resource")
        .filterWith(
            "meta.profile",
            or(
                stringValue(
                    AdditionalProductionUrl12
                ),
                stringValue(
                    AdditionalProductionUrl13
                )
            )
        )

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
                "1.2",
                "1.3"
            ) -> {
                dispense = extractPkvDispense(
                    resource,
                    processDispense
                )
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Apotheke",
                "1.2",
                "1.3"
            ) -> {
                pharmacy = extractOrganization(
                    resource,
                    processPharmacy,
                    processPharmacyAddress
                )
            }

            profileString.isProfileValue(
                "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abrechnungszeilen",
                "1.2",
                "1.3"
            ) -> {
                invoice = extractInvoice(
                    resource,
                    additionalDispenses,
                    additionalDispenseData,
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
    additionalDispensesJson: Sequence<JsonElement>,
    additionalDispenseDataJson: Sequence<JsonElement>,
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

            // Handle Teilmengenabgabe (partial quantity dispensing) for v1.3
            val partialQuantityDelivery = lineItem
                .findAll("extension")
                .filterWith(
                    "url",
                    stringValue("http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Zusatzattribute")
                )
                .firstOrNull()
                ?.findAll("extension")
                ?.filterWith(
                    "url",
                    stringValue("ZusatzattributTeilmengenabgabe")
                )
                ?.firstOrNull()
                ?.findAll("extension")
                ?.filterWith(
                    "url",
                    stringValue("Schluessel")
                )
                ?.firstOrNull()
                ?.containedBooleanOrNull("valueBoolean")
                ?: false

            val spenderPzn = lineItem
                .findAll("extension")
                .filterWith(
                    "url",
                    stringValue(
                        "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Zusatzattribute"
                    )
                )
                .firstOrNull()
                ?.findAll("extension")
                ?.filterWith(
                    "url",
                    stringValue("ZusatzattributTeilmengenabgabe")
                )
                ?.firstOrNull()
                ?.findAll("extension")
                ?.filterWith(
                    "url",
                    stringValue("Spender-PZN")
                )
                ?.firstOrNull()
                ?.contained("valueCodeableConcept")
                ?.contained("coding")
                ?.containedString("code")
                .takeIf { partialQuantityDelivery }

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
                                price,
                                partialQuantityDelivery,
                                spenderPzn
                            )

                        "http://TA1.abda.de" ->
                            InvoiceData.ChargeableItem(
                                InvoiceData.ChargeableItem.Description.TA1(code),
                                text,
                                factor,
                                price,
                                partialQuantityDelivery,
                                spenderPzn
                            )

                        "http://fhir.de/sid/gkv/hmnr" ->
                            InvoiceData.ChargeableItem(
                                InvoiceData.ChargeableItem.Description.HMNR(code),
                                text,
                                factor,
                                price,
                                partialQuantityDelivery,
                                spenderPzn
                            )

                        else -> null
                    }
                }

            item
        }.toList()
    var additionalDispenses = listOf<InvoiceData.ChargeableItem>()
    var additionalInformation = listOf<String>()

    when (items[0].description) {
        InvoiceData.ChargeableItem.Description.TA1("02567053") -> // separation
            additionalDispenses = chargeableItems(additionalDispensesJson)
        InvoiceData.ChargeableItem.Description.TA1("09999092") -> // parentale Zytostatica
            additionalInformation = joinZytostaticaProductionSteps(additionalDispensesJson, additionalDispenseDataJson)
        else -> // must be compounding
            additionalInformation = listOf(joinComponents(additionalDispensesJson))
    }
    return processInvoice(
        totalAdditionalFee,
        totalBruttoAmount,
        currency,
        items,
        additionalDispenses,
        additionalInformation
    )
}

fun joinZytostaticaProductionSteps(
    additionalDispensesJson: Sequence<JsonElement>,
    additionalDispensDataJson: Sequence<JsonElement>
): List<String> {
    val additionalInformation: MutableList<String> = mutableListOf()
    var productionItems = ""
    val dispenseData = additionalDispensDataJson.toList()
    val dispenses = additionalDispensesJson.toList()

    if (dispenseData.isNotEmpty()) {
        additionalInformation.add("Bestandteile (Nettopreise):")
        dispenseData.forEachIndexed { index, data ->
            val whenPrepared =
                data.containedString("whenPrepared").toFhirTemporal().toInstant().toFormattedDateTime()
            val reference = data.findAll("extension").filterWith(
                "url",
                stringValue(
                    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-ZusatzdatenEinheit"
                )
            ).firstOrNull()?.contained("valueReference")
                ?.containedString("reference")?.removePrefix("urn:uuid:")

            val dispense = dispenses.filter {
                it.containedString("id") == reference
            }.first()

            val productionStep = index + 1

            productionItems = joinProductionItems(dispense)

            additionalInformation.add("Herstellung $productionStep - $whenPrepared: $productionItems")
        }
    }

    return additionalInformation
}

fun joinProductionItems(additionalDispensesJson: JsonElement): String {
    val productionItems: MutableList<String> = mutableListOf()
    val items = additionalDispensesJson.findAll("lineItem").toList()

    items.forEachIndexed { index, lineItem ->
        productionItems += additionalDispensesJson.findAll("extension")
            .filterWith(
                "url",
                stringValue(
                    "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Zaehler"
                )
            ).first().containedInt("valuePositiveInt").toString()

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
        productionItems += code

        val indicator = lineItem
            .contained("priceComponent").findAll("extension")
            .filterWith(
                "url",
                stringValue(IndicatorUrl)
            )
            .first().contained("valueCodeableConcept").contained("coding").containedString("code")
        productionItems += indicator

        val factor = lineItem.contained("priceComponent")
            .containedDouble("factor") / Denominator // TA_DAV_PKV 6.2.8.2
        productionItems += factor.toString()

        val value = lineItem
            .contained("priceComponent")
            .contained("amount")
            .containedDouble("value")
        productionItems += value.toString() + "€"

        if (index + 1 != items.count()) {
            productionItems += "/"
        }
    }
    return productionItems.joinToString(" ")
}

fun joinComponents(additionalDispensesJson: Sequence<JsonElement>): String {
    val components = mutableListOf("Bestandteile:")
    val items = chargeableItems(additionalDispensesJson)
    val itemsSize = items.size
    items.forEachIndexed { index, item ->
        when (item.description) {
            is InvoiceData.ChargeableItem.Description.PZN -> components += item.description.pzn
            is InvoiceData.ChargeableItem.Description.TA1 -> components += item.description.ta1
            is InvoiceData.ChargeableItem.Description.HMNR -> components += item.description.hmnr
        }
        components += item.factor.toString()
        components += item.price.value.toString()
        if (index + 1 != itemsSize) {
            components += "/"
        }
    }
    components += " (Nettopreise)"

    return components.joinToString(" ")
}

private fun chargeableItems(additionalDispensesJson: Sequence<JsonElement>) =
    additionalDispensesJson.findAll("lineItem")
        .mapNotNull { lineItem ->
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
                .firstOrNull()
                ?.containedDoubleOrNull("valueDecimal") ?: 0.0

            val price = InvoiceData.PriceComponent(value, tax)

            val factor = lineItem.contained("priceComponent")
                .containedDouble("factor") / Denominator // TA_DAV_PKV 6.2.8.2

            val item = chargeableItem(lineItem, code, text, factor, price)
            item
        }.toList()

private fun chargeableItem(
    lineItem: JsonElement,
    code: String,
    text: String,
    factor: Double,
    price: InvoiceData.PriceComponent
) = when (
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
