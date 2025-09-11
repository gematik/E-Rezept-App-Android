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

@file:Suppress("unused")

package de.gematik.ti.erp.app.fhir.pkv.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirPeriod
import de.gematik.ti.erp.app.fhir.constant.SafeResourceTypeDiscriminatorJson
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceBinaryErpModel
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundleSignature.Companion.getBinary
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom
import de.gematik.ti.erp.app.utils.Reference
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Marker interface for resources that can occur inside a
 * [FhirPkvInvoiceBinaryBundle].
 *
 * According to [GEM_ERP_PR_Bundle](https://simplifier.net/erezept-workflow/gem_erp_pr_bundle),
 * such bundles may contain `Composition`, `Device`, and `Binary` (Digest) resources.
 */
@Serializable
internal sealed interface FhirPkvInvoiceBinaryBundleResource

/**
 * FHIR Bundle resource representing the **PKV invoice binary bundle**.
 *
 * This bundle is used for transmitting receipts ("Quittungen")
 * related to the redemption of e-prescriptions in the German
 * private health insurance (PKV) context.
 *
 * @property resourceType Always `"Bundle"`.
 * @property id Logical identifier of the bundle.
 * @property meta Metadata including profile declaration (GEM_ERP_PR_Bundle).
 * @property identifier Technical prescription ID or bundle identifier.
 * @property timestamp Time of bundle creation.
 * @property entries List of contained resources (Composition, Device, Binary).
 * @property signature Optional signature element ensuring authenticity.
 */
@Reference(
    info = "Dokumentenbündel für Quittung (GEM_ERP_PR_Bundle)",
    url = "https://simplifier.net/erezept-workflow/gem_erp_pr_bundle"
)
@Serializable
internal data class FhirPkvInvoiceBinaryBundle(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifier: FhirIdentifier? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("entry") val entries: List<FhirPkvInvoiceBinaryBundleEntry> = emptyList(),
    @SerialName("signature") val signature: FhirPkvInvoiceBundleSignature? = null
) {
    companion object {
        /**
         * Attempts to parse this [JsonElement] into a [FhirPkvInvoiceBinaryBundle].
         *
         * Uses [SafeResourceTypeDiscriminatorJson] to safely decode only
         * if the `resourceType` matches a known type. Logs an error via [Napier]
         * if parsing fails.
         *
         * @return Parsed [FhirPkvInvoiceBinaryBundle] or `null` if decoding fails.
         */
        fun JsonElement.getPkvInvoiceBinaryBundle(): FhirPkvInvoiceBinaryBundle? {
            return try {
                SafeResourceTypeDiscriminatorJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(
                    tag = "fhir-parser",
                    message = "Error parsing FHIR PKV Invoice Binary Bundle: ${e.message}"
                )
                null
            }
        }

        internal fun FhirPkvInvoiceBinaryBundle.toErpModel(): FhirPkvInvoiceBinaryErpModel {
            return FhirPkvInvoiceBinaryErpModel(
                binary = signature?.getBinary()
            )
        }
    }
}

/**
 * Entry of a [FhirPkvInvoiceBinaryBundle].
 *
 * Each entry references a contained FHIR resource such as
 * [FhirPkvInvoiceBinaryBundleComposition], [FhirPkvInvoiceBundleDevice],
 * or [FhirPkvInvoiceBundleDigest].
 *
 * @property fullUrl Absolute or relative reference to the resource.
 * @property resource The contained resource instance.
 */
@Serializable
internal data class FhirPkvInvoiceBinaryBundleEntry(
    val fullUrl: String? = null,
    val resource: FhirPkvInvoiceBinaryBundleResource
)

/**
 * FHIR Composition resource inside the invoice bundle.
 *
 * Represents the **receipt document** for the redemption
 * of an e-prescription (Quittung).
 *
 * @property resourceType Always `"Composition"`.
 * @property id Logical identifier.
 * @property meta Metadata including applied profiles.
 * @property extensions Optional additional metadata.
 * @property status Workflow status of the document.
 * @property type Document type (usually a coded concept for "Quittung").
 * @property date Creation date of the receipt.
 * @property title Human-readable title.
 * @property event List of periods describing the clinical/business context.
 */
@Reference(
    info = "Quittung für die Einlösung eines E-Rezepts (GEM_ERP_PR_Composition)",
    url = "https://simplifier.net/erezept-workflow/gem_erp_pr_composition"
)
@Serializable
@SerialName("Composition")
internal data class FhirPkvInvoiceBinaryBundleComposition(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("type") val type: FhirCodeableConcept? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("event") val event: List<FhirPkvPeriod> = emptyList()
) : FhirPkvInvoiceBinaryBundleResource

/**
 * FHIR Device resource inside the invoice bundle.
 *
 * According to [GEM_ERP_PR_Device](https://simplifier.net/erezept-workflow/gem_erp_pr_device),
 * this describes the **system or device** that generated the receipt,
 * e.g. pharmacy system or connector.
 *
 * @property resourceType Always `"Device"`.
 * @property id Logical identifier.
 * @property meta Metadata including applied profiles.
 * @property status Operational status.
 * @property serialNumber Optional serial number of the device.
 * @property deviceName One or more names assigned to the device.
 * @property version Device version information.
 * @property contact Technical contact information.
 */
@Reference(
    info = "GEM_ERP_PR_Device",
    url = "https://simplifier.net/erezept-workflow/gem_erp_pr_device"
)
@Serializable
@SerialName("Device")
internal data class FhirPkvInvoiceBundleDevice(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("serialNumber") val serialNumber: String? = null,
    @SerialName("deviceName") val deviceName: List<FhirPkvDeviceName> = emptyList(),
    @SerialName("version") val version: List<FhirPkvValue> = emptyList(),
    @SerialName("contact") val contact: List<FhirTelecom> = emptyList()
) : FhirPkvInvoiceBinaryBundleResource

/**
 * FHIR Binary resource representing the **QES Digest**.
 *
 * According to [GEM_ERP_PR_Digest](https://simplifier.net/erezept-workflow/gem_erp_pr_digest),
 * this contains the cryptographic digest (hash) of the signed
 * Qualified Electronic Signature (QES) for integrity validation.
 *
 * @property resourceType Always `"Binary"`.
 * @property id Logical identifier.
 * @property meta Metadata including applied profiles.
 * @property contentType MIME type of the binary data (usually `application/pkcs7-mime`).
 * @property data Base64-encoded digest data.
 */
@Reference(
    info = "QES-Digest in Binary (GEM_ERP_PR_Digest)",
    url = "https://simplifier.net/erezept-workflow/gem_erp_pr_digest"
)
@Serializable
@SerialName("Binary")
internal data class FhirPkvInvoiceBundleDigest(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("contentType") val contentType: String? = null,
    @SerialName("data") val data: String? = null
) : FhirPkvInvoiceBinaryBundleResource

/**
 * Wrapper around a [FhirPeriod] to represent the
 * temporal context of an event in a Composition.
 */
@Serializable
internal data class FhirPkvPeriod(
    @SerialName("period") val period: FhirPeriod? = null
)

/**
 * Simple wrapper for a string value.
 *
 * Often used for device version representation.
 */
@Serializable
internal data class FhirPkvValue(
    @SerialName("value") val value: String? = null
)

/**
 * Part of [FhirPkvInvoiceBundleDevice].
 *
 * Describes a device name and its classification.
 *
 * @property name Human-readable name.
 * @property type Type/category of name (e.g. manufacturer name, model name).
 */
@Serializable
internal data class FhirPkvDeviceName(
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null
)
