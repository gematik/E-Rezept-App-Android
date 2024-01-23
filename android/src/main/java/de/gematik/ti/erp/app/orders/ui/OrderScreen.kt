/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackOrderPopUps
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenController
import de.gematik.ti.erp.app.mainscreen.ui.RefreshScaffold
import de.gematik.ti.erp.app.orders.usecase.OrderUseCase
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.ui.UserNotAuthenticatedDialog
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.ProfileHandler
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.timeDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.rememberInstance
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun OrderScreen(
    mainNavController: NavController,
    mainScreenController: MainScreenController,
    onElevateTopBar: (Boolean) -> Unit
) {
    val profileHandler = LocalProfileHandler.current

    var showUserNotAuthenticatedDialog by remember { mutableStateOf(false) }

    val onShowCardWall = {
        mainNavController.navigate(
            MainNavigationScreens.CardWall.path(profileHandler.activeProfile.id)
        )
    }
    if (showUserNotAuthenticatedDialog) {
        UserNotAuthenticatedDialog(
            onCancel = { showUserNotAuthenticatedDialog = false },
            onShowCardWall = onShowCardWall
        )
    }

    RefreshScaffold(
        profileId = profileHandler.activeProfile.id,
        onUserNotAuthenticated = { showUserNotAuthenticatedDialog = true },
        mainScreenController = mainScreenController,
        onShowCardWall = onShowCardWall
    ) { onRefresh ->
        Orders(
            profileHandler = profileHandler,
            onClickOrder = { orderId ->
                mainNavController.navigate(
                    MainNavigationScreens.Messages.path(orderId)
                )
            },
            onClickRefresh = {
                onRefresh(true, MutatePriority.UserInput)
            },
            onElevateTopBar = onElevateTopBar
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageScreen(
    orderId: String,
    mainNavController: NavController
) {
    val listState = rememberLazyListState()

    val state =
        rememberMessageState(orderId = orderId)

    val order by state.order

    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            analytics.trackOrderPopUps()
        } else {
            analytics.onPopUpClosed()
            val route = Uri.parse(mainNavController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
        }
    }

    val scope = rememberCoroutineScope()
    var selectedMessage: OrderUseCaseData.Message? by remember { mutableStateOf(null) }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            MessageSheetContent(
                order = order,
                message = selectedMessage,
                onClickClose = { scope.launch { sheetState.hide() } }
            )
        },
        sheetShape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) }
    ) {
        AnimatedElevationScaffold(
            modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
            topBarTitle = stringResource(R.string.orders_details_title),
            listState = listState,
            navigationMode = NavigationBarMode.Back,
            onBack = {
                scope.launch(Dispatchers.Main) {
                    state.consumeAllMessages()
                    mainNavController.popBackStack()
                }
            }
        ) {
            Messages(
                listState = listState,
                messageState = state,
                onClickMessage = {
                    selectedMessage = it
                    scope.launch { sheetState.show() }
                },
                onClickPrescription = {
                    mainNavController.navigate(
                        MainNavigationScreens.PrescriptionDetail.path(taskId = it)
                    )
                }
            )
        }
    }

    BackHandler {
        scope.launch {
            state.consumeAllMessages()
            mainNavController.popBackStack()
        }
    }
}

@Stable
class MessageState(
    orderId: String,
    private val orderUseCase: OrderUseCase,
    coroutineScope: CoroutineScope
) {
    enum class States {
        LoadingMessages,
        HasMessages,
        NoMessages
    }

    var state by mutableStateOf(States.LoadingMessages)
        private set

    private val messageFlow = orderUseCase
        .messages(orderId)
        .onEach {
            state = if (it.isEmpty()) {
                States.NoMessages
            } else {
                States.HasMessages
            }
        }
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)

    val messages
        @Composable
        get() = messageFlow
            .collectAsState(emptyList())

    private val orderFlow = orderUseCase
        .order(orderId)
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)

    val order
        @Composable
        get() = orderFlow
            .collectAsState(null)

    suspend fun consumeAllMessages() {
        withContext(NonCancellable) {
            orderFlow.first()?.let {
                if (it.hasUnreadMessages) {
                    orderUseCase.consumeOrder(it.orderId)
                    messageFlow.first().forEach {
                        orderUseCase.consumeCommunication(it.communicationId)
                    }
                }
            }
        }
    }
}

@Composable
fun rememberMessageState(
    orderId: String
): MessageState {
    val orderUseCase by rememberInstance<OrderUseCase>()
    val coroutineScope = rememberCoroutineScope()
    return remember(orderId) {
        MessageState(
            orderId = orderId,
            orderUseCase = orderUseCase,
            coroutineScope = coroutineScope
        )
    }
}

@Stable
class OrderState(
    profileIdentifier: ProfileIdentifier,
    orderUseCase: OrderUseCase
) {
    enum class States {
        LoadingOrders,
        HasOrders,
        NoOrders
    }

    var state by mutableStateOf(States.LoadingOrders)
        private set

    private val orderFlow = orderUseCase
        .orders(profileIdentifier)
        .onEach {
            state = if (it.isEmpty()) {
                States.NoOrders
            } else {
                States.HasOrders
            }
        }

    // keep; implementation follows
    val errorFlow = orderUseCase.pharmacyCacheError

    val orders
        @Composable
        get() = orderFlow.collectAsState(emptyList())
}

@Composable
fun rememberOrderState(
    profileIdentifier: ProfileIdentifier
): OrderState {
    val orderUseCase by rememberInstance<OrderUseCase>()
    return remember(profileIdentifier) {
        OrderState(profileIdentifier, orderUseCase)
    }
}

@Composable
private fun Orders(
    profileHandler: ProfileHandler,
    onClickOrder: (orderId: String) -> Unit,
    onClickRefresh: () -> Unit,
    onElevateTopBar: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val activeProfile = profileHandler.activeProfile
    val orderState = rememberOrderState(activeProfile.id)
    val orders by orderState.orders

    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }.collect {
            onElevateTopBar(it)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.Orders.Content),
        state = listState
    ) {
        when (orderState.state) {
            OrderState.States.LoadingOrders -> {
                // keep empty
                item {}
            }

            OrderState.States.HasOrders -> {
                orders.forEachIndexed { index, order ->
                    item {
                        val sentOn by timeDescription(order.sentOn)
                        Order(
                            pharmacy = order.pharmacy.pharmacyName(),
                            time = sentOn,
                            hasUnreadMessages = order.hasUnreadMessages,
                            nrOfPrescriptions = order.taskIds.size,
                            onClick = {
                                onClickOrder(order.orderId)
                            }
                        )
                        if (index < orders.size - 1) {
                            Divider(Modifier.padding(start = PaddingDefaults.Medium))
                        }
                    }
                }
            }

            OrderState.States.NoOrders -> {
                item {
                    val connectionState = profileHandler.connectionState(activeProfile)
                    OrderEmptyScreen(connectionState, onClickRefresh = onClickRefresh)
                }
            }
        }
    }
}

@Composable
fun Order(
    pharmacy: String,
    time: String,
    hasUnreadMessages: Boolean,
    nrOfPrescriptions: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
            .testTag(TestTag.Orders.OrderListItem),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(pharmacy, style = AppTheme.typography.subtitle1)
            SpacerTiny()
            Text(time, style = AppTheme.typography.body2l)
        }
        if (hasUnreadMessages) {
            NewLabel()
        } else {
            PrescriptionLabel(nrOfPrescriptions)
        }
        Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppTheme.colors.neutral400)
    }
}

@Composable
fun NewLabel() {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.primary100)
            .padding(horizontal = PaddingDefaults.Small, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.orders_label_new),
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.primary900
        )
    }
}

@Composable
fun PrescriptionLabel(count: Int) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.neutral100)
            .padding(horizontal = PaddingDefaults.Small, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            annotatedPluralsResource(
                R.plurals.orders_plurals_label_nr_of_prescriptions,
                count,
                AnnotatedString(count.toString())
            ),
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.neutral600
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Messages(
    listState: LazyListState,
    messageState: MessageState,
    onClickMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit
) {
    val order by messageState.order
    val messages by messageState.messages

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.Orders.Details.Content),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.orders_history_title),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerMedium()
        }

        when (messageState.state) {
            MessageState.States.HasMessages -> {
                messages.forEachIndexed { index, message ->
                    item {
                        ReplyMessage(
                            message = message,
                            isFirstMessage = index == 0,
                            onClick = {
                                onClickMessage(message)
                            }
                        )
                    }
                }
            }

            else -> {}
        }

        order?.let {
            item {
                DispenseMessage(
                    hasReplyMessages = messages.isNotEmpty(),
                    order = it
                )
                SpacerXXLarge()
            }
        }

        item {
            Divider(color = AppTheme.colors.neutral300)
            SpacerXXLarge()
            Text(
                stringResource(R.string.orders_cart_title),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
        }

        order?.let {
            item(key = "prescriptions") {
                Column(
                    Modifier.padding(PaddingDefaults.Medium),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    it.medicationNames.forEachIndexed { index, med ->
                        Surface(
                            modifier = Modifier
                                .testTag(TestTag.Orders.Details.PrescriptionListItem)
                                .semantics {
                                    prescriptionId = it.taskIds[index]
                                },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AppTheme.colors.neutral300),
                            color = AppTheme.colors.neutral050,
                            onClick = {
                                onClickPrescription(it.taskIds[index])
                            }
                        ) {
                            Row(
                                Modifier.padding(PaddingDefaults.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    med,
                                    style = AppTheme.typography.subtitle1,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                SpacerMedium()
                                Icon(
                                    Icons.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = AppTheme.colors.neutral400
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyMessage(
    message: OrderUseCaseData.Message,
    isFirstMessage: Boolean,
    onClick: () -> Unit
) {
    val info = when (message.type) {
        OrderUseCaseData.Message.Type.Link -> stringResource(R.string.orders_show_cart)
        OrderUseCaseData.Message.Type.Code -> stringResource(R.string.orders_show_code)
        OrderUseCaseData.Message.Type.Text -> null
        else -> stringResource(R.string.orders_show_general_message)
    }
    val description = when (message.type) {
        OrderUseCaseData.Message.Type.Text -> message.message ?: ""
        else -> null
    }

    val date = remember(message) {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        dateFormatter.format(message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    val time = remember(message) {
        val dateFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        dateFormatter.format(message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }

    Column(
        Modifier
            .drawConnectedLine(
                top = !isFirstMessage,
                bottom = true
            )
            .clickable(
                onClick = onClick,
                enabled = message.type != OrderUseCaseData.Message.Type.Text
            )
            .fillMaxWidth()
            .testTag(TestTag.Orders.Details.MessageListItem)
    ) {
        Row {
            Spacer(Modifier.width(48.dp))
            Column(
                Modifier
                    .weight(1f)
                    .padding(PaddingDefaults.Medium)
            ) {
                Text(
                    stringResource(R.string.orders_timestamp, date, time),
                    style = AppTheme.typography.subtitle2
                )
                description?.let {
                    SpacerTiny()
                    Text(
                        text = it,
                        style = AppTheme.typography.body2l
                    )
                }
                info?.let {
                    SpacerTiny()
                    val txt = buildAnnotatedString {
                        append(it)
                        append(" ")
                        appendInlineContent("button", "button")
                    }
                    val c = mapOf(
                        "button" to InlineTextContent(
                            Placeholder(
                                width = 0.em,
                                height = 0.em,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                            )
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppTheme.colors.primary600
                            )
                        }
                    )
                    DynamicText(
                        txt,
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.primary600,
                        inlineContent = c
                    )
                }
            }
        }
        Divider(Modifier.padding(start = 64.dp))
    }
}

@Composable
private fun DispenseMessage(
    order: OrderUseCaseData.Order,
    hasReplyMessages: Boolean
) {
    val date = remember(order) {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        dateFormatter.format(order.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    val time = remember(order) {
        val dateFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        dateFormatter.format(order.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    Row(
        Modifier.drawConnectedLine(
            top = true,
            bottom = false,
            topDashed = !hasReplyMessages
        )
    ) {
        Spacer(Modifier.width(48.dp))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            Text(
                stringResource(R.string.orders_timestamp, date, time),
                style = AppTheme.typography.subtitle2
            )
            SpacerTiny()
            val highlightedPharmacyName = buildAnnotatedString {
                withStyle(SpanStyle(color = AppTheme.colors.primary600)) {
                    append(order.pharmacy.pharmacyName())
                }
            }
            Text(
                text = annotatedStringResource(R.string.orders_prescription_sent_to, highlightedPharmacyName),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
fun OrderUseCaseData.Pharmacy.pharmacyName() =
    name.ifBlank {
        stringResource(R.string.orders_generic_pharmacy_name)
    }

private fun Modifier.drawConnectedLine(
    top: Boolean,
    bottom: Boolean,
    topDashed: Boolean = false
) = composed {
    val color = AppTheme.colors.neutral300
    val background = AppTheme.colors.neutral000

    drawBehind {
        val center = Offset(x = 24.dp.toPx(), y = center.y)
        val start = if (top) {
            Offset(x = center.x, y = 0f)
        } else {
            center
        }
        val end = if (bottom) {
            Offset(x = center.x, y = size.height)
        } else {
            center
        }
        drawLine(
            color = color,
            strokeWidth = 2.dp.toPx(),
            start = start,
            end = end,
            pathEffect = if (topDashed) PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 2.dp.toPx())) else null
        )
        drawCircle(color = color, center = center, radius = 8.dp.toPx())
        drawCircle(color = background, center = center, radius = 3.dp.toPx())
    }
}
