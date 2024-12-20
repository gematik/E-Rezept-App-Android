/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.messages.presentation.ui.screens

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.messages.presentation.rememberMessageListController
import de.gematik.ti.erp.app.messages.presentation.ui.components.Orders
import de.gematik.ti.erp.app.messages.presentation.ui.model.ViewState
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageListParameterProvider
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class MessageListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val messagesController = rememberMessageListController()
        val listState = rememberLazyListState()
        val viewState by messagesController.viewState.collectAsStateWithLifecycle()
        var showOrderFeatureChangedLabel by remember(viewState.isMessagesListFeatureChangeSeen) { mutableStateOf(!viewState.isMessagesListFeatureChangeSeen) }

        MessageListScreenContent(
            viewState = viewState,
            listState = listState,
            showOrderFeatureChangedLabel = showOrderFeatureChangedLabel, // a feature change info label shown to the user
            onClickRetry = messagesController::retryFetchMessagesList,
            onClickInfoLabel = {
                showOrderFeatureChangedLabel = false
                messagesController.markProfileTopBarRemovedChangeSeen()
            },
            onClickOrder = { orderId, isLocalMessage ->
                navController.navigate(
                    MessagesRoutes.MessageDetailScreen.path(orderId, isLocalMessage)
                )
            }
        )
    }
}

@Composable
private fun MessageListScreenContent(
    viewState: ViewState,
    listState: LazyListState,
    showOrderFeatureChangedLabel: Boolean,
    onClickOrder: (String, Boolean) -> Unit,
    onClickInfoLabel: () -> Unit,
    onClickRetry: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.messages_title),
        listState = listState
    ) {
        Orders(
            listState = listState,
            showOrderFeatureChangedLabel = showOrderFeatureChangedLabel,
            ordersData = viewState.messagesList,
            onClickInfoLabel = onClickInfoLabel,
            onClickOrder = onClickOrder,
            onClickRetry = onClickRetry
        )
    }
}

@LightDarkPreview
@Composable
fun MessageScreenContentPreview(
    @PreviewParameter(MessageListParameterProvider::class)
    ordersData: UiState<List<InAppMessage>>
) {
    PreviewAppTheme {
        MessageListScreenContent(
            listState = rememberLazyListState(),
            showOrderFeatureChangedLabel = false,
            viewState = ViewState(false, ordersData),
            onClickOrder = { _, _ -> },
            onClickInfoLabel = {},
            onClickRetry = {}
        )
    }
}
