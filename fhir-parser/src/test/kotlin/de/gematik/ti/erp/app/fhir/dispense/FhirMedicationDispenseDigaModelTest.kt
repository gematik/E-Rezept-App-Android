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

import de.gematik.ti.erp.app.data.medication_dispense_diga_deeplink
import de.gematik.ti.erp.app.data.medication_dispense_diga_name_and_pzn
import de.gematik.ti.erp.app.data.medication_dispense_diga_no_redeem_code
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_diga_deeplink
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_diga_name_and_pzn
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhir_model_medication_dispense_diga_no_redeem_code
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14DispenseModel.Companion.getMedicationDispenseV14
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseDigaModelTest {

    @Test
    fun `medication dispense with diga deeplink`() {
        val bundle = Json.parseToJsonElement(medication_dispense_diga_deeplink)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_diga_deeplink)
        val fhirModel = bundle.getMedicationDispenseV14()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `medication dispense with diga name and pzn`() {
        val bundle = Json.parseToJsonElement(medication_dispense_diga_name_and_pzn)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_diga_name_and_pzn)
        val fhirModel = bundle.getMedicationDispenseV14()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }

    @Test
    fun `medication dispense with diga no redeem code`() {
        val bundle = Json.parseToJsonElement(medication_dispense_diga_no_redeem_code)
        val result = Json.parseToJsonElement(fhir_model_medication_dispense_diga_no_redeem_code)
        val fhirModel = bundle.getMedicationDispenseV14()
        val serializedFhirModel = Json.encodeToJsonElement(serializer(), fhirModel)
        assertEquals(result, serializedFhirModel)
    }
}
