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

import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.button.GemIconButtonDefaults
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationAvailabilityResponseErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestReservationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.InTransportETA
import de.gematik.ti.erp.app.communication.model.payload.InTransportPosition
import de.gematik.ti.erp.app.communication.model.payload.PaymentMethod
import de.gematik.ti.erp.app.core.ProvideFakeCacheForPreview
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.database.datastore.featuretoggle.COMM_RES_V3_SEND_MESSAGE
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mapper.dispenseSupplyOption
import de.gematik.ti.erp.app.messages.mapper.pickupMessageHint
import de.gematik.ti.erp.app.messages.mapper.toCommResV3OrderModel
import de.gematik.ti.erp.app.messages.mapper.userVisibleText
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutesBackStackEntryArguments
import de.gematik.ti.erp.app.messages.presentation.rememberOrderMessageDetailController
import de.gematik.ti.erp.app.messages.ui.components.DeliveryStatusMessageCard
import de.gematik.ti.erp.app.messages.ui.components.EmptyMessageCard
import de.gematik.ti.erp.app.messages.ui.components.ImmediatlyReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.components.InvoiceMessageCard
import de.gematik.ti.erp.app.messages.ui.components.LinkMessageCard
import de.gematik.ti.erp.app.messages.ui.components.MessageDetailContent
import de.gematik.ti.erp.app.messages.ui.components.MessageDetailDropdownMenu
import de.gematik.ti.erp.app.messages.ui.components.NextDayAMReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.components.NextDayPMReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.components.NextDayReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.components.NotAvailableReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.components.OrderedDeliveryMessageCard
import de.gematik.ti.erp.app.messages.ui.components.OrderedReservationMessageCard
import de.gematik.ti.erp.app.messages.ui.components.OrderedShippingMessageCard
import de.gematik.ti.erp.app.messages.ui.components.PaymentInfoMessageCard
import de.gematik.ti.erp.app.messages.ui.components.PickUpCodeMessageCard
import de.gematik.ti.erp.app.messages.ui.components.ReceivedMessageCard
import de.gematik.ti.erp.app.messages.ui.components.SameDayReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.components.SentMessageCard
import de.gematik.ti.erp.app.messages.ui.components.TranslateChip
import de.gematik.ti.erp.app.messages.ui.components.UnknownReservationStateMessageCard
import de.gematik.ti.erp.app.messages.ui.preview.MessageOrderDetailPreviewParameterProvider
import de.gematik.ti.erp.app.messages.ui.preview.OrderMessageDetail
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.isOpenAt
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.translation.navigation.TranslationRoutes
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.gotoCoordinates
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import de.gematik.ti.erp.app.utils.letNotNull
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.rememberInstance
import java.util.Locale
import kotlin.time.Duration

class CommResV3OrderMessageDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val uiScope = uiScope
        val view = accessibilityView
        val snackbarScaffold = LocalSnackbarScaffold.current
        val snackbarOk = stringResource(R.string.ok)

        val arguments = MessagesRoutesBackStackEntryArguments(navBackStackEntry)
        val messageController = rememberOrderMessageDetailController(
            orderId = arguments.orderId
        )

        LaunchedEffect(Unit) { messageController.init() }

        var selectedMessage by remember { mutableStateOf<OrderUseCaseData.Message?>(null) }

        val order by messageController.order.collectAsStateWithLifecycle()
        val messages by messageController.messages.collectAsStateWithLifecycle()
        val hasReplyMessages by messageController.hasReplyMessages.collectAsStateWithLifecycle()
        val profileData by messageController.profile.collectAsStateWithLifecycle()
        val pharmacyState by messageController.pharmacy.collectAsStateWithLifecycle()
        val isTranslationInProgress by messageController.translationInProgress.collectAsStateWithLifecycle()
        val isTranslationsAllowed by messageController.isTranslationsAllowed.collectAsStateWithLifecycle()
        val showTranslationFeature by messageController.showTranslationFeature.collectAsStateWithLifecycle()
        val draftReplyMessage by messageController.draftReplyMessage.collectAsStateWithLifecycle()
        val sentReplyMessages by messageController.sentReplyMessages.collectAsStateWithLifecycle()
        val isSendingReply by messageController.isSendingReply.collectAsStateWithLifecycle()
        val isFeatureToggleEnabledUseCase by rememberInstance<IsFeatureToggleEnabledUseCase>()
        val isSendMessageEnabled by remember(isFeatureToggleEnabledUseCase) {
            isFeatureToggleEnabledUseCase(COMM_RES_V3_SEND_MESSAGE)
        }.collectAsStateWithLifecycle(initialValue = false)
        val sendSuccessMessage = stringResource(R.string.server_return_code_200_title)
        val sendFailureMessage = stringResource(R.string.server_return_code_title_failure)

        val handleTranslationClick: (String, String) -> Unit = remember(isTranslationsAllowed, snackbarScaffold, uiScope, view) {
            { communicationId, message ->
                if (isTranslationsAllowed) {
                    messageController.translateText(communicationId, message) { translatedText ->
                        view?.announceForAccessibility(translatedText)
                        snackbarScaffold.showWithDismissButton(
                            message = translatedText,
                            actionLabel = snackbarOk,
                            scope = uiScope
                        )
                    }
                } else {
                    navController.navigate(
                        TranslationRoutes.TranslationConsentBottomSheetScreen.path()
                    )
                }
            }
        }

        val onClickReplyMessage: (OrderUseCaseData.Message) -> Unit = remember(order) {
            { message ->
                selectedMessage = message
                letNotNull(order.data, selectedMessage) { orderDetail, selected ->
                    navController.navigate(
                        MessagesRoutes.MessageBottomSheetScreen.path(
                            orderDetail = orderDetail,
                            selectedMessage = selected
                        )
                    )
                }
            }
        }

        val onClickPrescription: (String) -> Unit = remember {
            { taskId ->
                navController.navigate(
                    PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId = taskId)
                )
            }
        }

        val onClickInvoiceMessage: (String) -> Unit = remember(profileData) {
            { taskId ->
                profileData?.let { profile ->
                    navController.navigate(
                        PkvRoutes.InvoiceDetailsScreen.path(taskId = taskId, profileId = profile.id)
                    )
                }
            }
        }

        val onClickPharmacy: () -> Unit = remember(pharmacyState) {
            {
                pharmacyState.data?.let { pharmacy ->
                    navController.navigate(
                        PharmacyRoutes.PharmacyDetailsFromMessageScreen.path(
                            pharmacy = pharmacy,
                            taskId = pharmacy.telematikId
                        )
                    )
                }
            }
        }

        LaunchedEffect(order.isLoading, messages.isLoading) {
            if (!order.isLoading && !messages.isLoading) {
                messageController.consumeAllMessages()
            }
        }

        val onToggleTranslationConsent = remember { messageController::toggleTranslationConsentUseCase }
        val onReplyMessageChange = remember { messageController::updateReplyMessage }
        val onSendReplyMessage: () -> Unit = remember(
            messageController,
            snackbarScaffold,
            snackbarOk,
            sendSuccessMessage,
            sendFailureMessage,
            uiScope
        ) {
            {
                messageController.sendReplyMessage(
                    onSuccess = {
                        snackbarScaffold.showWithDismissButton(
                            message = sendSuccessMessage,
                            actionLabel = snackbarOk,
                            scope = uiScope
                        )
                    },
                    onError = {
                        snackbarScaffold.showWithDismissButton(
                            message = sendFailureMessage,
                            actionLabel = snackbarOk,
                            scope = uiScope
                        )
                    }
                )
            }
        }

        val onBack: () -> Unit = remember(navController) {
            { navController.popBackStack() }
        }

        CommResV3OrderMessageDetailScreenScaffold(
            listState = listState,
            hasReplyMessages = hasReplyMessages,
            order = order,
            messages = messages.data ?: emptyList(),
            pharmacyDetails = pharmacyState.data,
            isTranslationsAllowed = isTranslationsAllowed,
            showTranslationFeature = showTranslationFeature,
            isTranslationInProgress = isTranslationInProgress,
            draftReplyMessage = draftReplyMessage,
            sentReplyMessages = sentReplyMessages,
            isSendingReply = isSendingReply,
            isSendMessageEnabled = isSendMessageEnabled,
            onClickTranslation = handleTranslationClick,
            onClickReplyMessage = onClickReplyMessage,
            onClickPrescription = onClickPrescription,
            onClickInvoiceMessage = onClickInvoiceMessage,
            onClickPharmacy = onClickPharmacy,
            onReplyMessageChange = onReplyMessageChange,
            onSendReplyMessage = onSendReplyMessage,
            onToggleTranslationConsent = onToggleTranslationConsent,
            onBack = onBack
        )

        BackHandler { onBack() }
    }
}

@Composable
fun CommResV3OrderMessageDetailScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    order: UiState<OrderUseCaseData.OrderDetail>,
    messages: List<OrderUseCaseData.Message>,
    pharmacyDetails: PharmacyDetailsErpModel? = null,
    hasReplyMessages: Boolean,
    isTranslationsAllowed: Boolean,
    showTranslationFeature: Boolean,
    isTranslationInProgress: Map<String, Boolean>,
    draftReplyMessage: String,
    sentReplyMessages: List<CommunicationErpModel>,
    isSendingReply: Boolean,
    isSendMessageEnabled: Boolean = false,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickPharmacy: () -> Unit,
    onReplyMessageChange: (String) -> Unit,
    onSendReplyMessage: () -> Unit,
    onToggleTranslationConsent: () -> Unit = {},
    onClickTranslation: (String, String) -> Unit = { _, _ -> }
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        topBarTitle = {
            CommResV3OrderTopBarTitle(
                pharmacyName = order.data?.pharmacy?.name ?: stringResource(R.string.messages_title),
                isOpenNow = pharmacyDetails?.openingHours?.isOpenAt(
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )
            )
        },
        listState = listState,
        actions = {
            if (hasReplyMessages && showTranslationFeature) {
                MessageDetailDropdownMenu(
                    isTranslationAllowed = isTranslationsAllowed
                ) {
                    onToggleTranslationConsent()
                }
            }
        },
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        topBarPadding = PaddingValues(),
        bottomBar = {
            if (isSendMessageEnabled) {
                CommResV3ReplyComposer(
                    replyMessage = draftReplyMessage,
                    isSendingReply = isSendingReply,
                    enabled = order.data != null,
                    onReplyMessageChange = onReplyMessageChange,
                    onSendReplyMessage = onSendReplyMessage
                )
            }
        }
    ) { innerPadding ->
        val orderModel = remember(order.data, messages, sentReplyMessages) {
            order.data?.toCommResV3OrderModel(messages, sentReplyMessages)
        }

        if (orderModel != null) {
            CommResV3OrderMessageDetailContent(
                listState = listState,
                innerPadding = innerPadding,
                order = orderModel,
                pharmacyDetails = pharmacyDetails,
                showTranslationFeature = showTranslationFeature,
                isTranslationsAllowed = isTranslationsAllowed,
                isTranslationInProgress = isTranslationInProgress,
                onClickReplyMessage = onClickReplyMessage,
                onClickPrescription = onClickPrescription,
                onClickInvoiceMessage = onClickInvoiceMessage,
                onClickPharmacy = onClickPharmacy,
                onClickTranslation = onClickTranslation
            )
        } else {
            MessageDetailContent(
                listState = listState,
                innerPadding = innerPadding,
                order = order,
                messages = messages,
                isTranslationsAllowed = isTranslationsAllowed,
                showTranslationFeature = showTranslationFeature,
                isTranslationInProgress = isTranslationInProgress,
                onClickReplyMessage = onClickReplyMessage,
                onClickPrescription = onClickPrescription,
                onClickInvoiceMessage = onClickInvoiceMessage,
                onClickPharmacy = onClickPharmacy,
                onClickTranslation = onClickTranslation
            )
        }
    }
}

@Composable
private fun CommResV3ReplyComposer(
    replyMessage: String,
    isSendingReply: Boolean,
    enabled: Boolean,
    onReplyMessageChange: (String) -> Unit,
    onSendReplyMessage: () -> Unit
) {
    val canSend = enabled && !isSendingReply && replyMessage.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Small)
    ) {
        Divider(color = AppTheme.colors.neutral300)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PaddingDefaults.Small)
                .windowInsetsPadding(WindowInsets.ime),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .testTag(TestTag.Orders.Details.ReplyMessageInput),
                value = replyMessage,
                onValueChange = onReplyMessageChange,
                placeholder = {
                    Text(text = stringResource(R.string.message_reply_input_placeholder))
                },
                singleLine = true,
                enabled = enabled && !isSendingReply,
                shape = RoundedCornerShape(SizeDefaults.twelvefold),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (canSend) {
                            onSendReplyMessage()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppTheme.colors.neutral000,
                    unfocusedContainerColor = AppTheme.colors.neutral000,
                    disabledContainerColor = AppTheme.colors.neutral000
                )
            )
            IconButton(
                onClick = onSendReplyMessage,
                enabled = canSend,
                colors = GemIconButtonDefaults.gemPrimaryIconButtonColors(),
                modifier = Modifier
                    .size(SizeDefaults.sixfold)
                    .testTag(TestTag.Orders.Details.ReplyMessageSendButton)
            ) {
                if (isSendingReply) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(SizeDefaults.triple),
                        strokeWidth = SizeDefaults.half,
                        color = AppTheme.colors.neutral000
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(R.string.pharmacy_order_send),
                        tint = AppTheme.colors.neutral000
                    )
                }
            }
        }
    }
}

@Composable
private fun CommResV3OrderTopBarTitle(
    pharmacyName: String,
    isOpenNow: Boolean?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.message_from_pharmacie),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(SizeDefaults.fourfold)
        )
        Column(
            modifier = Modifier
                .padding(start = PaddingDefaults.Small)
                .weight(1f)
        ) {
            Text(
                text = pharmacyName,
                style = AppTheme.typography.h6,
                color = AppTheme.colors.neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isOpenNow != null) {
                Text(
                    text = stringResource(
                        if (isOpenNow) {
                            R.string.search_pharmacies_filter_open_now
                        } else {
                            R.string.search_pharmacy_closed
                        }
                    ),
                    style = AppTheme.typography.caption1,
                    color = if (isOpenNow) AppTheme.colors.green700 else AppTheme.colors.neutral700
                )
            }
        }
    }
}

@Composable
private fun CommResV3OrderMessageDetailContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    order: OrderUseCaseData.OrderErpModel,
    pharmacyDetails: PharmacyDetailsErpModel?,
    showTranslationFeature: Boolean,
    isTranslationsAllowed: Boolean,
    isTranslationInProgress: Map<String, Boolean>,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickPharmacy: () -> Unit,
    onClickTranslation: (String, String) -> Unit
) {
    val firstUnreadCardIndex = remember(order.cards) {
        order.cards.indexOfFirst { !it.hasSeen }
    }

    val supplyOption = remember(order.cards) {
        order.cards.firstNotNullOfOrNull {
            (it as? OrderUseCaseData.OrderCardErpModel.DispenseRequestCard)?.payload?.dispenseSupplyOption()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.neutral100)
            .padding(innerPadding)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = PaddingDefaults.Medium,
                vertical = PaddingDefaults.Large
            ),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)
        ) {
            itemsIndexed(
                items = order.cards,
                key = { index, card -> "${card::class.simpleName}-$index-${card.sentOn}" }
            ) { index, card ->
                Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)) {
                    if (firstUnreadCardIndex > 0 && firstUnreadCardIndex == index) {
                        CommResV3UnreadDivider()
                    }
                    CommResV3OrderCardItem(
                        card = card,
                        pharmacyName = order.pharmacy.name,
                        pharmacyDetails = pharmacyDetails,
                        supplyOption = supplyOption,
                        showTranslationFeature = showTranslationFeature,
                        isTranslationsAllowed = isTranslationsAllowed,
                        isTranslationInProgress = isTranslationInProgress,
                        onClickReplyMessage = onClickReplyMessage,
                        onClickPrescription = onClickPrescription,
                        onClickInvoiceMessage = onClickInvoiceMessage,
                        onClickPharmacy = onClickPharmacy,
                        onClickTranslation = onClickTranslation
                    )
                }
            }
        }
    }
}

@Composable
private fun CommResV3UnreadDivider() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.primary300
        )
        Text(
            text = stringResource(R.string.orders_label_new),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.primary700,
            modifier = Modifier
                .background(AppTheme.colors.neutral100)
                .padding(horizontal = PaddingDefaults.Small)
        )
    }
}

@Composable
private fun CommResV3OrderCardItem(
    card: OrderUseCaseData.OrderCardErpModel,
    pharmacyName: String,
    pharmacyDetails: PharmacyDetailsErpModel?,
    supplyOption: CommunicationSupplyOptionTypeErpModel?,
    showTranslationFeature: Boolean,
    isTranslationsAllowed: Boolean,
    isTranslationInProgress: Map<String, Boolean>,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickPharmacy: () -> Unit,
    onClickTranslation: (String, String) -> Unit
) {
    val formatter = rememberErpTimeFormatter()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    when (card) {
        is OrderUseCaseData.OrderCardErpModel.DispenseRequestCard -> {
            when (card.payload.dispenseSupplyOption()) {
                CommunicationSupplyOptionTypeErpModel.DELIVERY -> OrderedDeliveryMessageCard(
                    date = formatter.date(card.sentOn),
                    pharmacyName = card.pharmacy.name,
                    prescriptions = card.tasks,
                    onClickPrescription = onClickPrescription,
                    onClickPharmacy = onClickPharmacy
                )

                CommunicationSupplyOptionTypeErpModel.SHIPMENT -> OrderedShippingMessageCard(
                    date = formatter.date(card.sentOn),
                    pharmacyName = card.pharmacy.name,
                    prescriptions = card.tasks,
                    onClickPrescription = onClickPrescription,
                    onClickPharmacy = onClickPharmacy
                )

                else -> OrderedReservationMessageCard(
                    date = formatter.date(card.sentOn),
                    pharmacyName = card.pharmacy.name,
                    prescriptions = card.tasks,
                    onClickPrescription = onClickPrescription,
                    onClickPharmacy = onClickPharmacy
                )
            }
        }

        is OrderUseCaseData.OrderCardErpModel.UserReplyCard -> {
            card.payload.userVisibleText()?.let { message ->
                SentMessageCard(
                    time = formatter.time(card.sentOn),
                    message = message
                )
            }
        }

        is OrderUseCaseData.OrderCardErpModel.PharmacyReplyCard -> {
            var pickupCardRendered = false
            Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)) {
                if (card.message.type == OrderUseCaseData.Message.Type.Empty) {
                    EmptyMessageCard(
                        time = formatter.time(card.sentOn)
                    )
                } else {
                    card.payloads.forEach { payload ->
                        when (payload) {
                            is CommunicationReplyTextPayloadErpModel -> {
                                ReceivedMessageCard(
                                    time = formatter.time(card.sentOn),
                                    sender = pharmacyName,
                                    message = payload.text
                                )
                                if (showTranslationFeature) {
                                    TranslateChip(
                                        isEnabled = isTranslationsAllowed,
                                        isTranslationInProgress = isTranslationInProgress[card.message.communicationId] == true
                                    ) {
                                        onClickTranslation(card.message.communicationId, payload.text)
                                    }
                                }
                            }

                            is CommunicationReplyLinkPayloadErpModel -> {
                                LinkMessageCard(
                                    time = formatter.time(card.sentOn),
                                    message = payload.text.takeUnless { it.isBlank() },
                                    onClickOpenLink = { onClickReplyMessage(card.message) }
                                )
                            }

                            is CommunicationReplyPickupCodeDMCPayloadErpModel,
                            is CommunicationReplyPickupCodeHRPayloadErpModel -> {
                                if (!pickupCardRendered) {
                                    pickupCardRendered = true
                                    PickUpCodeMessageCard(
                                        time = formatter.time(card.sentOn),
                                        message = payload.pickupMessageHint(),
                                        onClickOpenPickUpCode = { onClickReplyMessage(card.message) }
                                    )
                                }
                            }

                            is CommunicationReplyReservationStatusPayloadErpModel -> {
                                when (payload.readyForCollection) {
                                    CommunicationAvailabilityResponseErpModel.Immediately ->
                                        ImmediatlyReservationStateMessageCard(formatter.time(card.sentOn))

                                    CommunicationAvailabilityResponseErpModel.SameDay ->
                                        SameDayReservationStateMessageCard(formatter.time(card.sentOn))

                                    CommunicationAvailabilityResponseErpModel.NextDay ->
                                        NextDayReservationStateMessageCard(formatter.time(card.sentOn))

                                    CommunicationAvailabilityResponseErpModel.NextDayAM ->
                                        NextDayAMReservationStateMessageCard(formatter.time(card.sentOn))

                                    CommunicationAvailabilityResponseErpModel.NextDayPM ->
                                        NextDayPMReservationStateMessageCard(formatter.time(card.sentOn))

                                    CommunicationAvailabilityResponseErpModel.Unknown ->
                                        UnknownReservationStateMessageCard(formatter.time(card.sentOn))

                                    CommunicationAvailabilityResponseErpModel.NotAvailable ->
                                        NotAvailableReservationStateMessageCard(formatter.time(card.sentOn))
                                }
                            }

                            is CommunicationReplyPaymentInfoPayloadErpModel -> {
                                PaymentInfoMessageCard(
                                    time = formatter.time(card.sentOn),
                                    payload = payload,
                                    onClickPaymentMethod = uriHandler::openUriWhenValid
                                )
                            }

                            is CommunicationReplyDeliveryStatusPayloadErpModel -> {
                                val isPickup = supplyOption == CommunicationSupplyOptionTypeErpModel.ON_PREMISE
                                if (payload.deliveryStatus == CommunicationDeliveryStatusErpModel.Incident && isPickup) {
                                    NotAvailableReservationStateMessageCard(
                                        time = formatter.time(card.sentOn)
                                    )
                                } else {
                                    DeliveryStatusMessageCard(
                                        time = formatter.time(card.sentOn),
                                        payload = payload,
                                        supplyOption = supplyOption,
                                        phoneNumber = pharmacyDetails?.contact?.phone?.takeUnless { it.isBlank() },
                                        mailAddress = pharmacyDetails?.contact?.mail?.takeUnless { it.isBlank() },
                                        isPharmacyOpen = pharmacyDetails?.openingHours?.isOpenAt(
                                            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                        ),
                                        pharmacyCoordinates = pharmacyDetails?.coordinates,
                                        onClickPhone = { uriHandler.openUriWhenValid("tel:$it") },
                                        onClickMail = { uriHandler.openUriWhenValid("mailto:$it") },
                                        onClickLocation = { coordinates -> context.gotoCoordinates(coordinates) }
                                    )
                                }
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }

        is OrderUseCaseData.OrderCardErpModel.InvoiceCard -> {
            val taskId = card.tasks.firstOrNull()?.taskId
            if (taskId != null) {
                InvoiceMessageCard(
                    time = formatter.time(card.sentOn)
                ) {
                    onClickInvoiceMessage(taskId)
                }
            }
        }
    }
}

@LightDarkLongPreview
@Composable
fun CommResV3OrderMessageDetailScreenPreview(
    @PreviewParameter(MessageOrderDetailPreviewParameterProvider::class)
    orderMessageDetail: UiState<List<OrderMessageDetail>>
) {
    val fakeConfig = Configuration().apply {
        setLocales(
            LocaleList(Locale.ENGLISH)
        )
    }
    CompositionLocalProvider(LocalConfiguration provides fakeConfig) {
        PreviewTheme {
            ProvideFakeCacheForPreview()
            CommResV3OrderMessageDetailScreenScaffold(
                listState = rememberLazyListState(),
                onBack = {},
                order = orderMessageDetail.data
                    ?.firstOrNull()
                    ?.orderDetail
                    ?.let { UiState.Data(it) }
                    ?: UiState.Empty(),
                messages = orderMessageDetail.data?.map { it.message } ?: emptyList(),
                isTranslationsAllowed = true,
                isTranslationInProgress = mapOf("123" to true),
                draftReplyMessage = "Nachricht eingeben",
                sentReplyMessages = emptyList(),
                isSendingReply = false,
                isSendMessageEnabled = true,
                onClickReplyMessage = {},
                onClickPrescription = {},
                onClickInvoiceMessage = {},
                onClickPharmacy = {},
                onReplyMessageChange = {},
                onSendReplyMessage = {},
                showTranslationFeature = true,
                hasReplyMessages = true
            )
        }
    }
}

@LightDarkLongPreview
@Composable
fun CommResV3FullTimelinePreview() {
    val time = Instant.parse("2023-06-14T10:15:30Z")
    val pharmacy = OrderUseCaseData.Pharmacy("123", "Apotheke am Rathaus")
    val prescription = TaskErpModel.Scanned(
        profileId = "testProfileId",
        taskId = "123",
        name = "Ibuprofen 400mg",
        redeemedOn = time,
        scannedOn = time,
        index = 1,
        accessCode = "accessCode",
        isEuRedeemable = false
    )

    val sampleCards = listOf(
        OrderUseCaseData.OrderCardErpModel.DispenseRequestCard(
            sentOn = time,
            pharmacy = pharmacy,
            tasks = listOf(prescription),
            payload = DispenseRequestReservationPayloadErpModel(
                version = 3,
                communicationType = CommunicationTypeErpModel.Order,
                supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
                transactionID = "tx-1",
                phone = "030123456",
                text = "Bitte für mich reservieren."
            ),
            hasSeen = true
        ),
        OrderUseCaseData.OrderCardErpModel.UserReplyCard(
            sentOn = time.plus(Duration.parse("1m")),
            payload = DispenseRequestReservationPayloadErpModel(
                version = 3,
                communicationType = CommunicationTypeErpModel.Order,
                supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
                transactionID = "tx-1",
                phone = "030123456",
                text = "Bitte für mich reservieren."
            ),
            hasSeen = true
        ),
        OrderUseCaseData.OrderCardErpModel.PharmacyReplyCard(
            message = OrderUseCaseData.Message(
                communicationId = "m1",
                sentOn = time.plus(Duration.parse("5m")),
                content = "Hallo! Ihre Medikamente sind abholbereit.",
                pickUpCodeDMC = null,
                pickUpCodeHR = null,
                link = null,
                consumed = true,
                prescriptions = listOf(prescription)
            ),
            payloads = listOf(
                CommunicationReplyTextPayloadErpModel(
                    version = 3,
                    communicationType = CommunicationTypeErpModel.Text,
                    transactionID = "tx-2",
                    text = "Hallo! Ihre Medikamente sind abholbereit."
                ),
                CommunicationReplyReservationStatusPayloadErpModel(
                    version = 3,
                    communicationType = CommunicationTypeErpModel.ReservationStatus,
                    transactionID = "tx-3",
                    readyForCollection = CommunicationAvailabilityResponseErpModel.Immediately
                ),
                CommunicationReplyPickupCodeHRPayloadErpModel(
                    version = 3,
                    communicationType = CommunicationTypeErpModel.PickUpCodeHR,
                    transactionID = "tx-4",
                    pickUpCode = "1234 5678",
                    text = "Abholcode"
                ),
                CommunicationReplyDeliveryStatusPayloadErpModel(
                    version = 3,
                    communicationType = CommunicationTypeErpModel.DeliveryStatus,
                    transactionID = "tx-5",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.InTransport,
                    inTransportETA = InTransportETA(
                        from = 1735736400,
                        to = 1735741800
                    ),
                    inTransportPosition = InTransportPosition(
                        latitude = 52.522529939635795,
                        longitude = 13.387595793605172
                    ),
                    text = "Ihr Bote ist unterwegs."
                ),
                CommunicationReplyPaymentInfoPayloadErpModel(
                    version = 3,
                    communicationType = CommunicationTypeErpModel.PaymentInfo,
                    transactionID = "tx-6",
                    totalAmount = 1250.0,
                    paymentMethods = listOf(
                        PaymentMethod(
                            paymentMethod = "paypal",
                            url = "https://paypal.com"
                        )
                    ),
                    text = "Bitte zahlen Sie bequem vorab online."
                )
            ),
            sentOn = time.plus(Duration.parse("5m")),
            hasSeen = true
        ),
        OrderUseCaseData.OrderCardErpModel.InvoiceCard(
            sentOn = time.plus(Duration.parse("10m")),
            invoiceInfo = OrderUseCaseData.InvoiceInfo(
                hasInvoice = true,
                invoiceSentOn = time.plus(Duration.parse("10m")),
                medicationName = "Ibuprofen 400mg",
                consumed = true
            ),
            tasks = listOf(prescription),
            hasSeen = true
        )
    )

    val orderErpModel = OrderUseCaseData.OrderErpModel(
        orderId = "order-123",
        tasks = listOf(prescription),
        pharmacy = pharmacy,
        cards = sampleCards
    )

    val fakeConfig = Configuration().apply {
        setLocales(LocaleList(Locale.ENGLISH))
    }
    CompositionLocalProvider(LocalConfiguration provides fakeConfig) {
        PreviewTheme {
            ProvideFakeCacheForPreview()
            CommResV3OrderMessageDetailContent(
                listState = rememberLazyListState(),
                innerPadding = PaddingValues(),
                order = orderErpModel,
                pharmacyDetails = null,
                showTranslationFeature = true,
                isTranslationsAllowed = true,
                isTranslationInProgress = emptyMap(),
                onClickReplyMessage = {},
                onClickPrescription = {},
                onClickInvoiceMessage = {},
                onClickPharmacy = {},
                onClickTranslation = { _, _ -> }
            )
        }
    }
}
