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

package de.gematik.ti.erp.app.mocks.order.model

import de.gematik.ti.erp.app.messages.model.MessageTimeState
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.DATE_3023_12_31
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.model.InternalMessage
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.TaskStateSerializationType
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.model.ProfilesData
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
private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

val CACHED_PHARMACY = CachedPharmacy(name = PHARMACY_NAME, telematikId = TELEMATIK_ID)

val COMMUNICATION_DATA = Communication(
    taskId = TASK_ID,
    communicationId = COMMUNICATION_ID,
    orderId = ORDER_ID,
    profile = CommunicationProfile.ErxCommunicationDispReq,
    sentOn = DATE_2024_01_01,
    sender = "",
    recipient = CachedPharmacy(name = PHARMACY_NAME, telematikId = TELEMATIK_ID).name,
    payload = "",
    consumed = true,
    taskIds = emptyList()
)

fun communicationDataReply(
    taskId: String = TASK_ID,
    telematikId: String = TELEMATIK_ID,
    communicationId: String = COMMUNICATION_ID,
    consumed: Boolean = true,
    taskIds: List<String> = emptyList(),
    date: Instant = DATE_3023_12_31
) = Communication(
    taskId = taskId,
    communicationId = communicationId,
    orderId = "",
    profile = CommunicationProfile.ErxCommunicationReply,
    sentOn = date,
    sender = CachedPharmacy(name = PHARMACY_NAME, telematikId = telematikId).name,
    recipient = "",
    payload = "",
    consumed = consumed,
    taskIds = taskIds
)

val COMMUNICATION_DATA_WITH_TASK_ID = Communication(
    taskId = TASK_ID,
    communicationId = COMMUNICATION_ID,
    orderId = ORDER_ID,
    profile = CommunicationProfile.ErxCommunicationDispReq,
    sentOn = DATE_2024_01_01,
    sender = "",
    recipient = TELEMATIK_ID,
    payload = "",
    consumed = true,
    taskIds = listOf("testId1")
)

val MOCK_MESSAGE = OrderUseCaseData.Message(
    communicationId = COMMUNICATION_ID,
    sentOn = DATE_2024_01_01,
    content = null,
    pickUpCodeDMC = null,
    pickUpCodeHR = null,
    link = null,
    consumed = true,
    prescriptions = listOf(
        Prescription.SyncedPrescription(
            taskId = "testId1",
            name = null,
            redeemedOn = null,
            expiresOn = Instant.parse("1970-01-02T10:17:36Z"),
            state = SyncedTaskData.SyncedTask.Expired(type = TaskStateSerializationType.Expired, expiredOn = Instant.parse("1970-01-02T10:17:36Z")),
            isIncomplete = false,
            organization = "Dr. John Doe",
            authoredOn = Instant.parse("1970-01-02T10:17:36Z"),
            acceptUntil = Instant.parse("1970-01-02T10:17:36Z"),
            isDirectAssignment = false,
            prescriptionChipInformation = Prescription.PrescriptionChipInformation(
                isSelfPayPrescription = false,
                isPartOfMultiplePrescription = false,
                numerator = null,
                denominator = null,
                start = null
            )
        )
    ),
    taskIds = listOf("testId1")
)

val inAppMessages = listOf(
    InAppMessage(
        id = ORDER_ID,
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = ""
    ),
    InAppMessage(
        id = ORDER_ID,
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = ""
    ),
    InAppMessage(
        id = WELCOME_MESSAGE_ID,
        from = WELCOME_MESSAGE_FROM,
        text = WELCOME_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 0,
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION
    )
)

val inAppMessage = listOf(
    InAppMessage(
        id = "01",
        from = "Team",
        text = "This is a long message to see how it looks like when the message is long and how the UI should handle it properly",
        timeState = MessageTimeState.ShowDate(Instant.parse("2024-01-01T10:00:00Z")),
        prescriptionsCount = 0,
        tag = "1",
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = "1.27.1"
    ),
    InAppMessage(
        id = WELCOME_MESSAGE_ID,
        from = WELCOME_MESSAGE_FROM,
        text = WELCOME_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 0,
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION
    )

)

val inAppMessagesFiltered = listOf(
    InAppMessage(
        id = ORDER_ID,
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = ""
    ),
    InAppMessage(
        id = WELCOME_MESSAGE_ID,
        from = WELCOME_MESSAGE_FROM,
        text = WELCOME_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 0,
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION
    )
)

val inAppMessagesVersion = listOf(
    InAppMessage(
        id = "orderId1",
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = "2.0.0"
    ),
    InAppMessage(
        id = "orderId1",
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timeState = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = "1.0.0"
    )
)

val welcomeMessage =
    InternalMessage(
        id = WELCOME_MESSAGE_ID,
        sender = WELCOME_MESSAGE_FROM,
        text = WELCOME_MESSAGE_TEXT,
        time = MessageTimeState.ShowDate(Instant.parse(WELCOME_MESSAGE_TIMESTAMP)),
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        messageProfile = CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION,
        languageCode = WELCOME_MESSAGE_LANG
    )

val ORDER_DETAIL = OrderUseCaseData.OrderDetail(
    orderId = ORDER_ID,
    taskDetailedBundles = listOf(
        OrderUseCaseData.TaskDetailedBundle(
            invoiceInfo = OrderUseCaseData.InvoiceInfo(
                hasInvoice = false,
                invoiceSentOn = null
            ),
            prescription = Prescription.SyncedPrescription(
                taskId = TASK_ID,
                name = null,
                redeemedOn = null,
                expiresOn = Instant.parse("1970-01-02T10:17:36Z"),
                state = SyncedTaskData.SyncedTask.Expired(type = TaskStateSerializationType.Expired, expiredOn = Instant.parse("1970-01-02T10:17:36Z")),
                isIncomplete = false,
                organization = MOCK_PRACTITIONER_NAME,
                authoredOn = Instant.parse("1970-01-02T10:17:36Z"),
                acceptUntil = Instant.parse("1970-01-02T10:17:36Z"),
                isDirectAssignment = false,
                prescriptionChipInformation = Prescription.PrescriptionChipInformation(
                    isSelfPayPrescription = false,
                    isPartOfMultiplePrescription = false,
                    numerator = null,
                    denominator = null,
                    start = null
                )
            )
        )
    ),
    sentOn = DATE_2024_01_01,
    pharmacy = OrderUseCaseData.Pharmacy("123", "Apotheke Adelheid Ulmendorfer TEST-ONLY"),
    hasUnreadMessages = false
)

val MOCK_PROFILE = ProfilesData.Profile(
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

val MOCK_PRACTITIONER = SyncedTaskData.Practitioner(
    name = MOCK_PRACTITIONER_NAME,
    qualification = "",
    practitionerIdentifier = " "
)

val MOCK_SYNCED_TASK_DATA_01_NEW = SyncedTaskData.SyncedTask(
    profileId = "testProfileId",
    taskId = "testId1",
    accessCode = "testAccessCode",
    lastModified = Instant.fromEpochSeconds(123456),
    organization = MOCK_ORGANIZATION,
    practitioner = MOCK_PRACTITIONER,
    patient = MOCK_PATIENT,
    insuranceInformation = SyncedTaskData.InsuranceInformation(
        name = "TestInsurance",
        status = "Active",
        coverageType = SyncedTaskData.CoverageType.GKV
    ),
    expiresOn = Instant.fromEpochSeconds(123456),
    acceptUntil = Instant.fromEpochSeconds(123456),
    authoredOn = Instant.fromEpochSeconds(123456),
    status = SyncedTaskData.TaskStatus.Ready,
    isIncomplete = false,
    pvsIdentifier = "testPvsIdentifier",
    failureToReport = "testFailureToReport",
    medicationRequest = MOCK_MEDICATION_REQ,
    lastMedicationDispense = null,
    medicationDispenses = emptyList(),
    communications = emptyList()
)
