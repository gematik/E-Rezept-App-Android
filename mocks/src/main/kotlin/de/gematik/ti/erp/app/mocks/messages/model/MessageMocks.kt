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

package de.gematik.ti.erp.app.mocks.messages.model

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationPayloadParser
import de.gematik.ti.erp.app.invoice.model.ChargeableItemDescriptionErpModel
import de.gematik.ti.erp.app.invoice.model.ChargeableItemErpModel
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PriceComponentErpModel
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.mockk.mockk
import de.gematik.ti.erp.app.task.model.AccidentType
import de.gematik.ti.erp.app.task.model.AdditionalFeeErpModel
import de.gematik.ti.erp.app.task.model.AddressErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant

object MessageMocks {

    const val MOCK_ORDER_ID = "testOrderId"

    const val MOCK_TASK_ID_01 = "123-001"

    const val MOCK_TASK_ID_02 = "456-002"

    const val MOCK_COMMUNICATION_ID_01 = "CID-123-001"

    const val MOCK_COMMUNICATION_ID_02 = "CID-456-002"

    const val MOCK_TRANSACTION_ID = "transactionMockId"

    data class MockPharmacy(val name: String, val telematikId: String)

    val MOCK_PHARMACY_O1 = MockPharmacy(name = "Pharmacy1", telematikId = "123")

    val MOCK_PHARMACY_O2 = MockPharmacy(name = "Pharmacy2", telematikId = "456")

    const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

    const val MOCK_PROFILE_IDENTIFIER = "testProfileIdentifier"

    const val MESSAGE_TIMESTAMP = "2024-01-01T10:00:00Z"

    val MOCK_PAYLOAD = """
            {
            "version":1 , 
            "supplyOptionsType":"onPremise" , 
            "info_text":"mock message." , 
            "pickUpCodeHR":"T01__R01" , 
            "pickUpCodeDMC":"Test_01___Rezept_01___abcdefg12345" , 
            "url":"https://www.tree.fm/forest/33"
            }
    """.trimIndent()

    val MOCK_PAYLOAD_02 = """
            {
            "version":1 , 
            "supplyOptionsType":"onPremise" , 
            "info_text":"mock message_02." , 
            "pickUpCodeHR":"T01__R02" , 
            "pickUpCodeDMC":"Test_01___Rezept_02___abcdefg12345" , 
            "url":"https://www.tree.fm/forest/35"
            }
    """.trimIndent()

    val MOCK_PRACTITIONER = PractitionerErpModel(
        name = MOCK_PRACTITIONER_NAME,
        qualification = "",
        practitionerIdentifier = " "
    )

    val MOCK_PRESCRIPTION_01 = TaskErpModel.Synced.Prescription(
        profileId = "testProfileId",
        name = null,
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        isEuRedeemable = false,
        lastModified = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        isEuRedeemableByPatientAuthorization = false,
        organization = null,
        practitioner = null,
        patient = null,
        insuranceInformation = null,
        expiresOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "",
        failureToReport = "",
        medicationRequest = null
    )

    val MOCK_PRESCRIPTION_02 = TaskErpModel.Synced.Prescription(
        profileId = "testProfileId",
        name = null,
        taskId = MOCK_TASK_ID_02,
        accessCode = "testAccessCode",
        isEuRedeemable = false,
        lastModified = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        isEuRedeemableByPatientAuthorization = false,
        organization = null,
        practitioner = null,
        patient = null,
        insuranceInformation = null,
        expiresOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "",
        failureToReport = "",
        medicationRequest = null
    )

    val MOCK_TASK_DETAIL_BUNDLE_01 = OrderUseCaseData.TaskDetailedBundle(
        prescription = MOCK_PRESCRIPTION_01,
        invoiceInfo = OrderUseCaseData.InvoiceInfo(
            hasInvoice = true,
            invoiceSentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP)
        )
    )

    val MOCK_TASK_DETAIL_BUNDLE_02 = OrderUseCaseData.TaskDetailedBundle(
        prescription = MOCK_PRESCRIPTION_02,
        invoiceInfo = OrderUseCaseData.InvoiceInfo(
            hasInvoice = true,
            invoiceSentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP)
        )
    )

    val MOCK_SYNCED_TASK_DATA_01 = TaskErpModel.Synced.Prescription(
        profileId = "testProfileId",
        name = null,
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        isEuRedeemable = false,
        lastModified = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        isEuRedeemableByPatientAuthorization = false,
        organization = OrganizationErpModel(name = "TestOrganization"),
        practitioner = PractitionerErpModel(
            name = MOCK_PRACTITIONER_NAME,
            qualification = "",
            practitionerIdentifier = " ",
            dentistIdentifier = null,
            telematikId = null
        ),
        patient = PatientErpModel(name = "Jane", address = null, dateOfBirth = null, insuranceIdentifier = "ins123"),
        insuranceInformation = InsuranceErpModel(name = "TestInsurance", status = "Active", coverageType = InsuranceErpModelCoverageType.GKV),
        expiresOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = null,
        medicationDispenses = emptyList(),
        lastMedicationDispense = null
    )

    val MOCK_SYNCED_TASK_DATA_02 = TaskErpModel.Synced.Prescription(
        profileId = "testProfileId",
        name = null,
        taskId = MOCK_TASK_ID_02,
        accessCode = "testAccessCode",
        isEuRedeemable = false,
        lastModified = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        isEuRedeemableByPatientAuthorization = false,
        organization = OrganizationErpModel(name = "TestOrganization"),
        practitioner = PractitionerErpModel(
            name = MOCK_PRACTITIONER_NAME,
            qualification = "",
            practitionerIdentifier = " ",
            dentistIdentifier = null,
            telematikId = null
        ),
        patient = PatientErpModel(name = "Jane", address = null, dateOfBirth = null, insuranceIdentifier = "ins123"),
        insuranceInformation = InsuranceErpModel(name = "TestInsurance", status = "Active", coverageType = InsuranceErpModelCoverageType.GKV),
        expiresOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = null,
        medicationDispenses = emptyList(),
        lastMedicationDispense = null
    )

    val MOCK_ORDER_DETAIL = OrderUseCaseData.OrderDetail(
        orderId = MOCK_ORDER_ID,
        taskDetailedBundles = listOf(
            OrderUseCaseData.TaskDetailedBundle(
                prescription = MOCK_SYNCED_TASK_DATA_01,
                invoiceInfo = OrderUseCaseData.InvoiceInfo(
                    hasInvoice = true,
                    invoiceSentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP)
                )
            ),
            OrderUseCaseData.TaskDetailedBundle(
                prescription = MOCK_SYNCED_TASK_DATA_02,
                invoiceInfo = OrderUseCaseData.InvoiceInfo(
                    hasInvoice = true,
                    invoiceSentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP)
                )
            )
        ),
        sentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        pharmacy = OrderUseCaseData.Pharmacy(id = MOCK_PHARMACY_O1.telematikId, name = MOCK_PHARMACY_O1.name),
        hasUnreadMessages = false
    )

    val MOCK_ORDER_01 = OrderUseCaseData.Order(
        orderId = MOCK_ORDER_ID,
        prescriptions = listOf(null),
        sentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        pharmacy = OrderUseCaseData.Pharmacy(MOCK_PHARMACY_O1.telematikId, MOCK_PHARMACY_O1.name),
        hasUnreadMessages = true,
        latestCommunicationMessage = null
    )

    val MOCK_DISP_REQ_COMMUNICATION_01 = CommunicationErpModel(
        taskId = MOCK_TASK_ID_01,
        communicationId = MOCK_COMMUNICATION_ID_01,
        orderId = MOCK_ORDER_ID,
        profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
        timeStamp = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        senderTelematikId = "sender1",
        recipient = MOCK_PHARMACY_O1.telematikId,
        pharmacyName = MOCK_PHARMACY_O1.name,
        payload = null,
        consumed = true,
        profileId = ""
    )
    val MOCK_DISP_REPLY_COMMUNICATION_01 = MOCK_DISP_REQ_COMMUNICATION_01.copy(
        profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
        consumed = false,
        payload = CommunicationPayloadParser.extract(MOCK_PAYLOAD, isRequest = false)
    )
    val MOCK_DISP_REQ_COMMUNICATION_02 = CommunicationErpModel(
        taskId = MOCK_TASK_ID_02,
        communicationId = MOCK_COMMUNICATION_ID_02,
        orderId = MOCK_ORDER_ID,
        profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
        timeStamp = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        senderTelematikId = "sender2",
        recipient = MOCK_PHARMACY_O2.telematikId,
        pharmacyName = MOCK_PHARMACY_O2.name,
        payload = null,
        consumed = true,
        profileId = ""
    )
    val MOCK_DISP_REPLY_COMMUNICATION_02 = MOCK_DISP_REQ_COMMUNICATION_02.copy(
        profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
        consumed = false,
        payload = CommunicationPayloadParser.extract(MOCK_PAYLOAD_02, isRequest = false)
    )

    val MOCK_DISP_REQ_COMMUNICATION_01_ERP = MOCK_DISP_REQ_COMMUNICATION_01
    val MOCK_DISP_REQ_COMMUNICATION_02_ERP = MOCK_DISP_REQ_COMMUNICATION_02
    val MOCK_DISP_REPLY_COMMUNICATION_01_ERP = MOCK_DISP_REPLY_COMMUNICATION_01
    val MOCK_DISP_REPLY_COMMUNICATION_02_ERP = MOCK_DISP_REPLY_COMMUNICATION_02

    val MOCK_ORGANIZATION = OrganizationErpModel(
        name = "TestOrganization",
        address = AddressErpModel(
            line1 = "123 Main Street",
            line2 = "Apt 4",
            postalCode = "12345",
            city = "City"
        ),
        uniqueIdentifier = "org123",
        phone = "123-456-7890",
        mail = "info@testorg.com"
    )

    val MOCK_PATIENT = PatientErpModel(
        name = "Jane",
        address = AddressErpModel(
            line1 = "",
            line2 = "",
            postalCode = "",
            city = ""
        ),
        dateOfBirth = null,
        insuranceIdentifier = "ins123"
    )

    val MOCK_MEDICATION_REQ = MedicationRequestErpModel(
        medication = null,
        authoredOn = null,
        dateOfAccident = null,
        accidentType = AccidentType.None,
        location = null,
        emergencyFee = null,
        substitutionAllowed = false,
        dosageInstruction = null,
        multiplePrescriptionInfo = MultiplePrescriptionInfo(false),
        quantity = 1,
        note = null,
        bvg = null,
        additionalFee = AdditionalFeeErpModel.None
    )

    fun mockChargeItem(
        itemDescription: String,
        itemFactor: Double,
        itemPrice: Double
    ): ChargeableItemErpModel {
        return ChargeableItemErpModel(
            description = ChargeableItemDescriptionErpModel.PZN("PZN123"),
            text = itemDescription,
            factor = itemFactor,
            price = PriceComponentErpModel(value = itemPrice, tax = 0.0)
        )
    }

    val MOCK_INVOICE = InvoiceErpModel(
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

    val MOCK_INVOICE_01 = PKVInvoiceErpModel(
        profileId = "testProfileId",
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        timestamp = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        pharmacyOrganization = MOCK_ORGANIZATION,
        practitionerOrganization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        medicationRequest = MOCK_MEDICATION_REQ,
        whenHandedOver = null,
        invoice = MOCK_INVOICE,
        consumed = false
    )

    val MOCK_INVOICE_02 = PKVInvoiceErpModel(
        profileId = "testProfileId",
        taskId = MOCK_TASK_ID_02,
        accessCode = "testAccessCode",
        timestamp = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        pharmacyOrganization = MOCK_ORGANIZATION,
        practitionerOrganization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        medicationRequest = MOCK_MEDICATION_REQ,
        whenHandedOver = null,
        invoice = MOCK_INVOICE,
        consumed = false
    )

    val MOCK_MESSAGE_01 = OrderUseCaseData.Message(
        communicationId = MOCK_COMMUNICATION_ID_01,
        sentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        content = "mock message.",
        pickUpCodeDMC = "Test_01___Rezept_01___abcdefg12345",
        pickUpCodeHR = "T01__R01",
        link = "https://www.tree.fm/forest/33",
        consumed = false,
        prescriptions = listOf(MOCK_PRESCRIPTION_01)
    )

    val MOCK_MESSAGE_02 = OrderUseCaseData.Message(
        communicationId = MOCK_COMMUNICATION_ID_02,
        sentOn = Instant.Companion.parse(MESSAGE_TIMESTAMP),
        content = "mock message_02.",
        pickUpCodeDMC = "Test_01___Rezept_02___abcdefg12345",
        pickUpCodeHR = "T01__R02",
        link = "https://www.tree.fm/forest/35",
        consumed = false,
        prescriptions = listOf(MOCK_PRESCRIPTION_02)
    )

    val MOCK_PROFILE = ProfileErpModel(
        id = PROFILE_ID,
        name = "Erna Mustermann",
        active = true,
        isNewlyCreated = false,
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.PINK,
            avatar = Avatar.Baby,
            image = null
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = "Erna Mustermann",
            insuranceIdentifier = "AOK",
            insuranceName = null,
            insuranceType = InsuranceType.GKV,
            organizationIdentifier = null
        ),
        isConsentDrawerShown = true,
        lastAuthenticated = mockk(),
        lastAuditEventSynced = null,
        lastTaskSynced = mockk(),
        userAuthentication = UserAuthenticationErpModel.NotInitialized
    )
}
