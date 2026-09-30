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

package de.gematik.ti.erp.app.pushnotifications.ui.components

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.presentation.rememberPushOnboardingController
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText

@Composable
fun FirstLoginPushNotificationOnboarding(
    profileId: ProfileIdentifier,
    isAuthenticated: Boolean
) {
    val controller = rememberPushOnboardingController(profileId)
    val context = LocalContext.current
    val showConsentDialog by controller.showConsentDialog.collectAsStateWithLifecycle()

    var showEnableSettingsDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        controller.onConsentAccepted()
    }

    LaunchedEffect(profileId, isAuthenticated) {
        if (isAuthenticated) {
            controller.checkFirstLogin()
        }
    }

    controller.showEnableNotificationsEvent.listen {
        showEnableSettingsDialog = !NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    if (showConsentDialog) {
        PushNotificationConsentDialog(
            onDismissRequest = {
                controller.onConsentDeclined()
            },
            onConfirmRequest = {
                controller.onConsentConfirmed()
                val needsRuntimePermission =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (needsRuntimePermission) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    controller.onConsentAccepted()
                }
            }
        )
    }

    if (showEnableSettingsDialog) {
        PushNotificationEnableSettingsDialog(
            onDismissRequest = { showEnableSettingsDialog = false },
            onOpenSettings = {
                showEnableSettingsDialog = false
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            }
        )
    }
}

@Composable
private fun PushNotificationConsentDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.profile_push_notifications_permission_prompt_title),
        titleAlignment = ErezeptText.TextAlignment.Default,
        bodyText = stringResource(R.string.profile_push_notifications_permission_prompt_body),
        dismissText = stringResource(R.string.profile_push_notifications_permission_prompt_dismiss),
        confirmText = stringResource(R.string.profile_push_notifications_permission_prompt_confirm),
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onConfirmRequest
    )
}

@Composable
private fun PushNotificationEnableSettingsDialog(
    onDismissRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.profile_push_notifications_enable_title),
        titleAlignment = ErezeptText.TextAlignment.Default,
        bodyText = stringResource(R.string.profile_push_notifications_enable_body),
        dismissText = stringResource(R.string.profile_push_notifications_enable_dismiss),
        confirmText = stringResource(R.string.profile_push_notifications_enable_confirm),
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onOpenSettings
    )
}
