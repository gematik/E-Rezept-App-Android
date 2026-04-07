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

package de.gematik.ti.erp.app.debugsettings.encryption.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.database.room.security.RoomEncryptionConfig
import de.gematik.ti.erp.app.debugsettings.timeout.intent.restartApp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.debugsettings.ui.components.DebugCard
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

/**
 * Debug-only screen for testing database encryption in debug builds.
 *
 * - "Enable Encryption" generates a passphrase, flags encryption as forced, deletes the
 *   current DB (clean slate) and restarts the app — the DB then reopens encrypted.
 * - "Disable Encryption" clears the flag, deletes the passphrase and DB, restarts the app
 *   — the DB reopens as plain SQLite.
 * - "Delete Key Only" removes the passphrase without touching the DB; the encrypted DB will
 *   be unreadable on the next start (useful to test error-recovery paths).
 *
 * In release builds this screen is never shown; encryption is always on automatically.
 */
object DebugDatabaseEncryptionScreen {

    @Composable
    fun Content(onBack: () -> Unit) {
        val context = LocalContext.current
        val activity = LocalActivity.current
        val listState = rememberLazyListState()

        val isEncrypted = remember { RoomEncryptionConfig.isDebugEncryptionForced(context) }
        val passphrase = remember { RoomEncryptionConfig.getPassphraseBase64OrNull(context) }

        // Confirmation state for the destructive "Delete Key Only" action
        var showDeleteKeyConfirmation by remember { mutableStateOf(false) }

        AnimatedElevationScaffold(
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            topBarTitle = "DB Encryption",
            backLabel = "Back",
            closeLabel = "Close",
            onBack = onBack
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(PaddingDefaults.Medium),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
            ) {
                item {
                    StatusCard(isEncrypted = isEncrypted)
                }
                item {
                    PassphraseCard(passphrase = passphrase, context = context)
                }
                item {
                    ActionsCard(
                        isEncrypted = isEncrypted,
                        onEnableEncryption = {
                            enableEncryption(context)
                            restartApp<MainActivity>(activity)
                        },
                        onDisableEncryption = {
                            disableEncryption(context)
                            restartApp<MainActivity>(activity)
                        },
                        showDeleteKeyConfirmation = showDeleteKeyConfirmation,
                        onRequestDeleteKey = { showDeleteKeyConfirmation = true },
                        onConfirmDeleteKey = {
                            RoomEncryptionConfig.deletePassphrase(context)
                            showDeleteKeyConfirmation = false
                        },
                        onCancelDeleteKey = { showDeleteKeyConfirmation = false }
                    )
                }
            }
        }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun enableEncryption(context: Context) {
        val dbName = "room.db"
        // Flag must be set BEFORE the DB files are deleted so getDatabaseBuilder
        // picks it up and applies the plaintext-detection logic on restart.
        RoomEncryptionConfig.setDebugEncryptionForced(context, true)
        // Delete DB so Room creates a fresh encrypted one on next start.
        context.getDatabasePath(dbName).delete()
        context.getDatabasePath("$dbName-shm").delete()
        context.getDatabasePath("$dbName-wal").delete()
    }

    private fun disableEncryption(context: Context) {
        val dbName = "room.db"
        RoomEncryptionConfig.setDebugEncryptionForced(context, false)
        RoomEncryptionConfig.deletePassphrase(context)
        context.getDatabasePath(dbName).delete()
        context.getDatabasePath("$dbName-shm").delete()
        context.getDatabasePath("$dbName-wal").delete()
    }
}

// ─── UI components ───────────────────────────────────────────────────────────

@Composable
private fun StatusCard(isEncrypted: Boolean) {
    DebugCard(title = "Status") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            Icon(
                imageVector = if (isEncrypted) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                contentDescription = null,
                tint = if (isEncrypted) AppTheme.colors.green600 else AppTheme.colors.red600
            )
            Text(
                text = if (isEncrypted) "Encrypted (debug override active)" else "Not encrypted (debug build default)",
                style = AppTheme.typography.body2,
                fontWeight = FontWeight.SemiBold,
                color = if (isEncrypted) AppTheme.colors.green600 else AppTheme.colors.red600
            )
        }
        SpacerSmall()
        Text(
            text = "In release builds the DB is always encrypted. This screen lets you test encryption in debug builds.",
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral600
        )
    }
}

@Composable
private fun PassphraseCard(passphrase: String?, context: Context) {
    DebugCard(title = "Passphrase (Base64)") {
        if (passphrase != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = passphrase,
                    style = AppTheme.typography.caption1,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = AppTheme.colors.neutral800
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("DB passphrase", passphrase))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy passphrase",
                        tint = AppTheme.colors.primary600
                    )
                }
            }
        } else {
            Text(
                text = "No passphrase stored yet.",
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.neutral500
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ActionsCard(
    isEncrypted: Boolean,
    onEnableEncryption: () -> Unit,
    onDisableEncryption: () -> Unit,
    showDeleteKeyConfirmation: Boolean,
    onRequestDeleteKey: () -> Unit,
    onConfirmDeleteKey: () -> Unit,
    onCancelDeleteKey: () -> Unit
) {
    DebugCard(title = "Actions") {
        if (!isEncrypted) {
            DebugFullWidthButton(
                text = "Enable Encryption & Restart",
                color = AppTheme.colors.primary600,
                onClick = onEnableEncryption
            )
        } else {
            DebugFullWidthButton(
                text = "Disable Encryption & Restart",
                color = AppTheme.colors.primary600,
                onClick = onDisableEncryption
            )
            SpacerTiny()
            if (!showDeleteKeyConfirmation) {
                DebugFullWidthButton(
                    text = "Delete Key Only (DB becomes unreadable)",
                    color = AppTheme.colors.red600,
                    onClick = onRequestDeleteKey
                )
            } else {
                Text(
                    text = "⚠ The DB will be unreadable on next start. Disable encryption afterwards to recover.",
                    style = AppTheme.typography.caption1,
                    color = AppTheme.colors.red600
                )
                SpacerTiny()
                Row(horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
                    DebugFullWidthButton(
                        modifier = Modifier.weight(1f),
                        text = "Confirm",
                        color = AppTheme.colors.red600,
                        onClick = onConfirmDeleteKey
                    )
                    DebugFullWidthButton(
                        modifier = Modifier.weight(1f),
                        text = "Cancel",
                        color = AppTheme.colors.neutral600,
                        onClick = onCancelDeleteKey
                    )
                }
            }
        }
        SpacerMedium()
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = AppTheme.colors.yellow600,
                modifier = Modifier.padding(top = SizeDefaults.quarter)
            )
            Text(
                text = "Enable/Disable will delete the current database. All local app data will be lost.",
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@Composable
private fun DebugFullWidthButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = color)
    ) {
        Text(
            text = text,
            color = AppTheme.colors.neutral000,
            style = MaterialTheme.typography.button
        )
    }
}
