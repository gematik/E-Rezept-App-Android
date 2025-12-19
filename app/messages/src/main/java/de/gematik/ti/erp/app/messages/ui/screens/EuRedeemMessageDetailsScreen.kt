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

package de.gematik.ti.erp.app.messages.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag.Orders.Messages.EuMessageDetails
import de.gematik.ti.erp.app.column.PrescriptionListForMessages
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutesBackStackEntryArguments
import de.gematik.ti.erp.app.messages.presentation.rememberEuRedeemMessageDetailsController
import de.gematik.ti.erp.app.messages.ui.components.InfoChip
import de.gematik.ti.erp.app.messages.ui.components.MessageActionButton
import de.gematik.ti.erp.app.messages.ui.components.MessagePrescriptionDividerWithTitle
import de.gematik.ti.erp.app.messages.ui.components.MessageTimeline
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.messages.ui.preview.EuRedeemMessageDetailsPreviewParameterProvider
import de.gematik.ti.erp.app.messages.ui.preview.MessagePreviewMocks.MOCK_SYNCED_TASK_DATA_01
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.TaskData
import de.gematik.ti.erp.app.preview.LightPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState

/**
 * Screen that displays detailed message history for EU prescription redemption orders.
 * Shows a timeline of messages related to access code creation, revocation, and redemption status,
 * along with the associated prescriptions.
 *
 * @param navController The navigation controller for handling screen navigation
 * @param navBackStackEntry The back stack entry containing navigation arguments (orderId, threadStart, threadEnd)
 */
internal class EuRedeemMessageDetailsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val listState = rememberLazyListState()

        val arguments = MessagesRoutesBackStackEntryArguments(navBackStackEntry)
        val threadStart = arguments.threadStart
        val threadEnd = arguments.threadEnd
        val orderId = arguments.orderId

        val controller = rememberEuRedeemMessageDetailsController(orderId, threadStart, threadEnd)

        LaunchedEffect(orderId, threadStart, threadEnd) {
            controller.init()
        }

        val messages by controller.euMessages.collectAsStateWithLifecycle()
        val tasks by controller.euTasks.collectAsStateWithLifecycle()

        EuRedeemMessageContent(
            listState = listState,
            messages = messages,
            tasks = tasks,
            onBack = navController::popBackStack,
            markEventsAsRead = {
                controller.markEuEventsRead(it)
            },
            onPrescriptionClick = { taskId ->
                navController.navigate(PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId = taskId))
            },
            onShowCode = { accessCode ->
                navController.navigate(EuRoutes.EuRedemptionCodeScreen.path(accessCode))
            },
            onRevokeAccess = {
                navController.navigate(EuRoutes.EuDeleteAccessCodeBottomSheetScreen.path())
            }
        )
    }
}

/**
 * Main content composable for displaying EU redemption message details.
 * Renders a scrollable list of messages in a timeline format, along with associated prescriptions.
 * Handles different UI states (loading, empty, error, data) and automatically marks events as read when displayed.
 *
 * @param listState The lazy list state for managing scroll position and elevation
 * @param messages UI state containing the list of EU order messages to display
 * @param tasks UI state containing the list of prescriptions associated with the order
 * @param markEventsAsRead Callback invoked to mark all displayed events as read
 * @param onPrescriptionClick Callback invoked when a prescription is clicked, receives the taskId
 * @param onShowCode Callback invoked when user wants to view the access code, receives the access code
 * @param onRevokeAccess Callback invoked when user wants to revoke an access code, receives the access code
 * @param onBack Callback invoked when the back button is pressed
 */
@Composable
private fun EuRedeemMessageContent(
    listState: LazyListState,
    messages: UiState<List<EuOrderMessageUiModel>>,
    tasks: UiState<List<TaskData>>,
    markEventsAsRead: (List<EuOrderMessageUiModel>) -> Unit,
    onPrescriptionClick: (String) -> Unit,
    onShowCode: (String) -> Unit,
    onRevokeAccess: (String) -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(""),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        topBarTitle = stringResource(R.string.eu_messages_list_latest_title),
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        topBarPadding = PaddingValues()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            UiStateMachine(
                state = messages,
                onEmpty = {
                    ErrorScreenComponent(
                        titleText = stringResource(R.string.messages_empty_title),
                        bodyText = stringResource(R.string.messages_empty_subtitle)
                    )
                },
                onError = {
                    ErrorScreenComponent(
                        titleText = stringResource(R.string.generic_error_title),
                        bodyText = stringResource(R.string.generic_error_info)
                    )
                }
            ) { items ->
                OnItemsAvailable(items) {
                    markEventsAsRead(it)
                }
                LazyColumn(
                    modifier = Modifier.testTag(EuMessageDetails),
                    contentPadding = padding,
                    state = listState
                ) {
                    item {
                        SpacerMedium()
                        Text(
                            stringResource(R.string.messages_history_title),
                            style = AppTheme.typography.h6,
                            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                        )
                    }
                    items(items) {
                        EuOrderMessage(
                            item = it,
                            onShowCode = onShowCode,
                            onRevokeAccess = onRevokeAccess
                        )
                    }

                    item {
                        tasks.data?.let {
                            MessagePrescriptionDividerWithTitle()
                        }
                    }

                    item {
                        UiStateMachine(tasks) { tasks ->
                            if (tasks.isNotEmpty()) {
                                PrescriptionListForMessages(
                                    items = tasks.map { it.taskId },
                                    onName = { taskId ->
                                        val task = tasks.find { it.taskId == taskId }
                                        when (task) {
                                            is SyncedTaskData.SyncedTask -> task.medicationName() ?: ""
                                            is ScannedTaskData.ScannedTask -> task.name
                                            else -> ""
                                        }
                                    }
                                ) { taskId ->
                                    onPrescriptionClick(taskId)
                                }
                            }
                        }
                    }
                    item {
                        SpacerMedium()
                    }
                }
            }
        }
    }
}

/**
 * Helper composable that triggers a callback when items become available.
 * Uses LaunchedEffect to execute the onRead callback once when the items list transitions from empty to non-empty.
 * This is typically used to mark messages as read when they are first displayed.
 *
 * @param T The type of items in the list
 * @param items The list of items to monitor
 * @param onRead Callback invoked when items are available and non-empty
 */
@Composable
private fun <T> OnItemsAvailable(
    items: List<T>,
    onRead: (List<T>) -> Unit
) {
    LaunchedEffect(items.isNotEmpty()) {
        if (items.isNotEmpty()) {
            onRead(items)
        }
    }
}

/**
 * Displays a single EU order message in a timeline format.
 * Shows the message timestamp, prescription info chips, title, description, and action buttons
 * (show code, revoke code) based on the message state.
 *
 * The message appearance changes based on its state:
 * - Shows action buttons if the message is active and not revoked
 * - Shows a disabled "Code Revoked" button if the access code has been revoked
 * - Adds a country flag emoji to the title for AccessCodeCreated messages
 *
 * @param item The EU order message to display
 * @param onShowCode Callback invoked when the "Show Code" button is clicked, receives the access code
 * @param onRevokeAccess Callback invoked when the "Revoke Code" button is clicked, receives the access code
 */
@Composable
private fun EuOrderMessage(
    item: EuOrderMessageUiModel,
    onShowCode: (String) -> Unit,
    onRevokeAccess: (String) -> Unit
) {
    MessageTimeline(
        drawFilledTop = !item.isFirst,
        drawFilledBottom = !item.isLast,
        isClickable = false,
        timestamp = {
            Text(
                text = item.dateTimeString,
                style = AppTheme.typography.subtitle2
            )
        },
        content = {
            FlowRow(
                modifier = Modifier.padding(
                    top = PaddingDefaults.Small,
                    bottom = PaddingDefaults.Tiny
                )
            ) {
                item.prescriptionNames.forEach { InfoChip(it) }
            }
            // Title
            if (item.title.isNotBlank()) {
                val title = when (item is EuOrderMessageUiModel.AccessCodeCreated) {
                    true -> item.title + " " + item.flagEmoji
                    false -> item.title
                }

                Text(
                    modifier = Modifier.semantics { contentDescription = item.title },
                    text = title,
                    style = AppTheme.typography.subtitle1

                )
            }

            // Description
            item.description?.let {
                SpacerTiny()
                Text(
                    text = it,
                    style = AppTheme.typography.body2
                )
            }

            if (item.isRevoked) {
                SpacerTiny()
                MessageActionButton(
                    text = stringResource(R.string.eu_messages_code_revoked_button_text),
                    enabled = false,
                    tint = AppTheme.colors.red700
                ) {}
            } else if (item.showButtons) {
                SpacerTiny()
                MessageActionButton(stringResource(R.string.eu_messages_show_code_button_text)) {
                    onShowCode(item.accessCode)
                }
                SpacerTiny()
                MessageActionButton(stringResource(R.string.eu_messages_revoke_code_button_text)) {
                    onRevokeAccess(item.accessCode)
                }
            }
        }
    )
}

/**
 * Preview for the EU redemption message details screen.
 * Displays mock EU order messages and prescriptions to visualize the timeline layout and message components.
 *
 * @param previewData Mock list of EU order messages provided by the preview parameter provider
 */
@LightPreview
@Composable
fun EuRedeemMessageContentPreview(
    @PreviewParameter(EuRedeemMessageDetailsPreviewParameterProvider::class)
    previewData: List<EuOrderMessageUiModel>
) {
    PreviewTheme {
        EuRedeemMessageContent(
            listState = rememberLazyListState(),
            messages = UiState.Data(previewData),
            tasks = UiState.Data(listOf(MOCK_SYNCED_TASK_DATA_01)),
            onPrescriptionClick = {},
            onShowCode = {},
            onRevokeAccess = {},
            markEventsAsRead = {},
            onBack = {}
        )
    }
}
