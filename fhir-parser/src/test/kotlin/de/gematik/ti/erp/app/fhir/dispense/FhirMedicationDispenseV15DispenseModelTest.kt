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

import de.gematik.ti.erp.app.data.medication_dispense_1_5_diga_deeplink
import de.gematik.ti.erp.app.data.medication_dispense_1_5_diga_name_and_pzn
import de.gematik.ti.erp.app.data.medication_dispense_1_5_diga_no_redeem_code
import de.gematik.ti.erp.app.data.medication_dispense_1_5_kombipackung
import de.gematik.ti.erp.app.data.medication_dispense_1_5_rezeptur
import de.gematik.ti.erp.app.data.medication_dispense_1_5_without_medication
import de.gematik.ti.erp.app.fhir.dispense.mocks.erpMedicationDispenseDiGADeepLinkV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.erpMedicationDispenseDiGANameAndPznV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.erpMedicationDispenseDiGANoRedeemCodeV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.erpMedicationDispenseKombipackungV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.erpMedicationDispenseRezepturV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.erpMedicationDispenseWithoutMedicationV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseDiGADeepLinkV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseDiGANameAndPznV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseDiGANoRedeemCodeV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseKombipackungV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseRezepturV15
import de.gematik.ti.erp.app.fhir.dispense.mocks.fhirMedicationDispenseWithoutMedicationV15
import de.gematik.ti.erp.app.fhir.dispense.model.erp.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel.Companion.extractMedicationDispense
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class FhirMedicationDispenseV15DispenseModelTest {

    @Test
    fun `medication dispense workflow 1_5 diga deeplink`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_5_diga_deeplink)
        val fhirModel = bundle.extractMedicationDispense()
        val erpModel = fhirModel.toErpModel(null)
        assertEquals(fhirMedicationDispenseDiGADeepLinkV15, fhirModel)
        assertEquals(erpMedicationDispenseDiGADeepLinkV15, erpModel)
    }

    @Test
    fun `medication dispense workflow 1_5 diga name and pzn`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_5_diga_name_and_pzn)
        val fhirModel = bundle.extractMedicationDispense()
        val erpModel = fhirModel.toErpModel(null)
        assertEquals(fhirMedicationDispenseDiGANameAndPznV15, fhirModel)
        assertEquals(erpMedicationDispenseDiGANameAndPznV15, erpModel)
    }

    @Test
    fun `medication dispense workflow 1_5 diga no redeem code`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_5_diga_no_redeem_code)
        val fhirModel = bundle.extractMedicationDispense()
        val erpModel = fhirModel.toErpModel(null)
        assertEquals(fhirMedicationDispenseDiGANoRedeemCodeV15, fhirModel)
        assertEquals(erpMedicationDispenseDiGANoRedeemCodeV15, erpModel)
    }

    @Test
    fun `medication dispense workflow 1_5 kombipackung`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_5_kombipackung)
        val fhirModel = bundle.extractMedicationDispense()
        val erpModel = fhirModel.toErpModel(null)
        assertEquals(fhirMedicationDispenseKombipackungV15, fhirModel)
        assertEquals(erpMedicationDispenseKombipackungV15, erpModel)
    }

    @Test
    fun `medication dispense workflow 1_5 rezeptur`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_5_rezeptur)
        val fhirModel = bundle.extractMedicationDispense()
        val erpModel = fhirModel.toErpModel(null)
        assertEquals(fhirMedicationDispenseRezepturV15, fhirModel)
        assertEquals(erpMedicationDispenseRezepturV15, erpModel)
    }

    @Test
    fun `medication dispense workflow 1_5 no medication`() {
        val bundle = Json.parseToJsonElement(medication_dispense_1_5_without_medication)
        val fhirModel = bundle.extractMedicationDispense()
        val erpModel = fhirModel.toErpModel(null)
        assertEquals(fhirMedicationDispenseWithoutMedicationV15, fhirModel)
        assertEquals(erpMedicationDispenseWithoutMedicationV15, erpModel)
    }
}
