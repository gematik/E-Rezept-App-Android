/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription

import ca.uhn.fhir.context.FhirContext
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.repository.FhirCoverage
import de.gematik.ti.erp.app.prescription.repository.FhirMedication
import de.gematik.ti.erp.app.prescription.repository.FhirMedicationRequest
import de.gematik.ti.erp.app.prescription.repository.FhirOrganization
import de.gematik.ti.erp.app.prescription.repository.FhirPatient
import de.gematik.ti.erp.app.prescription.repository.FhirPractitioner
import de.gematik.ti.erp.app.prescription.repository.Mapper
import de.gematik.ti.erp.app.prescription.repository.NormSize
import de.gematik.ti.erp.app.prescription.repository.accessCode
import de.gematik.ti.erp.app.prescription.repository.extractKBVBundle
import de.gematik.ti.erp.app.prescription.repository.extractKBVBundleReference
import de.gematik.ti.erp.app.prescription.repository.extractResource
import de.gematik.ti.erp.app.prescription.repository.extractResourceForReference
import de.gematik.ti.erp.app.prescription.repository.extractResources
import de.gematik.ti.erp.app.prescription.repository.findReferences
import de.gematik.ti.erp.app.prescription.repository.mapToUi
import de.gematik.ti.erp.app.prescription.repository.prescriptionId
import de.gematik.ti.erp.app.utils.emptyTestBundle
import de.gematik.ti.erp.app.utils.taskWithDirectAssignmentWithoutKBVBundle
import de.gematik.ti.erp.app.utils.testBundle
import de.gematik.ti.erp.app.utils.testCommunicationBundle
import de.gematik.ti.erp.app.utils.testMedicationDispenseBundle
import de.gematik.ti.erp.app.utils.testSingleKBVBundle
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Composition
import org.hl7.fhir.r4.model.MedicationDispense
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

const val KBVBUNDLE_REFERENCE = "a02c3b44-0500-0000-0002-000000000000"
const val ACCESS_CODE = "68db761b666f7e75a32090fd4d109e2766e02693741278ab6dc2df90f1cbb3af"
const val PRESCRIPTION_ID = "160.000.088.357.676.93"
const val PRACTITIONER_ID = "Practitioner/20597e0e-cb2a-45b3-95f0-dc3dbdb617c3"
const val TASK_ID = "160.000.088.357.676.93"
const val KBV_REFERENCE = "#5c310594-1dd2-11b2-803b-63bf44e44fb8"
const val PATIENT_ID = "Patient/9774f67f-a238-4daf-b4e6-679deeef3811"
const val PATIENT_IDENTIFIER = "X110498793"
const val PATIENT_ADDRESS_LINE = "Siegburger Str. 155, 51105, Köln"
const val PATIENT_BIRTH_DATE = "1964-04-04"
const val PRESCRIPTION_FLOW_TYPE_CODE = 160
const val PATIENT_NAME = "Prof. Dr. Karl-Friederich Graf Freiherr von Schaumberg"
const val PATIENT_INSURANCE_NAME = "AOK Rheinland/Hamburg"
const val PATIENT_TITLE = ""
const val PRACTITIONER_NAME = "Prof. Dr. Hannelore Popówitsch"
const val PRACTITIONER_QUALIFICATION = "Innere und Allgemeinmedizin (Hausarzt)"
const val PRACTITIONER_LANR = "445588777"
const val MEDICATION_ID = "Medication/5fe6e06c-8725-46d5-aecd-e65e041ca3de"

const val ORGANIZATION_NAME = "Universitätsklinik Campus Süd"
const val ORGANIZATION_PHONE = "06841/7654321"
val ORGANIZATION_MAIL = "unikliniksued@test.de"
const val ORGANIZATION_ADDRESS = "Kirrberger Str. 100"
const val ORGANIZATION_BSNR = "998877665"

const val AUTHORED_ON = "2021-11-30"

const val EXPIRES_ON = "2022-03-02"
const val ACCEPT_UNTIL = "2021-12-28"
const val LAST_MODIFIED = "2021-11-30T14:17:39.222+00:00"

const val MEDICATION_TEXT = "Olanzapin Heumann 20mg"
const val MEDICATION_TYPE = R.string.kbv_code_dosage_form_smt
val MEDICATION_SIZE = NormSize("N3", R.string.kbv_norm_size_n3) // "N3 - Normgröße 3"
const val MEDICATION_PZN = "08850519"

const val COVERAGE_NAME = "AOK Nordost - Die Gesundheitskasse"
const val COVERAGE_STATUS = R.string.kbv_member_status_1 // "Mitglied"

val ACCIDENT_DATE = null
val ACCIDENT_LOCATION = null
const val EMERGENCY_FEE = false
const val SUBSTITUTION_ALLOWED = false

const val MED_DISPENSE_ID = "160.000.000.012.852.10"
const val MED_DISPENSE_PATIENT_ID = "X110497056"
const val MED_DISPENSE_UNIQUE_ID = "06313728"
const val MED_DISPENSE_WAS_SUBSTITUTED = false
const val MED_DISPENSE_DOSAGE_INSTRUCTION = "1-0-1-0"
const val MED_DISPENSE_PERFORMER = "3-SMC-B-Testkarte-883110000129068"
const val MED_DISPENSE_WHEN_HANDED_OVER = "2021-06-29T07:29:19Z"

class MapperTest {

    lateinit var mapper: Mapper
    lateinit var bundle: Bundle
    lateinit var singleKBVBundle: Bundle
    lateinit var medicationDispenseBundle: MedicationDispense

    @Before
    fun setup() {
        mapper = Mapper(FhirContext.forR4().newJsonParser())
        bundle = testBundle()
        singleKBVBundle = testSingleKBVBundle()
        medicationDispenseBundle = testMedicationDispenseBundle()
    }

    @Test
    fun `parse bundle for tasks and assure tasks are not null`() {
        val task = bundle.extractResources<Task>()
        assertNotNull(task)
    }

    @Test
    fun `parse bundle for task with direct assignment and assure tasks are not null`() {
        bundle = taskWithDirectAssignmentWithoutKBVBundle()
        val task = bundle.extractResources<Task>()
        assertNotNull(task)
    }

    @Test
    fun `parse bundle for communications and assure communications are not null`() {
        val commBundle = testCommunicationBundle()
        val communication = commBundle.extractResources<Communication>()
        assertNotNull(communication)
    }

    @Test
    fun `parse bundle for communications - no given communications`() {
        bundle = emptyTestBundle()
        val communications = bundle.extractResources<Communication>()
        assertNotNull(communications)
        assert(communications!!.isEmpty())
    }

    @Test
    fun `map bundle to communication`() {
        val commBundle = testCommunicationBundle()
        val communication = mapper.mapFhirBundleToCommunications(commBundle, "")
        communication[0].let {
            assertEquals("16d2cfc8-2023-11b2-81e1-783a425d8e87", it.communicationId)
            assertEquals("39c67d5b-1df3-11b2-80b4-783a425d8e87", it.taskId)
            assertEquals("3-09.2.S.10.743", it.telematicsId)
            assertEquals("{do something}", it.payload)
        }
        communication[1].let {
            assertEquals("e277e66f-2345-11b2-86e3-783a425d8e87", it.communicationId)
            assertEquals("39c67d5b-1df3-11b2-80b4-783a425d8e87", it.taskId)
            assertEquals("3-17.2.1234560000.10.372", it.telematicsId)
            assertEquals("{do something}", it.payload)
        }
    }

    @Test
    fun `parse bundle for tasks - no given tasks`() {
        bundle = emptyTestBundle()
        val tasks = bundle.extractResources<Task>()
        assertNotNull(tasks)
        assert(tasks!!.isEmpty())
    }

    @Test
    fun `search for resourceType that is not there in bundle - should return empty list`() {
        val tasks = bundle.extractResources<Composition>()
        assertNotNull(tasks)
        assert(tasks!!.isEmpty())
    }

    @Test
    fun `read bundle and extract KBVBundle reference`() {
        val tasks = bundle.extractResources<Task>()
        val kbvBundleReference = tasks!![0].extractKBVBundleReference()
        assertNotNull(kbvBundleReference)
        assertEquals(KBVBUNDLE_REFERENCE, kbvBundleReference)
    }

    @Test
    fun `extract KBVBundle with given reference`() {
        val tasks = bundle.extractResources<Task>()
        val kbvBundleReference = tasks!![0].extractKBVBundleReference()
        assertNotNull(kbvBundleReference)
        val kbvBundle = bundle.extractKBVBundle(kbvBundleReference!!)
        assertNotNull(kbvBundle)
    }

    @Test
    fun `extract accessCode from Task - should not be null`() {
        val tasks = bundle.extractResources<Task>()
        val accessCode = tasks!![0].accessCode()
        assertNotNull(accessCode)
        assertEquals(ACCESS_CODE, accessCode)
    }

    @Test
    fun `extract prescriptionId from Task - should not be null`() {
        val tasks = bundle.extractResources<Task>()
        val prescriptionID = tasks!![0].prescriptionId()
        assertNotNull(prescriptionID)
        assertEquals(PRESCRIPTION_ID, prescriptionID)
    }

    @Test
    fun `extract resource for reference`() {
        val tasks = bundle.extractResources<Task>()
        val kbvReference = tasks!![0].extractKBVBundleReference()
        assertNotNull(kbvReference)
        val kbvBundle = bundle.extractKBVBundle(kbvReference!!)

        val medicationRequest =
            kbvBundle?.extractResource<MedicationRequest>()
        val references = medicationRequest?.findReferences()
        references?.let {
            val patient =
                kbvBundle.extractResourceForReference<Patient>(reference = it["patient"] ?: "foo")
            assertNotNull(patient)
        }
    }

    @Test
    fun `map bundle to Task`() {
        val task = mapper.mapFhirBundleToTaskWithKBVBundle(bundle, "")

        assertEquals(TASK_ID, task.taskId)
        assertEquals(ACCESS_CODE, task.accessCode)
        assertEquals(
            OffsetDateTime.parse(LAST_MODIFIED).truncatedTo(ChronoUnit.SECONDS),
            task.lastModified
        )
        assertEquals(ORGANIZATION_NAME, task.organization)
        assertEquals(MEDICATION_TEXT, task.medicationText)

        assertEquals(
            LocalDate.parse(EXPIRES_ON),
            task.expiresOn
        )
        assertEquals(
            LocalDate.parse(ACCEPT_UNTIL),
            task.acceptUntil
        )
        assertEquals(
            LocalDate.parse(AUTHORED_ON),
            task.authoredOn?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDate()
        )
    }

    @Test
    fun `parse kbv bundle`() {
        val task = mapper.mapFhirBundleToTaskWithKBVBundle(bundle, "")

        mapper.parseKBVBundle(task.rawKBVBundle!!)
    }

    @Test
    fun `map kbv bundle to PatientDetail`() {
        val patientDetail =
            testSingleKBVBundle().extractResources<FhirPatient>()!!.first().mapToUi()

        assertEquals(PATIENT_NAME, patientDetail.name)
        assertEquals(PATIENT_ADDRESS_LINE, patientDetail.address)
        assertEquals(LocalDate.parse(PATIENT_BIRTH_DATE), patientDetail.birthdate)
        assertEquals(PATIENT_IDENTIFIER, patientDetail.insuranceIdentifier)
    }

    @Test
    fun `map kbv bundle to PractitionerDetail`() {
        val practitionerDetail =
            testSingleKBVBundle().extractResources<FhirPractitioner>()!!.first().mapToUi()

        assertEquals(PRACTITIONER_NAME, practitionerDetail.name)
        assertEquals(PRACTITIONER_QUALIFICATION, practitionerDetail.qualification)
        assertEquals(PRACTITIONER_LANR, practitionerDetail.practitionerIdentifier)
    }

    @Test
    fun `map kbv bundle to MedicationDetail`() {
        val medicationDetail =
            testSingleKBVBundle().extractResources<FhirMedication>()!!.first().mapToUi()

        assertEquals(MEDICATION_TEXT, medicationDetail.text)
        assertEquals(MEDICATION_SIZE, medicationDetail.normSize)
        assertEquals(MEDICATION_TYPE, medicationDetail.type)
        assertEquals(MEDICATION_PZN, medicationDetail.uniqueIdentifier)
    }

    @Test
    fun `map kbv bundle to InsuranceCompany`() {
        val coverageDetail =
            testSingleKBVBundle().extractResources<FhirCoverage>()!!.first().mapToUi()

        assertEquals(COVERAGE_NAME, coverageDetail.name)
        assertEquals(COVERAGE_STATUS, coverageDetail.status)
    }

    @Test
    fun `map kbv bundle to OrganizationDetail`() {
        val organizationDetail =
            testSingleKBVBundle().extractResources<FhirOrganization>()!!.first().mapToUi()

        assertEquals(ORGANIZATION_NAME, organizationDetail.name)
        assertEquals(ORGANIZATION_ADDRESS, organizationDetail.address)
        assertEquals(ORGANIZATION_MAIL, organizationDetail.mail)
        assertEquals(ORGANIZATION_PHONE, organizationDetail.phone)
        assertEquals(ORGANIZATION_BSNR, organizationDetail.uniqueIdentifier)
    }

    @Test
    fun `map kbv bundle to MedicationRequestDetail`() {
        val accidentDetail =
            testSingleKBVBundle().extractResources<FhirMedicationRequest>()!!.first().mapToUi()

        assertEquals(ACCIDENT_DATE, accidentDetail.dateOfAccident)
        assertEquals(ACCIDENT_LOCATION, accidentDetail.location)
        assertEquals(EMERGENCY_FEE, accidentDetail.emergencyFee)
        assertEquals(SUBSTITUTION_ALLOWED, accidentDetail.substitutionAllowed)
    }

    @Test
    fun `map medication dispense to MedicationDispenseSimple`() {
        val medicationDispenseSimple =
            mapper.mapMedicationDispenseToMedicationDispenseSimple(medicationDispenseBundle)
        assertEquals(MED_DISPENSE_ID, medicationDispenseSimple.taskId)
        assertEquals(MED_DISPENSE_PATIENT_ID, medicationDispenseSimple.patientIdentifier)
        assertEquals(MED_DISPENSE_UNIQUE_ID, medicationDispenseSimple.uniqueIdentifier)
        assertEquals(MED_DISPENSE_WAS_SUBSTITUTED, medicationDispenseSimple.wasSubstituted)
        assertEquals(MED_DISPENSE_DOSAGE_INSTRUCTION, medicationDispenseSimple.dosageInstruction)
        assertEquals(MED_DISPENSE_PERFORMER, medicationDispenseSimple.performer)
        assertEquals(
            OffsetDateTime.parse(MED_DISPENSE_WHEN_HANDED_OVER).truncatedTo(ChronoUnit.SECONDS),
            medicationDispenseSimple.whenHandedOver
        )
    }
}
