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
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.messages.domain.usecase.GetArchivedMessageKeysUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetCombinedMessagesAsInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetHideCompletedUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetCompletedMessageStateUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetHideCompletedUseCase
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
data class CommResV3MessageListScreenState(
    val visibleMessages: UiState<List<InAppMessage>> = UiState.Loading(),
    val hasAnyMessages: Boolean = false,
    val searchValue: String = "",
    val hideCompleted: Boolean = true,
    val isSelectionMode: Boolean = false,
    val completedMessageKeys: Set<String> = emptySet(),
    val pendingCompletedMessageKeys: Set<String> = emptySet(),
    val activeProfileName: String? = null
)

private data class CommResV3VisibilityState(
    val visibleMessages: UiState<List<InAppMessage>>,
    val hasAnyMessages: Boolean,
    val searchValue: String,
    val hideCompleted: Boolean,
    val completedMessageKeys: Set<String>
)

@Stable
class CommResV3MessageListController(
    private val getCombinedMessagesAsInAppMessageUseCase: GetCombinedMessagesAsInAppMessageUseCase,
    private val getArchivedMessageKeysUseCase: GetArchivedMessageKeysUseCase,
    private val setCompletedMessageStateUseCase: SetCompletedMessageStateUseCase,
    private val getHideCompletedUseCase: GetHideCompletedUseCase,
    private val setHideCompletedUseCase: SetHideCompletedUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val tracker: Tracker,
    private val context: Context
) : Controller() {
    private val selectedAppLanguage = context.resources.configuration.locales[0].language
    private var fetchJob: Job? = null
    private val _messagesList = MutableStateFlow<UiState<List<InAppMessage>>>(UiState.Loading())
    private val _searchValue = MutableStateFlow("")
    private val _selectionMode = MutableStateFlow(false)
    private val _pendingCompletedMessageKeys = MutableStateFlow<Set<String>>(emptySet())

    private val activeProfileName = getActiveProfileUseCase()
        .map { it.name }
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), null)

    private val completedMessageKeys = getArchivedMessageKeysUseCase()
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), emptySet())

    private val hideCompletedFlow = getHideCompletedUseCase()
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), true)

    private val visibleMessagesState = combine(
        _messagesList,
        _searchValue,
        hideCompletedFlow,
        completedMessageKeys
    ) { messagesState, searchValue, hideCompleted, completedKeys ->
        buildVisibleMessagesState(messagesState, searchValue, hideCompleted, completedKeys)
    }

    private val visibilityState = combine(
        visibleMessagesState,
        _messagesList,
        _searchValue,
        hideCompletedFlow,
        completedMessageKeys
    ) { visibleMessages, messagesState, searchValue, hideCompleted, completedKeys ->
        CommResV3VisibilityState(
            visibleMessages = visibleMessages,
            hasAnyMessages = messagesState.data?.isNotEmpty() == true,
            searchValue = searchValue,
            hideCompleted = hideCompleted,
            completedMessageKeys = completedKeys
        )
    }

    val screenState: StateFlow<CommResV3MessageListScreenState> = combine(
        visibilityState,
        _selectionMode,
        _pendingCompletedMessageKeys,
        activeProfileName
    ) { visibilityState, selectionMode, pendingKeys, profileName ->
        CommResV3MessageListScreenState(
            visibleMessages = visibilityState.visibleMessages,
            hasAnyMessages = visibilityState.hasAnyMessages,
            searchValue = visibilityState.searchValue,
            hideCompleted = visibilityState.hideCompleted,
            isSelectionMode = selectionMode,
            completedMessageKeys = visibilityState.completedMessageKeys,
            pendingCompletedMessageKeys = pendingKeys,
            activeProfileName = profileName
        )
    }.stateIn(controllerScope, SharingStarted.WhileSubscribed(), CommResV3MessageListScreenState())

    init {
        fetchMessagesList()
    }

    fun retryFetchMessagesList() {
        fetchMessagesList()
    }

    fun onSearchValueChange(value: String) {
        _searchValue.value = value
    }

    fun clearSearchValue() {
        _searchValue.value = ""
    }

    fun onHideCompletedChange(value: Boolean) {
        controllerScope.launch {
            setHideCompletedUseCase(value)
        }
    }

    fun onSelectionModeChange(enabled: Boolean) {
        if (enabled) {
            _pendingCompletedMessageKeys.value = completedMessageKeys.value
        }
        _selectionMode.value = enabled
    }

    fun toggleMessageCompleted(message: InAppMessage) {
        val messageKey = message.archiveKey()
        val currentPending = _pendingCompletedMessageKeys.value
        _pendingCompletedMessageKeys.value = if (messageKey in currentPending) {
            currentPending - messageKey
        } else {
            currentPending + messageKey
        }
    }

    fun applyArchivedStatusChanges() {
        val currentCompleted = completedMessageKeys.value
        val pendingCompleted = _pendingCompletedMessageKeys.value

        val toArchive = pendingCompleted - currentCompleted
        val toUnarchive = currentCompleted - pendingCompleted

        controllerScope.launch {
            if (toArchive.isNotEmpty()) {
                setCompletedMessageStateUseCase(messageKeys = toArchive, isCompleted = true)
            }
            if (toUnarchive.isNotEmpty()) {
                setCompletedMessageStateUseCase(messageKeys = toUnarchive, isCompleted = false)
            }
            _selectionMode.value = false
        }
    }

    fun trackMessageCount() {
        val messageCount = _messagesList.value.data?.size ?: return
        // Runs while the screen is being disposed, so it must survive the scope cancellation
        controllerScope.launch(NonCancellable) {
            tracker.trackMetric(TrackedEvent.MessageCount(messageCount))
        }
    }

    private fun fetchMessagesList() {
        // Cancel the previous collector, otherwise every refresh adds another live collection of the heavy order flow
        fetchJob?.cancel()
        fetchJob = controllerScope.launch {
            _messagesList.value = UiState.Loading()
            try {
                getCombinedMessagesAsInAppMessageUseCase(selectedAppLanguage).collect { combinedList ->
                    _messagesList.value = if (combinedList.isEmpty()) UiState.Empty() else UiState.Data(combinedList)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e { "combining messages failed: ${e.stackTraceToString()}" }
                _messagesList.value = UiState.Error(e)
            }
        }
    }
}

private fun buildVisibleMessagesState(
    messagesState: UiState<List<InAppMessage>>,
    searchValue: String,
    hideCompleted: Boolean,
    completedKeys: Set<String>
): UiState<List<InAppMessage>> {
    val error = messagesState.error
    val messages = messagesState.data

    return when {
        messagesState.isLoading -> UiState.Loading()
        error != null -> UiState.Error(error)
        messages != null -> {
            val visibleMessages = messages.filter { message ->
                message.matchesSearch(searchValue) &&
                    (!hideCompleted || message.archiveKey() !in completedKeys)
            }
            if (visibleMessages.isEmpty()) UiState.Empty() else UiState.Data(visibleMessages)
        }

        else -> UiState.Empty()
    }
}

private fun InAppMessage.matchesSearch(searchValue: String): Boolean {
    if (searchValue.isBlank()) {
        return true
    }
    return from.contains(searchValue, ignoreCase = true) ||
        text.orEmpty().contains(searchValue, ignoreCase = true)
}

fun InAppMessage.archiveKey(): String {
    val primaryId = when {
        id.isNotBlank() -> id
        taskId != null -> taskId
        threadOrderId != null -> threadOrderId
        version != null -> version
        else -> from
    }
    return "${messageProfile?.name.orEmpty()}:$primaryId"
}

@Composable
fun rememberCommResV3MessageListController(): CommResV3MessageListController {
    val getCombinedMessagesAsInAppMessageUseCase by rememberInstance<GetCombinedMessagesAsInAppMessageUseCase>()
    val getArchivedMessageKeysUseCase by rememberInstance<GetArchivedMessageKeysUseCase>()
    val setCompletedMessageStateUseCase by rememberInstance<SetCompletedMessageStateUseCase>()
    val getHideCompletedUseCase by rememberInstance<GetHideCompletedUseCase>()
    val setHideCompletedUseCase by rememberInstance<SetHideCompletedUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val tracker by rememberInstance<Tracker>()
    val context = LocalContext.current
    val controller = remember {
        CommResV3MessageListController(
            getCombinedMessagesAsInAppMessageUseCase = getCombinedMessagesAsInAppMessageUseCase,
            getArchivedMessageKeysUseCase = getArchivedMessageKeysUseCase,
            setCompletedMessageStateUseCase = setCompletedMessageStateUseCase,
            getHideCompletedUseCase = getHideCompletedUseCase,
            setHideCompletedUseCase = setHideCompletedUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            tracker = tracker,
            context = context
        )
    }
    controller.CancelScopeOnDispose()
    return controller
}
