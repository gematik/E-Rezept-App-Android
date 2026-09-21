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

package de.gematik.ti.erp.app.database.room.v2.task

import de.gematik.ti.erp.app.database.api.model.PrescriptionDataNotFoundException
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TaskStatusV1
import de.gematik.ti.erp.app.database.room.v2.task.diga.ErpTaskMedicationDeviceRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.ErpInsuranceInformationEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.TaskRoomEntityBundle
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toRoomEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toRoomEntityBundle
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpIngredientEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpQuantityEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpRatioEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpTeratogenicPrescriptionEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.multipleprescription.ErpMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.patient.ErpPatientEntity
import de.gematik.ti.erp.app.database.room.v2.task.practitioner.ErpPractitionerEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.prescription.TaskTypeValues
import de.gematik.ti.erp.app.database.room.v2.task.util.AddressEmbeddable
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTeratogenicPrescriptionErpModel
import de.gematik.ti.erp.app.diga.model.DigaStatusSteps
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.task.model.AddressErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val NOW: Instant = Instant.parse("2024-06-01T10:00:00Z")
private val AUTHORED_ON: Instant = Instant.parse("2024-05-01T08:00:00Z")
private val SCANNED_ON: Instant = Instant.parse("2024-05-15T12:00:00Z")

class TaskErpModelRoomMappersTest {

    @Test
    fun ingredientEntity_toErpModel_mapsAllFields() {
        val entity = ErpIngredientEntity(
            ingredientId = "ing1",
            medicationId = "med1",
            text = "Ingredient A",
            form = "Tablet",
            amount = "10mg",
            number = "123456",
            strength = ErpRatioEmbeddable(
                numerator = ErpQuantityEmbeddable("5", "mg"),
                denominator = ErpQuantityEmbeddable("1", "tab")
            )
        )

        val model = entity.toErpModel()

        assertEquals("Ingredient A", model.text)
        assertEquals("Tablet", model.form)
        assertEquals("10mg", model.amount)
        assertEquals("123456", model.number)
        assertNotNull(model.strength)
        assertEquals("5", model.strength?.numerator?.value)
        assertEquals("mg", model.strength?.numerator?.unit)
        assertEquals("1", model.strength?.denominator?.value)
        assertEquals("tab", model.strength?.denominator?.unit)
    }

    @Test
    fun ingredientModel_toRoomEntity_mapsAllFields() {
        val model = FhirMedicationIngredientErpModel(
            text = "Ingredient B",
            amount = "20mg",
            form = "Capsule",
            strengthRatio = FhirRatioErpModel(
                numerator = FhirQuantityErpModel("10", "mg"),
                denominator = FhirQuantityErpModel("1", "cap")
            ),
            identifier = FhirMedicationIdentifierErpModel(pzn = "654321", atc = null, ask = null, snomed = null)
        )

        val entity = model.toRoomEntity("ing2", "med1", "999888")

        assertEquals("ing2", entity.ingredientId)
        assertEquals("med1", entity.medicationId)
        assertEquals("Ingredient B", entity.text)
        assertEquals("Capsule", entity.form)
        assertEquals("20mg", entity.amount)
        assertEquals("999888", entity.number)
        assertNotNull(entity.strength)
        assertEquals("10", entity.strength.numerator.value)
        assertEquals("mg", entity.strength.numerator.unit)
        assertEquals("1", entity.strength.denominator.value)
        assertEquals("cap", entity.strength.denominator.unit)
    }

    @Test
    fun fhirTaskKbvMedicationErpModel_toRoomEntity_mapsMedicationProfile() {
        val profile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        )
        val model = FhirTaskKbvMedicationErpModel(
            text = "Ibuprofen",
            form = "TAB",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            medicationProfile = profile,
            amount = null,
            isVaccine = false,
            normSizeCode = "N2",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = emptyList(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "12345678",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        )

        val entity = model.toRoomEntity(medicationId = "med1", taskId = "task1")

        assertEquals(profile, entity.medicationProfile)
    }

    @Test
    fun erpMedicationEntity_toErpModel_mapsMedicationProfile() {
        val profile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.Compounding,
            version = ErpMedicationProfileVersion.V_16
        )
        val entity = ErpMedicationEntity(
            medicationId = "med1",
            taskId = "task1",
            ratioId = null,
            parentDispenseId = null,
            text = "Compounding text",
            medicationCategory = MedicationCategory.SONSTIGES.name,
            form = null,
            vaccine = false,
            manufacturingInstructions = "",
            packaging = "",
            amount = null,
            identifier = null,
            medicationProfile = profile,
            normSizeCode = "",
            pzn = null
        )

        val model = entity.toErpModel(dispense = null)

        assertEquals(profile, model.medicationProfile)
    }

    @Test
    fun erpMultiplePrescriptionEntity_toErpModel_mapsAllFields() {
        val entity = ErpMultiplePrescriptionEntity(
            multiplePrescriptionId = "mp1",
            taskId = "task1",
            indicator = true,
            numbering = ErpRatioEmbeddable(
                numerator = ErpQuantityEmbeddable("1", "4"),
                denominator = ErpQuantityEmbeddable("4", "4")
            ),
            start = Instant.parse("2024-06-01T00:00:00Z"),
            end = Instant.parse("2024-06-30T23:59:59Z")
        )

        val model = entity.toErpModel()

        assertEquals(true, model.indicator)
        assertEquals("1", model.numbering?.numerator?.value)
        assertEquals("4", model.numbering?.denominator?.value)
        assertEquals(Instant.parse("2024-06-01T00:00:00Z"), model.start)
        assertEquals(Instant.parse("2024-06-30T23:59:59Z"), model.end)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PatientErpModel ↔ ErpPatientEntity
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun patientEntity_toErpModel_mapsAllFields() {
        val entity = ErpPatientEntity(
            patientId = "p1",
            taskId = "task1",
            name = "Max Mustermann",
            dob = "1990-01-15",
            insuranceIdentifier = "X123456789",
            address = AddressEmbeddable(
                line1 = "Musterstraße 1",
                line2 = "",
                postalCode = "10115",
                additionalAddressInformation = "c/o Someone",
                city = "Berlin"
            )
        )

        val model = entity.toErpModel()

        assertEquals("Max Mustermann", model.name)
        assertEquals("X123456789", model.insuranceIdentifier)
        assertNotNull(model.address)
        assertEquals("Musterstraße 1", model.address!!.line1)
        assertEquals("c/o Someone", model.address!!.additionalAddressInformation)
        assertEquals("Berlin", model.address!!.city)
        assertNotNull(model.dateOfBirth)
    }

    @Test
    fun patientEntity_nullAddress_producesNullAddressInModel() {
        val entity = ErpPatientEntity(
            patientId = "p2",
            taskId = "task2",
            name = null,
            dob = null,
            insuranceIdentifier = null,
            address = null
        )

        val model = entity.toErpModel()

        assertNull(model.address)
        assertNull(model.name)
        assertNull(model.dateOfBirth)
    }

    @Test
    fun patientModel_toRoomEntity_roundTrip() {
        val dob = FhirTemporal.LocalDate(LocalDate.parse("1985-07-20"))
        val model = PatientErpModel(
            name = "Erika Muster",
            address = AddressErpModel(
                line1 = "Hauptstr. 5",
                line2 = "Etage 2",
                postalCode = "80331",
                additionalAddressInformation = null,
                city = "München"
            ),
            dateOfBirth = dob,
            insuranceIdentifier = "K9876543210"
        )

        val entity = model.toRoomEntity("patient_task1", "task1")
        assertEquals("patient_task1", entity.patientId)
        assertEquals("Erika Muster", entity.name)
        assertEquals("1985-07-20", entity.dob)
        assertEquals("K9876543210", entity.insuranceIdentifier)
        assertEquals("Hauptstr. 5", entity.address?.line1)
        assertEquals("München", entity.address?.city)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PractitionerErpModel ↔ ErpPractitionerEntity
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun practitionerEntity_toErpModel_mapsAllFields() {
        val entity = ErpPractitionerEntity(
            practitionerId = "pr1",
            taskId = "task1",
            name = "Dr. Anna Schmidt",
            qualification = "Allgemeinmedizinerin",
            practitionerIdentifier = "LANR-987654321",
            dentistIdentifier = null,
            telematikId = "1-987654321"
        )

        val model = entity.toErpModel()

        assertEquals("Dr. Anna Schmidt", model.name)
        assertEquals("Allgemeinmedizinerin", model.qualification)
        assertEquals("LANR-987654321", model.practitionerIdentifier)
        assertNull(model.dentistIdentifier)
        assertEquals("1-987654321", model.telematikId)
    }

    @Test
    fun practitionerModel_toRoomEntity_roundTrip() {
        val model = PractitionerErpModel(
            name = "Dr. Karl Müller",
            qualification = "Zahnarzt",
            practitionerIdentifier = null,
            dentistIdentifier = "KZVA-111",
            telematikId = "9-111222333"
        )

        val entity = model.toRoomEntity("practitioner_taskX", "taskX")
        assertEquals("practitioner_taskX", entity.practitionerId)
        assertEquals("Dr. Karl Müller", entity.name)
        assertEquals("KZVA-111", entity.dentistIdentifier)
        assertEquals("9-111222333", entity.telematikId)
        assertNull(entity.practitionerIdentifier)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OrganizationErpModel ↔ ErpOrganizationEntity
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun organizationEntity_toErpModel_mapsAllFields() {
        val entity = ErpOrganizationEntity(
            organizationId = "org1",
            taskId = "task1",
            name = "Praxis Muster",
            bsnr = "BSNR-123456789",
            phone = "+49-30-123456",
            mail = "praxis@example.com",
            address = AddressEmbeddable(
                line1 = "Berliner Str. 10",
                line2 = null,
                postalCode = "10117",
                additionalAddressInformation = null,
                city = "Berlin"
            )
        )

        val model = entity.toErpModel()

        assertEquals("Praxis Muster", model.name)
        assertEquals("BSNR-123456789", model.uniqueIdentifier)
        assertEquals("+49-30-123456", model.phone)
        assertEquals("praxis@example.com", model.mail)
        assertNotNull(model.address)
        assertEquals("Berliner Str. 10", model.address!!.line1)
    }

    @Test
    fun organizationModel_toRoomEntity_roundTrip() {
        val model = OrganizationErpModel(
            name = "MVZ Berlin",
            address = AddressErpModel(
                line1 = "Karl-Marx-Allee 1",
                line2 = "",
                postalCode = "10178",
                additionalAddressInformation = "Eingang B",
                city = "Berlin"
            ),
            uniqueIdentifier = "KV-001",
            phone = "030-999999",
            mail = null
        )

        val entity = model.toRoomEntity("org_task1", "task1")
        assertEquals("org_task1", entity.organizationId)
        assertEquals("MVZ Berlin", entity.name)
        assertEquals("KV-001", entity.bsnr)
        assertEquals("Eingang B", entity.address?.additionalAddressInformation)
        assertNull(entity.mail)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // InsuranceErpModel ↔ ErpInsuranceInformationEntity
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun insuranceEntity_toErpModel_mapsAllFields() {
        val entity = ErpInsuranceInformationEntity(
            insuranceInformationId = "ins1",
            taskId = "task1",
            name = "AOK Plus",
            status = "1",
            identifierNumber = "IK-123456789",
            coverageType = "GKV"
        )

        val model = entity.toErpModel()

        assertEquals("AOK Plus", model.name)
        assertEquals("1", model.status)
        assertEquals("IK-123456789", model.identifierNumber)
        assertEquals(InsuranceErpModelCoverageType.GKV, model.coverageType)
    }

    @Test
    fun insuranceModel_toRoomEntity_roundTrip() {
        val model = InsuranceErpModel(
            name = "Techniker Krankenkasse",
            status = "3",
            identifierNumber = "IK-987654321",
            coverageType = InsuranceErpModelCoverageType.PKV
        )

        val entity = model.toRoomEntity("ins_task1", "task1")
        assertEquals("ins_task1", entity.insuranceInformationId)
        assertEquals("task1", entity.taskId)
        assertEquals("Techniker Krankenkasse", entity.name)
        assertEquals("PKV", entity.coverageType)
    }

    @Test
    fun insuranceEntity_unknownCoverageType_fallsBackToUnknown() {
        val entity = ErpInsuranceInformationEntity(
            insuranceInformationId = "ins2",
            taskId = "task2",
            name = null,
            status = null,
            identifierNumber = null,
            coverageType = "NOT_A_VALID_COVERAGE"
        )

        val model = entity.toErpModel()
        assertEquals(InsuranceErpModelCoverageType.UNKNOWN, model.coverageType)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scanned task: domain → entity bundle → domain
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun scannedTask_toRoomEntityBundle_containsOnlyTaskEntity() {
        val scanned = scannedTaskModel()

        val bundle = scanned.toRoomEntityBundle()

        assertEquals(TaskTypeValues.SCANNED, bundle.task.taskType)
        assertEquals("task-scanned-1", bundle.task.taskId)
        assertEquals("profile-1", bundle.task.parentProfileId)
        assertEquals("ACCESSCODE123", bundle.task.accessCode)
        assertEquals("Ibuprofen 400mg", bundle.task.name)
        assertEquals(SCANNED_ON, bundle.task.scannedOn)
        assertEquals(2, bundle.task.index)
        assertNull(bundle.patient)
        assertNull(bundle.practitioner)
        assertNull(bundle.organization)
        assertNull(bundle.insuranceInformation)
    }

    @Test
    fun scannedTaskWithRefs_toErpModel_reconstructsScannedTask() {
        val scanned = scannedTaskModel()
        val bundle = scanned.toRoomEntityBundle()
        val withRefs = bundle.toTaskWithRefs()

        val result = withRefs.toErpModel()

        assertIs<TaskErpModel.Scanned>(result)
        assertEquals("task-scanned-1", result.taskId)
        assertEquals("profile-1", result.profileId)
        assertEquals("ACCESSCODE123", result.accessCode)
        assertEquals("Ibuprofen 400mg", result.name)
        assertEquals(SCANNED_ON, result.scannedOn)
        assertEquals(2, result.index)
        assertNull(result.redeemedOn)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Prescription task: domain → entity bundle → domain
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun prescriptionTaskWithRefs_toErpModel_roundTrip() {
        val prescription = prescriptionTaskModel()
        val bundle = prescription.toRoomEntityBundle()
        val withRefs = bundle.toTaskWithRefs()

        val result = withRefs.toErpModel()

        assertIs<TaskErpModel.Synced.Prescription>(result)
        assertEquals("task-rx-1", result.taskId)
        assertEquals("profile-1", result.profileId)
        assertEquals(TaskStatusEnum.Ready, result.status)
        assertEquals(true, result.isEuRedeemable)
        assertEquals(AUTHORED_ON, result.authoredOn)
        assertEquals(NOW, result.lastModified)

        // Patient
        assertNotNull(result.patient)
        assertEquals("Erika Muster", result.patient!!.name)
        assertEquals("Berlin", result.patient!!.address!!.city)

        // Practitioner
        assertNotNull(result.practitioner)
        assertEquals("Dr. Anna Schmidt", result.practitioner!!.name)
        assertEquals("9-111222333", result.practitioner!!.telematikId)

        // Organization
        assertNotNull(result.organization)
        assertEquals("Praxis Muster", result.organization!!.name)

        // Insurance
        assertNotNull(result.insuranceInformation)
        assertEquals(InsuranceErpModelCoverageType.GKV, result.insuranceInformation!!.coverageType)
        assertEquals("AOK Plus", result.insuranceInformation!!.name)
    }

    @Test
    fun prescriptionTask_withNullSubModels_toRoomEntityBundle_hasNullChildren() {
        val prescription = prescriptionTaskModel().copy(
            patient = null,
            practitioner = null,
            organization = null,
            insuranceInformation = null
        )

        val bundle = prescription.toRoomEntityBundle()

        assertNull(bundle.patient)
        assertNull(bundle.practitioner)
        assertNull(bundle.organization)
        assertNull(bundle.insuranceInformation)
        assertNull(bundle.task.patientId)
        assertNull(bundle.task.practitionerId)
        assertNull(bundle.task.organizationId)
        assertNull(bundle.task.insuranceInformationId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Diga task: domain → entity bundle → domain
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun digaTask_toRoomEntityBundle_hasCorrectTaskType() {
        val diga = digaTaskModel()

        val bundle = diga.toRoomEntityBundle()

        assertEquals(TaskTypeValues.DIGA, bundle.task.taskType)
        assertNotNull(bundle.patient)
        assertNotNull(bundle.insuranceInformation)
    }

    @Test
    fun digaTaskWithRefs_toErpModel_roundTrip() {
        val diga = digaTaskModel()
        val bundle = diga.toRoomEntityBundle()
        val withRefs = bundle.toTaskWithRefs()

        val result = withRefs.toErpModel()

        assertIs<TaskErpModel.Synced.Diga>(result)
        assertEquals("task-diga-1", result.taskId)
        assertEquals(TaskStatusEnum.InProgress, result.status)
        assertNotNull(result.patient)
        assertNull(result.deviceRequest) // not yet mapped
    }

    @Test
    fun digaTaskWithArchivedDeviceRequestStatus_mapsArchivedFlagTrue() {
        val task = ErpTaskEntity(
            taskId = "task-diga-archived",
            taskType = TaskTypeValues.DIGA,
            parentProfileId = "profile-1",
            accessCode = "DIGA-ACCESS",
            name = null,
            redeemedOn = null,
            authoredOn = AUTHORED_ON,
            lastModified = NOW
        )
        val deviceRequest = ErpTaskMedicationDeviceRequestEntity(
            deviceRequestId = "device_request_task-diga-archived",
            taskId = task.taskId,
            accidentInfoId = null,
            intent = "order",
            status = "archived",
            pzn = "12345678",
            appName = "Archive Test DiGA",
            isSelfUse = true,
            isNew = false,
            userActionState = null,
            sentCommunicationOn = null,
            authoredOn = AUTHORED_ON
        )

        val result = ErpTaskWithRefs(
            task = task,
            organization = null,
            practitioner = null,
            patient = null,
            insuranceInformation = null,
            medication = null,
            deviceRequest = deviceRequest,
            multiplePrescription = null,
            accidentInfo = null,
            medicationRequest = null,
            medicationDispenses = emptyList(),
            communications = emptyList()
        ).toErpModel()

        assertIs<TaskErpModel.Synced.Diga>(result)
        assertTrue(result.deviceRequest?.isArchived == true)
    }

    @Test
    fun digaTaskWithActiveDeviceRequestStatus_mapsArchivedFlagFalse() {
        val task = ErpTaskEntity(
            taskId = "task-diga-active",
            taskType = TaskTypeValues.DIGA,
            parentProfileId = "profile-1",
            accessCode = "DIGA-ACCESS",
            name = null,
            redeemedOn = null,
            authoredOn = AUTHORED_ON,
            lastModified = NOW
        )
        val deviceRequest = ErpTaskMedicationDeviceRequestEntity(
            deviceRequestId = "device_request_task-diga-active",
            taskId = task.taskId,
            accidentInfoId = null,
            intent = "order",
            status = "active",
            pzn = "12345678",
            appName = "Active Test DiGA",
            isSelfUse = true,
            isNew = false,
            userActionState = null,
            sentCommunicationOn = null,
            authoredOn = AUTHORED_ON
        )

        val result = ErpTaskWithRefs(
            task = task,
            organization = null,
            practitioner = null,
            patient = null,
            insuranceInformation = null,
            medication = null,
            deviceRequest = deviceRequest,
            multiplePrescription = null,
            accidentInfo = null,
            medicationRequest = null,
            medicationDispenses = emptyList(),
            communications = emptyList()
        ).toErpModel()

        assertIs<TaskErpModel.Synced.Diga>(result)
        assertFalse(result.deviceRequest?.isArchived == true)
    }

    @Test
    fun digaTaskWithSelfArchiveStep_mapsArchivedFlagTrue() {
        val task = ErpTaskEntity(
            taskId = "task-diga-self-archive",
            taskType = TaskTypeValues.DIGA,
            parentProfileId = "profile-1",
            accessCode = "DIGA-ACCESS",
            name = null,
            redeemedOn = null,
            authoredOn = AUTHORED_ON,
            lastModified = NOW
        )
        val deviceRequest = ErpTaskMedicationDeviceRequestEntity(
            deviceRequestId = "device_request_task-diga-self-archive",
            taskId = task.taskId,
            accidentInfoId = null,
            intent = "order",
            status = "active",
            pzn = "12345678",
            appName = "SelfArchive Test DiGA",
            isSelfUse = true,
            isNew = false,
            userActionState = DigaStatusSteps.SelfArchiveDiga.step,
            sentCommunicationOn = null,
            authoredOn = AUTHORED_ON
        )

        val result = ErpTaskWithRefs(
            task = task,
            organization = null,
            practitioner = null,
            patient = null,
            insuranceInformation = null,
            medication = null,
            deviceRequest = deviceRequest,
            multiplePrescription = null,
            accidentInfo = null,
            medicationRequest = null,
            medicationDispenses = emptyList(),
            communications = emptyList()
        ).toErpModel()

        assertIs<TaskErpModel.Synced.Diga>(result)
        assertTrue(result.deviceRequest?.isArchived == true)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Error cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun scannedTask_missingScannedOn_throwsException() {
        val taskEntity = ErpTaskEntity(
            taskId = "bad-task",
            taskType = TaskTypeValues.SCANNED,
            parentProfileId = "profile-1",
            accessCode = "CODE",
            name = null,
            redeemedOn = null,
            scannedOn = null, // missing!
            index = 0
        )
        val withRefs = taskEntity.toEmptyTaskWithRefs()

        assertFailsWith<PrescriptionDataNotFoundException> { withRefs.toErpModel() }
    }

    @Test
    fun syncedTask_missingProfileId_throwsException() {
        val taskEntity = ErpTaskEntity(
            taskId = "bad-task",
            taskType = TaskTypeValues.PRESCRIPTION,
            parentProfileId = null, // missing!
            accessCode = "CODE",
            name = null,
            redeemedOn = null,
            lastModified = NOW,
            authoredOn = AUTHORED_ON
        )
        val withRefs = taskEntity.toEmptyTaskWithRefs()

        assertFailsWith<PrescriptionDataNotFoundException> { withRefs.toErpModel() }
    }

    @Test
    fun taskWithRefs_unknownTaskType_throwsException() {
        val taskEntity = ErpTaskEntity(
            taskId = "unknown-type",
            taskType = "WrongType",
            parentProfileId = "profile-1",
            accessCode = "CODE",
            name = null,
            redeemedOn = null
        )
        val withRefs = taskEntity.toEmptyTaskWithRefs()

        assertFailsWith<PrescriptionDataNotFoundException> { withRefs.toErpModel() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TaskStatusV1 ↔ TaskStatusEnum round-trip
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun taskStatus_roundTrip_allKnownValues() {
        val knownStatuses = listOf(
            TaskStatusEnum.Ready to TaskStatusV1.Ready,
            TaskStatusEnum.InProgress to TaskStatusV1.InProgress,
            TaskStatusEnum.Completed to TaskStatusV1.Completed
        )
        knownStatuses.forEach { (domainStatus, v1Status) ->
            val prescription = prescriptionTaskModel().copy(status = domainStatus)
            val bundle = prescription.toRoomEntityBundle()
            assertEquals(v1Status.name, bundle.task.status.name)

            val recovered = bundle.toTaskWithRefs().toErpModel() as TaskErpModel.Synced.Prescription
            assertEquals(domainStatus, recovered.status)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun scannedTaskModel() = TaskErpModel.Scanned(
        profileId = "profile-1",
        taskId = "task-scanned-1",
        accessCode = "ACCESSCODE123",
        name = "Ibuprofen 400mg",
        isEuRedeemable = false,
        scannedOn = SCANNED_ON,
        index = 2,
        redeemedOn = null
    )

    private fun prescriptionTaskModel() = TaskErpModel.Synced.Prescription(
        profileId = "profile-1",
        taskId = "task-rx-1",
        accessCode = "RXCODE456",
        name = "Ibuprofen 400mg",
        isEuRedeemable = true,
        isEuRedeemableByPatientAuthorization = false,
        lastModified = NOW,
        lastMedicationDispense = null,
        expiresOn = Instant.parse("2025-01-01T00:00:00Z"),
        acceptUntil = Instant.parse("2024-12-01T00:00:00Z"),
        authoredOn = AUTHORED_ON,
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "pvs-001",
        failureToReport = "",
        patient = PatientErpModel(
            name = "Erika Muster",
            address = AddressErpModel(
                line1 = "Hauptstr. 5",
                line2 = "",
                postalCode = "10115",
                additionalAddressInformation = null,
                city = "Berlin"
            ),
            dateOfBirth = FhirTemporal.LocalDate(LocalDate.parse("1985-07-20")),
            insuranceIdentifier = "X123456789"
        ),
        practitioner = PractitionerErpModel(
            name = "Dr. Anna Schmidt",
            qualification = "Allgemeinmedizin",
            practitionerIdentifier = "LANR-111222333",
            dentistIdentifier = null,
            telematikId = "9-111222333"
        ),
        organization = OrganizationErpModel(
            name = "Praxis Muster",
            address = AddressErpModel(
                line1 = "Musterstr. 1",
                line2 = null.orEmpty(),
                postalCode = "10178",
                additionalAddressInformation = null,
                city = "Berlin"
            ),
            uniqueIdentifier = "BSNR-123",
            phone = "030-555555",
            mail = null
        ),
        insuranceInformation = InsuranceErpModel(
            name = "AOK Plus",
            status = "1",
            identifierNumber = "IK-100696012",
            coverageType = InsuranceErpModelCoverageType.GKV
        ),
        medicationRequest = null,
        medicationDispenses = emptyList()
    )

    private fun digaTaskModel() = TaskErpModel.Synced.Diga(
        profileId = "profile-1",
        taskId = "task-diga-1",
        accessCode = "DIGACODE789",
        name = "Meine DiGA",
        isEuRedeemable = false,
        isEuRedeemableByPatientAuthorization = false,
        lastModified = NOW,
        expiresOn = null,
        acceptUntil = null,
        authoredOn = AUTHORED_ON,
        status = TaskStatusEnum.InProgress,
        isIncomplete = false,
        pvsIdentifier = "",
        failureToReport = "",
        patient = PatientErpModel(
            name = "Klaus Mustermann",
            address = null,
            dateOfBirth = null,
            insuranceIdentifier = "K999"
        ),
        practitioner = null,
        organization = null,
        insuranceInformation = InsuranceErpModel(
            name = "BKK VBU",
            status = "3",
            identifierNumber = "IK-103411401",
            coverageType = InsuranceErpModelCoverageType.GKV
        ),
        deviceRequest = null,
        medicationDispenses = emptyList()
    )

    /** Convenience: build an [de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefs] from a [TaskRoomEntityBundle]. */
    private fun TaskRoomEntityBundle.toTaskWithRefs() = ErpTaskWithRefs(
        task = task,
        organization = organization,
        practitioner = practitioner,
        patient = patient,
        insuranceInformation = insuranceInformation,
        medication = null as ErpMedicationWithRefs?,
        medicationRequest = null,
        deviceRequest = null,
        multiplePrescription = null,
        accidentInfo = null,
        medicationDispenses = emptyList(),
        communications = emptyList()
    )

    /** Convenience: build an empty [ErpTaskWithRefs] with just the task entity. */
    private fun ErpTaskEntity.toEmptyTaskWithRefs() = ErpTaskWithRefs(
        task = this,
        organization = null,
        practitioner = null,
        patient = null,
        insuranceInformation = null,
        medication = null as ErpMedicationWithRefs?,
        medicationRequest = null,
        deviceRequest = null,
        multiplePrescription = null,
        accidentInfo = null,
        medicationDispenses = emptyList(),
        communications = emptyList()
    )

    @Test
    fun fhirTaskKbvMedicationRequestErpModel_toRoomEntity_mapsTeratogenicPrescription() {
        val fhirTeratogenic = FhirTeratogenicPrescriptionErpModel(
            offLabel = true,
            gebaerfaehigeFrau = false,
            einhaltungSicherheitsmassnahmen = true,
            aushaendigungInformationsmaterialien = false,
            erklaerungSachkenntnis = true
        )
        val model = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = null,
            dateOfAccident = null,
            location = "Praxis",
            accidentType = FhirTaskAccidentType.None,
            emergencyFee = false,
            additionalFee = "1",
            substitutionAllowed = true,
            dosageInstruction = "1-0-0",
            note = "Note text",
            quantity = 2,
            multiplePrescriptionInfo = null,
            isSer = false,
            prescriberId = "presc1",
            teratogenicPrescription = fhirTeratogenic
        )

        val entity = model.toRoomEntity("medRequest1", "task1", "med1")

        assertEquals("medRequest1", entity.medicationRequestId)
        assertEquals("task1", entity.taskId)
        assertEquals("med1", entity.medicationId)
        val roomTeratogenic = entity.teratogenicPrescription
        assertNotNull(roomTeratogenic)
        assertEquals(true, roomTeratogenic.offLabel)
        assertEquals(false, roomTeratogenic.gebaerfaehigeFrau)
        assertEquals(true, roomTeratogenic.einhaltungSicherheitsmassnahmen)
        assertEquals(false, roomTeratogenic.aushaendigungInformationsmaterialien)
        assertEquals(true, roomTeratogenic.erklaerungSachkenntnis)
    }

    @Test
    fun erpMedicationRequestEntity_toErpModel_mapsTeratogenicPrescription() {
        val roomTeratogenic = ErpTeratogenicPrescriptionEmbeddable(
            offLabel = false,
            gebaerfaehigeFrau = true,
            einhaltungSicherheitsmassnahmen = false,
            aushaendigungInformationsmaterialien = true,
            erklaerungSachkenntnis = false
        )
        val entity = ErpMedicationRequestEntity(
            medicationRequestId = "medReq2",
            taskId = "task2",
            emergencyFee = true,
            quantity = 1,
            note = "Some note",
            location = "Clinic",
            authoredOn = "2024-06-01",
            substitutionAllowed = false,
            dosageInstruction = "0-0-1",
            bvg = true,
            additionalFee = "0",
            dateOfAccident = null,
            accidentType = "None",
            teratogenicPrescription = roomTeratogenic,
            medicationId = "med2"
        )

        val model = entity.toErpModel(medication = null, multiplePrescription = null)

        val domainTeratogenic = model.teratogenicPrescription
        assertNotNull(domainTeratogenic)
        assertEquals(false, domainTeratogenic.offLabel)
        assertEquals(true, domainTeratogenic.gebaerfaehigeFrau)
        assertEquals(false, domainTeratogenic.einhaltungSicherheitsmassnahmen)
        assertEquals(true, domainTeratogenic.aushaendigungInformationsmaterialien)
        assertEquals(false, domainTeratogenic.erklaerungSachkenntnis)
    }
}
