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

package de.gematik.ti.erp.app.messages.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.analytics.model.TrackedEvent
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.animated.AnimationTime
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.domain.usecase.GetInternalMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetLatestEuOrderMessageAsInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.model.LastMessageDetails
import de.gematik.ti.erp.app.timestate.getTimeState
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.kodein.di.compose.rememberInstance

@Suppress("StaticFieldLeak")
@Stable
class MessageListController(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getInternalMessagesUseCase: GetInternalMessagesUseCase,
    private val getLatestEuOrderMessageAsInAppMessageUseCase: GetLatestEuOrderMessageAsInAppMessageUseCase,
    private val tracker: Tracker,
    private val context: Context
) : Controller() {
    private val selectedAppLanguage = context.resources.configuration.locales[0].language
    private val _messagesList: MutableStateFlow<UiState<List<InAppMessage>>> = MutableStateFlow(
        UiState.Loading()
    )
    val messagesList: StateFlow<UiState<List<InAppMessage>>> = _messagesList.asStateFlow()

    init {
        fetchMessagesList()
    }

    fun retryFetchMessagesList() {
        controllerScope.launch {
            _messagesList.value = UiState.Loading()
            delay(AnimationTime.SHORT_DELAY)
            fetchMessagesList()
        }
    }

    private fun fetchMessagesList() {
        controllerScope.launch {
            _messagesList.value = UiState.Loading()

            try {
                combine(
                    flow { emit(getMessagesUseCase.invoke().map { it.toInAppMessage() }) },
                    getInternalMessagesUseCase.invoke(selectedAppLanguage),
                    getLatestEuOrderMessageAsInAppMessageUseCase.invoke()
                ) { externalMessages, internalMessages, euMessage ->

                    val internalMapped = listOf(
                        internalMessages.firstOrNull()?.copy(
                            isUnread = internalMessages.any { it.isUnread }
                        )
                    )

                    buildList {
                        addAll(externalMessages)
                        addAll(internalMapped.filterNotNull())
                        addAll(euMessage)
                    }.sortedByDescending { it.timeState.timestamp }
                }.collect { combinedList ->

                    _messagesList.value =
                        if (combinedList.isEmpty()) UiState.Empty()
                        else UiState.Data(combinedList)
                }
            } catch (e: Exception) {
                Napier.e { "combining messages failed: ${e.stackTraceToString()}" }
                _messagesList.value = UiState.Error(e)
            }
        }
    }

    private fun OrderUseCaseData.Order.toInAppMessage(): InAppMessage =
        InAppMessage(
            id = orderId,
            from = pharmacy.name,
            timeState = getTimeState(sentOn),
            text = getMessageText(
                latestCommunicationMessage,
                pharmacy.name,
                invoiceInfo,
                sentOn
            ),
            prescriptionsCount = prescriptions.size,
            tag = "",
            isUnread = hasUnreadMessages,
            lastMessage = latestCommunicationMessage,
            messageProfile = latestCommunicationMessage?.profile,
            version = ""
        )

    private fun getMessageText(
        latestCommunicationMessage: LastMessage?,
        pharmacy: String,
        invoiceInfo: OrderUseCaseData.InvoiceInfo,
        sentOn: Instant
    ): String = when {
        invoiceInfo.hasInvoice && invoiceInfo.invoiceSentOn == sentOn ->
            context.getString(R.string.cost_receipt_is_ready)

        latestCommunicationMessage?.profile == CommunicationProfile.ErxCommunicationReply ->
            getReplyMessageText(latestCommunicationMessage.lastMessageDetails)

        latestCommunicationMessage?.profile == CommunicationProfile.ErxCommunicationDispReq ->
            context.getString(
                R.string.orders_prescription_sent_to,
                latestCommunicationMessage.lastMessageDetails.content ?: pharmacy
            )

        else -> context.getString(R.string.order_message_empty)
    }

    private fun getReplyMessageText(lastMessageDetails: LastMessageDetails): String = when {
        (lastMessageDetails.pickUpCodeDMC != null || lastMessageDetails.pickUpCodeHR != null) ->
            lastMessageDetails.content?.takeIf { it.isNotEmpty() }
                ?: context.getString(R.string.order_pickup_general_message)

        lastMessageDetails.link != null ->
            lastMessageDetails.content ?: context.getString(R.string.order_message_link)

        else ->
            lastMessageDetails.content ?: context.getString(R.string.order_message_empty)
    }

    fun trackMessageCount() {
        controllerScope.launch {
            messagesList.first { it.isDataState }.data?.let { messages ->
                val messageCount = messages.size
                tracker.trackMetric(TrackedEvent.MessageCount(messageCount))
            }
        }
    }
}

@Composable
fun rememberMessageListController(): MessageListController {
    val getMessagesUseCase by rememberInstance<GetMessagesUseCase>()
    val getInternalMessagesUseCase by rememberInstance<GetInternalMessagesUseCase>()
    val getLatestEuOrderMessageAsInAppMessageUseCase by rememberInstance<GetLatestEuOrderMessageAsInAppMessageUseCase>()
    val tracker by rememberInstance<Tracker>()
    val context = LocalContext.current
    return remember {
        MessageListController(
            getMessagesUseCase = getMessagesUseCase,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            getLatestEuOrderMessageAsInAppMessageUseCase = getLatestEuOrderMessageAsInAppMessageUseCase,
            tracker = tracker,
            context = context
        )
    }
}
