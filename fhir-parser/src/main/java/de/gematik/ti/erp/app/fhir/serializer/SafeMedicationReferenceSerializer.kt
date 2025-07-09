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

import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseReference
import de.gematik.ti.erp.app.fhir.dispense.model.original.MedicationReferenceByExtension
import de.gematik.ti.erp.app.fhir.dispense.model.original.MedicationReferenceByIdentifier
import de.gematik.ti.erp.app.fhir.dispense.model.original.MedicationReferenceByReference
import de.gematik.ti.erp.app.fhir.error.fhirSerializationError
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Custom polymorphic serializer for `FhirMedicationDispenseReference`.
 *
 * This handles the deserialization of the `medicationReference` field in FHIR JSON, which can appear in one of
 * several structural variants:
 *
 * - A `reference` string (e.g., `"reference": "Medication/xyz"` or `"urn:uuid:..."`)
 * - An `identifier` block (e.g., `{ "identifier": { "system": "...", "value": "..." }, "display": "..." }`)
 * - An `extension` block (e.g., `{ "extension": [{ "url": "...", "valueCode": "..." }] }`) for data-absent-reason
 *
 * Based on the presence of specific keys in the JSON object, the appropriate subclass of
 * `FhirMedicationDispenseReference` is selected for deserialization:
 *
 * - `MedicationReferenceByIdentifier` if `"identifier"` is present
 * - `MedicationReferenceByExtension` if `"extension"` is present
 * - `MedicationReferenceByReference` if `"reference"` is present
 *
 * @throws SerializationException if the structure does not match any known format
 */
internal object MedicationReferenceSerializer : JsonContentPolymorphicSerializer<FhirMedicationDispenseReference>(
    FhirMedicationDispenseReference::class
) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<FhirMedicationDispenseReference> {
        return when {
            "identifier" in element.jsonObject -> MedicationReferenceByIdentifier.serializer()
            "extension" in element.jsonObject -> MedicationReferenceByExtension.serializer()
            "reference" in element.jsonObject -> MedicationReferenceByReference.serializer()
            else -> fhirSerializationError("Unknown medicationReference type: $element")
        }
    }
}
