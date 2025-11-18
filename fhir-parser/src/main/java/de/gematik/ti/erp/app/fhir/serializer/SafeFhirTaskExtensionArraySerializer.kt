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

package de.gematik.ti.erp.app.fhir.serializer

import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TaskMetaDataExtensionDates.ACCEPT_DATE_EXTENSION
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TaskMetaDataExtensionDates.EU_REDEEM_ALLOWED_BY_PATIENT_AUTHORIZATION
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TaskMetaDataExtensionDates.EU_REDEEM_POSSIBLE_BY_PROPERTIES
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TaskMetaDataExtensionDates.EXPIRY_DATE_EXTENSION
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TaskMetaDataExtensionDates.LAST_MEDICATION_DISPENSE_EXTENSION
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TaskMetaDataExtensionDates.PRESCRIPTION_TYPE_EXTENSION
import de.gematik.ti.erp.app.fhir.error.fhirSerializationError
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTaskExtensionValues
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * **Custom serializer for FHIR Task Extension Array (`extension` field).**
 *
 * This serializer extracts specific values from the `extension` array in a **FHIR Task** resource.
 * It matches JSON objects inside the array based on their `url` and extracts:
 *
 * - `acceptDate` → Extracted from `valueDate` when `url == ACCEPT_DATE_EXTENSION.url`
 * - `expiryDate` → Extracted from `valueDate` when `url == EXPIRY_DATE_EXTENSION.url`
 * - `lastMedicationDispense` → Extracted from `valueDate` when `url == LAST_MEDICATION_DISPENSE_EXTENSION.url`
 * - `prescriptionType` → Extracted from `valueCoding["code"]` when `url == PRESCRIPTION_TYPE_EXTENSION.url`
 *
 * The extracted values are stored in a **single `FhirTaskExtensionValues` object**.
 *
 * ### **Example JSON Input**
 * ```json
 * [
 *   {
 *     "url": "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType",
 *     "valueCoding": {
 *       "code": "160",
 *       "system": "https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType",
 *       "display": "Muster 16 (Apothekenpflichtige Arzneimittel)"
 *     }
 *   },
 *   {
 *     "url": "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AcceptDate",
 *     "valueDate": "2022-04-02"
 *   },
 *   {
 *     "url": "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_ExpiryDate",
 *     "valueDate": "2022-06-02"
 *   }
 * ]
 * ```
 *
 * ### **Example Extracted Values**
 * ```
 * acceptDate = "2022-04-02"
 * expiryDate = "2022-06-02"
 * prescriptionType = "160"
 * ```
 */
internal object SafeFhirTaskExtensionArraySerializer : KSerializer<FhirTaskExtensionValues> {

    /**
     * **Defines the structure of the serialized data.**
     *
     * This class descriptor specifies four nullable string elements:
     * - `"acceptDate"` → Extracted from `"valueDate"` of the Accept Date extension
     * - `"expiryDate"` → Extracted from `"valueDate"` of the Expiry Date extension
     * - `"lastMedicationDispense"` → Extracted from `"valueDate"` of Last Medication Dispense extension
     * - `"prescriptionType"` → Extracted from `"valueCoding['code']"` of the Prescription Type extension
     */
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("FhirTaskExtensionArray") {
            element("acceptDate", PrimitiveSerialDescriptor("acceptDate", PrimitiveKind.STRING).nullable)
            element("expiryDate", PrimitiveSerialDescriptor("expiryDate", PrimitiveKind.STRING).nullable)
            element("lastMedicationDispense", PrimitiveSerialDescriptor("lastMedicationDispense", PrimitiveKind.STRING).nullable)
            element("prescriptionType", PrimitiveSerialDescriptor("prescriptionType", PrimitiveKind.STRING).nullable)
            element("isEuRedeemableByProperties", PrimitiveSerialDescriptor("isEuRedeemableByProperties", PrimitiveKind.BOOLEAN).nullable)
            element("isEuRedeemableByPatientAuthorization", PrimitiveSerialDescriptor("isEuRedeemableByPatientAuthorization", PrimitiveKind.BOOLEAN).nullable)
        }

    /**
     * **Serializes `FhirTaskExtensionValues` into JSON.**
     *
     * Converts a `FhirTaskExtensionValues` object into a structured JSON format
     * by writing each non-null property as an element inside the JSON object.
     *
     * @param encoder The encoder used to write the serialized JSON.
     * @param value The `FhirTaskExtensionValues` instance to serialize.
     */
    @Suppress("MagicNumber")
    override fun serialize(encoder: Encoder, value: FhirTaskExtensionValues) {
        val compositeEncoder = encoder.beginStructure(descriptor)

        value.acceptDate?.let { compositeEncoder.encodeStringElement(descriptor, 0, it) }
        value.expiryDate?.let { compositeEncoder.encodeStringElement(descriptor, 1, it) }
        value.lastMedicationDispense?.let { compositeEncoder.encodeStringElement(descriptor, 2, it) }
        value.prescriptionType?.let { compositeEncoder.encodeStringElement(descriptor, 3, it) }

        compositeEncoder.endStructure(descriptor)
    }

    /**
     * **Deserializes a JSON array of FHIR Task Extensions into `FhirTaskExtensionValues`.**
     *
     * This function:
     * - Expects a `JsonArray`
     * - Iterates through the array, matching known extension `url`s
     * - Extracts corresponding values (`valueDate` or `valueCoding["code"]`)
     * - Returns a `FhirTaskExtensionValues` object containing extracted values
     *
     * @param decoder The decoder used to read the JSON input.
     * @return A `FhirTaskExtensionValues` instance with extracted values.
     * @throws SerializationException If the input is not a valid `JsonArray`.
     */
    override fun deserialize(decoder: Decoder): FhirTaskExtensionValues {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())

        if (jsonElement !is JsonArray) {
            fhirSerializationError(
                "FhirTaskExtensionArraySerializer failed as expected JsonArray but found ${jsonElement::class}"
            )
        }

        var acceptDate: String? = null
        var expiryDate: String? = null
        var prescriptionType: String? = null
        var lastMedicationDispense: String? = null
        var isEuRedeemableByProperties: Boolean? = null
        var isEuRedeemableByPatientAuthorization: Boolean? = null

        val extensionMappings = mapOf(
            with(ACCEPT_DATE_EXTENSION) { url to { json: JsonObject -> json[valueString]?.jsonPrimitive?.content } },
            with(EXPIRY_DATE_EXTENSION) { url to { json: JsonObject -> json[valueString]?.jsonPrimitive?.content } },
            with(LAST_MEDICATION_DISPENSE_EXTENSION) { url to { json: JsonObject -> json[valueString]?.jsonPrimitive?.content } },
            with(PRESCRIPTION_TYPE_EXTENSION) { url to { json: JsonObject -> json[valueString]?.jsonObject?.get("code")?.jsonPrimitive?.content } }
        )
        val euRedeemExtensionMappings = mapOf(
            with(EU_REDEEM_POSSIBLE_BY_PROPERTIES) { url to { json: JsonObject -> json[valueString]?.jsonPrimitive?.booleanOrNull } },
            with(EU_REDEEM_ALLOWED_BY_PATIENT_AUTHORIZATION) { url to { json: JsonObject -> json[valueString]?.jsonPrimitive?.booleanOrNull } }
        )

        // Iterate through the JSON array and extract values based on `url`
        for (element in jsonElement) {
            val jsonObject = element.jsonObject

            val url = jsonObject["url"]?.jsonPrimitive?.content ?: continue

            extensionMappings[url]?.invoke(jsonObject)?.let { extractedValue ->
                when (url) {
                    ACCEPT_DATE_EXTENSION.url -> acceptDate = extractedValue
                    EXPIRY_DATE_EXTENSION.url -> expiryDate = extractedValue
                    LAST_MEDICATION_DISPENSE_EXTENSION.url -> lastMedicationDispense = extractedValue
                    PRESCRIPTION_TYPE_EXTENSION.url -> prescriptionType = extractedValue
                }
            }

            euRedeemExtensionMappings[url]?.invoke(jsonObject)?.let { extractedValue ->
                when (url) {
                    EU_REDEEM_POSSIBLE_BY_PROPERTIES.url -> isEuRedeemableByProperties = extractedValue
                    EU_REDEEM_ALLOWED_BY_PATIENT_AUTHORIZATION.url -> isEuRedeemableByPatientAuthorization = extractedValue
                }
            }
        }

        return FhirTaskExtensionValues(
            acceptDate = acceptDate,
            expiryDate = expiryDate,
            lastMedicationDispense = lastMedicationDispense,
            prescriptionType = prescriptionType,
            isEuRedeemableByProperties = isEuRedeemableByProperties ?: false,
            isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization ?: false
        )
    }
}
