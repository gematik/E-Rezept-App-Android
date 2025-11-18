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

package de.gematik.ti.erp.app.fhir.dispense

import de.gematik.ti.erp.app.data.fhir_model_medication_eu_dispense
import de.gematik.ti.erp.app.data.fhir_model_medication_eu_multiple_dispense
import de.gematik.ti.erp.app.data.fhir_model_medication_eu_single_dispense_urn
import de.gematik.ti.erp.app.data.medication_dispense_eu_1_0_multiple_dispense
import de.gematik.ti.erp.app.data.medication_dispense_eu_1_0_single_dispense
import de.gematik.ti.erp.app.data.medication_dispense_eu_1_0_single_dispense_urn
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseEuV10Model
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseEuV10Model.Companion.extractEuMedicationDispense
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseEuModelTest {

    private fun extractMedicationDispenseFromBundle(bundleJson: String): JsonElement {
        val bundle = Json.parseToJsonElement(bundleJson).jsonObject
        return bundle["entry"]?.jsonArray
            ?.first { entry ->
                entry.jsonObject["resource"]?.jsonObject?.get("resourceType")?.jsonPrimitive?.content == "MedicationDispense"
            }?.jsonObject?.get("resource")
            ?: throw IllegalStateException("MedicationDispense resource not found in bundle")
    }

    @Test
    fun `test parser getting dispense from eu medication dispense single`() {
        val medicationDispense = extractMedicationDispenseFromBundle(medication_dispense_eu_1_0_single_dispense)
        val result = Json.parseToJsonElement(fhir_model_medication_eu_dispense)
        val fhirModel: FhirMedicationDispenseEuV10Model = medicationDispense.extractEuMedicationDispense()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting dispense from eu medication dispense multiple`() {
        val medicationDispense = extractMedicationDispenseFromBundle(medication_dispense_eu_1_0_multiple_dispense)
        val result = Json.parseToJsonElement(fhir_model_medication_eu_multiple_dispense)
        val fhirModel: FhirMedicationDispenseEuV10Model = medicationDispense.extractEuMedicationDispense()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting dispense from eu medication dispense single with URN references`() {
        val medicationDispense = extractMedicationDispenseFromBundle(medication_dispense_eu_1_0_single_dispense_urn)
        val result = Json.parseToJsonElement(fhir_model_medication_eu_single_dispense_urn)
        val fhirModel: FhirMedicationDispenseEuV10Model = medicationDispense.extractEuMedicationDispense()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }
}
