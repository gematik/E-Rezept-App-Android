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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.navigation.HandleMessageListRefresh
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.messages.presentation.rememberMessageListController
import de.gematik.ti.erp.app.messages.ui.components.MessageListScreenContent
import de.gematik.ti.erp.app.messages.ui.preview.MessageListParameterProvider
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

// TODO CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-29059&m=dev
// Not part of MVP, only change in MVP are the Tags https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17435&m=dev
// Big Ones are on Prescription Screen, small ones for MessageListScreen

class MessageListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val messagesController = rememberMessageListController()
        val listState = rememberLazyListState()
        val messagesList by messagesController.messagesList.collectAsStateWithLifecycle()

        DisposableEffect(messagesList) {
            onDispose {
                messagesController.trackMessageCount()
            }
        }

        navBackStackEntry.HandleMessageListRefresh {
            messagesController.retryFetchMessagesList()
        }

        MessageListScreenScaffold(
            messagesList = messagesList,
            listState = listState,
            onClickRetry = messagesController::retryFetchMessagesList,
            onClickInternalMessage = {
                navController.navigate(
                    MessagesRoutes.InternalMessageDetailScreen.path()
                )
            },
            onClickOrder = { orderId ->
                navController.navigate(
                    MessagesRoutes.OrderMessageDetailScreen.path(orderId)
                )
            },
            onClickUnknownOrder = { taskId ->
                navController.navigate(
                    MessagesRoutes.UnknownOrderMessageDetailScreen.path(taskId)
                )
            },
            onClickEuOrder = { threadOrderId, threadStart, threadEnd, pharmacyName ->
                navController.navigate(
                    MessagesRoutes.EuRedeemMessageDetailsScreen.path(
                        orderId = threadOrderId,
                        threadStart = threadStart,
                        threadEnd = threadEnd,
                        pharmacyName = pharmacyName
                    )
                )
            }
        )
    }
}

@Composable
internal fun MessageListScreenScaffold(
    messagesList: UiState<List<InAppMessage>>,
    listState: LazyListState,
    onClickOrder: (String) -> Unit,
    onClickUnknownOrder: (String) -> Unit,
    onClickInternalMessage: () -> Unit,
    onClickEuOrder: (threadOrderId: String?, threadStart: Instant?, threadEnd: Instant?, pharmacyName: String?) -> Unit,
    onClickRetry: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.messages_title),
        listState = listState
    ) {
        MessageListScreenContent(
            listState = listState,
            ordersData = messagesList,
            onClickInternalMessage = onClickInternalMessage,
            onClickOrder = onClickOrder,
            onClickUnknownOrder = onClickUnknownOrder,
            onClickEuOrder = onClickEuOrder,
            onClickRetry = onClickRetry
        )
    }
}

@LightDarkPreview
@Composable
fun MessageScreenScaffoldPreview(
    @PreviewParameter(MessageListParameterProvider::class)
    ordersData: UiState<List<InAppMessage>>
) {
    PreviewTheme {
        MessageListScreenScaffold(
            messagesList = ordersData,
            listState = rememberLazyListState(),
            onClickOrder = { _ -> },
            onClickInternalMessage = { },
            onClickEuOrder = { _, _, _, _ -> },
            onClickRetry = {},
            onClickUnknownOrder = { _ -> }
        )
    }
}
