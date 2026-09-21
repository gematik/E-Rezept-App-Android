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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.debugsettings.pushnotifications.ui.components

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.DebugPushNotificationsViewModel
import de.gematik.ti.erp.app.material3.components.switchs.GemSwitch
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.ui.DebugActionButton
import de.gematik.ti.erp.app.ui.DebugCard
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@Composable
fun DebugSendFcmSection(
    viewModel: DebugPushNotificationsViewModel,
    pushEnabled: Boolean,
    permissionGranted: Boolean,
    onToggle: (Boolean) -> Unit,
    onFetchToken: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val oauthToken by viewModel.oauthToken.collectAsStateWithLifecycle()
    val isSending by viewModel.isFcmSending.collectAsStateWithLifecycle()
    val projectId = viewModel.fcmProjectId

    LaunchedEffect(pushEnabled, permissionGranted) {
        if (pushEnabled && permissionGranted) onFetchToken()
    }

    viewModel.fcmErrorEvent.listen { message ->
        snackbarHostState.showSnackbar(message)
    }

    DebugSendFcmSectionContent(
        pushEnabled = pushEnabled,
        permissionGranted = permissionGranted,
        fcmToken = fcmToken,
        oauthToken = oauthToken,
        isSending = isSending,
        projectId = projectId,
        onToggle = onToggle,
        onUpdateOauthToken = { viewModel.updateOauthToken(it) },
        onSendFcmPush = { targetToken, title, body -> viewModel.sendFcmPush(targetToken, title, body) },
        snackbarHostState = snackbarHostState
    )
}

@Suppress("LongParameterList")
@Composable
internal fun DebugSendFcmSectionContent(
    pushEnabled: Boolean,
    permissionGranted: Boolean,
    fcmToken: String?,
    oauthToken: String,
    isSending: Boolean,
    projectId: String,
    onToggle: (Boolean) -> Unit,
    onUpdateOauthToken: (String) -> Unit,
    onSendFcmPush: (targetToken: String, title: String, body: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    DebugCard(
        title = "🔔 Send FCM Push",
        collapsible = true,
        initiallyExpanded = pushEnabled && permissionGranted
    ) {
        PushToggle(pushEnabled, permissionGranted, onToggle)

        if (pushEnabled && permissionGranted) {
            SpacerMedium()
            HorizontalDivider(color = AppTheme.colors.neutral200)
            SpacerMedium()

            SendFcmCurlSection(
                fcmToken = fcmToken,
                oauthToken = oauthToken,
                isSending = isSending,
                projectId = projectId,
                onUpdateOauthToken = onUpdateOauthToken,
                onSendFcmPush = onSendFcmPush,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
private fun PushToggle(
    pushEnabled: Boolean,
    permissionGranted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Push Testing",
                style = AppTheme.typography.body1,
                color = AppTheme.colors.neutral900
            )
            SpacerTiny()
            Text(
                text = when {
                    pushEnabled && permissionGranted -> "Permission granted"
                    !permissionGranted -> "Tap to grant notification permission"
                    else -> "Inactive"
                },
                style = AppTheme.typography.caption1,
                color = if (pushEnabled && permissionGranted) AppTheme.colors.primary600
                else AppTheme.colors.neutral600
            )
        }
        GemSwitch(
            checked = pushEnabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun SendFcmCurlSection(
    fcmToken: String?,
    oauthToken: String,
    isSending: Boolean,
    projectId: String,
    onUpdateOauthToken: (String) -> Unit,
    onSendFcmPush: (targetToken: String, title: String, body: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var targetToken by remember { mutableStateOf(fcmToken ?: "") }
    var title by remember { mutableStateOf("Test Push") }
    var body by remember { mutableStateOf("This is a test FCM push via cURL!") }
    var showCurl by remember { mutableStateOf(false) }

    LaunchedEffect(fcmToken) {
        if (fcmToken != null && targetToken.isBlank()) targetToken = fcmToken
    }

    val canSend = !isSending && targetToken.isNotBlank() && oauthToken.isNotBlank()

    val curlCommand = remember(targetToken, oauthToken, title, body, projectId) {
        if (targetToken.isNotBlank() && oauthToken.isNotBlank()) {
            """
curl -X POST 'https://fcm.googleapis.com/v1/projects/$projectId/messages:send' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer $oauthToken' \
  -d '{
  "message": {
    "token": "$targetToken",
    "notification": {
      "title": "$title",
      "body": "$body"
    }
  }
}'
            """.trimIndent()
        } else null
    }

    Text(
        text = "Send FCM Push",
        style = AppTheme.typography.subtitle2,
        fontWeight = FontWeight.SemiBold
    )
    SpacerSmall()
    Text(
        text = "Send FCM Push to this device or any other device by pasting their FCM token.",
        style = AppTheme.typography.caption1,
        color = AppTheme.colors.neutral600
    )
    SpacerSmall()

    Surface(
        shape = RoundedCornerShape(SizeDefaults.one),
        color = AppTheme.colors.neutral100,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
    ) {
        Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
            Text(
                text = "Get an OAuth2 token (~1hr validity)",
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.neutral600,
                fontWeight = FontWeight.SemiBold
            )
            SpacerTiny()
            Text(
                text = "$ gcloud auth print-access-token",
                style = AppTheme.typography.caption1.copy(fontFamily = FontFamily.Monospace),
                color = AppTheme.colors.neutral800
            )
        }
    }
    SpacerTiny()
    DebugActionButton(
        text = "Copy gcloud Command",
        onClick = {
            scope.launch {
                clipboard.setClipEntry(
                    ClipData.newPlainText("gcloud", "gcloud auth print-access-token").toClipEntry()
                )
                snackbarHostState.showSnackbar("gcloud command copied!")
            }
        }
    )

    SpacerMedium()
    ErezeptOutlineText(
        modifier = Modifier.fillMaxWidth(),
        value = oauthToken,
        onValueChange = { onUpdateOauthToken(it) },
        label = "OAuth2 Token (saved, ~1hr validity)",
        placeholder = null
    )
    SpacerSmall()

    ErezeptOutlineText(
        modifier = Modifier.fillMaxWidth(),
        value = targetToken,
        onValueChange = { targetToken = it },
        label = "Target Device FCM Token (this device by default)",
        placeholder = null
    )
    SpacerTiny()
    Row {
        if (fcmToken != null) {
            DebugActionButton(
                text = "Reset to My Device",
                enabled = targetToken != fcmToken,
                onClick = { targetToken = fcmToken }
            )
        }
    }

    SpacerSmall()
    ErezeptOutlineText(
        modifier = Modifier.fillMaxWidth(),
        value = title,
        onValueChange = { title = it },
        label = "Notification Title",
        placeholder = null
    )
    SpacerSmall()
    ErezeptOutlineText(
        modifier = Modifier.fillMaxWidth(),
        value = body,
        onValueChange = { body = it },
        label = "Notification Body",
        placeholder = null
    )
    SpacerMedium()

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isSending) {
            CircularProgressIndicator(
                modifier = Modifier.size(SizeDefaults.doubleHalf),
                strokeWidth = SizeDefaults.quarter
            )
            SpacerSmall()
        }
        DebugActionButton(
            text = if (isSending) "Sending…" else "Send Notification Now",
            enabled = canSend,
            onClick = { onSendFcmPush(targetToken, title, body) }
        )
    }

    SpacerMedium()
    DebugActionButton(
        text = if (showCurl) "Hide cURL Command" else "Show cURL Command",
        onClick = { showCurl = !showCurl }
    )
    if (showCurl && curlCommand != null) {
        SpacerSmall()
        Surface(
            shape = RoundedCornerShape(SizeDefaults.one),
            color = AppTheme.colors.neutral100,
            border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(PaddingDefaults.Small)
            ) {
                Text(
                    text = curlCommand,
                    style = AppTheme.typography.caption1.copy(fontFamily = FontFamily.Monospace),
                    color = AppTheme.colors.neutral800
                )
            }
        }
        SpacerTiny()
        DebugActionButton(
            text = "Copy cURL",
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(ClipData.newPlainText("FCM cURL", curlCommand).toClipEntry())
                    snackbarHostState.showSnackbar("cURL copied!")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DebugSendFcmSectionPreview() {
    PreviewAppTheme {
        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            DebugSendFcmSectionContent(
                pushEnabled = true,
                permissionGranted = true,
                fcmToken = "sample-fcm-token",
                oauthToken = "sample-oauth-token",
                isSending = false,
                projectId = "my-firebase-project",
                onToggle = {},
                onUpdateOauthToken = {},
                onSendFcmPush = { _, _, _ -> },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }
}
