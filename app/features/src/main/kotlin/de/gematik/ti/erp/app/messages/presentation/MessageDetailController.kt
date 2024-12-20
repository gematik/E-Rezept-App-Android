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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.domain.usecase.FetchInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.FetchWelcomeMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessageUsingOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetRepliedMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessageAsReadUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationByCommunicationIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationByOrderIdAndCommunicationIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateInvoicesByOrderIdAndTaskIdUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class MessageDetailController(
    private val orderId: String,
    private val isLocalMessage: Boolean = false,
    private val getRepliedMessagesUseCase: GetRepliedMessagesUseCase,
    private val getMessageUsingOrderIdUseCase: GetMessageUsingOrderIdUseCase,
    private val fetchInAppMessageUseCase: FetchInAppMessageUseCase,
    private val fetchWelcomeMessageUseCase: FetchWelcomeMessageUseCase,
    private val setInternalMessageIsReadUseCase: SetInternalMessageAsReadUseCase,
    private val updateCommunicationByCommunicationIdUseCase: UpdateCommunicationByCommunicationIdUseCase,
    private val updateCommunicationByOrderIdAndCommunicationIdUseCase: UpdateCommunicationByOrderIdAndCommunicationIdUseCase,
    private val updateInvoicesByOrderIdAndTaskIdUseCase: UpdateInvoicesByOrderIdAndTaskIdUseCase,
    private val getPharmacyByTelematikIdUseCase: GetPharmacyByTelematikIdUseCase,
    private val getProfileByOrderIdUseCase: GetProfileByOrderIdUseCase
) : Controller() {

    val _localMessages = MutableStateFlow<List<InAppMessage?>>(listOf())
    private val _messages = MutableStateFlow<UiState<List<OrderUseCaseData.Message>>>(UiState.Loading())
    private val _order = MutableStateFlow<UiState<OrderUseCaseData.OrderDetail>>(UiState.Loading())
    private val _pharmacy = MutableStateFlow<UiState<PharmacyUseCaseData.Pharmacy>>(UiState.Loading())
    private val _profile = MutableStateFlow<ProfilesUseCaseData.Profile?>(null)

    val localMessages: StateFlow<List<InAppMessage?>> = _localMessages
    val messages: StateFlow<UiState<List<OrderUseCaseData.Message>>> = _messages
    val order: StateFlow<UiState<OrderUseCaseData.OrderDetail>> = _order
    val pharmacy: StateFlow<UiState<PharmacyUseCaseData.Pharmacy>> = _pharmacy
    val profile: StateFlow<ProfilesUseCaseData.Profile?> = _profile

    fun init() {
        if (isLocalMessage) {
            fetchLocalMessages()
        } else {
            loadOrdersAndInvoiceMessage()
            loadReplyMessages()
            loadProfile()
        }
    }

    private fun fetchLocalMessages() {
        controllerScope.launch {
            setInternalMessageIsReadUseCase.invoke()
            combine(
                fetchInAppMessageUseCase.invoke(),
                fetchWelcomeMessageUseCase.invoke()
            ) { localMessages, welcomeMessage ->
                buildList {
                    addAll(localMessages)
                    welcomeMessage?.let { add(it) }
                }
            }.catch {
                _localMessages.value = listOf()
            }.collect { messagesList ->
                _localMessages.value = messagesList.sortedByDescending { it.timestamp }
            }
        }
    }

    private fun loadOrdersAndInvoiceMessage() {
        controllerScope.launch {
            val result = runCatching {
                getMessageUsingOrderIdUseCase(orderId)
            }
            result.fold(onSuccess = { orderList ->
                val order = orderList.firstOrNull() ?: run {
                    _order.value = UiState.Empty()
                    return@fold
                }

                _order.value = UiState.Data(order)
                getPharmacy(order.pharmacy.id)
            }, onFailure = {
                    _order.value = UiState.Error(it)
                    _pharmacy.value = UiState.Error(it)
                })
        }
    }

    private fun loadReplyMessages() {
        controllerScope.launch {
            _order.collect { state ->
                if (state.isDataState) {
                    state.data?.pharmacy?.id?.let { telematikId ->
                        val result = runCatching {
                            getRepliedMessagesUseCase(orderId, telematikId)
                        }

                        result.fold(onSuccess = { messages ->
                            val messageList = messages.firstOrNull() ?: emptyList()
                            if (messageList.isEmpty()) {
                                _messages.value = UiState.Empty()
                            } else {
                                _messages.value = UiState.Data(messageList)
                            }
                        }, onFailure = {
                                _messages.value = UiState.Error(it)
                            })
                    }
                }
            }
        }
    }

    fun consumeAllMessages(onMessagesConsumed: () -> Unit) {
        controllerScope.launch {
            // Marks the replied messages as read
            _messages.value.data?.forEach { message ->
                updateCommunicationByCommunicationIdUseCase(message.communicationId)
            }

            // Marks the dispense request and invoice messages as read
            _order.value.data?.let { orderDetail ->
                updateCommunicationByOrderIdAndCommunicationIdUseCase(orderDetail.orderId)
                updateInvoicesByOrderIdAndTaskIdUseCase(orderDetail.orderId)
            }

            onMessagesConsumed()
        }
    }

    private fun getPharmacy(telematikId: String) {
        controllerScope.launch {
            getPharmacyByTelematikIdUseCase(telematikId).fold(onSuccess = { pharmacy ->
                if (pharmacy != null) {
                    _pharmacy.value = UiState.Data(pharmacy)
                } else {
                    _pharmacy.value = UiState.Empty()
                }
            }, onFailure = {
                    _pharmacy.value = UiState.Error(it)
                })
        }
    }

    private fun loadProfile() {
        controllerScope.launch {
            getProfileByOrderIdUseCase(orderId).collect { profile ->
                _profile.value = profile
            }
        }
    }
}

@Composable
fun rememberMessageDetailController(
    orderId: String,
    isLocalMessage: Boolean
): MessageDetailController {
    val getRepliedMessagesUseCase: GetRepliedMessagesUseCase by rememberInstance()
    val getMessageUsingOrderIdUseCase: GetMessageUsingOrderIdUseCase by rememberInstance()
    val fetchInAppMessageUseCase: FetchInAppMessageUseCase by rememberInstance()
    val setInternalMessageAsReadUseCase: SetInternalMessageAsReadUseCase by rememberInstance()
    val fetchWelcomeMessageUseCase: FetchWelcomeMessageUseCase by rememberInstance()
    val updateCommunicationByCommunicationIdUseCase: UpdateCommunicationByCommunicationIdUseCase by rememberInstance()
    val updateCommunicationByOrderIdAndCommunicationIdUseCase: UpdateCommunicationByOrderIdAndCommunicationIdUseCase by rememberInstance()
    val updateInvoicesByOrderIdAndTaskIdUseCase: UpdateInvoicesByOrderIdAndTaskIdUseCase by rememberInstance()
    val getPharmacyByTelematikIdUseCase by rememberInstance<GetPharmacyByTelematikIdUseCase>()
    val getProfileByOrderIdUseCase by rememberInstance<GetProfileByOrderIdUseCase>()

    return remember(orderId) {
        MessageDetailController(
            orderId = orderId,
            isLocalMessage = isLocalMessage,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageAsReadUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdAndCommunicationIdUseCase = updateCommunicationByOrderIdAndCommunicationIdUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase
        )
    }
}
