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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.presentation.rememberInternalMessageDetailScreenController
import de.gematik.ti.erp.app.messages.ui.components.InAppMessage
import de.gematik.ti.erp.app.messages.ui.model.InAppMessageUiModel.Companion.toInAppMessage
import de.gematik.ti.erp.app.messages.ui.model.MessageDetailBundle
import de.gematik.ti.erp.app.messages.ui.model.MessageType
import de.gematik.ti.erp.app.messages.ui.preview.InternalMessageDetailScreenPreviewParameterProvider
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import kotlinx.datetime.Instant
import kotlin.collections.map

class InternalMessageDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val messageDetailScreenController = rememberInternalMessageDetailScreenController()

        val internalMessages by messageDetailScreenController.internalMessages.collectAsStateWithLifecycle()

        val onBack: () -> Unit = remember(navController, messageDetailScreenController) {
            { messageDetailScreenController.consumeAllMessages { navController.popBackStack() } }
        }
        BackHandler { onBack() }

        InternalMessageDetailScreenScaffold(
            listState = listState,
            internalMessages = internalMessages,
            onBack = onBack
        )
    }
}

@Composable
fun InternalMessageDetailScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    internalMessages: List<InAppMessage>
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        topBarTitle = stringResource(R.string.internal_message_from),
        listState = listState,
        actions = {},
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        topBarPadding = PaddingValues()
    ) { innerPadding ->
        InternalMessageDetailScreenContent(
            innerPadding = innerPadding,
            listState = listState,
            internalMessages = internalMessages
        )
    }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun InternalMessageDetailScreenContent(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    innerPadding: PaddingValues,
    internalMessages: List<InAppMessage>
) {
    LazyColumn(
        modifier = Modifier
            .testTag(TestTag.Orders.Details.Content)
            .then(modifier),
        state = listState,
        contentPadding = innerPadding
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.messages_history_title),
                style = AppTheme.typography.h6,
                modifier = Modifier
                    .padding(horizontal = PaddingDefaults.Medium)
                    .semanticsHeading()
            )
        }

        internalMessages.map { local ->
            MessageDetailBundle(
                type = MessageType.IN_APP,
                message = OrderUseCaseData.Message(
                    content = local.text.orEmpty(),
                    additionalInfo = local.tag.orEmpty(),
                    sentOn = Instant.parse(local.timeState.timestamp.toString()),
                    communicationId = local.id.orEmpty(),
                    link = null,
                    consumed = true,
                    pickUpCodeDMC = null,
                    pickUpCodeHR = null,
                    prescriptions = emptyList()
                ),
                timestamp = Instant.parse(local.timeState.timestamp.toString())
            )
        }.forEachIndexed { index, displayMessage ->
            val isFirstMessage = index == 0
            val isLastMessage = index == internalMessages.size - 1

            displayMessage.message?.let { message ->
                item {
                    InAppMessage(
                        item = message.toInAppMessage(isFirstMessage, isLastMessage)
                    )
                }
            }
        }
    }
}

@LightDarkLongPreview
@Composable
fun InternalMessageDetailScreenPreview(
    @PreviewParameter(InternalMessageDetailScreenPreviewParameterProvider::class)
    inAppMessages: List<InAppMessage>
) {
    PreviewTheme {
        InternalMessageDetailScreenScaffold(
            listState = rememberLazyListState(),
            onBack = {},
            internalMessages = inAppMessages
        )
    }
}
