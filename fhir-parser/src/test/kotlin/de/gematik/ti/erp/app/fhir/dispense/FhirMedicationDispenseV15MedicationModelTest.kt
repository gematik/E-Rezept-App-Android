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

import de.gematik.ti.erp.app.data.medication_dispense_medication_resource_1_5_without_strength_numerator
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseMedicationV15WithoutStrengthNumerator
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.extractDispensedMedication
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseV15MedicationModelTest {

    @Test
    fun `medication from dispense v1_5 without strength numerator`() {
        val bundle = Json.parseToJsonElement(medication_dispense_medication_resource_1_5_without_strength_numerator)
        val fhirModel = bundle.extractDispensedMedication()
        assertEquals(fhirMedicationDispenseMedicationV15WithoutStrengthNumerator, fhirModel)
    }
}
