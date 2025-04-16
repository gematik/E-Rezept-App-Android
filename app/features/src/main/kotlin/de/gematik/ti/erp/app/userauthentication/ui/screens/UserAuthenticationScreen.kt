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

package de.gematik.ti.erp.app.userauthentication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.userauthentication.navigation.UserAuthenticationRoutes
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.userauthentication.presentation.rememberAuthenticationController
import de.gematik.ti.erp.app.userauthentication.ui.components.GematikLogo
import de.gematik.ti.erp.app.userauthentication.ui.components.PasswordAuthenticationDialog
import de.gematik.ti.erp.app.userauthentication.ui.components.UserAuthenticationDataScreenContent
import de.gematik.ti.erp.app.userauthentication.ui.components.UserAuthenticationErrorScreenContent
import de.gematik.ti.erp.app.userauthentication.ui.components.UserAuthenticationEmptyScreenContent
import de.gematik.ti.erp.app.userauthentication.ui.preview.UserAuthenticationPreviewParameter
import de.gematik.ti.erp.app.userauthentication.ui.preview.UserAuthenticationPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState

class UserAuthenticationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val focusManager = LocalFocusManager.current
        val dialog = LocalDialog.current

        val authenticationController = rememberAuthenticationController()
        val authenticationState by authenticationController.authenticationState.collectAsStateWithLifecycle()
        val uiState by authenticationController.uiState.collectAsStateWithLifecycle(UiState.Loading())

        val authenticationEvent = authenticationController.authenticationWithPasswordEvent

        val onLeaveUserAuthenticationScreen: () -> Unit = {
            navController.popBackStack(
                UserAuthenticationRoutes.subGraphName(),
                inclusive = true
            )
        }

        LaunchedEffect(Unit) {
            if (authenticationState.authentication.methodIsUnspecified) {
                onLeaveUserAuthenticationScreen() // leave screen if no authentication is required
            }
        }

        AuthenticationWithPasswordEventListener(
            event = authenticationEvent,
            focusManager = focusManager,
            dialog = dialog,
            onChangePassword = { authenticationController.onChangePassword(password = it) },
            onAuthenticateWithPassword = {
                authenticationController.onAuthenticateWithPassword(
                    onSuccessLeaveAuthScreen = onLeaveUserAuthenticationScreen
                )
            }
        )

        BackHandler {} // override back swiping to not skip the auth process

        UserAuthenticationScreenScaffold(
            authenticationState = authenticationState,
            uiState = uiState,
            onSkipAuthentication = {
                authenticationController.onSuccessfulAuthentication {
                    onLeaveUserAuthenticationScreen()
                }
            },
            onShowPasswordDialog = { authenticationEvent.trigger(Unit) },
            onAuthenticate = {
                authenticationController.onClickAuthenticate(
                    onSuccessLeaveAuthScreen = onLeaveUserAuthenticationScreen
                )
            }
        )
    }
}

@Composable
private fun UserAuthenticationScreenScaffold(
    authenticationState: AuthenticationStateData.AuthenticationState,
    uiState: UiState<AuthenticationStateData.AuthenticationState>,
    onSkipAuthentication: () -> Unit,
    onShowPasswordDialog: () -> Unit,
    onAuthenticate: () -> Unit
) {
    Scaffold(
        topBar = {
            GematikLogo {
                onSkipAuthentication()
            }
        }
    ) { innerPadding ->
        UiStateMachine(
            state = uiState,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onEmpty = {
                UserAuthenticationEmptyScreenContent(
                    contentPadding = innerPadding,
                    authenticationState = authenticationState,
                    onAuthenticate = onAuthenticate,
                    onShowPasswordDialog = onShowPasswordDialog
                )
            },
            onError = {
                UserAuthenticationErrorScreenContent(
                    contentPadding = innerPadding,
                    authenticationState = authenticationState,
                    onAuthenticate = onAuthenticate,
                    onShowPasswordDialog = onShowPasswordDialog
                )
            },
            onContent = {
                UserAuthenticationDataScreenContent(
                    contentPadding = innerPadding,
                    authenticationState = authenticationState,
                    onAuthenticate = onAuthenticate,
                    onShowPasswordDialog = onShowPasswordDialog
                )
            }
        )
    }
}

@Composable
fun AuthenticationWithPasswordEventListener(
    event: ComposableEvent<Unit>,
    focusManager: FocusManager,
    dialog: DialogScaffold,
    onChangePassword: (String) -> Unit,
    onAuthenticateWithPassword: () -> Unit
) {
    event.listen {
        focusManager.clearFocus(true)
        dialog.show {
            PasswordAuthenticationDialog(
                onChangePassword = onChangePassword,
                onAuthenticate = {
                    onAuthenticateWithPassword()
                    it.dismiss()
                },
                onCancel = {
                    it.dismiss()
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun UserAuthenticationScreenPreview(
    @PreviewParameter(UserAuthenticationPreviewParameterProvider::class) previewData: UserAuthenticationPreviewParameter
) {
    PreviewAppTheme {
        UserAuthenticationScreenScaffold(
            authenticationState = previewData.authenticationState,
            uiState = previewData.uiState,
            onSkipAuthentication = {},
            onAuthenticate = {},
            onShowPasswordDialog = {}
        )
    }
}
