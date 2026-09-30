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

package de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.DebugPushNotificationsViewModel
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.pushnotifications.BuildConfig
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.PushChannelStatus
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.ui.DebugActionButton
import de.gematik.ti.erp.app.ui.DebugCard
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.uistate.UiState

@Composable
fun DebugPushRegistrationSection(viewModel: DebugPushNotificationsViewModel) {
    val channelsState by viewModel.channels.collectAsStateWithLifecycle()
    val pushersState by viewModel.pushers.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val pusherRegistrationState by viewModel.pusherRegistration.collectAsStateWithLifecycle()
    val selectedGatewayUrl by viewModel.selectedGatewayUrl.collectAsStateWithLifecycle()
    val failPushGatewayTest by viewModel.failPushGatewayTest.collectAsStateWithLifecycle()
    val showRawPushNotification by viewModel.showRawPushNotification.collectAsStateWithLifecycle()

    DebugPushRegistrationSectionContent(
        failPushGatewayTest = failPushGatewayTest,
        onFailPushGatewayTestChange = viewModel::updateFailPushGatewayTest,
        showRawPushNotification = showRawPushNotification,
        onShowRawPushNotificationChange = viewModel::setShowRawPushNotification,
        channelsState = channelsState,
        pushersState = pushersState,
        fcmToken = fcmToken,
        pusherRegistrationState = pusherRegistrationState,
        selectedGatewayUrl = selectedGatewayUrl,
        onSelectGatewayUrl = viewModel::updatePushGatewayUrl,
        onRegisterPusher = viewModel::registerPusher,
        onRefreshStatus = viewModel::refreshStatus
    )
}

@Composable
private fun DebugPushRegistrationSectionContent(
    channelsState: UiState<List<PushChannel>>,
    pushersState: UiState<List<Pusher>>,
    fcmToken: String?,
    pusherRegistrationState: UiState<Unit>,
    selectedGatewayUrl: String,
    failPushGatewayTest: Boolean,
    showRawPushNotification: Boolean,
    onSelectGatewayUrl: (String) -> Unit,
    onFailPushGatewayTestChange: (Boolean) -> Unit,
    onShowRawPushNotificationChange: (Boolean) -> Unit,
    onRegisterPusher: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    val statusLoading = pushersState.isLoading || channelsState.isLoading
    val actionLoading = statusLoading || pusherRegistrationState.isLoading
    val showPusherStatus = pushersState.hasRequestState
    val showChannelStatus = channelsState.hasRequestState

    DebugCard(title = "Push Registration & Channels", collapsible = true) {
        Text(
            text = "Register the current device and inspect its pusher and channel status.",
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral600
        )
        SpacerMedium()
        if (fcmToken != null) {
            SpacerMedium()
            Text(
                text = "FCM Token (Push Key)",
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.neutral900
            )
            SpacerTiny()
            val clipboardManager = LocalClipboardManager.current
            OutlinedTextField(
                value = fcmToken,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = AppTheme.typography.caption2.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.primary600,
                    unfocusedBorderColor = AppTheme.colors.primary600,
                    focusedTextColor = AppTheme.colors.neutral900,
                    unfocusedTextColor = AppTheme.colors.neutral900
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        clipboardManager.setText(buildAnnotatedString { append(fcmToken) })
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy token",
                            tint = AppTheme.colors.primary600
                        )
                    }
                }
            )
        }

        SpacerMedium()
        PushEnvironmentSelector(
            selectedUrl = selectedGatewayUrl,
            onSelectUrl = onSelectGatewayUrl
        )

        SpacerMedium()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFailPushGatewayTestChange(!failPushGatewayTest) }
                .padding(vertical = PaddingDefaults.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Use non.standard app-id",
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral900,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = failPushGatewayTest,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.colors.primary500,
                    checkedTrackColor = AppTheme.colors.primary100,
                    uncheckedThumbColor = AppTheme.colors.neutral500,
                    uncheckedTrackColor = AppTheme.colors.neutral200
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowRawPushNotificationChange(!showRawPushNotification) }
                .padding(vertical = PaddingDefaults.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show raw payload in system notification",
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral900,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showRawPushNotification,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.colors.primary500,
                    checkedTrackColor = AppTheme.colors.primary100,
                    uncheckedThumbColor = AppTheme.colors.neutral500,
                    uncheckedTrackColor = AppTheme.colors.neutral200
                )
            )
        }
        SpacerMedium()
        DebugActionButton(
            text = "Register Pusher",
            enabled = !actionLoading,
            loading = pusherRegistrationState.isLoading,
            onClick = onRegisterPusher
        )
        PusherRegistrationStatus(
            state = pusherRegistrationState,
            selectedGatewayUrl = selectedGatewayUrl
        )
        SpacerSmall()
        DebugActionButton(
            text = "Refresh Status",
            enabled = !actionLoading,
            loading = statusLoading,
            backgroundColor = AppTheme.colors.neutral200,
            contentColor = AppTheme.colors.neutral900,
            onClick = onRefreshStatus
        )

        if (showPusherStatus) {
            SpacerMedium()
            StatusHeader(title = "Pushers", count = pushersState.data?.size)
            PusherStatus(
                state = pushersState,
                currentFcmToken = fcmToken
            )
        }

        if (showChannelStatus) {
            SpacerMedium()
            StatusHeader(title = "Channels", count = channelsState.data?.size)
            ChannelStatus(state = channelsState)
        }
    }
}

private val UiState<*>.hasRequestState: Boolean
    get() = isLoading || data != null || error != null

@Composable
private fun StatusHeader(title: String, count: Int?) {
    Text(
        text = if (count != null) "$title ($count)" else title,
        style = AppTheme.typography.subtitle2,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.colors.neutral900
    )
}

@Composable
private fun ChannelStatus(state: UiState<List<PushChannel>>) {
    val error = state.error
    val channels = state.data

    when {
        state.isLoading -> Unit
        error != null -> {
            SpacerSmall()
            StatusMessage(
                text = "Could not load channel status: ${error.message ?: "Unknown error"}",
                tone = StatusTone.Error
            )
        }

        channels != null -> {
            SpacerSmall()
            if (channels.isEmpty()) {
                StatusMessage(
                    text = "No channels returned for this device.",
                    tone = StatusTone.Neutral
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.neutral100,
                    shape = RoundedCornerShape(SizeDefaults.one)
                ) {
                    Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
                        channels.sortedBy { it.id }.forEachIndexed { index, channel ->
                            ChannelRow(channel = channel)
                            if (index < channels.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = PaddingDefaults.Small),
                                    color = AppTheme.colors.neutral200
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
private fun PusherRegistrationStatus(
    state: UiState<Unit>,
    selectedGatewayUrl: String
) {
    val error = state.error

    when {
        state.isLoading -> Unit
        error != null -> {
            SpacerSmall()
            StatusMessage(
                text = "Could not register pusher: ${error.message ?: "Unknown error"}",
                tone = StatusTone.Error
            )
        }

        state.data != null -> {
            val envLabel = when (selectedGatewayUrl) {
                BuildConfig.PUSH_GATEWAY_URL_DEV -> "DEV"
                BuildConfig.PUSH_GATEWAY_URL_PU -> "PU"
                else -> "RU"
            }
            SpacerSmall()
            StatusMessage(
                text = "Pusher registered for the active profile on $envLabel.",
                tone = StatusTone.Success
            )
        }
    }
}

@Composable
private fun PusherStatus(
    state: UiState<List<Pusher>>,
    currentFcmToken: String?
) {
    val error = state.error
    val pushers = state.data

    when {
        state.isLoading -> Unit
        error != null -> {
            SpacerSmall()
            StatusMessage(
                text = "Could not load pushers: ${error.message ?: "Unknown error"}",
                tone = StatusTone.Error
            )
        }

        pushers != null -> {
            SpacerSmall()
            val currentPusherIsRegistered = currentFcmToken != null && pushers.any { pusher ->
                pusher.pushKey == currentFcmToken
            }
            StatusMessage(
                text = when {
                    currentFcmToken == null -> "Current FCM token is not available yet."
                    currentPusherIsRegistered -> "Current FCM token is registered."
                    else -> "Current FCM token is not registered for this profile."
                },
                tone = when {
                    currentFcmToken == null -> StatusTone.Neutral
                    currentPusherIsRegistered -> StatusTone.Success
                    else -> StatusTone.Error
                }
            )
            SpacerSmall()
            if (pushers.isEmpty()) {
                StatusMessage(
                    text = "No pushers returned for this profile.",
                    tone = StatusTone.Neutral
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.neutral100,
                    shape = RoundedCornerShape(SizeDefaults.one)
                ) {
                    Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
                        pushers.forEachIndexed { index, pusher ->
                            val isCurrentDevice = pusher.pushKey == currentFcmToken
                            PusherRow(pusher = pusher, isCurrentDevice = isCurrentDevice)
                            if (index < pushers.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = PaddingDefaults.Small),
                                    color = AppTheme.colors.neutral200
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class StatusTone {
    Success,
    Error,
    Neutral
}

private data class StatusMessageStyle(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val icon: ImageVector
)

@Composable
private fun statusMessageStyle(tone: StatusTone) = when (tone) {
    StatusTone.Success -> StatusMessageStyle(
        containerColor = AppTheme.colors.green100,
        contentColor = AppTheme.colors.green900,
        borderColor = AppTheme.colors.green200,
        icon = Icons.Rounded.CheckCircle
    )

    StatusTone.Error -> StatusMessageStyle(
        containerColor = AppTheme.colors.red100,
        contentColor = AppTheme.colors.red900,
        borderColor = AppTheme.colors.red200,
        icon = Icons.Rounded.ErrorOutline
    )

    StatusTone.Neutral -> StatusMessageStyle(
        containerColor = AppTheme.colors.neutral100,
        contentColor = AppTheme.colors.neutral700,
        borderColor = AppTheme.colors.neutral200,
        icon = Icons.Rounded.Info
    )
}

@Composable
private fun StatusMessage(text: String, tone: StatusTone) {
    val style = statusMessageStyle(tone)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = style.containerColor,
        shape = RoundedCornerShape(SizeDefaults.one),
        border = BorderStroke(SizeDefaults.eighth, style.borderColor)
    ) {
        Row(
            modifier = Modifier.padding(PaddingDefaults.Small),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.contentColor,
                modifier = Modifier.size(SizeDefaults.double)
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.caption1,
                color = style.contentColor
            )
        }
    }
}

@Composable
private fun ChannelRow(channel: PushChannel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = channel.id,
            modifier = Modifier.weight(1f),
            style = AppTheme.typography.subtitle2,
            color = AppTheme.colors.neutral800
        )
        ChannelStatusBadge(status = channel.status)
    }
}

@Composable
private fun ChannelStatusBadge(status: String) {
    val normalizedStatus = status.trim().lowercase()
    val label: String
    val containerColor: Color
    val contentColor: Color
    val icon: ImageVector

    when (normalizedStatus) {
        PushChannelStatus.ENABLED -> {
            label = "Enabled"
            containerColor = AppTheme.colors.green100
            contentColor = AppTheme.colors.green900
            icon = Icons.Rounded.Check
        }

        PushChannelStatus.DISABLED -> {
            label = "Disabled"
            containerColor = AppTheme.colors.red100
            contentColor = AppTheme.colors.red900
            icon = Icons.Rounded.Close
        }

        else -> {
            label = normalizedStatus.replace('_', ' ').replaceFirstChar { it.uppercase() }
            containerColor = AppTheme.colors.neutral200
            contentColor = AppTheme.colors.neutral700
            icon = Icons.Rounded.Info
        }
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(SizeDefaults.one)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PaddingDefaults.Small,
                vertical = PaddingDefaults.Tiny
            ),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SizeDefaults.double),
                tint = contentColor
            )
            Text(
                text = label,
                style = AppTheme.typography.caption2,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun PusherRow(pusher: Pusher, isCurrentDevice: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.PhoneAndroid,
            contentDescription = null,
            tint = if (isCurrentDevice) AppTheme.colors.primary600 else AppTheme.colors.neutral500,
            modifier = Modifier.size(SizeDefaults.triple)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pusher.deviceDisplayName ?: "Unnamed device",
                style = AppTheme.typography.subtitle2,
                fontWeight = if (isCurrentDevice) FontWeight.SemiBold else FontWeight.Normal,
                color = AppTheme.colors.neutral800
            )
            SpacerTiny()
            Text(
                text = pusher.appId,
                style = AppTheme.typography.caption2,
                fontFamily = FontFamily.Monospace,
                color = AppTheme.colors.neutral600
            )
        }
        if (isCurrentDevice) {
            CurrentDeviceBadge()
        }
    }
}

@Composable
private fun CurrentDeviceBadge() {
    Surface(
        color = AppTheme.colors.primary100,
        shape = RoundedCornerShape(SizeDefaults.one)
    ) {
        Text(
            text = "Current",
            modifier = Modifier.padding(
                horizontal = PaddingDefaults.Small,
                vertical = PaddingDefaults.Tiny
            ),
            style = AppTheme.typography.caption2,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.primary900
        )
    }
}

private enum class PushEnvironmentOption(val label: String, val url: String) {
    RU("RU", BuildConfig.PUSH_GATEWAY_URL_RU),
    DEV("DEV", BuildConfig.PUSH_GATEWAY_URL_DEV),
    PU("PU", BuildConfig.PUSH_GATEWAY_URL_PU)
}

@Composable
private fun PushEnvironmentSelector(
    selectedUrl: String,
    onSelectUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val isKonnektathon = context.packageName.contains("konnektathon", ignoreCase = true)
    var customUrlInput by remember(selectedUrl) { mutableStateOf(selectedUrl) }

    Column {
        Text(
            text = "Push Gateway Environment",
            style = AppTheme.typography.subtitle2,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.neutral900
        )
        SpacerTiny()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            PushEnvironmentOption.entries.forEach { option ->
                val selected = selectedUrl == option.url
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSelectUrl(option.url) }
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = { onSelectUrl(option.url) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AppTheme.colors.primary600,
                            unselectedColor = AppTheme.colors.neutral600
                        )
                    )
                    Text(
                        text = option.label,
                        style = AppTheme.typography.body2,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = AppTheme.colors.neutral900
                    )
                }
            }
        }
        SpacerTiny()
        val displayUrl = if (isKonnektathon) "xxxx" else selectedUrl
        Surface(
            shape = RoundedCornerShape(SizeDefaults.half),
            color = AppTheme.colors.neutral100,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Active URL: $displayUrl",
                style = AppTheme.typography.caption2,
                fontFamily = FontFamily.Monospace,
                color = AppTheme.colors.neutral800,
                modifier = Modifier.padding(PaddingDefaults.Small)
            )
        }
        SpacerTiny()
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = customUrlInput,
                onValueChange = { customUrlInput = it },
                label = { Text("Custom Gateway URL") },
                modifier = Modifier.weight(1f),
                textStyle = AppTheme.typography.caption2.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.primary600,
                    unfocusedBorderColor = AppTheme.colors.primary600,
                    focusedLabelColor = AppTheme.colors.primary600,
                    unfocusedLabelColor = AppTheme.colors.primary600,
                    cursorColor = AppTheme.colors.primary600,
                    focusedTextColor = AppTheme.colors.neutral900,
                    unfocusedTextColor = AppTheme.colors.neutral900
                )
            )
            Button(
                onClick = { onSelectUrl(customUrlInput) },
                modifier = Modifier.padding(start = PaddingDefaults.Small),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary600,
                    contentColor = Color.White
                )
            ) {
                Text("Update URL")
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewDebugPushRegistrationSection() {
    AppTheme {
        DebugPushRegistrationSectionContent(
            channelsState = UiState.Data(
                listOf(
                    PushChannel(id = "erp.task.activate", status = "enabled"),
                    PushChannel(id = "erp.task.cancel", status = "disabled")
                )
            ),
            pushersState = UiState.Data(
                listOf(
                    Pusher(
                        pushKey = "current-fcm-token",
                        kind = "http",
                        appId = "de.gematik.ti.erp.app",
                        deviceDisplayName = "Current Device"
                    ),
                    Pusher(
                        pushKey = "other-fcm-token",
                        kind = "http",
                        appId = "de.gematik.ti.erp.app",
                        deviceDisplayName = "Other Device"
                    )
                )
            ),
            fcmToken = "current-fcm-token",
            pusherRegistrationState = UiState.Data(Unit),
            selectedGatewayUrl = BuildConfig.PUSH_GATEWAY_URL_RU,
            failPushGatewayTest = false,
            showRawPushNotification = false,
            onSelectGatewayUrl = {},
            onFailPushGatewayTestChange = {},
            onShowRawPushNotificationChange = {},
            onRegisterPusher = {},
            onRefreshStatus = {}
        )
    }
}
