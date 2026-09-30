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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation.DebugPushNotificationsViewModel
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.ui.DebugActionButton
import de.gematik.ti.erp.app.ui.DebugCard
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.launch

private const val DEFAULT_TEST_PAYLOAD =
    """{"ChannelId":"erp.task.activate","Identifier":"160.000.000.000.001","IdentifierType":"TaskId"}"""

@Composable
fun DebugEncryptedPushSection(
    viewModel: DebugPushNotificationsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val oauthToken by viewModel.oauthToken.collectAsStateWithLifecycle()
    val cipherOutput by viewModel.cipherOutput.collectAsStateWithLifecycle()
    val decryptedOutput by viewModel.decryptedOutput.collectAsStateWithLifecycle()
    val fcmCurlCommand by viewModel.encryptedCurlCommand.collectAsStateWithLifecycle()
    val pushGatewayCurlCommand by viewModel.encryptedPushGatewayCurlCommand.collectAsStateWithLifecycle()
    val isSending by viewModel.isEncryptedSending.collectAsStateWithLifecycle()
    val latestKeyGen by viewModel.latestKeyGeneration.collectAsStateWithLifecycle()

    viewModel.pushErrorEvent.listen { message ->
        snackbarHostState.showSnackbar(message)
    }
    viewModel.pushSuccessEvent.listen { message ->
        snackbarHostState.showSnackbar(message)
    }

    DebugEncryptedPushSectionContent(
        fcmToken = fcmToken,
        oauthToken = oauthToken,
        cipherOutput = cipherOutput,
        decryptedOutput = decryptedOutput,
        fcmCurlCommand = fcmCurlCommand,
        pushGatewayCurlCommand = pushGatewayCurlCommand,
        isSending = isSending,
        latestKeyGen = latestKeyGen,
        onEncrypt = viewModel::encryptPayload,
        onUpdateOauthToken = viewModel::updateOauthToken,
        onUpdateCurlCommands = viewModel::updateEncryptedCurlCommands,
        onSendPush = viewModel::sendEncryptedPush,
        onDecrypt = viewModel::decryptPayload,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun DebugEncryptedPushSectionContent(
    fcmToken: String?,
    oauthToken: String,
    cipherOutput: String,
    decryptedOutput: String,
    fcmCurlCommand: String,
    pushGatewayCurlCommand: String,
    isSending: Boolean,
    latestKeyGen: PushNotificationKeyGeneration?,
    onEncrypt: (String, String) -> Unit,
    onUpdateOauthToken: (String) -> Unit,
    onUpdateCurlCommands: (String) -> Unit,
    onSendPush: (String) -> Unit,
    onDecrypt: () -> Unit,
    snackbarHostState: SnackbarHostState,
    initiallyExpanded: Boolean = false
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var plaintext by remember { mutableStateOf(DEFAULT_TEST_PAYLOAD) }
    var targetToken by remember { mutableStateOf(fcmToken ?: "") }

    LaunchedEffect(fcmToken) {
        if (fcmToken != null && targetToken.isBlank()) {
            targetToken = fcmToken
            onUpdateCurlCommands(fcmToken)
        }
    }

    val canSend = !isSending && cipherOutput.isNotBlank() && targetToken.isNotBlank() && oauthToken.isNotBlank()

    DebugCard(
        title = "🔐 Test Encrypted Push",
        collapsible = true,
        initiallyExpanded = initiallyExpanded
    ) {
        Text(
            text = "Encrypt a payload, test its delivery, and verify decryption.",
            style = AppTheme.typography.subtitle2,
            color = AppTheme.colors.neutral600
        )
        SpacerMedium()

        Text(
            text = "1. Encrypt payload",
            style = AppTheme.typography.subtitle2,
            fontWeight = FontWeight.SemiBold
        )
        SpacerSmall()

        latestKeyGen?.let { gen ->
            InfoSurface {
                Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
                    Text("Month: ${gen.month}", style = AppTheme.typography.caption2)
                    Text("Key ID: ${gen.keyIdentifier}", style = AppTheme.typography.caption2, maxLines = 1)
                    Text("Key: ${gen.encryptionKey}", style = AppTheme.typography.caption2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        SpacerMedium()
        ErezeptOutlineText(
            modifier = Modifier.fillMaxWidth(),
            value = plaintext,
            onValueChange = { plaintext = it },
            label = "Payload JSON",
            placeholder = null
        )
        SpacerMedium()

        DebugActionButton(
            text = "Encrypt",
            enabled = plaintext.isNotBlank(),
            onClick = { onEncrypt(plaintext, targetToken) }
        )

        if (cipherOutput.isNotBlank()) {
            SpacerMedium()

            Text("Ciphertext (Base64):", style = AppTheme.typography.subtitle2, fontWeight = FontWeight.SemiBold)
            SpacerTiny()
            InfoSurface {
                Text(
                    text = cipherOutput.take(80) + if (cipherOutput.length > 80) "…" else "",
                    style = AppTheme.typography.caption2,
                    modifier = Modifier.padding(PaddingDefaults.Small),
                    maxLines = 3
                )
            }
            SpacerSmall()
            DebugActionButton(
                text = "Copy Ciphertext",
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(ClipData.newPlainText("Ciphertext", cipherOutput).toClipEntry())
                        snackbarHostState.showSnackbar("Ciphertext copied!")
                    }
                }
            )

            SpacerMedium()
            HorizontalDivider(color = AppTheme.colors.neutral200)
            SpacerMedium()

            Text(
                text = "2. Deliver encrypted push",
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.SemiBold
            )
            SpacerMedium()

            Text(
                text = "Direct FCM",
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.SemiBold
            )

            SpacerSmall()
            Text(
                text = "Bypasses the Push Gateway and requires an OAuth2 access token.",
                style = AppTheme.typography.caption2,
                color = AppTheme.colors.neutral600
            )
            SpacerSmall()

            InfoSurface {
                Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
                    Text(
                        text = "Get an OAuth2 token (~1hr validity)",
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral600,
                        fontWeight = FontWeight.SemiBold
                    )
                    SpacerTiny()
                    Text(
                        text = "$ gcloud auth print-access-token",
                        style = AppTheme.typography.caption2,
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
                onValueChange = { token ->
                    onUpdateOauthToken(token)
                    onUpdateCurlCommands(targetToken)
                },
                label = "OAuth2 Token (saved, ~1hr validity)",
                placeholder = null
            )
            if (oauthToken.isBlank()) {
                SpacerTiny()
                Text(
                    text = "OAuth2 token required for direct FCM sending.",
                    style = AppTheme.typography.caption2,
                    color = AppTheme.colors.neutral600
                )
            }
            SpacerSmall()
            ErezeptOutlineText(
                modifier = Modifier.fillMaxWidth(),
                value = targetToken,
                onValueChange = { token ->
                    targetToken = token
                    onUpdateCurlCommands(token)
                },
                label = "Target Device FCM Token (this device by default)",
                placeholder = null
            )
            SpacerTiny()
            Row {
                if (fcmToken != null) {
                    DebugActionButton(
                        text = "Reset to My Device",
                        enabled = targetToken != fcmToken,
                        onClick = {
                            targetToken = fcmToken
                            onUpdateCurlCommands(fcmToken)
                        }
                    )
                }
            }
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
                    text = if (isSending) "Sending…" else "Send via Direct FCM",
                    enabled = canSend,
                    onClick = { onSendPush(targetToken) }
                )
            }

            SpacerMedium()
            HorizontalDivider(color = AppTheme.colors.neutral200)
            SpacerMedium()

            Text(
                text = "Terminal cURL commands",
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.SemiBold
            )
            SpacerSmall()

            CurlCommands(
                fcmCurlCommand = fcmCurlCommand,
                pushGatewayCurlCommand = pushGatewayCurlCommand,
                onCopy = { label, command ->
                    scope.launch {
                        clipboard.setClipEntry(ClipData.newPlainText(label, command).toClipEntry())
                        snackbarHostState.showSnackbar("$label copied!")
                    }
                }
            )

            SpacerMedium()
            HorizontalDivider(color = AppTheme.colors.neutral200)
            SpacerMedium()

            Text(
                text = "3. Verify payload",
                style = AppTheme.typography.subtitle2,
                fontWeight = FontWeight.SemiBold
            )
            SpacerSmall()

            DebugActionButton(
                text = "Decrypt and Compare",
                onClick = onDecrypt
            )

            if (decryptedOutput.isNotBlank()) {
                val payloadMatches = decryptedOutput == plaintext
                val resultText = if (payloadMatches) "Payload matches." else "Payload does not match."
                val resultColor = if (payloadMatches) AppTheme.colors.green100 else AppTheme.colors.red100

                SpacerSmall()
                Surface(
                    shape = RoundedCornerShape(SizeDefaults.one),
                    color = resultColor,
                    border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
                ) {
                    Column(modifier = Modifier.padding(PaddingDefaults.Small)) {
                        Text(
                            text = resultText,
                            style = AppTheme.typography.subtitle2,
                            fontWeight = FontWeight.SemiBold
                        )
                        SpacerTiny()
                        Text(
                            text = decryptedOutput,
                            style = AppTheme.typography.caption2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurlCommands(
    fcmCurlCommand: String,
    pushGatewayCurlCommand: String,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    DebugActionButton(
        text = if (expanded) "Hide cURL Commands" else "Show cURL Commands",
        onClick = { expanded = !expanded }
    )
    if (expanded) {
        CurlCommand(
            label = "Direct FCM cURL",
            description = "Bypasses the Push Gateway and uses the OAuth2 token above.",
            command = fcmCurlCommand,
            onCopy = onCopy
        )
        SpacerMedium()
        CurlCommand(
            label = "Push Gateway cURL",
            description = "Tests gateway delivery; replace the mTLS certificate paths before running.",
            command = pushGatewayCurlCommand,
            onCopy = onCopy
        )
    }
}

@Composable
private fun CurlCommand(
    label: String,
    description: String,
    command: String,
    onCopy: (String, String) -> Unit
) {
    if (command.isNotBlank()) {
        SpacerSmall()
        Text(
            text = label,
            style = AppTheme.typography.subtitle2,
            fontWeight = FontWeight.SemiBold
        )
        SpacerTiny()
        Text(
            text = description,
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.neutral600
        )
        SpacerTiny()
        InfoSurface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(PaddingDefaults.Small)
            ) {
                Text(
                    text = command,
                    style = AppTheme.typography.caption2,
                    color = AppTheme.colors.neutral800
                )
            }
        }
        SpacerTiny()
        DebugActionButton(
            text = "Copy $label",
            onClick = { onCopy(label, command) }
        )
    }
}

@Composable
private fun InfoSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(SizeDefaults.one),
        color = AppTheme.colors.neutral100,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
        modifier = modifier
    ) { content() }
}

@LightDarkLongPreview
@Composable
private fun DebugEncryptedPushSectionPreview() {
    PreviewAppTheme {
        DebugEncryptedPushSectionContent(
            fcmToken = "sample-fcm-token",
            oauthToken = "sample-oauth-token",
            cipherOutput = "sample-cipher-output-base64-content-that-is-long-enough-to-test-wrapping",
            decryptedOutput = DEFAULT_TEST_PAYLOAD,
            fcmCurlCommand = "curl -X POST FCM ...",
            pushGatewayCurlCommand = "curl -X POST Push Gateway ...",
            isSending = false,
            latestKeyGen = PushNotificationKeyGeneration(
                encryptionKey = "0123456789abcdef0123456789abcdef0123456789abcde",
                secret = "secret",
                month = "2023-10",
                keyIdentifier = "test-key-id-preview"
            ),
            onEncrypt = { _, _ -> },
            onUpdateOauthToken = {},
            onUpdateCurlCommands = {},
            onSendPush = {},
            onDecrypt = {},
            snackbarHostState = remember { SnackbarHostState() },
            initiallyExpanded = true
        )
    }
}
