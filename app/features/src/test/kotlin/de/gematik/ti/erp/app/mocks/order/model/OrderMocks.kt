/*
 * Copyright 2024, gematik GmbH
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

import de.gematik.ti.erp.app.db.entities.v1.changelogs.InAppMessageEntity
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
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
const val WELCOME_MESSAGE_VERSION = "1.27.0"
const val WELCOME_MESSAGE_ID = "1"
const val WELCOME_MESSAGE_TIMESTAMP = "2024-01-01T10:00:00Z"
const val WELCOME_MESSAGE_GET_MESSAGE_TAG = "Neuerungen in der App Version 1.27.0"

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
    consumed = true
)

val MOCK_MESSAGE = OrderUseCaseData.Message(
    communicationId = COMMUNICATION_ID,
    sentOn = DATE_2024_01_01,
    message = null,
    pickUpCodeDMC = null,
    pickUpCodeHR = null,
    link = null,
    consumed = true
)

val ORDER = OrderUseCaseData.Order(
    orderId = ORDER_ID,
    prescriptions = listOf(null), // getting a list of null is possibly not the best approach
    pharmacy = OrderUseCaseData.Pharmacy(PHARMACY_NAME, ""),
    sentOn = DATE_2024_01_01,
    hasUnreadMessages = true,
    latestCommunicationMessage = null
)

val inAppMessages = listOf(
    InAppMessage(
        id = "orderId1",
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = ""
    ),
    InAppMessage(
        id = "orderId1",
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
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
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
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
        timestamp = Instant.parse("2024-01-01T10:00:00Z"),
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
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
        prescriptionsCount = 0,
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION
    )

)

val inAppMessagesMore = listOf(
    InAppMessage(
        id = "orderId1",
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = ""
    ),
    InAppMessage(
        id = "orderId1",
        from = "",
        text = IN_APP_MESSAGE_TEXT,
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
        prescriptionsCount = 1,
        tag = "",
        isUnread = true,
        lastMessage = null,
        messageProfile = null,
        version = ""
    ),
    InAppMessage(
        id = "orderId1",
        from = WELCOME_MESSAGE_FROM,
        text = "This is a long message to see how it looks like when the message is long and how the UI should handle it properly",
        timestamp = Instant.parse("2024-01-01T10:00:00Z"),
        prescriptionsCount = 0,
        tag = WELCOME_MESSAGE_GET_MESSAGE_TAG,
        isUnread = false,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = ""
    )
)

val welcomeMessage =
    InAppMessage(
        id = WELCOME_MESSAGE_ID,
        from = WELCOME_MESSAGE_FROM,
        text = WELCOME_MESSAGE_TEXT,
        timestamp = Instant.parse(WELCOME_MESSAGE_TIMESTAMP),
        prescriptionsCount = 0,
        tag = WELCOME_MESSAGE_TAG,
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = WELCOME_MESSAGE_VERSION
    )

var internalEntity = listOf(
    InAppMessageEntity().apply {
        id = "01"
        this.version = "1.27.1"
    }
)

val ORDER_DETAIL = OrderUseCaseData.OrderDetail(
    orderId = ORDER_ID,
    taskDetailedBundles = listOf(
        OrderUseCaseData.TaskDetailedBundle(
            invoiceInfo = OrderUseCaseData.InvoiceInfo(
                hasInvoice = false,
                invoiceSentOn = null
            ),
            prescription = null
        )
    ),
    sentOn = DATE_2024_01_01,
    pharmacy = OrderUseCaseData.Pharmacy(PHARMACY_NAME, ""),
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
