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

package de.gematik.ti.erp.app.messages.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.MessageTimeState
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

class MessageListParameterProvider : PreviewParameterProvider<UiState<List<InAppMessage>>> {
    override val values: Sequence<UiState<List<InAppMessage>>>
        get() = sequenceOf(
            UiState.Loading(),
            UiState.Empty(),
            UiState.Error(Throwable("Error")),
            UiState.Data(
                listOf(
                    PreviewListView1,
                    PreviewListView2,
                    PreviewListView3,
                    PreviewListView4,
                    PreviewListView5,
                    PreviewListView6,
                    PreviewListView7
                )
            )
        )
}

const val LATEST_MESSAGE = "order message here!"
const val LATEST_MESSAGE_ADDRESS = "Berlinder Strasse 123, 12345 Berlin"
const val LATEST_MESSAGE_LONG = "This is a long message to see how it looks like when the message is long and how the UI should handle it properly."

val mockMessageDetails = OrderUseCaseData.LastMessageDetails(
    content = LATEST_MESSAGE,
    pickUpCodeDMC = "DMC1234",
    pickUpCodeHR = "HR5678",
    link = "http://pharmacy.com/pickup"
)

val mockMessageDetailsWithoutCode = OrderUseCaseData.LastMessageDetails(
    content = null,
    pickUpCodeDMC = null,
    pickUpCodeHR = null,
    link = "http://pharmacy.com/pickup"
)

val mockMessageDetailsWithoutCodeAndLink = OrderUseCaseData.LastMessageDetails(
    content = LATEST_MESSAGE,
    pickUpCodeDMC = null,
    pickUpCodeHR = null,
    link = null
)

val mockMessageDetailsAddress = OrderUseCaseData.LastMessageDetails(
    content = LATEST_MESSAGE_ADDRESS,
    pickUpCodeDMC = null,
    pickUpCodeHR = null,
    link = null
)
val mockLastMessageWithoutCodeAndLink = OrderUseCaseData.LastMessage(
    lastMessageDetails = mockMessageDetailsWithoutCodeAndLink,
    profile = CommunicationProfile.ErxCommunicationReply
)

val mockLastMessageWithoutCode = OrderUseCaseData.LastMessage(
    lastMessageDetails = mockMessageDetailsWithoutCode,
    profile = CommunicationProfile.ErxCommunicationReply
)

val mockLastMessage = OrderUseCaseData.LastMessage(
    lastMessageDetails = mockMessageDetails,
    profile = CommunicationProfile.ErxCommunicationReply
)

val mockLastMessageAdress = OrderUseCaseData.LastMessage(
    lastMessageDetails = mockMessageDetailsAddress,
    profile = CommunicationProfile.ErxCommunicationReply
)
val mockLastMessageLong = OrderUseCaseData.LastMessage(
    lastMessageDetails = mockMessageDetails.copy(content = LATEST_MESSAGE_LONG),
    profile = CommunicationProfile.ErxCommunicationReply

)

private val PreviewListView1 = InAppMessage(
    id = "1",
    from = "Apotheke",
    text = LATEST_MESSAGE,
    timeState = MessageTimeState.SentNow(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 3,
    tag = "tag",
    isUnread = false,
    lastMessage = mockLastMessage,
    messageProfile = CommunicationProfile.ErxCommunicationReply,
    version = "1.0.0"
)

private val PreviewListView2 = InAppMessage(
    id = "1",
    from = "Apotheke",
    text = LATEST_MESSAGE,
    timeState = MessageTimeState.ShowTime(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 1,
    tag = "tag",
    isUnread = true,
    lastMessage = mockLastMessageAdress,
    messageProfile = CommunicationProfile.ErxCommunicationDispReq,
    version = "1.0.0"
)

private val PreviewListView3 = InAppMessage(
    id = "1",
    from = "Team",
    text = LATEST_MESSAGE_LONG,
    timeState = MessageTimeState.ShowDate(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 1,
    tag = "Team",
    isUnread = true,
    lastMessage = null,
    messageProfile = CommunicationProfile.InApp,
    version = "1.0.0"
)

private val PreviewListView4 = InAppMessage(
    id = "1",
    from = "Apotheke",
    text = LATEST_MESSAGE,
    timeState = MessageTimeState.ShowDate(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 1,
    tag = "tag",
    isUnread = true,
    lastMessage = mockLastMessageWithoutCode,
    messageProfile = CommunicationProfile.ErxCommunicationReply,
    version = "1.0.0"
)

private val PreviewListView5 = InAppMessage(
    id = "1",
    from = "Apotheke",
    text = LATEST_MESSAGE,
    timeState = MessageTimeState.ShowDate(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 1,
    tag = "tag",
    isUnread = true,
    lastMessage = mockLastMessageWithoutCodeAndLink,
    messageProfile = CommunicationProfile.ErxCommunicationReply,
    version = "1.0.0"
)

private val PreviewListView6 = InAppMessage(
    id = "1",
    from = "Apotheke",
    text = LATEST_MESSAGE,
    timeState = MessageTimeState.ShowDate(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 1,
    tag = "tag",
    isUnread = true,
    lastMessage = null,
    messageProfile = CommunicationProfile.ErxCommunicationReply,
    version = "1.0.0"
)

private val PreviewListView7 = InAppMessage(
    id = "1",
    from = "Apotheke",
    text = LATEST_MESSAGE,
    timeState = MessageTimeState.ShowDate(Instant.parse("2023-07-08T15:20:00Z")),
    prescriptionsCount = 1,
    tag = "tag",
    isUnread = true,
    lastMessage = null,
    messageProfile = CommunicationProfile.ErxCommunicationDispReq,
    version = "1.0.0"
)
