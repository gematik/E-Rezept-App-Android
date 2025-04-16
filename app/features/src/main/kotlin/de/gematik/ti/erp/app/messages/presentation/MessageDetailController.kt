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

package de.gematik.ti.erp.app.messages.presentation

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.domain.usecase.GetInternalMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessageUsingOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetRepliedMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessagesAsReadUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase.Companion.CommunicationIdentifier
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateInvoicesByOrderIdAndTaskIdUseCase
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class MessageDetailController(
    private val orderId: String,
    private val isLocalMessage: Boolean = false,
    private val getRepliedMessagesUseCase: GetRepliedMessagesUseCase,
    private val getMessageUsingOrderIdUseCase: GetMessageUsingOrderIdUseCase,
    private val getInternalMessagesUseCase: GetInternalMessagesUseCase,
    private val setInternalMessageIsReadUseCase: SetInternalMessagesAsReadUseCase,
    private val updateCommunicationConsumedStatusUseCase: UpdateCommunicationConsumedStatusUseCase,
    private val updateInvoicesByOrderIdAndTaskIdUseCase: UpdateInvoicesByOrderIdAndTaskIdUseCase,
    private val getPharmacyByTelematikIdUseCase: GetPharmacyByTelematikIdUseCase,
    private val getProfileByOrderIdUseCase: GetProfileByOrderIdUseCase,
    private val context: Context
) : Controller() {
    private val selectedAppLanguage = context.resources.configuration.locales[0].language
    internal val _localMessages = MutableStateFlow<List<InAppMessage>>(listOf())
    private val _messages = MutableStateFlow<UiState<List<OrderUseCaseData.Message>>>(UiState.Loading())
    private val _order = MutableStateFlow<UiState<OrderUseCaseData.OrderDetail>>(UiState.Loading())
    private val _pharmacy = MutableStateFlow<UiState<PharmacyUseCaseData.Pharmacy>>(UiState.Loading())
    private val _profile = MutableStateFlow<ProfilesUseCaseData.Profile?>(null)

    val localMessages: StateFlow<List<InAppMessage>> = _localMessages
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
            try {
                getInternalMessagesUseCase.invoke(selectedAppLanguage).collect { messagesList ->
                    _localMessages.value = messagesList.sortedByDescending { it.timeState.timestamp }
                }
            } catch (e: Exception) {
                Napier.e { "Error Loading Internal Messages" }
                _localMessages.value = listOf()
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
                            getRepliedMessagesUseCase(orderId, telematikId).firstOrNull()
                        }
                        result.fold(onSuccess = { messages ->
                            val messageList = messages ?: emptyList()
                            if (messageList.isEmpty()) {
                                _messages.value = UiState.Empty()
                            } else {
                                val newMessageList = messageList.map {
                                    it.copy(prescriptions = getPrescriptions(it.taskIds, state.data))
                                }
                                _messages.value = UiState.Data(newMessageList)
                            }
                        }, onFailure = {
                                _messages.value = UiState.Error(it)
                            })
                    }
                }
            }
        }
    }

    @VisibleForTesting
    fun getPrescriptions(taskIds: List<String>, order: OrderUseCaseData.OrderDetail) = taskIds.map { taskId ->
        order.taskDetailedBundles.first { it.prescription?.taskId == taskId }.prescription
    }

    fun consumeAllMessages(onMessagesConsumed: () -> Unit) {
        controllerScope.launch {
            // Marks the replied messages as read
            _messages.value.data?.forEach { message ->
                updateCommunicationConsumedStatusUseCase(CommunicationIdentifier.Communication(message.communicationId))
            }

            // Marks the dispense request and invoice messages as read
            _order.value.data?.let { orderDetail ->
                updateCommunicationConsumedStatusUseCase(CommunicationIdentifier.Order(orderDetail.orderId))
                updateInvoicesByOrderIdAndTaskIdUseCase(orderDetail.orderId)
            }

            if (isLocalMessage) {
                setInternalMessageIsReadUseCase.invoke()
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
    val getInternalMessagesUseCase: GetInternalMessagesUseCase by rememberInstance()
    val setInternalMessagesAsReadUseCase: SetInternalMessagesAsReadUseCase by rememberInstance()
    val updateCommunicationConsumedStatusUseCase: UpdateCommunicationConsumedStatusUseCase by rememberInstance()
    val updateInvoicesByOrderIdAndTaskIdUseCase: UpdateInvoicesByOrderIdAndTaskIdUseCase by rememberInstance()
    val getPharmacyByTelematikIdUseCase by rememberInstance<GetPharmacyByTelematikIdUseCase>()
    val getProfileByOrderIdUseCase by rememberInstance<GetProfileByOrderIdUseCase>()
    val context = LocalContext.current

    return remember(orderId) {
        MessageDetailController(
            orderId = orderId,
            isLocalMessage = isLocalMessage,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageIsReadUseCase = setInternalMessagesAsReadUseCase,
            updateCommunicationConsumedStatusUseCase = updateCommunicationConsumedStatusUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase,
            context = context
        )
    }
}
