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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.ErpTimeFormatter.Style
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.domain.model.WELCOME_MESSAGE_ID
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.navigation.popBackStackRefreshingMessages
import de.gematik.ti.erp.app.messages.presentation.rememberInternalMessageDetailScreenController
import de.gematik.ti.erp.app.messages.ui.components.CommResV3InternalMessageCard
import de.gematik.ti.erp.app.messages.ui.preview.InternalMessageDetailScreenPreviewParameterProvider
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

class CommResV3InternalMessageDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val messageDetailScreenController = rememberInternalMessageDetailScreenController()

        val internalMessages by messageDetailScreenController.internalMessages.collectAsStateWithLifecycle()

        val onBack: () -> Unit = remember(navController, messageDetailScreenController) {
            { messageDetailScreenController.consumeAllMessages { navController.popBackStackRefreshingMessages() } }
        }
        BackHandler { onBack() }

        CommResV3InternalMessageDetailScreenScaffold(
            listState = listState,
            internalMessages = internalMessages,
            onBack = onBack
        )
    }
}

@Composable
fun CommResV3InternalMessageDetailScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    internalMessages: List<InAppMessage>
) {
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Orders.Details.Screen),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        topBarTitle = {
            InternalMessageDetailTopBarTitle(
                title = stringResource(R.string.internal_message_from)
            )
        },
        listState = listState,
        actions = {},
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        topBarPadding = PaddingValues(),
        topBarColor = AppTheme.colors.neutral025
    ) { innerPadding ->
        CommResV3InternalMessageDetailScreenContent(
            innerPadding = innerPadding,
            listState = listState,
            internalMessages = internalMessages
        )
    }
}

@Composable
internal fun CommResV3InternalMessageDetailScreenContent(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    innerPadding: PaddingValues,
    internalMessages: List<InAppMessage>
) {
    val readMessages = remember(internalMessages) {
        internalMessages
            .filterNot(InAppMessage::isUnread)
            .sortedBy { it.timeState.timestamp }
    }
    val unreadMessages = remember(internalMessages) {
        internalMessages
            .filter(InAppMessage::isUnread)
            .sortedBy { it.timeState.timestamp }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.neutral100)
            .testTag(TestTag.Orders.Details.Content)
            .then(modifier),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large),
        contentPadding = PaddingValues(
            start = PaddingDefaults.Medium,
            top = innerPadding.calculateTopPadding() + PaddingDefaults.Large,
            end = PaddingDefaults.Medium,
            bottom = innerPadding.calculateBottomPadding() + PaddingDefaults.Large
        )
    ) {
        itemsIndexed(readMessages, key = { _, message -> message.id }) { _, message ->
            InternalMessageCard(
                message = message
            )
        }

        if (readMessages.isNotEmpty() && unreadMessages.isNotEmpty()) {
            item {
                InternalMessageSectionDivider(
                    title = stringResource(R.string.diga_new)
                )
            }
        }

        itemsIndexed(unreadMessages, key = { _, message -> message.id }) { _, message ->
            InternalMessageCard(
                message = message
            )
        }
    }
}

@Composable
private fun InternalMessageDetailTopBarTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(SizeDefaults.fourfold)
                .clip(CircleShape)
                .background(AppTheme.colors.primary700),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_logo_outlined),
                contentDescription = null,
                tint = AppTheme.colors.neutral000,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }
        Text(
            text = title,
            style = AppTheme.typography.h6,
            color = AppTheme.colors.neutral900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = PaddingDefaults.Small)
        )
    }
}

@Composable
private fun InternalMessageSectionDivider(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = AppTheme.colors.neutral300
        )
        Text(
            text = title,
            style = AppTheme.typography.body2.copy(
                color = AppTheme.colors.primary700,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = PaddingDefaults.Tiny)
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = AppTheme.colors.neutral300
        )
    }
}

@Composable
private fun InternalMessageCard(
    message: InAppMessage
) {
    val formatter = rememberErpTimeFormatter()
    val isWelcomeMessage = message.id == WELCOME_MESSAGE_ID
    val timestamp = message.timeState.timestamp
    val formattedTime = stringResource(
        R.string.orders_timestamp,
        formatter.date(timestamp, Style.SHORT),
        formatter.time(timestamp, Style.SHORT)
    )

    CommResV3InternalMessageCard(
        modifier = Modifier.fillMaxWidth(),
        title = message.tag
            .takeIf { isWelcomeMessage && it.isNotBlank() }
            ?.trim(),
        content = message.text.orEmpty(),
        time = formattedTime,
        tag = message.tag.takeIf { !isWelcomeMessage && it.isNotBlank() },
        showAppIcon = isWelcomeMessage
    )
}

@LightDarkLongPreview
@Composable
fun CommResV3InternalMessageDetailScreenPreview(
    @PreviewParameter(InternalMessageDetailScreenPreviewParameterProvider::class)
    inAppMessages: List<InAppMessage>
) {
    PreviewTheme {
        CommResV3InternalMessageDetailScreenScaffold(
            listState = rememberLazyListState(),
            onBack = {},
            internalMessages = inAppMessages
        )
    }
}
