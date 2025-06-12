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

package de.gematik.ti.erp.app.messages.mocks

import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.datetime.Instant

object MessageMocks {

    internal const val MOCK_ORDER_ID = "testOrderId"

    internal const val MOCK_TASK_ID_01 = "123-001"

    internal const val MOCK_TASK_ID_02 = "456-002"

    private const val MOCK_COMMUNICATION_ID_01 = "CID-123-001"

    private const val MOCK_COMMUNICATION_ID_02 = "CID-456-002"

    internal const val MOCK_TRANSACTION_ID = "transactionMockId"

    internal val MOCK_PHARMACY_O1 = CachedPharmacy(name = "Pharmacy1", telematikId = "123")

    internal val MOCK_PHARMACY_O2 = CachedPharmacy(name = "Pharmacy2", telematikId = "456")

    private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

    internal const val MOCK_PROFILE_IDENTIFIER = "testProfileIdentifier"

    const val MESSAGE_TIMESTAMP = "2024-01-01T10:00:00Z"

    private val MOCK_PAYLOAD = """
            {
            "version":1 , 
            "supplyOptionsType":"onPremise" , 
            "info_text":"mock message." , 
            "pickUpCodeHR":"T01__R01" , 
            "pickUpCodeDMC":"Test_01___Rezept_01___abcdefg12345" , 
            "url":"https://www.tree.fm/forest/33"
            }
    """.trimIndent()

    private val MOCK_PAYLOAD_02 = """
            {
            "version":1 , 
            "supplyOptionsType":"onPremise" , 
            "info_text":"mock message_02." , 
            "pickUpCodeHR":"T01__R02" , 
            "pickUpCodeDMC":"Test_01___Rezept_02___abcdefg12345" , 
            "url":"https://www.tree.fm/forest/35"
            }
    """.trimIndent()

    private val MOCK_PRACTITIONER = SyncedTaskData.Practitioner(
        name = MOCK_PRACTITIONER_NAME,
        qualification = "",
        practitionerIdentifier = " "
    )

    private val MOCK_CHIP_INFO = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = null
    )

    val MOCK_PRESCRIPTION_01 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_01,
        name = null,
        redeemedOn = null,
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.parse(MESSAGE_TIMESTAMP)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        isNew = false,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.parse(MESSAGE_TIMESTAMP)
    )

    val MOCK_PRESCRIPTION_02 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_02,
        name = null,
        redeemedOn = null,
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.parse(MESSAGE_TIMESTAMP)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        isNew = false,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.parse(MESSAGE_TIMESTAMP)
    )

    private val MOCK_TASK_DETAIL_BUNDLE_01 = OrderUseCaseData.TaskDetailedBundle(
        prescription = MOCK_PRESCRIPTION_01,
        invoiceInfo = OrderUseCaseData.InvoiceInfo(
            hasInvoice = true,
            invoiceSentOn = Instant.parse(MESSAGE_TIMESTAMP)
        )
    )

    private val MOCK_TASK_DETAIL_BUNDLE_02 = OrderUseCaseData.TaskDetailedBundle(
        prescription = MOCK_PRESCRIPTION_02,
        invoiceInfo = OrderUseCaseData.InvoiceInfo(
            hasInvoice = true,
            invoiceSentOn = Instant.parse(MESSAGE_TIMESTAMP)
        )
    )

    internal val MOCK_ORDER_DETAIL = OrderUseCaseData.OrderDetail(
        orderId = MOCK_ORDER_ID,
        taskDetailedBundles = listOf(
            MOCK_TASK_DETAIL_BUNDLE_01,
            MOCK_TASK_DETAIL_BUNDLE_02
        ),
        sentOn = Instant.parse(MESSAGE_TIMESTAMP),
        pharmacy = OrderUseCaseData.Pharmacy(MOCK_PHARMACY_O1.name, ""),
        hasUnreadMessages = false
    )

    internal val MOCK_ORDER_01 = OrderUseCaseData.Order(
        orderId = MOCK_ORDER_ID,
        prescriptions = listOf(null),
        sentOn = Instant.parse(MESSAGE_TIMESTAMP),
        pharmacy = OrderUseCaseData.Pharmacy(MOCK_PHARMACY_O1.name, ""),
        hasUnreadMessages = true,
        latestCommunicationMessage = null
    )

    internal val MOCK_DISP_REQ_COMMUNICATION_01 = Communication(
        taskId = MOCK_TASK_ID_01,
        communicationId = MOCK_COMMUNICATION_ID_01,
        orderId = MOCK_ORDER_ID,
        profile = CommunicationProfile.ErxCommunicationDispReq,
        sentOn = Instant.parse(MESSAGE_TIMESTAMP),
        sender = "sender1",
        recipient = MOCK_PHARMACY_O1.name,
        payload = "payload1",
        consumed = true
    )
    internal val MOCK_DISP_REPLY_COMMUNICATION_01 = MOCK_DISP_REQ_COMMUNICATION_01.copy(
        profile = CommunicationProfile.ErxCommunicationReply,
        consumed = false,
        payload = MOCK_PAYLOAD
    )
    internal val MOCK_DISP_REQ_COMMUNICATION_02 = Communication(
        taskId = MOCK_TASK_ID_02,
        communicationId = MOCK_COMMUNICATION_ID_02,
        orderId = MOCK_ORDER_ID,
        profile = CommunicationProfile.ErxCommunicationDispReq,
        sentOn = Instant.parse(MESSAGE_TIMESTAMP),
        sender = "sender2",
        recipient = MOCK_PHARMACY_O2.name,
        payload = "payload2",
        consumed = true
    )
    internal val MOCK_DISP_REPLY_COMMUNICATION_02 = MOCK_DISP_REQ_COMMUNICATION_02.copy(
        profile = CommunicationProfile.ErxCommunicationReply,
        consumed = false,
        payload = MOCK_PAYLOAD_02
    )

    private val MOCK_ORGANIZATION = SyncedTaskData.Organization(
        name = "TestOrganization",
        address = SyncedTaskData.Address(
            line1 = "123 Main Street",
            line2 = "Apt 4",
            postalCode = "12345",
            city = "City"
        ),
        uniqueIdentifier = "org123",
        phone = "123-456-7890",
        mail = "info@testorg.com"
    )

    private val MOCK_PATIENT = SyncedTaskData.Patient(
        name = "Jane",
        address = SyncedTaskData.Address(
            line1 = "",
            line2 = "",
            postalCode = "",
            city = ""
        ),
        birthdate = null,
        insuranceIdentifier = "ins123"
    )

    private val MOCK_MEDICATION_REQ = SyncedTaskData.MedicationRequest(
        null, null, null, SyncedTaskData.AccidentType.None,
        null, null, false, null,
        SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
    )

    private fun mockChargeItem(
        itemDescription: String,
        itemFactor: Double,
        itemPrice: Double
    ): InvoiceData.ChargeableItem {
        return InvoiceData.ChargeableItem(
            description = InvoiceData.ChargeableItem.Description.PZN("PZN123"),
            text = itemDescription,
            factor = itemFactor,
            price = InvoiceData.PriceComponent(value = itemPrice, tax = 0.0)
        )
    }

    private val MOCK_INVOICE = InvoiceData.Invoice(
        totalAdditionalFee = 10.0,
        totalBruttoAmount = 120.0,
        currency = "EUR",
        chargeableItems = listOf(
            mockChargeItem("Description1", 2.0, 30.0),
            mockChargeItem("Description2", 1.5, 50.0)
        ),
        additionalDispenseItems = emptyList(),
        additionalInformation = listOf("AdditionalInfo1", "AdditionalInfo2")
    )

    internal val MOCK_INVOICE_01 = InvoiceData.PKVInvoiceRecord(
        profileId = "testProfileId",
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        timestamp = Instant.parse(MESSAGE_TIMESTAMP),
        pharmacyOrganization = MOCK_ORGANIZATION,
        practitionerOrganization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        medicationRequest = MOCK_MEDICATION_REQ,
        whenHandedOver = null,
        invoice = MOCK_INVOICE,
        dmcPayload = "testDmcPayload",
        consumed = false
    )

    internal val MOCK_INVOICE_02 = InvoiceData.PKVInvoiceRecord(
        profileId = "testProfileId",
        taskId = MOCK_TASK_ID_02,
        accessCode = "testAccessCode",
        timestamp = Instant.parse(MESSAGE_TIMESTAMP),
        pharmacyOrganization = MOCK_ORGANIZATION,
        practitionerOrganization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        medicationRequest = MOCK_MEDICATION_REQ,
        whenHandedOver = null,
        invoice = MOCK_INVOICE,
        dmcPayload = "testDmcPayload",
        consumed = false
    )

    internal val MOCK_SYNCED_TASK_DATA_01 = SyncedTaskData.SyncedTask(
        profileId = "testProfileId",
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        lastModified = Instant.parse(MESSAGE_TIMESTAMP),
        organization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = "TestInsurance",
            status = "Active",
            coverageType = SyncedTaskData.CoverageType.GKV
        ),
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        status = SyncedTaskData.TaskStatus.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = MOCK_MEDICATION_REQ,
        lastMedicationDispense = null,
        medicationDispenses = emptyList(),
        communications = emptyList()
    )

    internal val MOCK_SYNCED_TASK_DATA_02 = SyncedTaskData.SyncedTask(
        profileId = "testProfileId",
        taskId = MOCK_TASK_ID_02,
        accessCode = "testAccessCode",
        lastModified = Instant.parse(MESSAGE_TIMESTAMP),
        organization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = "TestInsurance",
            status = "Active",
            coverageType = SyncedTaskData.CoverageType.GKV
        ),
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        status = SyncedTaskData.TaskStatus.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = MOCK_MEDICATION_REQ,
        lastMedicationDispense = null,
        medicationDispenses = emptyList(),
        communications = emptyList()
    )

    internal val MOCK_MESSAGE_01 = OrderUseCaseData.Message(
        communicationId = MOCK_COMMUNICATION_ID_01,
        sentOn = Instant.parse(MESSAGE_TIMESTAMP),
        content = "mock message.",
        pickUpCodeDMC = "Test_01___Rezept_01___abcdefg12345",
        pickUpCodeHR = "T01__R01",
        link = "https://www.tree.fm/forest/33",
        consumed = false,
        prescriptions = listOf(MOCK_PRESCRIPTION_01)
    )

    internal val MOCK_MESSAGE_02 = OrderUseCaseData.Message(
        communicationId = MOCK_COMMUNICATION_ID_02,
        sentOn = Instant.parse(MESSAGE_TIMESTAMP),
        content = "mock message_02.",
        pickUpCodeDMC = "Test_01___Rezept_02___abcdefg12345",
        pickUpCodeHR = "T01__R02",
        link = "https://www.tree.fm/forest/35",
        consumed = false,
        prescriptions = listOf(MOCK_PRESCRIPTION_02)
    )

    internal val MOCK_PROFILE = ProfilesData.Profile(
        id = "1",
        name = "Mustermann",
        color = ProfilesData.ProfileColorNames.PINK,
        avatar = ProfilesData.Avatar.FemaleDoctor,
        insuranceIdentifier = "12345567890",
        insuranceType = ProfilesData.InsuranceType.GKV,
        insurantName = "Mustermann",
        insuranceName = "GesundheitsVersichert AG",
        singleSignOnTokenScope = null,
        active = false,
        isConsentDrawerShown = false,
        lastAuthenticated = null
    )
}
