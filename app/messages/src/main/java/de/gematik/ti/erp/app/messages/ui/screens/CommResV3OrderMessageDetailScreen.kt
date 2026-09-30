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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.ProvideFakeCacheForPreview
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutesBackStackEntryArguments
import de.gematik.ti.erp.app.messages.presentation.rememberOrderMessageDetailController
import de.gematik.ti.erp.app.messages.ui.components.MessageDetailContent
import de.gematik.ti.erp.app.messages.ui.components.MessageDetailDropdownMenu
import de.gematik.ti.erp.app.messages.ui.preview.MessageOrderDetailPreviewParameterProvider
import de.gematik.ti.erp.app.messages.ui.preview.OrderMessageDetail
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.translation.navigation.TranslationRoutes
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import de.gematik.ti.erp.app.utils.letNotNull
import de.gematik.ti.erp.app.utils.uistate.UiState
import java.util.Locale

// TODO CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41314-44069&m=dev

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

        val onBack: () -> Unit = remember(navController) {
            { navController.popBackStack() }
        }

        OrderMessageDetailScreenScaffold(
            listState = listState,
            hasReplyMessages = hasReplyMessages,
            order = order,
            messages = messages.data ?: emptyList(),
            isTranslationsAllowed = isTranslationsAllowed,
            showTranslationFeature = showTranslationFeature,
            isTranslationInProgress = isTranslationInProgress,
            onClickTranslation = handleTranslationClick,
            onClickReplyMessage = onClickReplyMessage,
            onClickPrescription = onClickPrescription,
            onClickInvoiceMessage = onClickInvoiceMessage,
            onClickPharmacy = onClickPharmacy,
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
    hasReplyMessages: Boolean,
    isTranslationsAllowed: Boolean,
    showTranslationFeature: Boolean,
    isTranslationInProgress: Map<String, Boolean>,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickPharmacy: () -> Unit,
    onToggleTranslationConsent: () -> Unit = {},
    onClickTranslation: (String, String) -> Unit = { _, _ -> }
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        topBarTitle = order.data?.pharmacy?.name ?: stringResource(R.string.messages_title),
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
        topBarPadding = PaddingValues()
    ) { innerPadding ->
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

@LightDarkLongPreview
@Composable
fun CommResV3OrderMessageDetailScreenWithPharmacyPreview(
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
                order = UiState.Data(orderMessageDetail.data?.first()?.orderDetail),
                messages = orderMessageDetail.data?.map { it.message } ?: emptyList(),
                isTranslationsAllowed = true,
                isTranslationInProgress = mapOf("123" to true),
                onClickReplyMessage = {},
                onClickPrescription = {},
                onClickInvoiceMessage = {},
                onClickPharmacy = {},
                showTranslationFeature = true,
                hasReplyMessages = true
            )
        }
    }
}
