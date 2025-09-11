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

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.base.ContextExtensions.getCurrentLocale
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
import de.gematik.ti.erp.app.translation.domain.model.TranslationState
import de.gematik.ti.erp.app.translation.usecase.DownloadLanguageModelUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.IsTargetLanguageSetUseCase
import de.gematik.ti.erp.app.translation.usecase.ToggleTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.TranslateTextUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class MessageDetailController(
    private val application: Application,
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
    private val getTranslationConsentUseCase: GetTranslationConsentUseCase,
    private val isTargetLanguageSetUseCase: IsTargetLanguageSetUseCase,
    private val toggleTranslationConsentUseCase: ToggleTranslationConsentUseCase,
    private val downloadedLanguagesUseCase: DownloadLanguageModelUseCase,
    private val translateTextUseCase: TranslateTextUseCase
) : AndroidViewModel(application) {
    internal val _localMessages = MutableStateFlow<List<InAppMessage>>(listOf())

    private val selectedAppLanguage = application.resources.configuration.locales[0].language
    private val isTargetLanguageSet: Flow<Boolean> by lazy { isTargetLanguageSetUseCase.invoke() }
    private val isTranslationEnabled: Flow<Boolean> by lazy { getTranslationConsentUseCase.invoke() }

    private val _messages = MutableStateFlow<UiState<List<OrderUseCaseData.Message>>>(UiState.Loading())
    private val _order = MutableStateFlow<UiState<OrderUseCaseData.OrderDetail>>(UiState.Loading())
    private val _pharmacy = MutableStateFlow<UiState<PharmacyUseCaseData.Pharmacy>>(UiState.Loading())
    private val _profile = MutableStateFlow<ProfilesUseCaseData.Profile?>(null)
    private val _translationInProgress = MutableStateFlow<Map<String, Boolean>>(mapOf())

    val localMessages = _localMessages.asStateFlow()
    val messages = _messages.asStateFlow()
    val order = _order.asStateFlow()
    val pharmacy = _pharmacy.asStateFlow()
    val profile = _profile.asStateFlow()
    val translationInProgress = _translationInProgress.asStateFlow()

    private val isAppGerman: Boolean = selectedAppLanguage.equals("de", ignoreCase = true)

    val showTranslationFeature: StateFlow<Boolean> = flowOf(!isAppGerman).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val isTranslationsAllowed: StateFlow<Boolean> =
        combine(isTargetLanguageSet, isTranslationEnabled) { targetSet, translationEnabled ->
            targetSet && translationEnabled
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(50),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val hasReplyMessages: StateFlow<Boolean> =
        messages
            .mapLatest { it.data?.count()?.let { count -> count > 0 } ?: false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun init() {
        if (isLocalMessage) {
            fetchLocalMessages()
        } else {
            loadOrdersAndInvoiceMessage()
            loadReplyMessages()
            loadProfile()
        }
    }

    fun toggleTranslationConsentUseCase() {
        viewModelScope.launch {
            val currentState = isTranslationEnabled.firstOrNull()
            currentState?.let {
                val expectedState = !it
                toggleTranslationConsentUseCase.invoke(expectedState)
                // silent download of the current locale language model
                if (expectedState) downloadedLanguagesUseCase.invoke(application.getCurrentLocale())
            }
        }
    }

    fun translateText(
        id: String,
        text: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            translateTextUseCase.invoke(text = text)
                .collectLatest { state ->
                    when (state) {
                        TranslationState.Loading -> startTranslationFor(id)
                        is TranslationState.Error -> endTranslationFor()
                        is TranslationState.Success -> {
                            endTranslationFor()
                            state.translation.let(onSuccess)
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
        viewModelScope.launch {
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

    private fun startTranslationFor(id: String) {
        val allIds = _translationInProgress.value.keys + id
        _translationInProgress.value = allIds.associateWith { it == id }
    }

    private fun endTranslationFor() {
        _translationInProgress.value = _translationInProgress.value.mapValues { false }
    }

    private fun fetchLocalMessages() {
        viewModelScope.launch {
            try {
                getInternalMessagesUseCase.invoke(selectedAppLanguage).collect { messagesList ->
                    _localMessages.value = messagesList.sortedByDescending { it.timeState.timestamp }
                }
            } catch (e: Exception) {
                Napier.e { "Error Loading Internal Messages ${e.stackTraceToString()}" }
                _localMessages.value = listOf()
            }
        }
    }

    private fun loadOrdersAndInvoiceMessage() {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
                                val newMessageList = messageList.mapNotNull { message ->
                                    state.data?.let {
                                        message.copy(prescriptions = getPrescriptions(message.taskIds, it))
                                    }
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

    private fun getPharmacy(telematikId: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
    val getTranslationConsentUseCase by rememberInstance<GetTranslationConsentUseCase>()
    val isTargetLanguageSetUseCase by rememberInstance<IsTargetLanguageSetUseCase>()
    val translateTextUseCase by rememberInstance<TranslateTextUseCase>()
    val toggleTranslationConsentUseCase by rememberInstance<ToggleTranslationConsentUseCase>()
    val downloadedLanguagesUseCase by rememberInstance<DownloadLanguageModelUseCase>()
    val application = LocalContext.current.applicationContext as Application

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
            translateTextUseCase = translateTextUseCase,
            isTargetLanguageSetUseCase = isTargetLanguageSetUseCase,
            getTranslationConsentUseCase = getTranslationConsentUseCase,
            toggleTranslationConsentUseCase = toggleTranslationConsentUseCase,
            downloadedLanguagesUseCase = downloadedLanguagesUseCase,
            application = application
        )
    }
}
