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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DriveEta
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.core.LocalApplicationInnerPadding
import de.gematik.ti.erp.app.core.LocalMainBottomBarVisibility
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.timeStateParser
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.material3.components.switchs.GemSwitch
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.model.InAppMessageStatus
import de.gematik.ti.erp.app.messages.navigation.HandleMessageListRefresh
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.messages.presentation.CommResV3MessageListScreenState
import de.gematik.ti.erp.app.messages.presentation.archiveKey
import de.gematik.ti.erp.app.messages.presentation.rememberCommResV3MessageListController
import de.gematik.ti.erp.app.messages.ui.components.InfoChip
import de.gematik.ti.erp.app.messages.ui.components.MessagesLoadingShimmer
import de.gematik.ti.erp.app.messages.ui.components.NoOrders
import de.gematik.ti.erp.app.messages.ui.preview.MessageListParameterProvider
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.padding.ApplicationInnerPadding
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.pulltorefresh.PullToRefresh
import de.gematik.ti.erp.app.pulltorefresh.extensions.triggerEnd
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.TextButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.animatedElevationStickySearchField
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.extensions.sanitizeMarkdownText
import de.gematik.ti.erp.app.utils.uistate.UiState

class CommResV3MessageListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val messagesController = rememberCommResV3MessageListController()
        val bottomBarVisibility = LocalMainBottomBarVisibility.current
        val applicationPadding = LocalApplicationInnerPadding.current
        val listState = rememberLazyListState()
        val screenState by messagesController.screenState.collectAsStateWithLifecycle()
        val pullToRefreshState = rememberPullToRefreshState()

        LaunchedEffect(screenState.isSelectionMode) {
            bottomBarVisibility.value = !screenState.isSelectionMode
        }

        DisposableEffect(Unit) {
            onDispose {
                bottomBarVisibility.value = true
                messagesController.trackMessageCount()
            }
        }

        LaunchedEffect(screenState.visibleMessages) {
            if (!screenState.visibleMessages.isLoading) {
                pullToRefreshState.triggerEnd()
            }
        }

        navBackStackEntry.HandleMessageListRefresh {
            messagesController.retryFetchMessagesList()
        }

        CommResV3MessageListScreenScaffold(
            screenState = screenState,
            listState = listState,
            pullToRefreshState = pullToRefreshState,
            applicationPadding = applicationPadding,
            onClickRetry = messagesController::retryFetchMessagesList,
            onSearchValueChange = messagesController::onSearchValueChange,
            onSearchClear = messagesController::clearSearchValue,
            onHideCompletedChange = messagesController::onHideCompletedChange,
            onSelectionModeChange = messagesController::onSelectionModeChange,
            onMessageCompletedChange = messagesController::toggleMessageCompleted,
            onCommitChanges = messagesController::applyArchivedStatusChanges,
            onOpenMessage = { message ->
                when (message.messageProfile) {
                    CommunicationErpModel.CommunicationProfile.InApp -> {
                        navController.navigate(MessagesRoutes.CommResV3InternalMessageDetailScreen.path())
                    }

                    CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
                    CommunicationErpModel.CommunicationProfile.ErxCommunicationReply -> {
                        if (message.id.isEmpty()) {
                            navController.navigate(MessagesRoutes.UnknownOrderMessageDetailScreen.path(message.taskId ?: ""))
                        } else {
                            navController.navigate(MessagesRoutes.CommResV3OrderMessageDetailScreen.path(message.id))
                        }
                    }

                    CommunicationErpModel.CommunicationProfile.EuOrder -> {
                        navController.navigate(
                            MessagesRoutes.EuRedeemMessageDetailsScreen.path(
                                orderId = message.threadOrderId,
                                threadStart = message.threadStart,
                                threadEnd = message.threadEnd,
                                pharmacyName = message.from
                            )
                        )
                    }

                    null -> Unit
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommResV3MessageListScreenScaffold(
    screenState: CommResV3MessageListScreenState,
    listState: LazyListState,
    pullToRefreshState: PullToRefreshState,
    applicationPadding: ApplicationInnerPadding? = null,
    onClickRetry: () -> Unit,
    onSearchValueChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onHideCompletedChange: (Boolean) -> Unit,
    onSelectionModeChange: (Boolean) -> Unit,
    onMessageCompletedChange: (InAppMessage) -> Unit,
    onOpenMessage: (InAppMessage) -> Unit,
    onCommitChanges: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.messages_title),
        listState = listState,
        applicationPadding = applicationPadding,
        actions = {
            if (screenState.isSelectionMode) {
                TextButton(
                    buttonText = stringResource(R.string.cancel),
                    onClick = { onSelectionModeChange(false) }
                )
            } else {
                TextButton(
                    buttonText = stringResource(R.string.invoice_header_select),
                    onClick = { onSelectionModeChange(true) },
                    enabled = screenState.hasAnyMessages
                )
            }
        },
        bottomBar = {
            if (screenState.isSelectionMode) {
                StickySelectionBottomBar(
                    modifier = Modifier.fillMaxWidth(),
                    onCommit = onCommitChanges
                )
            }
        }
    ) { innerPadding ->
        CommResV3MessageListContent(
            modifier = Modifier.padding(innerPadding),
            screenState = screenState,
            listState = listState,
            pullToRefreshState = pullToRefreshState,
            focusManager = LocalFocusManager.current,
            onClickRetry = onClickRetry,
            onSearchValueChange = onSearchValueChange,
            onSearchClear = onSearchClear,
            onHideCompletedChange = onHideCompletedChange,
            onMessageCompletedChange = onMessageCompletedChange,
            onOpenMessage = onOpenMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommResV3MessageListContent(
    modifier: Modifier,
    screenState: CommResV3MessageListScreenState,
    listState: LazyListState,
    pullToRefreshState: PullToRefreshState,
    focusManager: FocusManager,
    onClickRetry: () -> Unit,
    onSearchValueChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onHideCompletedChange: (Boolean) -> Unit,
    onMessageCompletedChange: (InAppMessage) -> Unit,
    onOpenMessage: (InAppMessage) -> Unit
) {
    val emptyStateDescription = stringResource(R.string.a11y_messages_empty_state)
    val loadingDescription = stringResource(R.string.a11y_messages_loading)
    val errorStateDescription = stringResource(R.string.a11y_messages_error_state)
    val searchHint = stringResource(R.string.messages_search_hint)
    val clearSearchLabel = stringResource(R.string.messages_search_clear)

    Box(modifier = Modifier.fillMaxSize()) {
        UiStateMachine(
            state = screenState.visibleMessages,
            onEmpty = {
                if (screenState.hasAnyMessages) {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .testTag(TestTag.Orders.Content),
                        state = listState,
                        contentPadding = PaddingValues(bottom = SizeDefaults.twentyfold)
                    ) {
                        screenState.activeProfileName?.let { name ->
                            item {
                                Text(
                                    text = stringResource(R.string.messages_active_profile_name_prefix, name),
                                    style = AppTheme.typography.subtitle2,
                                    color = AppTheme.colors.neutral700,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Small)
                                )
                            }
                        }
                        if (!screenState.isSelectionMode) {
                            animatedElevationStickySearchField(
                                lazyListState = listState,
                                focusManager = focusManager,
                                value = screenState.searchValue,
                                onValueChange = onSearchValueChange,
                                onRemoveValue = onSearchClear,
                                description = searchHint,
                                placeholderText = searchHint,
                                contentDescriptionText = clearSearchLabel
                            )
                            item {
                                CommResV3CompletedFilterRow(
                                    hideCompleted = screenState.hideCompleted,
                                    onHideCompletedChange = onHideCompletedChange
                                )
                            }
                        }
                        item {
                            FilteredMessagesEmptyState()
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentDescription = emptyStateDescription
                                liveRegion = LiveRegionMode.Polite
                            }
                    ) {
                        Center { NoOrders { onClickRetry() } }
                    }
                }
            },
            onLoading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = loadingDescription
                            liveRegion = LiveRegionMode.Polite
                        }
                ) {
                    Center { MessagesLoadingShimmer() }
                }
            },
            onError = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = errorStateDescription
                            liveRegion = LiveRegionMode.Assertive
                        }
                ) {
                    ErrorScreenComponent(
                        titleText = stringResource(R.string.generic_error_title),
                        bodyText = stringResource(R.string.generic_error_info),
                        tryAgainText = stringResource(R.string.cdw_fasttrack_try_again),
                        onClickRetry = onClickRetry
                    )
                }
            }
        ) { messages ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .testTag(TestTag.Orders.Content),
                state = listState,
                contentPadding = PaddingValues(bottom = SizeDefaults.twentyfold)
            ) {
                screenState.activeProfileName?.let { name ->
                    item {
                        Text(
                            text = stringResource(R.string.messages_active_profile_name_prefix, name),
                            style = AppTheme.typography.subtitle2,
                            color = AppTheme.colors.neutral700,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Small)
                        )
                    }
                }
                if (!screenState.isSelectionMode) {
                    animatedElevationStickySearchField(
                        lazyListState = listState,
                        focusManager = focusManager,
                        value = screenState.searchValue,
                        onValueChange = onSearchValueChange,
                        onRemoveValue = onSearchClear,
                        description = searchHint,
                        placeholderText = searchHint,
                        contentDescriptionText = clearSearchLabel
                    )
                    item {
                        CommResV3CompletedFilterRow(
                            hideCompleted = screenState.hideCompleted,
                            onHideCompletedChange = onHideCompletedChange
                        )
                    }
                }
                itemsIndexed(messages, key = { index, message -> "${message.archiveKey()}:$index" }) { _, message ->
                    val isCompleted = if (screenState.isSelectionMode) {
                        message.archiveKey() in screenState.pendingCompletedMessageKeys
                    } else {
                        message.archiveKey() in screenState.completedMessageKeys
                    }
                    CommResV3MessageListItem(
                        message = message,
                        isCompleted = isCompleted,
                        isSelectionMode = screenState.isSelectionMode,
                        onClick = {
                            if (screenState.isSelectionMode) {
                                onMessageCompletedChange(message)
                            } else {
                                onOpenMessage(message)
                            }
                        },
                        onCheckedChange = { onMessageCompletedChange(message) }
                    )
                }
                item { SpacerXXXLarge() }
            }
        }
        PullToRefresh(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = SizeDefaults.sixfoldAndQuarter),
            pullToRefreshState = pullToRefreshState
        )
    }
}

@Composable
private fun StickySelectionBottomBar(
    modifier: Modifier = Modifier,
    onCommit: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000), // Ensure background is set
        color = AppTheme.colors.neutral000,
        elevation = SizeDefaults.double
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(PaddingDefaults.Medium)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            PrimaryButton(
                onClick = onCommit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.messages_completed_label))
            }
        }
    }
}

@Composable
private fun CommResV3CompletedFilterRow(
    hideCompleted: Boolean,
    onHideCompletedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHideCompletedChange(!hideCompleted) }
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        GemSwitch(
            checked = hideCompleted,
            onCheckedChange = onHideCompletedChange
        )
        Text(
            text = stringResource(R.string.messages_hide_archived),
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral900
        )
    }
}

@Composable
private fun CommResV3MessageListItem(
    message: InAppMessage,
    isCompleted: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Medium)
                .testTag(TestTag.Orders.OrderListItem)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                verticalAlignment = Alignment.Top
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        checked = isCompleted,
                        onCheckedChange = { onCheckedChange() }
                    )
                }

                if (message.version != null || message.from == stringResource(R.string.internal_message_from)) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.message_from_team),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = SizeDefaults.fourfold)
                    )
                } else {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.message_from_pharmacie),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = SizeDefaults.fourfold)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.from,
                        style = AppTheme.typography.subtitle1,
                        color = AppTheme.colors.neutral900,
                        fontWeight = if (message.isUnread) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SpacerTiny()
                    Text(
                        text = message.text?.sanitizeMarkdownText().orEmpty(),
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.neutral900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SpacerTiny()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.showsOrderStatusChip()) {
                            CommResV3OrderStatusChip(
                                message = message,
                                isCompleted = isCompleted
                            )
                        } else if (isCompleted) {
                            CommResV3MetaChip(
                                text = stringResource(R.string.messages_completed_label),
                                backgroundColor = AppTheme.colors.neutral100,
                                textColor = AppTheme.colors.neutral700
                            )
                        }

                        if (!message.showsOrderStatusChip() && message.prescriptionsCount > 0) {
                            CommResV3MetaChip(
                                text = annotatedPluralsResource(
                                    R.plurals.orders_plurals_label_nr_of_prescriptions,
                                    message.prescriptionsCount,
                                    androidx.compose.ui.text.AnnotatedString(message.prescriptionsCount.toString())
                                ).text,
                                backgroundColor = AppTheme.colors.neutral100,
                                textColor = AppTheme.colors.neutral700
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    Text(
                        text = timeStateParser(message.timeState),
                        style = AppTheme.typography.body2l,
                        color = AppTheme.colors.neutral700,
                        textAlign = TextAlign.End
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                    ) {
                        if (message.isUnread) {
                            CommResV3MetaChip(
                                text = stringResource(R.string.orders_label_new),
                                backgroundColor = AppTheme.colors.primary100,
                                textColor = AppTheme.colors.primary900
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = AppTheme.colors.neutral700
                        )
                    }
                }
            }
            Divider(modifier = Modifier.padding(top = PaddingDefaults.Medium))
        }
    }
}

@Composable
private fun CommResV3OrderStatusChip(
    message: InAppMessage,
    isCompleted: Boolean
) {
    val optionText = when (message.supplyOption) {
        CommunicationSupplyOptionTypeErpModel.DELIVERY -> stringResource(R.string.search_pharmacies_filter_delivery_service)
        CommunicationSupplyOptionTypeErpModel.SHIPMENT -> stringResource(R.string.pharmacy_order_opt_mail_two_lines)
        else -> stringResource(R.string.pharmacy_order_opt_collect_two_lines)
    }
    val content = buildString {
        append(message.orderStatusLabel(isCompleted))
        append(" \u2022 ")
        append(optionText)
        append(" \u2022 ")
        append(
            annotatedPluralsResource(
                R.plurals.orders_plurals_label_nr_of_prescriptions,
                message.prescriptionsCount,
                androidx.compose.ui.text.AnnotatedString(message.prescriptionsCount.toString())
            ).text
        )
    }

    when (message.statusChipType(isCompleted)) {
        CommResV3StatusChipType.Pending -> WaitingStatusChip(content)
        CommResV3StatusChipType.Ordered -> OrderedStatusChip(content)
        CommResV3StatusChipType.ReadyTomorrow -> ReadyTomorrowStatusChip(content)
        CommResV3StatusChipType.ReadyForPickup -> {
            val icon = when (message.supplyOption) {
                CommunicationSupplyOptionTypeErpModel.DELIVERY -> Icons.Outlined.DriveEta
                CommunicationSupplyOptionTypeErpModel.SHIPMENT -> Icons.Outlined.LocalShipping
                else -> Icons.Outlined.Check
            }
            ReadyForPickupStatusChip(content, icon)
        }
        CommResV3StatusChipType.NotAvailable -> NotAvailableStatusChip(content)
        CommResV3StatusChipType.PickedUp -> PickedUpStatusChip(content)
    }
}

@Composable
private fun CommResV3MessageAvatar(name: String) {
    val label = name.firstOrNull()?.uppercase().orEmpty()
    Box(
        modifier = Modifier
            .size(SizeDefaults.fourfold)
            .clip(CircleShape)
            .background(AppTheme.colors.primary100),
        contentAlignment = Alignment.Center
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = AppTheme.typography.subtitle2,
                color = AppTheme.colors.primary700
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = AppTheme.colors.primary700
            )
        }
    }
}

@Composable
private fun OrderedStatusChip(content: String) =
    InfoChip(content = content, backgroundColor = AppTheme.colors.primary100, contentColor = AppTheme.colors.primary900)

@Composable
private fun WaitingStatusChip(content: String) =
    InfoChip(content = content, backgroundColor = AppTheme.colors.green100, contentColor = AppTheme.colors.green900)

@Composable
private fun ReadyTomorrowStatusChip(content: String) =
    InfoChip(content = content, backgroundColor = AppTheme.colors.green100, contentColor = AppTheme.colors.green900)

@Composable
private fun ReadyForPickupStatusChip(content: String, leadingIcon: ImageVector) =
    InfoChip(content = content, backgroundColor = AppTheme.colors.green100, contentColor = AppTheme.colors.green900, leadingIcon = leadingIcon)

@Composable
private fun NotAvailableStatusChip(content: String) =
    InfoChip(content = content, backgroundColor = AppTheme.colors.red100, contentColor = AppTheme.colors.red900)

@Composable
private fun PickedUpStatusChip(content: String) =
    InfoChip(
        content = content,
        backgroundColor = AppTheme.colors.neutral100,
        contentColor = AppTheme.colors.neutral900,
        leadingIcon = Icons.Rounded.Check
    )

@Composable
private fun CommResV3MetaChip(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(SizeDefaults.oneHalf))
            .background(backgroundColor)
            .padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.threeSeventyFifth),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.caption2,
            color = textColor
        )
    }
}

private enum class CommResV3StatusChipType {
    Pending,
    Ordered,
    ReadyTomorrow,
    ReadyForPickup,
    NotAvailable,
    PickedUp
}

private fun InAppMessage.showsOrderStatusChip(): Boolean =
    prescriptionsCount > 0 &&
        messageProfile in setOf(
        CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
        CommunicationErpModel.CommunicationProfile.ErxCommunicationReply
    )

private fun InAppMessage.statusChipType(isCompleted: Boolean): CommResV3StatusChipType {
    if (isCompleted || taskStatus == TaskStatusEnum.Completed || orderStatus == InAppMessageStatus.PICKED_UP) {
        return CommResV3StatusChipType.PickedUp
    }

    if (orderStatus == InAppMessageStatus.READY_TOMORROW ||
        orderStatus == InAppMessageStatus.READY_FOR_PICKUP ||
        orderStatus == InAppMessageStatus.NOT_AVAILABLE
    ) {
        return when (orderStatus) {
            InAppMessageStatus.NOT_AVAILABLE -> CommResV3StatusChipType.NotAvailable
            InAppMessageStatus.READY_TOMORROW -> CommResV3StatusChipType.ReadyTomorrow
            InAppMessageStatus.READY_FOR_PICKUP -> CommResV3StatusChipType.ReadyForPickup
            else -> CommResV3StatusChipType.Pending
        }
    }

    val content = lastMessage?.lastMessageDetails?.content.orEmpty()
    if (content.contains("unknown", ignoreCase = true) ||
        content.contains("abholinfo=unknown", ignoreCase = true)
    ) {
        return CommResV3StatusChipType.Pending
    }
    return when (orderStatus) {
        InAppMessageStatus.NOT_AVAILABLE -> CommResV3StatusChipType.NotAvailable
        InAppMessageStatus.READY_TOMORROW -> CommResV3StatusChipType.ReadyTomorrow
        InAppMessageStatus.READY_FOR_PICKUP -> CommResV3StatusChipType.ReadyForPickup
        InAppMessageStatus.PENDING -> CommResV3StatusChipType.Pending
        InAppMessageStatus.ORDERED -> CommResV3StatusChipType.Ordered
        InAppMessageStatus.PICKED_UP, null -> CommResV3StatusChipType.Ordered
    }
}

@Composable
private fun InAppMessage.orderStatusLabel(isCompleted: Boolean): String = when (statusChipType(isCompleted)) {
    CommResV3StatusChipType.Pending -> when (supplyOption) {
        CommunicationSupplyOptionTypeErpModel.DELIVERY,
        CommunicationSupplyOptionTypeErpModel.SHIPMENT -> stringResource(R.string.message_card_delivery_status_preparing)
        else -> stringResource(R.string.prescription_status_in_progress)
    }
    CommResV3StatusChipType.Ordered -> stringResource(R.string.prescription_status_ordered)
    CommResV3StatusChipType.ReadyTomorrow -> stringResource(R.string.messages_status_ready_tomorrow)
    CommResV3StatusChipType.ReadyForPickup -> when (supplyOption) {
        CommunicationSupplyOptionTypeErpModel.DELIVERY -> stringResource(R.string.prescription_status_on_the_way)
        CommunicationSupplyOptionTypeErpModel.SHIPMENT -> stringResource(R.string.prescription_status_shipped)
        else -> stringResource(R.string.prescription_status_ready_for_pickup)
    }
    CommResV3StatusChipType.NotAvailable -> when (supplyOption) {
        CommunicationSupplyOptionTypeErpModel.DELIVERY,
        CommunicationSupplyOptionTypeErpModel.SHIPMENT -> stringResource(R.string.prescription_status_delivery_delayed)
        else -> stringResource(R.string.prescription_status_not_deliverable)
    }
    CommResV3StatusChipType.PickedUp -> when (supplyOption) {
        CommunicationSupplyOptionTypeErpModel.DELIVERY,
        CommunicationSupplyOptionTypeErpModel.SHIPMENT -> stringResource(R.string.prescription_status_delivered)
        else -> stringResource(R.string.messages_status_picked_up)
    }
}

@Composable
private fun FilteredMessagesEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SizeDefaults.sixfold),
        contentAlignment = Alignment.Center
    ) {
        EmptyScreenComponent(
            title = stringResource(R.string.messages_filtered_empty_title),
            body = stringResource(R.string.messages_filtered_empty_subtitle),
            image = {},
            button = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreview
@Composable
fun CommResV3MessageListScreenPreview(
    @PreviewParameter(MessageListParameterProvider::class)
    ordersData: UiState<List<InAppMessage>>
) {
    PreviewTheme {
        CommResV3MessageListScreenScaffold(
            screenState = CommResV3MessageListScreenState(
                visibleMessages = ordersData,
                hasAnyMessages = ordersData.data?.isNotEmpty() == true,
                activeProfileName = "Erika Mustermann"
            ),
            listState = rememberLazyListState(),
            pullToRefreshState = rememberPullToRefreshState(),
            onClickRetry = {},
            onSearchValueChange = {},
            onSearchClear = {},
            onHideCompletedChange = {},
            onSelectionModeChange = {},
            onMessageCompletedChange = {},
            onOpenMessage = {},
            onCommitChanges = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreview
@Composable
fun CommResV3MessageListSelectionPreview(
    @PreviewParameter(MessageListParameterProvider::class)
    ordersData: UiState<List<InAppMessage>>
) {
    PreviewTheme {
        val firstKey = ordersData.data?.firstOrNull()?.archiveKey().orEmpty()
        CommResV3MessageListScreenScaffold(
            screenState = CommResV3MessageListScreenState(
                visibleMessages = ordersData,
                hasAnyMessages = ordersData.data?.isNotEmpty() == true,
                isSelectionMode = true,
                completedMessageKeys = setOf(firstKey)
            ),
            listState = rememberLazyListState(),
            pullToRefreshState = rememberPullToRefreshState(),
            onClickRetry = {},
            onSearchValueChange = {},
            onSearchClear = {},
            onHideCompletedChange = {},
            onSelectionModeChange = {},
            onMessageCompletedChange = {},
            onOpenMessage = {},
            onCommitChanges = {}
        )
    }
}
