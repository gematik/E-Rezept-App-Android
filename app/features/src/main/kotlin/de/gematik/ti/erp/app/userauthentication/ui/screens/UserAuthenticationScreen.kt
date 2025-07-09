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

package de.gematik.ti.erp.app.userauthentication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationActions
import de.gematik.ti.erp.app.userauthentication.navigation.UserAuthenticationRoutes
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.userauthentication.presentation.rememberAuthenticationController
import de.gematik.ti.erp.app.userauthentication.ui.components.GematikLogo
import de.gematik.ti.erp.app.userauthentication.ui.components.UserAuthenticationDataScreenContent
import de.gematik.ti.erp.app.userauthentication.ui.components.UserAuthenticationErrorScreenContent
import de.gematik.ti.erp.app.userauthentication.ui.components.UserAuthenticationEmptyScreenContent
import de.gematik.ti.erp.app.userauthentication.ui.preview.UserAuthenticationPreviewParameter
import de.gematik.ti.erp.app.userauthentication.ui.preview.UserAuthenticationPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class UserAuthenticationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val authenticationController = rememberAuthenticationController()
        val authenticationState by authenticationController.authenticationState.collectAsStateWithLifecycle()
        val uiState by authenticationController.uiState.collectAsStateWithLifecycle(UiState.Loading())

        val timeout by authenticationController.authenticationTimeOut.collectAsStateWithLifecycle(0)
        val showPasswordLogin by authenticationController.showPasswordLogin.collectAsStateWithLifecycle(false)
        val enteredPassword by authenticationController.enteredPassword.collectAsStateWithLifecycle("")
        val enteredPasswordError by authenticationController.enteredPasswordError.collectAsStateWithLifecycle(false)

        val onLeaveUserAuthenticationScreen: () -> Unit = {
            navController.popBackStack(
                UserAuthenticationRoutes.subGraphName(),
                inclusive = true
            )
        }

        val userAuthenticationActions = UserAuthenticationActions(
            onShowPasswordLogin = { authenticationController.onShowPasswordLogin() },
            onHidePasswordLogin = { authenticationController.onHidePasswordLogin() },
            onChangeEnteredPassword = { authenticationController.onChangeEnteredPassword(it) },
            onRemovePasswordError = { authenticationController.onRemovePasswordError() },
            onAuthenticateWithPassword = { authenticationController.onAuthenticateWithPassword { onLeaveUserAuthenticationScreen() } },
            onAuthenticateWithDeviceSecurity = { authenticationController.onAuthenticateWithDeviceSecurity { onLeaveUserAuthenticationScreen() } },
            onSkipAuthentication = { authenticationController.onSuccessfulAuthentication { onLeaveUserAuthenticationScreen() } }
        )

        LaunchedEffect(Unit) {
            if (authenticationState.authentication.methodIsUnspecified) {
                onLeaveUserAuthenticationScreen() // leave screen if no authentication is required
            }
        }

        BackHandler {} // override back swiping to not skip the auth process

        UserAuthenticationScreenScaffold(
            authenticationState = authenticationState,
            timeout = timeout,
            enteredPassword = enteredPassword,
            enteredPasswordError = enteredPasswordError,
            showPasswordLogin = showPasswordLogin,
            uiState = uiState,
            userAuthenticationActions = userAuthenticationActions
        )
    }
}

@Composable
private fun UserAuthenticationScreenScaffold(
    authenticationState: AuthenticationStateData.AuthenticationState,
    timeout: Long,
    enteredPassword: String,
    enteredPasswordError: Boolean,
    showPasswordLogin: Boolean,
    uiState: UiState<AuthenticationStateData.AuthenticationState>,
    userAuthenticationActions: UserAuthenticationActions
) {
    Scaffold(
        topBar = {
            GematikLogo {
                userAuthenticationActions.onSkipAuthentication()
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
                    timeout = timeout,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    showPasswordLogin = showPasswordLogin,
                    authenticationState = authenticationState,
                    userAuthenticationActions = userAuthenticationActions
                )
            },
            onError = {
                UserAuthenticationErrorScreenContent(
                    contentPadding = innerPadding,
                    timeout = timeout,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    showPasswordLogin = showPasswordLogin,
                    authenticationState = authenticationState,
                    userAuthenticationActions = userAuthenticationActions
                )
            },
            onContent = {
                UserAuthenticationDataScreenContent(
                    contentPadding = innerPadding,
                    timeout = timeout,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    showPasswordLogin = showPasswordLogin,
                    authenticationState = authenticationState,
                    userAuthenticationActions = userAuthenticationActions
                )
            }
        )
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
            timeout = 0,
            uiState = previewData.uiState,
            userAuthenticationActions = UserAuthenticationActions(
                onShowPasswordLogin = { },
                onHidePasswordLogin = { },
                onChangeEnteredPassword = { },
                onAuthenticateWithPassword = { },
                onAuthenticateWithDeviceSecurity = { },
                onSkipAuthentication = { },
                onRemovePasswordError = {}
            ),
            showPasswordLogin = false,
            enteredPasswordError = false,
            enteredPassword = ""
        )
    }
}
