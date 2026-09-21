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

package de.gematik.ti.erp.app.mocks.order.model

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.DATE_3023_12_31
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.mockk.mockk
import kotlinx.datetime.Instant

const val COMMUNICATION_ID = "communicationId1"
const val ORDER_ID = "orderId1"
const val TASK_ID = "testId1"
const val PHARMACY_NAME = "recipient"
const val PHARMACY_ID = "pharmacyId"
const val TELEMATIK_ID = "123"
const val WELCOME_MESSAGE_FROM = "E-Rezept App Team"
const val WELCOME_MESSAGE_TEXT = "Herzlich Willkommen in der E-Rezept App! Mit dieser App können Sie digital " +
    "E-Rezepte empfangen und an eine Apotheke Ihrer Wahl senden."
const val IN_APP_MESSAGE_TEXT = "This is a long message to see how it looks like when the message is long and how the UI should handle it properly"
const val WELCOME_MESSAGE_TAG = "Herzlich Willkommen!"
const val WELCOME_MESSAGE_VERSION = "1.29.0"
const val WELCOME_MESSAGE_LANG = "de"
const val WELCOME_MESSAGE_ID = "0"
const val WELCOME_MESSAGE_TIMESTAMP = "2024-01-01T10:00:00Z"
const val WELCOME_MESSAGE_GET_MESSAGE_TAG = "Neuerungen in der App Version 1.29.0"
const val SECURITY_WARNING_MESSAGE_TAG = "Warnung"
const val SECURITY_WARNING_MESSAGE_VERSION = "1.29.0"
const val SECURITY_WARNING_MESSAGE_LANG = "de"
const val SECURITY_WARNING_MESSAGE_ID = "0"
const val SECURITY_WARNING_MESSAGE_TIMESTAMP = "2024-01-01T10:00:00Z"
const val SECURITY_WARNING_MESSAGE_GET_MESSAGE_TAG = "Warnung"
const val SECURITY_WARNING_MESSAGE_FROM = "E-Rezept App Team"
const val SECURITY_WARNING_MESSAGE_TEXT = "Warnung nicht supported"
private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

val COMMUNICATION_DATA = CommunicationErpModel(
    taskId = TASK_ID,
    communicationId = COMMUNICATION_ID,
    orderId = ORDER_ID,
    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
    timeStamp = DATE_2024_01_01,
    senderTelematikId = "",
    recipient = TELEMATIK_ID,
    payload = "",
    consumed = true,
    profileId = ""
)

fun communicationDataReply(
    taskId: String = TASK_ID,
    telematikId: String = TELEMATIK_ID,
    communicationId: String = COMMUNICATION_ID,
    consumed: Boolean = true,
    taskIds: List<String> = emptyList(),
    date: Instant = DATE_3023_12_31
) = CommunicationErpModel(
    taskId = taskId,
    communicationId = communicationId,
    orderId = "",
    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
    timeStamp = date,
    senderTelematikId = telematikId,
    recipient = "",
    payload = "",
    consumed = consumed,
    taskIds = taskIds,
    profileId = ""
)

val COMMUNICATION_DATA_WITH_TASK_ID = CommunicationErpModel(
    taskId = TASK_ID,
    communicationId = COMMUNICATION_ID,
    orderId = ORDER_ID,
    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
    timeStamp = DATE_2024_01_01,
    senderTelematikId = "",
    recipient = TELEMATIK_ID,
    payload = "",
    consumed = true,
    taskIds = listOf("testId1"),
    profileId = ""
)

val COMMUNICATION_DATA_WITH_TASK_ID_ERP = CommunicationErpModel(
    taskId = TASK_ID,
    communicationId = COMMUNICATION_ID,
    orderId = ORDER_ID,
    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
    timeStamp = DATE_2024_01_01,
    senderTelematikId = "",
    recipient = TELEMATIK_ID,
    payload = "",
    consumed = true,
    profileId = ""
)

val MOCK_SYNCED_TASK_DATA_01_NEW = TaskErpModel.Synced.Prescription(
    profileId = "testProfileId",
    name = null,
    taskId = TASK_ID,
    accessCode = "testAccessCode",
    isEuRedeemable = false,
    lastModified = Instant.parse("2024-01-01T10:00:00Z"),
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
    expiresOn = Instant.parse("2024-01-01T10:00:00Z"),
    acceptUntil = Instant.parse("2024-01-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-01-01T10:00:00Z"),
    status = TaskStatusEnum.Ready,
    isIncomplete = false,
    pvsIdentifier = "testPvsIdentifier",
    failureToReport = "testFailureToReport",
    medicationRequest = null,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null
)

val MOCK_MESSAGE = OrderUseCaseData.Message(
    communicationId = COMMUNICATION_ID,
    sentOn = DATE_2024_01_01,
    content = null,
    pickUpCodeDMC = null,
    pickUpCodeHR = null,
    link = null,
    consumed = true,
    prescriptions = listOf(MOCK_SYNCED_TASK_DATA_01_NEW),
    taskIds = listOf("testId1")
)

val welcomeMessage =
    InternalMessageErpModel(
        id = WELCOME_MESSAGE_ID,
        sender = WELCOME_MESSAGE_FROM,
        text = WELCOME_MESSAGE_TEXT,
        time = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        messageProfile = CommunicationErpModel.CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION,
        languageCode = WELCOME_MESSAGE_LANG
    )

val securityWarningMessage =
    InternalMessageErpModel(
        id = SECURITY_WARNING_MESSAGE_ID,
        sender = SECURITY_WARNING_MESSAGE_FROM,
        text = SECURITY_WARNING_MESSAGE_TEXT,
        time = Instant.parse(SECURITY_WARNING_MESSAGE_TIMESTAMP),
        tag = SECURITY_WARNING_MESSAGE_TAG,
        isUnread = true,
        messageProfile = CommunicationErpModel.CommunicationProfile.InApp,
        version = SECURITY_WARNING_MESSAGE_VERSION,
        languageCode = SECURITY_WARNING_MESSAGE_LANG
    )

val ORDER_DETAIL = OrderUseCaseData.OrderDetail(
    orderId = ORDER_ID,
    taskDetailedBundles = listOf(
        OrderUseCaseData.TaskDetailedBundle(
            invoiceInfo = OrderUseCaseData.InvoiceInfo(
                hasInvoice = false,
                invoiceSentOn = null
            ),
            prescription = MOCK_SYNCED_TASK_DATA_01_NEW
        )
    ),
    sentOn = DATE_2024_01_01,
    pharmacy = OrderUseCaseData.Pharmacy("123", "Apotheke Adelheid Ulmendorfer TEST-ONLY"),
    hasUnreadMessages = false
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
