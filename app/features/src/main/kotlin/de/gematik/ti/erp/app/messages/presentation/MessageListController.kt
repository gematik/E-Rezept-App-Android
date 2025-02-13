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

package de.gematik.ti.erp.app.messages.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.animated.AnimationTime
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.featuretoggle.model.NewFeature
import de.gematik.ti.erp.app.featuretoggle.usecase.IsNewFeatureSeenUseCase
import de.gematik.ti.erp.app.featuretoggle.usecase.MarkNewFeatureSeenUseCase
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.domain.model.getTimeState
import de.gematik.ti.erp.app.messages.domain.usecase.FetchInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.FetchWelcomeMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("StaticFieldLeak")
@Stable
class MessageListController(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val fetchInAppMessageUseCase: FetchInAppMessageUseCase,
    private val fetchWelcomeMessageUseCase: FetchWelcomeMessageUseCase,
    private val isNewFeatureSeenUseCase: IsNewFeatureSeenUseCase,
    private val markNewFeatureSeenUseCase: MarkNewFeatureSeenUseCase,
    private val context: Context
) : Controller() {

    private val _messagesList: MutableStateFlow<UiState<List<InAppMessage>>> = MutableStateFlow(UiState.Loading())
    private val _isMessagesListFeatureChangeSeen: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val isMessagesListFeatureChangeSeen: StateFlow<Boolean> = _isMessagesListFeatureChangeSeen.asStateFlow()
    val messagesList: StateFlow<UiState<List<InAppMessage>>> = _messagesList.asStateFlow()

    init {
        fetchMessagesList()
        isProfileTopBarRemovedChangeSeen()
    }

    // the profile change was possible on this screen before. This change is informed to the user since release 1.24.0
    private fun isProfileTopBarRemovedChangeSeen() {
        controllerScope.launch {
            runCatching {
                getProfilesUseCase.invoke().first() to isNewFeatureSeenUseCase.invoke(NewFeature.ORDERS_SCREEN_NO_PROFILE_BAR)
            }.fold(onSuccess = { (profiles, isSeen) ->
                val hasMultipleProfiles = profiles.size > 1
                _isMessagesListFeatureChangeSeen.value = if (hasMultipleProfiles) isSeen else true
            }, onFailure = {
                    // don't show the feature change if there is an error
                    _isMessagesListFeatureChangeSeen.value = true
                })
        }
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
            combine(
                fetchInAppMessageUseCase.invoke(),
                fetchWelcomeMessageUseCase.invoke()
            ) { localMessages, welcomeMessage ->
                val inAppMessages = localMessages.toMutableList()
                welcomeMessage?.let { inAppMessages.add(it) }
                inAppMessages.sortByDescending { it.timeState.timestamp }
                combineMessages(inAppMessages.first())
            }.catch { exception ->
                _messagesList.value = UiState.Error(exception)
            }.collect { messagesList ->
                if (messagesList.isEmpty()) {
                    _messagesList.value = UiState.Empty()
                } else {
                    _messagesList.value = UiState.Data(messagesList)
                }
            }
        }
    }

    private suspend fun combineMessages(
        localMessages: InAppMessage
    ): List<InAppMessage> {
        val remoteMessages = getMessagesUseCase.invoke().map {
            InAppMessage(
                id = it.orderId,
                from = it.pharmacy.name,
                timeState = getTimeState(it.sentOn),
                // timestamp = it.sentOn,
                text = getMessageText(it.latestCommunicationMessage, it.pharmacy.name),
                prescriptionsCount = it.prescriptions.size,
                tag = "",
                isUnread = it.hasUnreadMessages,
                lastMessage = it.latestCommunicationMessage,
                messageProfile = it.latestCommunicationMessage?.profile,
                version = ""
            )
        }
        return buildList {
            addAll(remoteMessages)
            add(localMessages)
        }.sortedByDescending { it.timeState.timestamp }
    }

    private fun getMessageText(latestCommunicationMessage: OrderUseCaseData.LastMessage?, pharmacy: String): String {
        val messageDetails: String
        when (latestCommunicationMessage?.profile) {
            CommunicationProfile.ErxCommunicationReply -> {
                val lastMessageDetails = latestCommunicationMessage.lastMessageDetails
                messageDetails = when {
                    lastMessageDetails.pickUpCodeDMC != null || lastMessageDetails.pickUpCodeHR != null ->
                        context.getString(R.string.order_pickup_general_message)

                    lastMessageDetails.link != null -> lastMessageDetails.message ?: context.getString(R.string.order_message_link)

                    lastMessageDetails.message != null -> lastMessageDetails.message

                    else -> context.getString(R.string.order_message_empty)
                }
            }

            CommunicationProfile.ErxCommunicationDispReq -> messageDetails = context.getString(
                R.string.orders_prescription_sent_to,
                latestCommunicationMessage.lastMessageDetails.message ?: pharmacy
            )

            else -> return context.getString(R.string.order_message_empty)
        }
        return messageDetails
    }

    fun markProfileTopBarRemovedChangeSeen() {
        controllerScope.launch {
            markNewFeatureSeenUseCase.invoke(NewFeature.ORDERS_SCREEN_NO_PROFILE_BAR)
        }
    }
}

@Composable
fun rememberMessageListController(): MessageListController {
    val getMessagesUseCase by rememberInstance<GetMessagesUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val fetchInAppMessageUseCase by rememberInstance<FetchInAppMessageUseCase>()
    val fetchWelcomeMessageUseCase by rememberInstance<FetchWelcomeMessageUseCase>()
    val isNewFeatureSeenUseCase by rememberInstance<IsNewFeatureSeenUseCase>()
    val markNewFeatureSeenUseCase by rememberInstance<MarkNewFeatureSeenUseCase>()
    val context = LocalContext.current
    return remember {
        MessageListController(
            getMessagesUseCase = getMessagesUseCase,
            getProfilesUseCase = getProfilesUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            isNewFeatureSeenUseCase = isNewFeatureSeenUseCase,
            markNewFeatureSeenUseCase = markNewFeatureSeenUseCase,
            context = context
        )
    }
}
