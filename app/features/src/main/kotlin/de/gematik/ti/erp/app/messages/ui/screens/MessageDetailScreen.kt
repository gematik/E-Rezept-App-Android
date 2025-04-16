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

package de.gematik.ti.erp.app.messages.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutesBackStackEntryArguments
import de.gematik.ti.erp.app.messages.presentation.rememberMessageDetailController
import de.gematik.ti.erp.app.messages.ui.components.Messages
import de.gematik.ti.erp.app.messages.ui.preview.MessageDetailInAppPreviewParameterProvider
import de.gematik.ti.erp.app.messages.ui.preview.MessageOrderDetailPreviewParameterProvider
import de.gematik.ti.erp.app.messages.ui.preview.OrderMessageDetail
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.letNotNull
import de.gematik.ti.erp.app.utils.uistate.UiState

class MessageDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()

        val messageController = rememberMessageDetailController(
            orderId = MessagesRoutesBackStackEntryArguments(navBackStackEntry).orderId(),

            isLocalMessage = MessagesRoutesBackStackEntryArguments(navBackStackEntry).isLocalMessage()
        )

        LaunchedEffect(Unit) {
            messageController.init()
        }
        var selectedMessage by remember { mutableStateOf<OrderUseCaseData.Message?>(null) }

        val order by messageController.order.collectAsStateWithLifecycle()
        val messages by messageController.messages.collectAsStateWithLifecycle()
        val profileData by messageController.profile.collectAsStateWithLifecycle()
        val pharmacyState by messageController.pharmacy.collectAsStateWithLifecycle()
        val inAppMessages by messageController.localMessages.collectAsStateWithLifecycle()

        MessageDetailScreenScaffold(
            listState = listState,
            onBack = {
                messageController.consumeAllMessages { navController.popBackStack() }
            },
            onClickReplyMessage = { message ->
                selectedMessage = message
                letNotNull(order.data, selectedMessage) { orderDetail, selectedMessage ->
                    navController.navigate(
                        MessagesRoutes.MessageBottomSheetScreen.path(
                            orderDetail = orderDetail,
                            selectedMessage = selectedMessage
                        )
                    )
                }
            },
            onClickPrescription = { taskId ->
                navController.navigate(
                    PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId = taskId)
                )
            },
            onClickInvoiceMessage = { taskId ->
                profileData?.let { profile ->
                    navController.navigate(
                        PkvRoutes.InvoiceDetailsScreen
                            .path(
                                taskId = taskId,
                                profileId = profile.id
                            )
                    )
                }
            },
            onClickPharmacy = {
                pharmacyState.data?.let { pharmacy ->
                    navController.navigate(
                        PharmacyRoutes.PharmacyDetailsFromMessageScreen.path(
                            pharmacy = pharmacy,
                            taskId = pharmacy.telematikId
                        )
                    )
                }
            },
            order = order,
            messages = messages.data ?: emptyList(),
            inAppMessage = inAppMessages,
            isLocalMessage = MessagesRoutesBackStackEntryArguments(navBackStackEntry).isLocalMessage()
        )

        BackHandler {
            messageController.consumeAllMessages { navController.popBackStack() }
        }
    }
}

@Composable
fun MessageDetailScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickPharmacy: () -> Unit,
    order: UiState<OrderUseCaseData.OrderDetail>,
    messages: List<OrderUseCaseData.Message>,
    inAppMessage: List<InAppMessage>,
    isLocalMessage: Boolean
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
        topBarTitle = when {
            isLocalMessage -> stringResource(R.string.internal_message_from)
            else -> order.data?.pharmacy?.name ?: stringResource(R.string.messages_title)
        },
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        topBarPadding = PaddingValues(end = PaddingDefaults.Medium)
    ) {
        Messages(
            listState = listState,
            order = order,
            messages = messages,
            inAppMessages = inAppMessage,
            onClickReplyMessage = onClickReplyMessage,
            onClickPrescription = onClickPrescription,
            onClickInvoiceMessage = onClickInvoiceMessage,
            onClickPharmacy = onClickPharmacy
        )
    }
}

@LightDarkPreview
@Composable
fun MessageDetailScreenWithPharmacyPreview(
    @PreviewParameter(MessageOrderDetailPreviewParameterProvider::class)
    orderMessageDetail: UiState<List<OrderMessageDetail>>
) {
    PreviewAppTheme {
        MessageDetailScreenScaffold(
            listState = rememberLazyListState(),
            onBack = {},
            onClickReplyMessage = {},
            onClickPrescription = {},
            onClickInvoiceMessage = {},
            onClickPharmacy = {},
            order = UiState.Data(orderMessageDetail.data?.first()?.orderDetail),
            messages = orderMessageDetail.data?.map { it.message } ?: emptyList(),
            inAppMessage = emptyList(),
            isLocalMessage = false
        )
    }
}

@LightDarkPreview
@Composable
fun MessageDetailScreenInAppWithPharmacyPreview(
    @PreviewParameter(MessageDetailInAppPreviewParameterProvider::class)
    inAppMessage: List<InAppMessage>
) {
    PreviewAppTheme {
        MessageDetailScreenScaffold(
            listState = rememberLazyListState(),
            onBack = {},
            onClickReplyMessage = {},
            onClickPrescription = {},
            onClickInvoiceMessage = {},
            onClickPharmacy = {},
            order = UiState.Loading(),
            messages = emptyList(),
            inAppMessage = inAppMessage,
            isLocalMessage = true
        )
    }
}
