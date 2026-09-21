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

package de.gematik.ti.erp.app.pharmacy.mocks

import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.task.model.AccidentType
import de.gematik.ti.erp.app.task.model.AdditionalFeeErpModel
import de.gematik.ti.erp.app.task.model.Identifier
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.Quantity
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.Ratio
import de.gematik.ti.erp.app.task.model.RatioErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import io.mockk.mockk

internal val MOCK_ACTIVE_PROFILE = ProfileErpModel(
    id = PROFILE_ID,
    name = "Erna Mustermann",
    profileImageData = ProfileImageDataErpModel(
        avatar = Avatar.Baby,
        image = null,
        color = ProfileColorNames.PINK
    ),
    isConsentDrawerShown = true,
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "Erna Mustermann",
        insuranceIdentifier = "AOK",
        insuranceName = null,
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = null
    ),
    lastAuthenticated = mockk(),
    lastAuditEventSynced = null,
    lastTaskSynced = mockk(),
    active = true,
    isNewlyCreated = false,
    userAuthentication = UserAuthenticationErpModel.NotInitialized
)

internal val MOCK_SHIPPING_CONTACT = ShippingInfoErpModel(
    name = "Max Mustermann",
    street = "Musterstraße 1",
    addressDetail = "",
    zip = "12345",
    city = "Musterstadt",
    phone = "0123456789",
    mail = "",
    deliveryInfo = ""
)

internal val MOCK_SCANNED_TASK_DATA_REDEEMABLE_01 = TaskErpModel.Scanned(
    index = 1,
    name = "ScannedTaskName",
    profileId = "1",
    taskId = "1",
    accessCode = "1",
    redeemedOn = null,
    scannedOn = Clock.System.now(),
    isEuRedeemable = false
)

private val MOCK_SYNCED_TASK_BASE = TaskErpModel.Synced.Prescription(
    profileId = "testProfileId",
    taskId = "pharmacyTask1",
    name = null,
    accessCode = "testAccessCode",
    isEuRedeemable = false,
    lastModified = Clock.System.now(),
    isEuRedeemableByPatientAuthorization = false,
    organization = OrganizationErpModel(name = "TestOrg", address = null, uniqueIdentifier = null, phone = null, mail = null),
    practitioner = PractitionerErpModel(name = "Dr. Test", qualification = null, practitionerIdentifier = null, dentistIdentifier = null, telematikId = null),
    patient = PatientErpModel(name = null, address = null, dateOfBirth = null, insuranceIdentifier = null),
    insuranceInformation = InsuranceErpModel(name = "TestInsurance", status = "Active", coverageType = InsuranceErpModelCoverageType.GKV),
    expiresOn = Clock.System.now().plus(1.days),
    acceptUntil = Clock.System.now().plus(1.days),
    authoredOn = Clock.System.now(),
    status = TaskStatusEnum.Ready,
    isIncomplete = false,
    pvsIdentifier = "testPvsIdentifier",
    failureToReport = "",
    medicationRequest = MedicationRequestErpModel(
        medication = MedicationErpModel(
            category = MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
            medicationProfile = FhirTaskKbvMedicationProfileErpModel(type = ErpMedicationProfileType.PZN, version = ErpMedicationProfileVersion.V_110),
            isVaccine = false,
            text = "TestMedication",
            form = null,
            lotNumber = null,
            expirationDate = null,
            identifier = Identifier(),
            normSizeCode = null,
            amount = RatioErpModel(numerator = QuantityErpModel(value = "1", unit = "oz"), denominator = null),
            ingredientMedications = emptyList(),
            manufacturingInstructions = null,
            packaging = null,
            ingredients = emptyList()
        ),
        dateOfAccident = null,
        location = null,
        emergencyFee = null,
        substitutionAllowed = false,
        dosageInstruction = null,
        note = null,
        multiplePrescriptionInfo = MultiplePrescriptionInfo(),
        accidentType = AccidentType.None,
        additionalFee = AdditionalFeeErpModel.None
    )
)

internal val MOCK_SYNCED_TASK_DATA_REDEEMABLE_01 = MOCK_SYNCED_TASK_BASE.copy(taskId = "pharmacyTask1")

internal val MOCK_SCANNED_TASK_DATA_REDEEMABLE_02 = MOCK_SCANNED_TASK_DATA_REDEEMABLE_01.copy(index = 2)

internal val MOCK_SCANNED_TASK_DATA_REDEEMED_01 = MOCK_SCANNED_TASK_DATA_REDEEMABLE_01.copy(
    index = 3,
    redeemedOn = Clock.System.now().minus(1.days)
)

internal val MOCK_SYNCED_TASK_DATA_REDEEMABLE_02 = MOCK_SYNCED_TASK_BASE.copy(taskId = "pharmacyTask2")

internal val MEDICATION = SyncedTaskData.Medication(
    category = SyncedTaskData.MedicationCategory.entries[0],
    medicationProfile = FhirTaskKbvMedicationProfileErpModel(
        type = ErpMedicationProfileType.PZN,
        version = ErpMedicationProfileVersion.V_110
    ),
    vaccine = true,
    text = "MedicationName",
    form = "AEO",
    lotNumber = "1234567890",
    expirationDate = FhirTemporal.Instant(Instant.DISTANT_PAST),
    identifier = SyncedTaskData.Identifier("1234567890"),
    normSizeCode = "N1",
    ingredientMedications = emptyList(),
    manufacturingInstructions = null,
    packaging = null,
    ingredients = emptyList(),
    amount = Ratio(
        numerator = Quantity(
            value = "1",
            unit = "oz"
        ),
        denominator = null
    )
)

internal val MOCK_SYNCED_TASK_DATA_REDEEMABLE_SELF_PAYER_03 = MOCK_SYNCED_TASK_BASE.copy(
    taskId = "pharmacyTask3",
    insuranceInformation = InsuranceErpModel(
        name = "TestInsurance",
        status = "Active",
        coverageType = InsuranceErpModelCoverageType.SEL
    )
)

// Non-redeemable (expired) synced tasks — included in list but filtered out by use-case
internal val MOCK_SYNCED_TASK_DATA_NON_REDEEMABLE_01 = MOCK_SYNCED_TASK_BASE.copy(
    taskId = "pharmacyTaskExpired1",
    expiresOn = Clock.System.now().minus(1.days),
    status = TaskStatusEnum.Completed
)

internal val MOCK_SYNCED_TASK_DATA_NON_REDEEMABLE_02 = MOCK_SYNCED_TASK_BASE.copy(
    taskId = "pharmacyTaskExpired2",
    expiresOn = Clock.System.now().minus(1.days),
    status = TaskStatusEnum.Completed
)
