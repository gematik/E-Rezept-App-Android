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

package de.gematik.ti.erp.app.fhir.pkv.parser

import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItem
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleEntries
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleEntry
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.GEM_ERP_INVOICE_BINARY_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.GEM_ERP_KBV_PR_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.GEM_ERP_PKV_PR_MEDICATION_DISPENSE_URL
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvKbvBinaryErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.erp.toFhirPkvChargeItemErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundle.Companion.getBaseBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType.ChargeItem
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType.Invoice
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType.InvoiceBinary
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType.KBV
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType.MedicationDispense
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvBaseBundleType.Unknown
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBinaryBundle.Companion.getPkvInvoiceBinaryBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBinaryBundle.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundle.Companion.getPkvInvoiceBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundle.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundleSignature.Companion.getBinary
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundleSignature.Companion.getPkvInvoiceBundleSignature
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceMedicationDispenseBundle.Companion.getPkvInvoiceMedicationDispenseBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceMedicationDispenseBundle.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskMedicalDataParser
import de.gematik.ti.erp.app.utils.sanitize
import io.github.aakira.napier.Napier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class ChargeItemBundleParser(
    val taskMedicalDataParser: TaskMedicalDataParser = TaskMedicalDataParser()
) : BundleParser {

    private val loggerTag = "ChargeItemBundleParser"

    override fun extract(bundle: JsonElement): FhirPkvChargeItemsErpModelCollection {
        // 1) Enrich and type all entries once
        val enrichedEntries: List<FhirPkvBaseBundle> = bundle
            .getBundleEntries()
            .asSequence()
            .mapNotNull { entry -> entry.resource.getBaseBundle()?.updateBaseBundle(entry) }
            .toList()

        // 2) Collect charge items
        val chargeItems = enrichedEntries
            .asSequence()
            .filter { it.bundleType == ChargeItem }
            .map { it.toFhirPkvChargeItemErpModel() }
            .toList()

        // 3) Filter the non-charge item bundles
        val dataBundles = enrichedEntries.filterNot { it.bundleType == ChargeItem }

        // 4) Filter the dispense bundles
        val dispenseBundles =
            enrichedEntries.filter { it.bundleType == MedicationDispense }

        // 5) Build result structure (keeps your current List<Map<Item, List<...>>> shape)
        val sortedItems = chargeItems.map { item ->
            // original bundles
            val kbvBundle = dataBundles.findBundle(item.kbvReference)?.originalBundle
            val invoiceBinaryBundle = dataBundles.findBundle(item.invoiceBinaryReference)?.originalBundle
            val invoiceBundle = dataBundles.findBundle(item.invoiceReference)?.originalBundle
            val medicationDispenseBundle = dispenseBundles.find { it.identifier.first().value == item.taskId }?.originalBundle
            val kbvBinaryBundle = kbvBundle?.jsonObject["signature"]

            Napier.d(tag = loggerTag) { "KBV Bundle:\n${kbvBundle?.formatAsJson()}\n" }
            Napier.d(tag = loggerTag) { "Invoice Binary Bundle:\n${invoiceBinaryBundle?.formatAsJson()}\n" }
            Napier.d(tag = loggerTag) { "Invoice Bundle:\n${invoiceBundle?.formatAsJson()}\n" }
            Napier.d(tag = loggerTag) { "Medication Dispense:\n${medicationDispenseBundle?.formatAsJson()}\n" }

            FhirPkvChargeItem(
                taskId = item.taskId,
                accessCode = item.accessCode,
                kbvDataErpModel = kbvBundle?.let { taskMedicalDataParser.extract(it) },
                invoiceErpModel = invoiceBundle?.getPkvInvoiceBundle()?.toErpModel(),
                invoiceBinaryErpModel = invoiceBinaryBundle?.getPkvInvoiceBinaryBundle()?.toErpModel(),
                medicationDispenseErpModel = medicationDispenseBundle?.getPkvInvoiceMedicationDispenseBundle()?.toErpModel(),
                kbvBinaryErpModel = FhirPkvKbvBinaryErpModel(kbvBinaryBundle?.getPkvInvoiceBundleSignature()?.getBinary())
            )
        }

        Napier.d(tag = loggerTag) { "sortedItems $sortedItems" }
        return FhirPkvChargeItemsErpModelCollection(sortedItems)
    }

    private fun List<FhirPkvBaseBundle>.findBundle(url: String?): FhirPkvBaseBundle? {
        val bundleById = find { it.id == url }
        val bundleByFullUrl = find { it.fullUrl?.sanitize() == url }
        return bundleById ?: bundleByFullUrl
    }

    private fun FhirPkvBaseBundle.updateBaseBundle(
        entry: FhirBundleEntry
    ) = copy(
        fullUrl = entry.fullUrl,
        originalBundle = entry.resource,
        bundleType = getBundleType()
    )

    private fun FhirPkvBaseBundle.getBundleType(): FhirPkvBaseBundleType {
        val profileBase = meta?.profiles?.firstOrNull()?.trim()?.substringBefore('|')
        return when {
            resourceType == ChargeItem.name -> ChargeItem
            resourceType == MedicationDispense.name && profileBase.startsWithSafe(
                GEM_ERP_PKV_PR_MEDICATION_DISPENSE_URL
            ) -> MedicationDispense

            resourceType == Invoice.name && profileBase.startsWithSafe(
                DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL
            ) -> Invoice

            else -> when {
                profileBase.startsWithSafe(GEM_ERP_KBV_PR_BUNDLE_URL) -> KBV
                profileBase.startsWithSafe(GEM_ERP_INVOICE_BINARY_BUNDLE_URL) -> InvoiceBinary
                else -> Unknown
            }
        }
    }

    private fun String?.startsWithSafe(prefix: String): Boolean =
        this != null && startsWith(prefix)

    @OptIn(ExperimentalSerializationApi::class)
    private val prettyPrintJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  " // 2 spaces, change if you want tabs or 4 spaces
    }

    private fun JsonElement.formatAsJson(): String =
        prettyPrintJson.encodeToString(JsonElement.serializer(), this)
}
