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

package de.gematik.ti.erp.app.appauthentication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationActions
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationMethodErpModel
import de.gematik.ti.erp.app.appauthentication.navigation.AppAuthenticationRoutes
import de.gematik.ti.erp.app.appauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.appauthentication.presentation.rememberAuthenticationController
import de.gematik.ti.erp.app.appauthentication.ui.components.GematikLogo
import de.gematik.ti.erp.app.appauthentication.ui.components.AppAuthenticationDataScreenContent
import de.gematik.ti.erp.app.appauthentication.ui.components.AppAuthenticationEmptyScreenContent
import de.gematik.ti.erp.app.appauthentication.ui.components.AppAuthenticationErrorScreenContent
import de.gematik.ti.erp.app.appauthentication.ui.preview.AppAuthenticationPreviewParameter
import de.gematik.ti.erp.app.appauthentication.ui.preview.AppAuthenticationPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.android.awaitFrame

class AppAuthenticationScreen(
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
        val focusRequester = remember { FocusRequester() }
        val onLeaveAppAuthenticationScreen: () -> Unit = {
            navController.popBackStack(
                AppAuthenticationRoutes.subGraphName(),
                inclusive = true
            )
        }

        val appAuthenticationActions = AppAuthenticationActions(
            onShowPasswordLogin = { authenticationController.onShowPasswordLogin() },
            onHidePasswordLogin = { authenticationController.onHidePasswordLogin() },
            onChangeEnteredPassword = { authenticationController.onChangeEnteredPassword(it) },
            onRemovePasswordError = { authenticationController.onRemovePasswordError() },
            onAuthenticateWithPassword = { authenticationController.onAuthenticateWithPassword { onLeaveAppAuthenticationScreen() } },
            onAuthenticateWithDeviceSecurity = { authenticationController.onAuthenticateWithDeviceSecurity { onLeaveAppAuthenticationScreen() } },
            onSkipAuthentication = { authenticationController.onSuccessfulAuthentication { onLeaveAppAuthenticationScreen() } }
        )

        LaunchedEffect(Unit) {
            val authMethod = authenticationState.authentication.authenticationMethod
            when {
                authMethod is AppAuthenticationMethodErpModel.Both || authMethod is AppAuthenticationMethodErpModel.DeviceSecurity -> {
                    authenticationController.onAuthenticateWithDeviceSecurity { onLeaveAppAuthenticationScreen() }
                }

                authMethod is AppAuthenticationMethodErpModel.Password -> {
                    awaitFrame()
                    focusRequester.requestFocus()
                }

                authMethod is AppAuthenticationMethodErpModel.NotInitialised -> {
                    onLeaveAppAuthenticationScreen()
                }
            }
        }

        BackHandler {} // override back swiping to not skip the auth process

        AppAuthenticationScreenScaffold(
            authenticationState = authenticationState,
            timeout = timeout,
            focusRequester = focusRequester,
            enteredPassword = enteredPassword,
            enteredPasswordError = enteredPasswordError,
            showPasswordLogin = showPasswordLogin,
            uiState = uiState,
            appAuthenticationActions = appAuthenticationActions
        )
    }
}

@Composable
private fun AppAuthenticationScreenScaffold(
    authenticationState: AuthenticationStateData.AuthenticationState,
    timeout: Long,
    focusRequester: FocusRequester,
    enteredPassword: String,
    enteredPasswordError: Boolean,
    showPasswordLogin: Boolean,
    uiState: UiState<AuthenticationStateData.AuthenticationState>,
    appAuthenticationActions: AppAuthenticationActions
) {
    Scaffold(
        topBar = {
            GematikLogo {
                appAuthenticationActions.onSkipAuthentication()
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
                AppAuthenticationEmptyScreenContent(
                    contentPadding = innerPadding,
                    timeout = timeout,
                    focusRequester = focusRequester,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    showPasswordLogin = showPasswordLogin,
                    authenticationState = authenticationState,
                    appAuthenticationActions = appAuthenticationActions
                )
            },
            onError = {
                AppAuthenticationErrorScreenContent(
                    contentPadding = innerPadding,
                    timeout = timeout,
                    focusRequester = focusRequester,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    showPasswordLogin = showPasswordLogin,
                    authenticationState = authenticationState,
                    appAuthenticationActions = appAuthenticationActions
                )
            },
            onContent = {
                AppAuthenticationDataScreenContent(
                    contentPadding = innerPadding,
                    timeout = timeout,
                    focusRequester = focusRequester,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    showPasswordLogin = showPasswordLogin,
                    authenticationState = authenticationState,
                    appAuthenticationActions = appAuthenticationActions
                )
            }
        )
    }
}

@LightDarkPreview
@Composable
fun AppAuthenticationScreenPreview(
    @PreviewParameter(AppAuthenticationPreviewParameterProvider::class) previewData: AppAuthenticationPreviewParameter
) {
    PreviewAppTheme {
        AppAuthenticationScreenScaffold(
            authenticationState = previewData.authenticationState,
            timeout = 0,
            uiState = previewData.uiState,
            appAuthenticationActions = AppAuthenticationActions(
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
            focusRequester = remember { FocusRequester() },
            enteredPassword = ""
        )
    }
}
