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

import de.gematik.ti.erp.app.data.medication_dispense_medication_1_4_complex
import de.gematik.ti.erp.app.data.medication_dispense_medication_1_4_pharmaceutical_product
import de.gematik.ti.erp.app.data.medication_dispense_medication_1_4_simple
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_1_4_complex
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_1_4_pharma_product
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_1_4_simple
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.extractDispensedMedication
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseV14MedicationModelTest {

    @Test
    fun `test parser getting medication from medication dispense workflow 1_4 compounding`() {
        val bundle = Json.parseToJsonElement(medication_dispense_medication_1_4_complex)
        val result = Json.parseToJsonElement(fhir_model_medication_1_4_complex)
        val fhirModel = bundle.extractDispensedMedication()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting medication from medication dispense workflow 1_4 pharma product`() {
        val bundle = Json.parseToJsonElement(medication_dispense_medication_1_4_pharmaceutical_product)
        val result = Json.parseToJsonElement(fhir_model_medication_1_4_pharma_product)
        val fhirModel = bundle.extractDispensedMedication()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `test parser getting medication from medication dispense workflow 1_4 simple`() {
        val bundle = Json.parseToJsonElement(medication_dispense_medication_1_4_simple)
        val result = Json.parseToJsonElement(fhir_model_medication_1_4_simple)
        val fhirModel = bundle.extractDispensedMedication()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }
}
