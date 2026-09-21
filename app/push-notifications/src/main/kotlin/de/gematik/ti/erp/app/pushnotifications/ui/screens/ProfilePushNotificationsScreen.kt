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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.material3.components.switchs.LabeledSwitch
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkLongPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.pushnotifications.presentation.rememberProfilePushNotificationsController
import de.gematik.ti.erp.app.pushnotifications.ui.preview.PushNotificationsPreviewParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

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
        val notificationSettings by controller.notificationSettings.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }

        ProfilePushNotificationsScaffold(
            listState = listState,
            notificationSettings = notificationSettings,
            onToggleNotification = controller::onToggle,
            onBack = { onBack() }
        )
    }
}

@Composable
fun ProfilePushNotificationsScaffold(
    listState: LazyListState,
    notificationSettings: ProfilePushNotificationSettings,
    onToggleNotification: (ProfilePushNotificationType, Boolean) -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.profile_push_notifications),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack
    ) { contentPadding ->
        ProfilePushNotificationsContent(
            contentPadding = contentPadding,
            listState = listState,
            notificationSettings = notificationSettings,
            onToggleNotification = onToggleNotification
        )
    }
}

@Composable
private fun ProfilePushNotificationsContent(
    contentPadding: PaddingValues,
    listState: LazyListState,
    notificationSettings: ProfilePushNotificationSettings,
    onToggleNotification: (ProfilePushNotificationType, Boolean) -> Unit
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
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.NEW_PRESCRIPTION, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_new_message),
                checked = notificationSettings.newMessageEnabled,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.NEW_MESSAGE, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_status_change),
                checked = notificationSettings.statusChangeEnabled,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.STATUS_CHANGE, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_new_invoice),
                checked = notificationSettings.newInvoiceEnabled,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.NEW_INVOICE, it)
                }
            )
        }
        item {
            LabeledSwitch(
                text = stringResource(R.string.profile_push_notifications_external_access),
                checked = notificationSettings.externalAccessEnabled,
                onCheckedChange = {
                    onToggleNotification(ProfilePushNotificationType.EXTERNAL_ACCESS, it)
                }
            )
        }
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
            notificationSettings = notificationSettings,
            onToggleNotification = { _, _ -> },
            onBack = {}
        )
    }
}
