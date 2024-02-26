/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.settings.presentation.SettingStatesData
import de.gematik.ti.erp.app.settings.presentation.rememberDeviceSecuritySettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.ui.BiometricPromptWrapper
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium

class SettingsDeviceSecurityScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val settingsController = rememberDeviceSecuritySettingsController()
        val authenticationModeState by settingsController.authenticationModeState
        val listState = rememberLazyListState()
        var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }

        if (showBiometricPrompt) {
            BiometricPromptWrapper(
                title = stringResource(R.string.auth_prompt_headline),
                description = "",
                negativeButton = stringResource(R.string.auth_prompt_cancel),
                onAuthenticated = {
                    settingsController.onSelectDeviceSecurityAuthenticationMode()
                    showBiometricPrompt = false
                },
                onCancel = {
                    showBiometricPrompt = false
                },
                onAuthenticationError = {
                    showBiometricPrompt = false
                },
                onAuthenticationSoftError = {
                }
            )
        }

        AnimatedElevationScaffold(
            topBarTitle = stringResource(R.string.settings_device_security_header),
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            onBack = navController::popBackStack
        ) { contentPadding ->
            SettingsDeviceSecurityScreenContent(
                contentPadding,
                listState,
                authenticationModeState,
                onNavigateToPasswordScreen = {
                    navController.navigate(SettingsNavigationScreens.SettingsSetAppPasswordScreen.path())
                },
                onShowBiometricPrompt = { showBiometricPrompt = it }
            )
        }
    }
}

@Composable
private fun SettingsDeviceSecurityScreenContent(
    contentPadding: PaddingValues,
    listState: LazyListState,
    authenticationModeState: SettingStatesData.AuthenticationModeState,
    onNavigateToPasswordScreen: () -> Unit,
    onShowBiometricPrompt: (Boolean) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            SpacerMedium()
            AuthenticationModeCard(
                Icons.Outlined.Fingerprint,
                checked = authenticationModeState.authenticationMode is
                SettingsData.AuthenticationMode.DeviceSecurity,
                headline = stringResource(R.string.settings_appprotection_device_security_header),
                info = stringResource(R.string.settings_appprotection_device_security_info),
                deviceSecurity = true
            ) {
                onShowBiometricPrompt(true)
            }
        }
        item {
            @Requirement(
                "O.Pass_3",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "The user may change the app passwords within the settings."
            )
            AuthenticationModeCard(
                Icons.Outlined.Security,
                checked = authenticationModeState.authenticationMode is
                SettingsData.AuthenticationMode.Password,
                headline = stringResource(R.string.settings_appprotection_mode_password_headline),
                info = stringResource(R.string.settings_appprotection_mode_password_info)
            ) {
                onNavigateToPasswordScreen()
            }
        }
    }
}

@Composable
private fun AuthenticationModeCard(
    icon: ImageVector,
    checked: Boolean,
    headline: String,
    info: String,
    deviceSecurity: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var showAllowDeviceSecurity by remember { mutableStateOf(false) }

    if (deviceSecurity && showAllowDeviceSecurity && !checked) {
        CommonAlertDialog(
            header = stringResource(R.string.settings_biometric_dialog_title),
            info = stringResource(R.string.settings_biometric_dialog_text),
            actionText = stringResource(R.string.settings_device_security_allow),
            cancelText = stringResource(R.string.cancel),
            onCancel = { showAllowDeviceSecurity = false },
            onClickAction = {
                onClick()
                showAllowDeviceSecurity = false
            }
        )
    }

    val alpha = remember { Animatable(0.0f) }

    LaunchedEffect(checked) {
        if (checked) {
            alpha.animateTo(1.0f)
        } else {
            alpha.animateTo(0.0f)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(
                onClick = {
                    if (deviceSecurity) {
                        showAllowDeviceSecurity = true
                    } else {
                        onClick()
                    }
                },
                enabled = enabled
            )
            .padding(PaddingDefaults.Medium)
    ) {
        Icon(
            icon,
            null,
            tint = AppTheme.colors.primary500,
            modifier = Modifier.padding(end = PaddingDefaults.Small)
        )
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = headline,
                style = AppTheme.typography.body1
            )
            Text(
                text = info,
                style = AppTheme.typography.body2l
            )
        }

        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(
                Icons.Rounded.RadioButtonUnchecked,
                null,
                tint = AppTheme.colors.neutral400
            )
            Icon(
                Icons.Rounded.CheckCircle,
                null,
                tint = AppTheme.colors.primary600,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
