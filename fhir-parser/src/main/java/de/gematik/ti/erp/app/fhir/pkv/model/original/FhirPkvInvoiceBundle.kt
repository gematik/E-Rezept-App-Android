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

package de.gematik.ti.erp.app.fhir.pkv.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirAddress
import de.gematik.ti.erp.app.fhir.common.model.original.FhirAddress.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding.Companion.firstBySystem
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta.Companion.byProfile
import de.gematik.ti.erp.app.fhir.common.model.original.extractProfilesFromResourceMeta
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.toFormattedDateTime
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.SafeResourceTypeDiscriminatorJson
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvChargeItemConstants.TASKID_SYSTEM_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvCodeSystems
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvConstants.DAV_PR_ERP_INVOICE_BUNDLE_V_1_5_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceBundleConstats.DISPENSE_COMPOUNDING_DETAILS_PROFILE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceBundleConstats.DISPENSE_INFORMATION_PROFILE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceBundleConstats.FACTOR_FLAG
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceBundleConstats.INVOICE_LINE_ITEMS_PROFILE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceBundleConstats.INVOICE_SUPPLEMENTARY_UNIT_PROFILE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceBundleConstats.MANUFACTURING_STEP_COUNTER_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.ADDITIONAL_ATTRIBUTES_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.ADDITIONAL_ATTR_KEY_NAME
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.ADDITIONAL_ATTR_PARTIAL_QUANTITY_NAME
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.CHARGE_ITEM_TYPE_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.COMPONENTS_LABEL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.COMPONENTS_NET_PRICES_LABEL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.INVOICE_TOTAL_COPAYMENT_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.NET_PRICES_SUFFIX
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.PRODUCTION_STEP_TEMPLATE
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.SPENDER_PZN_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.SPENDER_PZN_KEY_NAME
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.SPENDER_PZN_PARTIAL_QUANTITY_NAME
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.TA1_PARENTERAL_CYTOSTATICS_CODE
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.TA1_SEPARATION_BILLING_CODE
import de.gematik.ti.erp.app.fhir.constant.pkv.FhirPkvInvoiceConstants.VAT_RATE_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceChargeItemErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundleOrganization.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundleSignature.Companion.getBinary
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceLineItem.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceTotalGross.Companion.getAdditionalFee
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvPriceComponent.Companion.getTax
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganizationBase
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom.Companion.getEmail
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom.Companion.getFax
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom.Companion.getPhone
import de.gematik.ti.erp.app.fhir.support.ChargeItemType
import de.gematik.ti.erp.app.fhir.support.FhirChargeableItemCodeErpModel
import de.gematik.ti.erp.app.fhir.support.FhirCostErpModel
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import de.gematik.ti.erp.app.utils.Reference
import de.gematik.ti.erp.app.utils.sanitize
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Marker interface for resources that may occur inside a [FhirPkvInvoiceBundle].
 *
 * According to the DAV PKV eRezept Abgabedaten specification,
 * invoice bundles may contain:
 * - `Composition`
 * - `MedicationDispense`
 * - `Invoice`
 * - `Organization`
 */
@Serializable
internal sealed interface FhirPkvInvoiceBundleResource

/**
 * FHIR Bundle resource representing a **PKV invoice bundle**.
 *
 * Defined by [DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL](https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.4.0/files/2780090),
 * it encapsulates all resources required for billing a privately insured
 * ePrescription redemption.
 *
 * @property resourceType Always `"Bundle"`.
 * @property id Logical identifier of the bundle.
 * @property meta Metadata including applied profile reference.
 * @property identifier Business identifier of the invoice bundle.
 * @property entries Contained resources (Composition, MedicationDispense, Invoice, Organization).
 * @property signature Optional signature ensuring authenticity.
 */
@Reference(
    info = "Parses invoice bundle",
    url = "https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.4.0/files/2780090"
)
@Serializable
internal data class FhirPkvInvoiceBundle(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifier: FhirIdentifier? = null,
    @SerialName("entry") val entries: List<FhirPkvInvoiceBundleEntry> = emptyList(),
    @SerialName("signature") val signature: FhirPkvInvoiceBundleSignature? = null
) {
    companion object {
        /**
         * Attempts to parse a [JsonElement] into a [FhirPkvInvoiceBundle].
         *
         * Only valid if the `meta.profile` contains the official
         * DAV PKV invoice bundle URL ([DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL]).
         * Logs and returns `null` if validation or decoding fails.
         */
        fun JsonElement.getPkvInvoiceBundle(): FhirPkvInvoiceBundle? {
            val profile = extractProfilesFromResourceMeta(this)
            val isValidInvoice = profile.any {
                it.contains(DAV_PKV_PR_ERP_INVOICE_BUNDLE_URL) ||
                    it.contains(DAV_PR_ERP_INVOICE_BUNDLE_V_1_5_URL)
            }
            if (!isValidInvoice) {
                return null.also {
                    Napier.e(tag = "fhir-parser") { "Invalid invoice bundle" }
                }
            }

            return try {
                SafeResourceTypeDiscriminatorJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.w(
                    tag = "fhir-parser",
                    message = "Error parsing FHIR PkvInvoiceBundle: ${e.message}"
                )
                null
            }
        }

        /**
         * Retrieves the first `Organization` resource from the bundle entries.
         *
         * @return the [FhirPkvInvoiceBundleOrganization] if present, or `null` otherwise.
         */
        private fun List<FhirPkvInvoiceBundleEntry>.getOrganization(): FhirPkvInvoiceBundleOrganization? {
            return find { it.resource is FhirPkvInvoiceBundleOrganization }
                ?.resource as? FhirPkvInvoiceBundleOrganization
        }

        private fun List<FhirPkvInvoiceBundleEntry>.getDispense(): FhirPkvInvoiceBundleMedicationDispense? {
            return find { it.resource is FhirPkvInvoiceBundleMedicationDispense }
                ?.resource as? FhirPkvInvoiceBundleMedicationDispense
        }

        /**
         * Collects all additional invoice information from a list of invoices.
         *
         * Internally delegates to [FhirPkvInvoiceBundleInvoice.additionalInvoiceInformation]
         * for each invoice and flattens the results into a single list.
         *
         * @return list of additional charge item models, possibly empty.
         */
        private fun List<FhirPkvInvoiceBundleInvoice>.additionalInvoiceInformation(): List<FhirPkvInvoiceChargeItemErpModel> =
            mapNotNull { it.additionalInvoiceInformation() }.flatten()

        /**
         * Extracts additional invoice information from a single invoice.
         *
         * According to TA_DAV_PKV 6.2.8.2, the `factor` must be scaled down by 1000.
         *
         * @return list of charge items with normalized factor values, or `null` if none exist.
         */
        private fun FhirPkvInvoiceBundleInvoice.additionalInvoiceInformation(): List<FhirPkvInvoiceChargeItemErpModel>? =
            lineItem.toErpModel().map { lineItem ->
                // TA_DAV_PKV 6.2.8.2
                lineItem.copy(
                    factor = lineItem.factor?.toDouble()?.let { numerator -> numerator / 1000.0 }
                        .toString()
                )
            }

        /**
         * Filters this list of invoices by matching the given profile URL and optional version.
         *
         * @param url profile URL to match (default: supplementary invoice unit profile).
         * @param version optional version string to match; if `null` all versions are accepted.
         * @return list of invoices conforming to the given profile.
         */
        private fun List<FhirPkvInvoiceBundleInvoice>.invoiceByProfile(
            url: String = INVOICE_SUPPLEMENTARY_UNIT_PROFILE_URL,
            version: String? = null
        ): List<FhirPkvInvoiceBundleInvoice> =
            filter { inv -> inv.meta?.byProfile(url, version) == true }

        /**
         * Filters this list of `MedicationDispense` resources by matching the given profile URL
         * and optional version.
         *
         * @param url profile URL to match (default: compounding details profile).
         * @param version optional version string to match; if `null` all versions are accepted.
         * @return list of dispenses conforming to the given profile.
         */
        private fun List<FhirPkvInvoiceBundleMedicationDispense>.dispenseByProfile(
            url: String = DISPENSE_COMPOUNDING_DETAILS_PROFILE_URL,
            version: String? = null
        ): List<FhirPkvInvoiceBundleMedicationDispense> =
            filter { inv -> inv.meta?.byProfile(url, version) == true }

        /**
         * Builds additional information text for parenteral cytostatics preparations.
         *
         * Each `MedicationDispense` is linked back to its referenced invoice and expanded into
         * a human-readable production step string containing:
         * - preparation time,
         * - invoice step counter,
         * - PZN/TA1/HMNR codes,
         * - factor flag,
         * - and price amounts.
         *
         * @param additionalInvoices supplementary invoices available in the bundle.
         * @param additionalMedicalDispenses dispenses carrying compounding/manufacturing details.
         * @return list of descriptive strings, one for each production step.
         */
        private fun buildParenteralCytostaticsInformation(
            additionalInvoices: List<FhirPkvInvoiceBundleInvoice>,
            additionalMedicalDispenses: List<FhirPkvInvoiceBundleMedicationDispense>
        ): List<String> {
            if (additionalMedicalDispenses.isEmpty()) return emptyList()

            val additionalInformation = mutableListOf<String>()
            additionalInformation.add(COMPONENTS_NET_PRICES_LABEL)

            additionalMedicalDispenses.forEachIndexed { index, medicationDispense ->
                val whenPrepared = medicationDispense.whenPrepared
                    ?.asFhirTemporal()?.toInstant()?.toFormattedDateTime()

                val reference = medicationDispense.extensions
                    .findExtensionByUrl(DISPENSE_INFORMATION_PROFILE_URL)
                    ?.valueReference?.value?.sanitize()

                val referencedInvoiceFromDispense = additionalInvoices.find { it.id == reference }
                val productionStep = index + 1
                val productionItems = mutableListOf<String>()

                referencedInvoiceFromDispense?.lineItem?.forEachIndexed { liIndex, lineItem ->
                    // add the valueInt
                    referencedInvoiceFromDispense.extensions
                        .findExtensionByUrl(MANUFACTURING_STEP_COUNTER_EXTENSION_URL)
                        ?.valuePositiveInt?.let { productionItems += it }

                    // add the code (PZN, TA1, HMNR)
                    lineItem.chargeItemCodeableConcept?.coding
                        ?.firstBySystem(FhirPkvCodeSystems.PRIORITY)
                        ?.code?.let { productionItems += it }

                    // add the price component factor flag
                    lineItem.priceComponent
                        .flatMap { it.extensions }
                        .firstOrNull { ext -> ext.url == FACTOR_FLAG }
                        ?.valueCodeableConcept?.coding?.firstOrNull()?.code
                        ?.let { productionItems += it }

                    // add the amount
                    lineItem.priceComponent.firstOrNull()
                        ?.amount?.value?.let { productionItems += "$it €" }

                    if (liIndex + 1 != referencedInvoiceFromDispense.lineItem.size) {
                        productionItems += "/"
                    }
                }

                additionalInformation.add(
                    PRODUCTION_STEP_TEMPLATE.format(
                        productionStep,
                        whenPrepared ?: "?",
                        productionItems.joinToString(" ")
                    )
                )
            }

            return additionalInformation.toList()
        }

        /**
         * Builds additional information text for general compounding cases.
         *
         * Iterates through all supplementary invoice items and concatenates
         * charge item codes, factors, and prices into a human-readable list.
         *
         * @return list of descriptive component strings, or empty if none exist.
         */
        private fun List<FhirPkvInvoiceBundleInvoice>.buildCompoundingInformation(): List<String> {
            val items = this.additionalInvoiceInformation()
            if (items.isEmpty()) return emptyList()

            val components = mutableListOf(COMPONENTS_LABEL)
            val itemSize = items.size

            items.forEachIndexed { index, model ->
                model.chargeItemCode?.code?.let { components.add(it) }
                model.factor?.let { components.add(it) }
                model.price?.let { components.add(it) }
                if (index + 1 != itemSize) {
                    components += "/"
                }
            }

            components += NET_PRICES_SUFFIX
            return components.toList()
        }

        /**
         * Converts this [FhirPkvInvoiceBundle] into a simplified ERP model representation.
         *
         * The mapping extracts:
         * - task identifier,
         * - bundle timestamp,
         * - dispensing organization,
         * - digital signature binary,
         * - invoice line items,
         * - additional invoice/dispense information depending on the TA1 code
         *   (separation billing, parenteral cytostatics, or general compounding).
         *
         * @return a [FhirPkvInvoiceErpModel] suitable for application-level use.
         */
        // TODO: (Improvements) We could possibly make the additional information into type safe information
        internal fun FhirPkvInvoiceBundle.toErpModel(): FhirPkvInvoiceErpModel {
            val allInvoices = entries.mapNotNull { it.resource as? FhirPkvInvoiceBundleInvoice }
            val additionalInvoices =
                allInvoices.invoiceByProfile(INVOICE_SUPPLEMENTARY_UNIT_PROFILE_URL)
            val originalInvoice =
                allInvoices.invoiceByProfile(INVOICE_LINE_ITEMS_PROFILE_URL).firstOrNull()

            val allMedicalDispenses =
                entries.mapNotNull { it.resource as? FhirPkvInvoiceBundleMedicationDispense }
            val additionalMedicalDispenses =
                allMedicalDispenses.dispenseByProfile(DISPENSE_COMPOUNDING_DETAILS_PROFILE_URL)

            val lineItem = originalInvoice?.lineItem?.toErpModel()?.firstOrNull()
            val codeableItem = lineItem?.chargeItemCode

            var additionalInvoiceDispenseItems: List<FhirPkvInvoiceChargeItemErpModel>? =
                emptyList()
            var additionalInvoiceInformation: List<String>? = emptyList()

            when {
                codeableItem?.type == ChargeItemType.Ta1 && codeableItem.code?.trim() == TA1_SEPARATION_BILLING_CODE -> {
                    additionalInvoiceDispenseItems = additionalInvoices
                        .additionalInvoiceInformation()
                }

                codeableItem?.type == ChargeItemType.Ta1 && codeableItem.code?.trim() == TA1_PARENTERAL_CYTOSTATICS_CODE -> {
                    additionalInvoiceInformation = buildParenteralCytostaticsInformation(
                        additionalInvoices = additionalInvoices,
                        additionalMedicalDispenses = additionalMedicalDispenses
                    )
                }

                else -> {
                    // must be compounding
                    additionalInvoiceInformation = additionalInvoices.buildCompoundingInformation()
                }
            }

            return FhirPkvInvoiceErpModel(
                taskId = identifier?.takeIf { it.system == TASKID_SYSTEM_URL }?.value,
                timestamp = timestamp?.asFhirTemporal(),
                organization = entries.getOrganization()?.toErpModel(),
                binary = signature?.getBinary(),
                lineItems = originalInvoice?.lineItem?.toErpModel() ?: emptyList(),
                totalAdditionalFee = originalInvoice?.totalGross?.getAdditionalFee(),
                whenHandedOver = entries.getDispense()?.whenHandedOver?.asFhirTemporal(),
                totalGrossFee = FhirCostErpModel(
                    value = originalInvoice?.totalGross?.value,
                    unit = originalInvoice?.totalGross?.currency
                ),
                additionalInvoiceInformation = additionalInvoiceInformation ?: emptyList(),
                additionalDispenseItems = additionalInvoiceDispenseItems ?: emptyList()
            )
        }
    }
}

/**
 * Entry of a [FhirPkvInvoiceBundle].
 *
 * Each entry references a FHIR resource that is part of the invoice bundle.
 *
 * @property fullUrl Absolute or relative reference to the resource.
 * @property resource The actual contained resource instance.
 */
@Serializable
internal data class FhirPkvInvoiceBundleEntry(
    val fullUrl: String? = null,
    val resource: FhirPkvInvoiceBundleResource
)

/**
 * FHIR Composition resource inside the invoice bundle.
 *
 * Contains high-level metadata and structure of the invoice document.
 *
 * @property resourceType Always `"Composition"`.
 * @property id Logical identifier.
 * @property meta Metadata including profile.
 * @property date Creation date of the invoice bundle.
 * @property title Human-readable title for the composition.
 */
@SerialName("Composition")
@Serializable
internal data class FhirPkvInvoiceBundleComposition(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("title") val title: String? = null
) : FhirPkvInvoiceBundleResource {
    companion object {
        @Suppress("unused")
        fun JsonElement.getInvoiceBundleComposition(): FhirPkvInvoiceBundleComposition? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.w(
                    tag = "fhir-parser",
                    message = "Error parsing FHIR FhirPkvInvoiceComposition: ${e.message}"
                )
                null
            }
        }
    }
}

/**
 * FHIR MedicationDispense resource inside the invoice bundle.
 *
 * Represents the **dispensed medication details** relevant for billing.
 * See [MedicationDispense profile](https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.4.0/files/2780092).
 *
 * @property resourceType Always `"MedicationDispense"`.
 * @property id Logical identifier.
 * @property meta Metadata including applied profiles.
 * @property medicationCodeableConcept Codeable concept describing the dispensed drug.
 * @property extensions Additional billing-relevant extensions.
 * @property type Dispense type (e.g., delivery form).
 * @property status Current dispense status.
 * @property whenHandedOver Timestamp of handover to the patient.
 * @property authorizingPrescription Reference(s) to the prescriptions being fulfilled.
 */
@Reference(
    info = "Pkv Invoice Bundle's Medication Dispense information",
    url = "https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.4.0/files/2780092"
)
@SerialName("MedicationDispense")
@Serializable
internal data class FhirPkvInvoiceBundleMedicationDispense(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("medicationCodeableConcept") val medicationCodeableConcept: FhirCodeableConcept? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList(),
    @SerialName("type") val type: FhirCoding? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("whenHandedOver") val whenHandedOver: String? = null,
    @SerialName("whenPrepared") val whenPrepared: String? = null,
    @SerialName("authorizingPrescription") val authorizingPrescription: List<FhirPkvPrescriptionIdentifier> = emptyList()
) : FhirPkvInvoiceBundleResource {
    companion object {
        @Suppress("unused")
        fun JsonElement.getInvoiceBundleMedicationDispense(): FhirPkvInvoiceBundleMedicationDispense? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(
                    tag = "fhir-parser",
                    message = "Error parsing FHIR FhirPkvInvoiceBundleMedicationDispense: ${e.message}"
                )
                null
            }
        }
    }
}

/**
 * FHIR Invoice resource inside the invoice bundle.
 *
 * Represents the **billing information** for the dispensed prescription,
 * including line items and totals.
 * See [Invoice profile](https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.4.0/files/2780093).
 *
 * @property resourceType Always `"Invoice"`.
 * @property id Logical identifier.
 * @property meta Metadata including applied profiles.
 * @property status Invoice status (e.g., issued, balanced).
 * @property lineItem Individual billed items (medication, services).
 * @property totalGross Total gross amount including all items.
 */
@Reference(
    info = "Pkv Invoice Bundle's Invoice information",
    url = "https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.4.0/files/2780093"
)
@SerialName("Invoice")
@Serializable
internal data class FhirPkvInvoiceBundleInvoice(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("lineItem") val lineItem: List<FhirPkvInvoiceLineItem> = emptyList(),
    @SerialName("totalGross") val totalGross: FhirPkvInvoiceTotalGross? = null
) : FhirPkvInvoiceBundleResource {
    companion object {
        @Suppress("unused")
        fun JsonElement.getInvoiceBundleInvoice(): FhirPkvInvoiceBundleInvoice? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(
                    tag = "fhir-parser",
                    message = "Error parsing FHIR PkvInvoiceBundleInvoice: ${e.message}"
                )
                null
            }
        }
    }
}

/**
 * FHIR Organization resource inside the invoice bundle.
 *
 * Represents the **pharmacy or organization** that dispensed
 * and billed the medication. Structure is aligned with KBV-ITA-FOR v1.2.0.
 *
 * @property resourceType Always `"Organization"`.
 * @property id Logical identifier.
 * @property meta Metadata including applied profiles.
 * @property identifiers Identifiers such as IKNR or pharmacy ID.
 * @property name Organization's official name.
 * @property telecoms Contact information.
 * @property addresses Postal addresses of the organization.
 */
@Reference(
    info = "Organization version 1.2.0",
    url = "https://simplifier.net/packages/kbv.ita.for/1.2.0/files/2777636/~overview"
)
// NOTE: This organization and the on inside the MedicalDataParser (kbv) follow the same structure
@SerialName("Organization")
@Serializable
internal data class FhirPkvInvoiceBundleOrganization(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") override val identifiers: List<FhirIdentifier>? = emptyList(),
    @SerialName("name") val name: String? = null,
    @SerialName("telecom") val telecoms: List<FhirTelecom>? = emptyList(),
    @SerialName("address") val addresses: List<FhirAddress>? = emptyList()
) : FhirPkvInvoiceBundleResource, FhirOrganizationBase {
    companion object {
        @Suppress("unused")
        fun JsonElement.getInvoiceBundleOrganization(): FhirPkvInvoiceBundleOrganization? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(
                    tag = "fhir-parser",
                    message = "Error parsing FHIR PkvInvoiceBundleOrganization: ${e.message}"
                )
                null
            }
        }

        fun FhirPkvInvoiceBundleOrganization.toErpModel(): FhirTaskOrganizationErpModel {
            return FhirTaskOrganizationErpModel(
                name = name,
                address = addresses?.firstOrNull()?.toErpModel(),
                bsnr = bsnr,
                iknr = iknr,
                telematikId = telematikId,
                phone = telecoms?.getPhone(),
                email = telecoms?.getEmail(),
                fax = telecoms?.getFax()
            )
        }
    }
}

/**
 * Single line item of an invoice.
 *
 * Represents an individual billed element (e.g., one medication package).
 *
 * @property sequence Sequence number.
 * @property chargeItemCodeableConcept Codeable concept for the billed item.
 * @property priceComponent Price breakdown of this item.
 */
@Serializable
internal data class FhirPkvInvoiceLineItem(
    @SerialName("sequence") val sequence: String? = null,
    @SerialName("chargeItemCodeableConcept") val chargeItemCodeableConcept: FhirCodeableConcept? = null,
    @SerialName("priceComponent") val priceComponent: List<FhirPkvPriceComponent> = emptyList(),
    @SerialName("extension") val extension: List<FhirExtension> = emptyList()
) {
    companion object {

        fun List<FhirPkvInvoiceLineItem>.toErpModel(): List<FhirPkvInvoiceChargeItemErpModel> =
            map { lineItem ->
                Napier.e { "dinesh now the tax amount is ${lineItem.tax()}" }
                FhirPkvInvoiceChargeItemErpModel(
                    price = lineItem.priceComponent.firstOrNull()?.amount?.value,
                    tax = lineItem.tax(),
                    factor = lineItem.priceComponent.firstOrNull()?.factor,
                    isPartialQuantityDelivery = lineItem.partialQuantityDelivery(),
                    spenderPzn = lineItem.spenderPzn(),
                    chargeItemCode = lineItem.chargeItemCodeableConcept()
                )
            }

        // Handle Teilmengenabgabe (partial quantity dispensing) for v1.3
        private fun FhirPkvInvoiceLineItem.partialQuantityDelivery(): Boolean = extension
            .findExtensionByUrl(ADDITIONAL_ATTRIBUTES_EXTENSION_URL)
            ?.extensions
            ?.findExtensionByUrl(ADDITIONAL_ATTR_PARTIAL_QUANTITY_NAME)
            ?.extensions
            ?.findExtensionByUrl(ADDITIONAL_ATTR_KEY_NAME)
            ?.valueBoolean ?: false

        private fun FhirPkvInvoiceLineItem.tax() = priceComponent.firstOrNull()?.getTax()

        private fun FhirPkvInvoiceLineItem.spenderPzn(): String? = extension
            .findExtensionByUrl(SPENDER_PZN_EXTENSION_URL)
            ?.extensions
            ?.findExtensionByUrl(SPENDER_PZN_PARTIAL_QUANTITY_NAME)
            ?.extensions
            ?.findExtensionByUrl(SPENDER_PZN_KEY_NAME)
            ?.valueCodeableConcept?.coding?.firstOrNull()?.code

        private fun FhirPkvInvoiceLineItem.firstCodeableConceptBySystem(url: String): FhirCodeableConcept? =
            chargeItemCodeableConcept?.coding?.let { coding ->
                coding.firstOrNull { it.system == url }
                    ?.let { match -> chargeItemCodeableConcept.copy(coding = listOf(match)) }
            }

        private fun FhirPkvInvoiceLineItem.chargeItemCodeableConcept(): FhirChargeableItemCodeErpModel? {
            val (url, type) = CHARGE_ITEM_TYPE_URL.entries.firstOrNull { (url, _) ->
                firstCodeableConceptBySystem(url) != null
            } ?: return null

            val concept = firstCodeableConceptBySystem(url) ?: return null
            val coding = concept.coding?.firstOrNull() ?: return null

            return FhirChargeableItemCodeErpModel(
                type = type,
                code = coding.code,
                text = concept.text
            )
        }
    }
}

/**
 * Represents the gross total amount of an invoice.
 *
 * @property extensions Additional modifiers or flags.
 * @property value Numeric gross total value.
 * @property currency Currency code (e.g., EUR).
 */
@Serializable
internal data class FhirPkvInvoiceTotalGross(
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList(),
    @SerialName("value") val value: String? = null,
    @SerialName("currency") val currency: String? = null
) {
    companion object {
        internal fun FhirPkvInvoiceTotalGross.getAdditionalFee(): FhirCostErpModel {
            val cost =
                extensions.findExtensionByUrl(INVOICE_TOTAL_COPAYMENT_EXTENSION_URL)?.valueMoney
            return FhirCostErpModel(
                value = cost?.value,
                unit = cost?.unit
            )
        }
    }
}

/**
 * Represents a price component for a line item.
 *
 * Includes type (e.g., base, surcharge), factor, and calculated amount.
 */
@Serializable
internal data class FhirPkvPriceComponent(
    @SerialName("type") val type: String? = null,
    @SerialName("factor") val factor: String? = null,
    @SerialName("amount") val amount: FhirPkvInvoiceAmount? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
) {
    companion object {
        fun FhirPkvPriceComponent.getTax(): String? {
            return extensions.findExtensionByUrl(VAT_RATE_EXTENSION_URL)?.valueDecimal
        }
    }
}

/**
 * Represents a monetary amount in the invoice.
 *
 * @property value Numeric value.
 * @property currency ISO 4217 currency code (e.g., EUR).
 */
@Serializable
internal data class FhirPkvInvoiceAmount(
    @SerialName("value") val value: String? = null,
    @SerialName("currency") val currency: String? = null
)

/**
 * Wrapper for the identifier of the prescription being billed.
 *
 * Used in [FhirPkvInvoiceBundleMedicationDispense] to reference the
 * corresponding ePrescription Task.
 */
@Serializable
internal data class FhirPkvPrescriptionIdentifier(
    @SerialName("identifier") val id: FhirIdentifier? = null
)
