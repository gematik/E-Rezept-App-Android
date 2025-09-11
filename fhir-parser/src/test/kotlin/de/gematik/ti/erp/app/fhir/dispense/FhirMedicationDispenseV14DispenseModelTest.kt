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

import de.gematik.ti.erp.app.data.medication_dispense_1_4_compounding
import de.gematik.ti.erp.app.data.medication_dispense_1_4_free_text
import de.gematik.ti.erp.app.data.medication_dispense_1_4_no_medication
import de.gematik.ti.erp.app.data.medication_dispense_1_4_simple
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseV14ExampleWithoutMedication
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_compounding
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_free_text
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_simple
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel.Companion.extractMedicationDispense
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseV14DispenseModelTest {

    @Test
    fun `test parser getting dispense from medication dispense workflow 1_4 compounding`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_4_compounding)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_compounding)
        val fhirModel: FhirMedicationDispenseV14V15DispenseModel = bundle.extractMedicationDispense()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting dispense from medication dispense workflow 1_4 free text`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_4_free_text)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_free_text)
        val fhirModel = bundle.extractMedicationDispense()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting dispense from medication dispense workflow 1_4 simple`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_4_simple)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_simple)
        val fhirModel = bundle.extractMedicationDispense()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting dispense from medication dispense workflow 1_4 no medication`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_4_no_medication)
        val fhirModel = bundle.extractMedicationDispense()
        assertEquals(fhirMedicationDispenseV14ExampleWithoutMedication, fhirModel)
    }
}
