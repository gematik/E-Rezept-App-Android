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

package de.gematik.ti.erp.app.pushnotifications.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.material3.components.switchs.LabeledSwitch
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.pushnotifications.navigation.PushNotificationsRoutes
import de.gematik.ti.erp.app.pushnotifications.presentation.ProfilePushNotificationsUiState
import de.gematik.ti.erp.app.pushnotifications.presentation.rememberProfilePushNotificationsController
import de.gematik.ti.erp.app.pushnotifications.ui.preview.PushNotificationsPreviewParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

class ProfilePushNotificationsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val profileId = remember {
            requireNotNull(navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID))
        }
        val controller = rememberProfilePushNotificationsController(profileId)
        val uiState by controller.uiState.collectAsStateWithLifecycle()
        val activeProfile by controller.combinedProfile.collectAsStateWithLifecycle()
        val isSsoTokenValid by controller.isSsoTokenValidForSelectedProfile.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val dialog = LocalDialog.current
        val onBack by rememberUpdatedState { navController.popBackStack() }
        val context = LocalContext.current
        BackHandler { onBack() }

        ChooseAuthenticationNavigationEventsListener(
            controller,
            navController,
            dialogScaffold = dialog
        )

        LaunchedEffect(isSsoTokenValid) {
            controller.onAuthenticationStateChanged(isSsoTokenValid)
        }

        LaunchedEffect(uiState, isSsoTokenValid) {
            if (isSsoTokenValid && uiState is ProfilePushNotificationsUiState.RegistrationRequired) {
                dialog.show { shownDialog ->
                    ProfilePushNotificationsPermissionPromptDialog(
                        onDismissRequest = {
                            controller.onPermissionPromptDecision(false)
                            shownDialog.dismiss()
                        },
                        onConfirmRequest = {
                            controller.onPermissionPromptDecision(true)
                            shownDialog.dismiss()
                        }
                    )
                }
            }
        }

        controller.showEnableNotificationsEvent.listen {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                dialog.show { shownDialog ->
                    ProfilePushNotificationsEnableDialog(
                        onDismissRequest = shownDialog::dismiss,
                        onOpenSettings = {
                            shownDialog.dismiss()
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            )
                        }
                    )
                }
            }
        }

        when (val currentUiState = uiState) {
            is ProfilePushNotificationsUiState.Error -> ProfilePushNotificationsSyncErrorDialog(
                onRetry = controller::retryPushNotificationSync,
                onDismissRequest = {
                    controller.dismissPushNotificationSyncError()
                    if (currentUiState.isInitialSetup) onBack()
                }
            )
            else -> Unit
        }

        val notificationSettings = when (val currentUiState = uiState) {
            is ProfilePushNotificationsUiState.Ready -> currentUiState.settings
            is ProfilePushNotificationsUiState.Loading -> currentUiState.previousSettings
            is ProfilePushNotificationsUiState.Error -> currentUiState.previousSettings
            else -> null
        } ?: ProfilePushNotificationSettings(
            newPrescriptionEnabled = false,
            newMessageEnabled = false,
            statusChangeEnabled = false,
            newInvoiceEnabled = false,
            externalAccessEnabled = false
        )
        val areTogglesEnabled = isSsoTokenValid && uiState.allowsToggleChanges()
        val isLoading = uiState.isInitialLoading()

        ProfilePushNotificationsScaffold(
            listState = listState,
            isSsoTokenValid = isSsoTokenValid,
            isSyncing = !areTogglesEnabled,
            isLoading = isLoading,
            notificationSettings = notificationSettings,
            onToggleNotification = controller::onToggle,
            onLogin = { activeProfile.data?.selectedProfile?.let { controller.chooseAuthenticationMethod(it) } },
            onOpenRegisteredDevices = {
                navController.navigate(PushNotificationsRoutes.RegisteredPushDevicesScreen.path(profileId))
            },
            onBack = { onBack() }
        )
    }
}

private fun ProfilePushNotificationsUiState.allowsToggleChanges(): Boolean =
    this is ProfilePushNotificationsUiState.Ready ||
        this is ProfilePushNotificationsUiState.Disabled

private fun ProfilePushNotificationsUiState.isInitialLoading(): Boolean =
    this is ProfilePushNotificationsUiState.Loading && previousSettings == null

@Composable
private fun ProfilePushNotificationsPermissionPromptDialog(
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
private fun ProfilePushNotificationsSyncErrorDialog(
    onRetry: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.generic_error_title),
        bodyText = stringResource(R.string.generic_error_info),
        dismissText = stringResource(R.string.cancel),
        confirmText = stringResource(R.string.generic_error_retry),
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onRetry
    )
}

@Composable
private fun ProfilePushNotificationsEnableDialog(
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

@Composable
fun ProfilePushNotificationsScaffold(
    listState: LazyListState,
    isSsoTokenValid: Boolean,
    isSyncing: Boolean,
    notificationSettings: ProfilePushNotificationSettings,
    onToggleNotification: (ProfilePushNotificationType, Boolean) -> Unit,
    onOpenRegisteredDevices: () -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.profile_push_notifications),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        bottomBar = {
            if (!isSsoTokenValid) {
                ProfilePushNotificationsLoginBottomBar(
                    infoText = stringResource(R.string.profile_push_notifications_login_hint),
                    buttonText = stringResource(R.string.profile_push_notifications_login_button),
                    onClick = onLogin
                )
            }
        },
        onBack = onBack
    ) { contentPadding ->
        if (isLoading) {
            Center {
                FullScreenLoadingIndicator()
            }
        } else {
            ProfilePushNotificationsContent(
                contentPadding = contentPadding,
                listState = listState,
                isSsoTokenValid = isSsoTokenValid,
                isSyncing = isSyncing,
                notificationSettings = notificationSettings,
                onToggleNotification = onToggleNotification,
                onOpenRegisteredDevices = onOpenRegisteredDevices
            )
        }
    }
}

@Composable
private fun ProfilePushNotificationsLoginBottomBar(
    infoText: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.primary100)
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Large
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = infoText,
            modifier = Modifier
                .weight(1f)
                .padding(end = PaddingDefaults.Large),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.neutral900
        )
        Button(onClick = onClick) {
            Text(text = buttonText)
        }
    }
}

@Composable
private fun ProfilePushNotificationsContent(
    contentPadding: PaddingValues,
    listState: LazyListState,
    isSsoTokenValid: Boolean,
    isSyncing: Boolean,
    notificationSettings: ProfilePushNotificationSettings,
    onToggleNotification: (ProfilePushNotificationType, Boolean) -> Unit,
    onOpenRegisteredDevices: () -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_push_notifications_info),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral700,
                modifier = Modifier.padding(PaddingDefaults.Medium)
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_new_prescription),
                checked = notificationSettings.newPrescriptionEnabled,
                enabled = isSsoTokenValid && !isSyncing,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.NEW_PRESCRIPTION, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_new_message),
                checked = notificationSettings.newMessageEnabled,
                enabled = isSsoTokenValid && !isSyncing,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.NEW_MESSAGE, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_status_change),
                checked = notificationSettings.statusChangeEnabled,
                enabled = isSsoTokenValid && !isSyncing,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.STATUS_CHANGE, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_new_invoice),
                checked = notificationSettings.newInvoiceEnabled,
                enabled = isSsoTokenValid && !isSyncing,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.NEW_INVOICE, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_external_access),
                checked = notificationSettings.externalAccessEnabled,
                enabled = isSsoTokenValid && !isSyncing,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.EXTERNAL_ACCESS, it)
                }
            )
        }
        item {
            ProfileRegisteredDevicesRow(
                enabled = isSsoTokenValid,
                onClick = onOpenRegisteredDevices
            )
        }
    }
}

@Composable
private fun ProfileRegisteredDevicesRow(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) AppTheme.colors.neutral900 else AppTheme.colors.neutral400
    val actionLabel = stringResource(R.string.a11y_open_registered_devices)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
                onClickLabel = actionLabel,
                role = Role.Button
            )
            .semantics { if (!enabled) disabled() }
            .padding(PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Devices,
                contentDescription = null,
                tint = contentColor
            )
            Text(
                text = stringResource(R.string.profile_push_notifications_registered_devices),
                modifier = Modifier.padding(start = PaddingDefaults.Medium),
                style = AppTheme.typography.body1,
                color = contentColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = contentColor
        )
    }
}

@LightDarkLongPreview
@Composable
fun ProfilePushNotificationsScreenScaffoldPreview(
    @PreviewParameter(PushNotificationsPreviewParameterProvider::class) notificationSettings: ProfilePushNotificationSettings
) {
    PreviewTheme {
        ProfilePushNotificationsScaffold(
            listState = rememberLazyListState(),
            isSsoTokenValid = false,
            isSyncing = false,
            notificationSettings = notificationSettings,
            onToggleNotification = { _, _ -> },
            onLogin = {},
            onOpenRegisteredDevices = {},
            onBack = {}
        )
    }
}
