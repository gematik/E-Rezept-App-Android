/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.dispense

import de.gematik.ti.erp.app.data.medication_dispense_legacy_simple
import de.gematik.ti.erp.app.data.medication_dispense_legacy_unknown_medication_list
import de.gematik.ti.erp.app.data.medication_dispense_legacy_unknown_medication_profile
import de.gematik.ti.erp.app.data.medication_dispense_legacy_without_category
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_unknown_medication_profile
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_without_category
import de.gematik.ti.erp.app.fhir.dispense.mocks.medicationDispenseNoCategory
import de.gematik.ti.erp.app.fhir.dispense.mocks.multipleMedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.mocks.simpleMedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.mocks.unknownMedicationProfileDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.getMedicationDispenseLegacy
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.toErpModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseLegacyModelTest {

    @Test
    fun `test parser legacy medication dispense`() {
        val bundle = Json.parseToJsonElement(medication_dispense_legacy_simple)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense)
        val fhirModel = bundle.getMedicationDispenseLegacy()
        val erpModel = fhirModel.toErpModel()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
        assertEquals(simpleMedicationDispense, erpModel)
    }

    @Test
    fun `test parser legacy medication dispense with unknown profile`() {
        val bundle = Json.parseToJsonElement(medication_dispense_legacy_unknown_medication_profile)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_unknown_medication_profile)
        val fhirModel = bundle.getMedicationDispenseLegacy()
        val erpModel = fhirModel.toErpModel()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
        assertEquals(unknownMedicationProfileDispense, erpModel)
    }

    /**
     * A dispense of this sort will not happen, this test is added to make sure that the parsing
     * is robust enough to handle unknown scenarios
     */
    @Test
    fun `test parser legacy medication dispense with unknown medication list`() {
        val bundle = Json.parseToJsonElement(medication_dispense_legacy_unknown_medication_list)
        val fhirModel = bundle.getMedicationDispenseLegacy()
        val erpModel = fhirModel.toErpModel()
        assertEquals(multipleMedicationDispense, erpModel)
    }

    @Test
    fun `test parser legacy medication dispense without category`() {
        val bundle = Json.parseToJsonElement(medication_dispense_legacy_without_category)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_without_category)
        val fhirModel = bundle.getMedicationDispenseLegacy()
        val erpModel = fhirModel.toErpModel()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
        assertEquals(medicationDispenseNoCategory, erpModel)
    }
}
