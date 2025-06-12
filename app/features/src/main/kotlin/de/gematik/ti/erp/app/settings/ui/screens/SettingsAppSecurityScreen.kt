/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.settings.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.ui.components.EnrollBiometricDialog
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.settings.presentation.rememberAppSecuritySettingsController
import de.gematik.ti.erp.app.settings.ui.preview.AppSecuritySettingsParameter
import de.gematik.ti.erp.app.settings.ui.preview.AppSecuritySettingsParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SwitchRightWithText
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

class SettingsAppSecurityScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val dialog = LocalDialog.current
        val deviceSecuritySettingsController = rememberAppSecuritySettingsController()
        val authenticationState by deviceSecuritySettingsController.authenticationState.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        with(deviceSecuritySettingsController.events) {
            OpenPasswordScreenEventListener(
                openPasswordScreenEvent = openPasswordScreenEvent,
                onNavigateToPasswordScreen = {
                    navController.navigate(SettingsNavigationScreens.SettingsSetAppPasswordScreen.path())
                }
            )
            EnrollBiometricDialog(
                context = context,
                dialog = dialog,
                event = enrollBiometryEvent
            )
        }

        BackHandler {
            navController.popBackStack()
        }

        SettingsAppSecurityScreenScaffold(
            listState = listState,
            authenticationState = authenticationState,
            onSwitchDeviceSecurityAuthentication = deviceSecuritySettingsController::onSwitchDeviceSecurityAuthentication,
            onSwitchPasswordAuthentication = deviceSecuritySettingsController::onSwitchPasswordAuthentication,
            onOpenPasswordScreen = {
                deviceSecuritySettingsController.events.openPasswordScreenEvent.trigger(Unit)
            },
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun SettingsAppSecurityScreenScaffold(
    listState: LazyListState,
    authenticationState: SettingsData.Authentication,
    onSwitchDeviceSecurityAuthentication: (Boolean) -> Unit,
    onSwitchPasswordAuthentication: (Boolean) -> Unit,
    onOpenPasswordScreen: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.settings_app_security_header),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack
    ) { contentPadding ->
        SettingsAppSecurityScreenContent(
            contentPadding,
            listState,
            authentication = authenticationState,
            onNavigateToPasswordScreen = onOpenPasswordScreen,
            onSwitchDeviceSecurityAuthentication = onSwitchDeviceSecurityAuthentication,
            onSwitchPasswordAuthentication = onSwitchPasswordAuthentication
        )
    }
}

@Composable
private fun SettingsAppSecurityScreenContent(
    contentPadding: PaddingValues,
    listState: LazyListState,
    authentication: SettingsData.Authentication,
    onNavigateToPasswordScreen: () -> Unit,
    onSwitchDeviceSecurityAuthentication: (Boolean) -> Unit,
    onSwitchPasswordAuthentication: (Boolean) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_app_security_info),
                style = AppTheme.typography.body1l,
                modifier = Modifier.padding(PaddingDefaults.Medium)
            )
        }
        item {
            DeviceSecuritySwitch(
                authentication = authentication,
                onSwitchDeviceSecurityAuthentication = onSwitchDeviceSecurityAuthentication
            )
        }
        item {
            PasswordSwitch(
                authentication = authentication,
                onSwitchPasswordAuthentication = onSwitchPasswordAuthentication
            )
        }
        item {
            if (authentication.passwordIsSet) {
                ChangePasswordSection(
                    text = stringResource(id = R.string.settings_app_security_change_password),
                    onOpenPasswordScreen = onNavigateToPasswordScreen
                )
            }
        }
    }
}

@Composable
private fun DeviceSecuritySwitch(
    authentication: SettingsData.Authentication,
    onSwitchDeviceSecurityAuthentication: (Boolean) -> Unit
) {
    SwitchRightWithText(
        text = stringResource(id = R.string.settings_app_security_device_security),
        checked = authentication.deviceSecurity,
        onCheckedChange = { onSwitchDeviceSecurityAuthentication(it) },
        enabled = !authentication.methodIsDeviceSecurity // enabled only if device security is not the only method
    )
}

@Composable
private fun PasswordSwitch(
    authentication: SettingsData.Authentication,
    onSwitchPasswordAuthentication: (Boolean) -> Unit
) {
    SwitchRightWithText(
        text = stringResource(id = R.string.settings_app_security_password),
        checked = authentication.passwordIsSet,
        onCheckedChange = { onSwitchPasswordAuthentication(it) },
        enabled = !authentication.methodIsPassword // enabled only if password is not the only method
    )
}

@Composable
private fun ChangePasswordSection(
    modifier: Modifier = Modifier,
    text: String,
    onOpenPasswordScreen: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SizeDefaults.double))
            .clickable {
                onOpenPasswordScreen()
            }
            .padding(PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = AppTheme.typography.body1,
            text = text
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = AppTheme.colors.primary700
        )
    }
}

@Composable
private fun OpenPasswordScreenEventListener(
    openPasswordScreenEvent: ComposableEvent<Unit>,
    onNavigateToPasswordScreen: () -> Unit
) {
    openPasswordScreenEvent.listen {
        onNavigateToPasswordScreen()
    }
}

@LightDarkPreview
@Composable
fun SettingsAppSecurityScreenScaffoldPreview(
    @PreviewParameter(AppSecuritySettingsParameterProvider::class) previewData: AppSecuritySettingsParameter
) {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        SettingsAppSecurityScreenScaffold(
            listState = listState,
            authenticationState = previewData.authentication,
            onSwitchDeviceSecurityAuthentication = {},
            onSwitchPasswordAuthentication = {},
            onOpenPasswordScreen = {},
            onBack = {}
        )
    }
}
