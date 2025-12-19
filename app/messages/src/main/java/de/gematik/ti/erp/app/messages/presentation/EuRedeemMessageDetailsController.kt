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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.messages.domain.usecase.GetEuOrderMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetEuOrderTasksUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.MarkEuEventsReadUseCase
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.prescription.model.TaskData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.kodein.di.compose.rememberInstance

/**
 * Controller for managing EU prescription redemption message details screen.
 * Handles loading and displaying message history and associated prescription tasks for a specific order,
 * and provides functionality to mark events as read.
 *
 * @param orderId The unique identifier for the EU prescription order
 * @param getEuOrderMessagesUseCase Use case for retrieving EU order messages from the repository
 * @param threadStart The start timestamp of the message thread to display
 * @param threadEnd The end timestamp of the message thread to display
 * @param getEuOrderTasksUseCase Use case for retrieving prescription tasks associated with the order
 * @param markEuEventsReadUseCase Use case for marking message events as read
 */
class EuRedeemMessageDetailsController(
    private val orderId: String,
    private val getEuOrderMessagesUseCase: GetEuOrderMessagesUseCase,
    private val threadStart: Instant,
    private val threadEnd: Instant,
    private val getEuOrderTasksUseCase: GetEuOrderTasksUseCase,
    private val markEuEventsReadUseCase: MarkEuEventsReadUseCase
) : Controller() {

    private val _euMessages = MutableStateFlow<UiState<List<EuOrderMessageUiModel>>>(UiState.Loading())

    private val _euTasks = MutableStateFlow<UiState<List<TaskData>>>(UiState.Loading())

    /**
     * State flow containing the list of EU order messages in different UI states (Loading, Empty, Data, Error).
     * Observing this flow will provide updates when messages are loaded or changed.
     */
    val euMessages: StateFlow<UiState<List<EuOrderMessageUiModel>>> = _euMessages.asStateFlow()

    /**
     * State flow containing the list of prescription tasks associated with the EU order in different UI states.
     * Observing this flow will provide updates when tasks are loaded or changed.
     */
    val euTasks: StateFlow<UiState<List<TaskData>>> = _euTasks.asStateFlow()

    /**
     * Initializes the controller by loading EU order messages and associated prescription tasks.
     * This should be called when the screen is first displayed.
     */
    fun init() {
        loadEuOrderMessages()
        loadEuTasks()
    }

    /**
     * Loads EU order messages for the specified order within the given time range.
     * Updates [euMessages] state flow with the loaded data or empty state if no messages are found.
     * This runs in the controller scope and continuously collects updates from the use case.
     */
    private fun loadEuOrderMessages() {
        controllerScope.launch {
            getEuOrderMessagesUseCase.invoke(orderId, threadStart, threadEnd).collectLatest { uiModels ->
                if (uiModels.isEmpty()) {
                    _euMessages.value = UiState.Empty()
                } else {
                    _euMessages.value = UiState.Data(uiModels)
                }
            }
        }
    }

    /**
     * Marks all currently loaded EU order messages as read.
     * Extracts message IDs from the current [euMessages] state and invokes the use case to mark them as read.
     * Also extracts the underlying event IDs from all messages which have been reduced to show on the screen and marks them as read.
     * This is typically called when the user views the message details screen.
     */
    fun markEuEventsRead(items: List<EuOrderMessageUiModel>) {
        controllerScope.launch {
            val allIds = buildList {
                addAll(items.map { it.id })
                addAll(items.flatMap { it.underlyingEventIds })
            }
            markEuEventsReadUseCase(allIds)
        }
    }

    /**
     * Loads prescription tasks associated with the EU order.
     * Updates [euTasks] state flow with the loaded data, empty state if no tasks are found,
     * or error state if the operation fails.
     * This runs in the controller scope.
     */
    private fun loadEuTasks() {
        controllerScope.launch {
            getEuOrderTasksUseCase.invoke(orderId)
                .fold(
                    onSuccess = { tasks ->
                        when {
                            tasks.isEmpty() -> _euTasks.value = UiState.Empty()
                            else -> _euTasks.value = UiState.Data(tasks)
                        }
                    },
                    onFailure = {
                        _euTasks.value = UiState.Error(it)
                    }
                )
        }
    }
}

/**
 * Composable function to remember and create an instance of [EuRedeemMessageDetailsController].
 * The controller instance is remembered across recompositions and recreated only when
 * orderId, threadStart, or threadEnd change.
 *
 * Use cases are retrieved from the DI container using Kodein.
 *
 * @param orderId The unique identifier for the EU prescription order
 * @param threadStart The start timestamp of the message thread to display
 * @param threadEnd The end timestamp of the message thread to display
 * @return A remembered instance of [EuRedeemMessageDetailsController]
 */
@Composable
internal fun rememberEuRedeemMessageDetailsController(
    orderId: String,
    threadStart: Instant,
    threadEnd: Instant
): EuRedeemMessageDetailsController {
    val getEuOrderMessagesUseCase by rememberInstance<GetEuOrderMessagesUseCase>()
    val getEuOrderTasksUseCase by rememberInstance<GetEuOrderTasksUseCase>()
    val markEuEventsReadUseCase by rememberInstance<MarkEuEventsReadUseCase>()
    return remember(orderId, threadStart, threadEnd) {
        EuRedeemMessageDetailsController(
            orderId = orderId,
            threadStart = threadStart,
            threadEnd = threadEnd,
            getEuOrderMessagesUseCase = getEuOrderMessagesUseCase,
            getEuOrderTasksUseCase = getEuOrderTasksUseCase,
            markEuEventsReadUseCase = markEuEventsReadUseCase
        )
    }
}
