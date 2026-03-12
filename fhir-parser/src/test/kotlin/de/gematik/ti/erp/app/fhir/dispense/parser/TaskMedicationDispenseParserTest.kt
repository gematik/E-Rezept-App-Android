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

package de.gematik.ti.erp.app.fhir.dispense.parser

import de.gematik.ti.erp.app.data.TestDataGenerator
import de.gematik.ti.erp.app.data.bundle_dispense_1_4_complex_bundle
import de.gematik.ti.erp.app.data.bundle_dispense_1_4_simple
import de.gematik.ti.erp.app.data.bundle_dispense_1_5_multiple
import de.gematik.ti.erp.app.data.bundle_dispense_1_5_single
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_DosageDaysOfWeek
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_DosageInterval
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_DosageTime
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_DosageTimeOfDay
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_DosageWeekDay
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_digaDeeplink
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_digaName
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_digaNoRedeemCode
import de.gematik.ti.erp.app.data.bundle_dispense_1_6_simple
import de.gematik.ti.erp.app.data.bundle_dispenses_1_4_multiple_simple_medications
import de.gematik.ti.erp.app.data.medication_dispense_1_4_diga_deeplink
import de.gematik.ti.erp.app.data.medication_dispense_1_4_diga_name_and_pzn
import de.gematik.ti.erp.app.data.medication_dispense_1_4_diga_no_redeem_code
import de.gematik.ti.erp.app.data.medication_dispense_bundle_version_1_2
import de.gematik.ti.erp.app.data.medication_dispense_eu_1_0_multiple_dispense
import de.gematik.ti.erp.app.data.medication_dispense_eu_1_0_single_dispense
import de.gematik.ti.erp.app.data.medication_dispense_eu_1_0_single_dispense_urn
import de.gematik.ti.erp.app.data.medication_dispense_medication_1_4_complex
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_2
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_4_complex
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_4_kombi_complex
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_4_multiple_simple
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_4_simple
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_diga_deeplink
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_diga_name
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_diga_no_redeem_code
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_dosage_dayofweek
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_dosage_interval
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_dosage_time
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_dosage_timeofday
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_dosage_weekday
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_1_6_simple
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_diga_deeplink
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_diga_name_and_pzn
import de.gematik.ti.erp.app.fhir.dispense.mocks.erp_model_diga_no_redeem_code
import de.gematik.ti.erp.app.fhir.dispense.mocks.medicationDispenseErpModelCollectionEuV10Multiple
import de.gematik.ti.erp.app.fhir.dispense.mocks.medicationDispenseErpModelCollectionEuV10Single
import de.gematik.ti.erp.app.fhir.dispense.mocks.medicationDispenseErpModelCollectionEuV10SingleUrn
import de.gematik.ti.erp.app.fhir.dispense.mocks.medicationDispenseErpModelCollectionV15MultipleMedications
import de.gematik.ti.erp.app.fhir.dispense.mocks.medicationDispenseErpModelCollectionV15SingleMedication
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.Test
import kotlin.test.assertEquals

class TaskMedicationDispenseParserTest {
    private val parser = TaskMedicationDispenseParser()
    private val testDataGenerator = TestDataGenerator()

    @Test
    fun `test  bundle for diga deeplink`() {
        val resource = Json.parseToJsonElement(medication_dispense_1_4_diga_deeplink)
        val result = Json.parseToJsonElement(erp_model_diga_deeplink)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test  bundle for diga name and pzn`() {
        val resource = Json.parseToJsonElement(medication_dispense_1_4_diga_name_and_pzn)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_diga_name_and_pzn)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test  bundle for diga no redeem code`() {
        val resource = Json.parseToJsonElement(medication_dispense_1_4_diga_no_redeem_code)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_diga_no_redeem_code)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test bundle for legacy model`() {
        val bundle = Json.parseToJsonElement(medication_dispense_bundle_version_1_2)
        val result = Json.parseToJsonElement(erp_model_1_2)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test bundle for 1_4 complex bundle`() {
        val bundle = Json.parseToJsonElement(bundle_dispense_1_4_complex_bundle)
        val result = Json.parseToJsonElement(erp_model_1_4_complex)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test bundle for 1_4 simple bundle`() {
        val bundle = Json.parseToJsonElement(bundle_dispense_1_4_simple)
        val result = Json.parseToJsonElement(erp_model_1_4_simple)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test bundle for 1_4 simple bundle with multiple dispense information`() {
        val bundle = Json.parseToJsonElement(bundle_dispenses_1_4_multiple_simple_medications)
        val result = Json.parseToJsonElement(erp_model_1_4_multiple_simple)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test bundle for 1_4 highest level complexity`() {
        val medication = Json.parseToJsonElement(medication_dispense_medication_1_4_complex)
        val bundle = testDataGenerator.create_1_4_DispenseBundleFromMedications(
            medications = listOf(medication)
        )
        val result = Json.parseToJsonElement(erp_model_1_4_kombi_complex)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test bundle for 1_5 single medication`() {
        val bundle = Json.parseToJsonElement(bundle_dispense_1_5_single)
        val erpModel = parser.extract(bundle)
        assertEquals(medicationDispenseErpModelCollectionV15SingleMedication, erpModel)
    }

    @Test
    fun `test bundle for 1_5 multiple medications`() {
        val bundle = Json.parseToJsonElement(bundle_dispense_1_5_multiple)
        val erpModel = parser.extract(bundle)
        assertEquals(medicationDispenseErpModelCollectionV15MultipleMedications, erpModel)
    }

    @Test
    fun `single eu medication dispense`() {
        val bundle = Json.parseToJsonElement(medication_dispense_eu_1_0_single_dispense)
        val erpModel = parser.extract(bundle)
        assertEquals(medicationDispenseErpModelCollectionEuV10Single, erpModel)
    }

    @Test
    fun `multiple eu medication dispense`() {
        val bundle = Json.parseToJsonElement(medication_dispense_eu_1_0_multiple_dispense)
        val erpModel = parser.extract(bundle)
        assertEquals(medicationDispenseErpModelCollectionEuV10Multiple, erpModel)
    }

    @Test
    fun `single eu medication dispense with URN references`() {
        val bundle = Json.parseToJsonElement(medication_dispense_eu_1_0_single_dispense_urn)
        val erpModel = parser.extract(bundle)
        assertEquals(medicationDispenseErpModelCollectionEuV10SingleUrn, erpModel)
    }

    // 1.6 version tests

    @Test
    fun `test simple bundle for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_simple)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_simple)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test diga deeplink bundle for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_digaDeeplink)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_diga_deeplink)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test diga no redeem code bundle for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_digaNoRedeemCode)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_diga_no_redeem_code)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test dispense bundle dosage of the week for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_DosageDaysOfWeek)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_dosage_dayofweek)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test dispense bundle dosage with interval for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_DosageInterval)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_dosage_interval)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test dispense bundle dosage with time for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_DosageTime)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_dosage_time)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test dispense bundle dosage with time of day for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_DosageTimeOfDay)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_dosage_timeofday)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test dispense bundle dosage with weekday for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_DosageWeekDay)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_dosage_weekday)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }

    @Test
    fun `test diga name bundle for 1_6 version`() {
        val resource = Json.parseToJsonElement(bundle_dispense_1_6_digaName)
        val bundle = testDataGenerator.createMedicationDispenseBundleFromResources(listOf(resource))
        val result = Json.parseToJsonElement(erp_model_1_6_diga_name)
        val erpModel = parser.extract(bundle)
        val serializedModel = Json.encodeToJsonElement(serializer(), erpModel)
        assertEquals(result, serializedModel)
    }
}
