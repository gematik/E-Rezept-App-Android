/*
 * Copyright 2024, gematik GmbH
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.presentation.rememberPasswordSettingsController
import de.gematik.ti.erp.app.settings.ui.preview.SetAppPasswordParameter
import de.gematik.ti.erp.app.settings.ui.preview.SetAppPasswordParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PasswordStrength
import de.gematik.ti.erp.app.utils.compose.PasswordTextField
import de.gematik.ti.erp.app.utils.compose.presentation.PasswordFieldsData
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class SettingsSetAppPasswordScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val focusRequester = remember { FocusRequester() }
        val listState = rememberLazyListState()

        val settingsController = rememberPasswordSettingsController()
        val passwordFieldsState by settingsController.passwordFieldsState.collectAsStateWithLifecycle()

        SettingsSetAppPasswordScreenScaffold(
            listState = listState,
            focusRequester = focusRequester,
            passwordFieldsState = passwordFieldsState,
            onBack = navController::popBackStack,
            setAppPassword = settingsController::setAppPassword,
            onPasswordChange = settingsController::onPasswordChange,
            onRepeatedPasswordChange = settingsController::onRepeatedPasswordChange
        )
    }
}

@Composable
private fun SettingsSetAppPasswordScreenScaffold(
    listState: LazyListState,
    focusRequester: FocusRequester,
    passwordFieldsState: PasswordFieldsData,
    onBack: () -> Unit,
    setAppPassword: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.settings_password_header),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = onBack,
        bottomBar = {
            SettingsSetAppPasswordBottomBar(
                onBack = onBack,
                setAppPassword = setAppPassword,
                passwordFieldsState = passwordFieldsState
            )
        }
    ) { innerPadding ->
        SettingsSetAppPasswordScreenContent(
            innerPadding,
            listState,
            focusRequester,
            passwordFieldsState = passwordFieldsState,
            onAuthenticateWithPassword = {
                setAppPassword()
                onBack()
            },
            onPasswordChange = {
                onPasswordChange(it)
            },
            onRepeatedPasswordChange = {
                onRepeatedPasswordChange(it)
            }
        )
    }
}

@Composable
private fun SettingsSetAppPasswordScreenContent(
    innerPadding: PaddingValues,
    listState: LazyListState,
    focusRequester: FocusRequester,
    passwordFieldsState: PasswordFieldsData,
    onAuthenticateWithPassword: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium),
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_password_body),
                style = AppTheme.typography.body1l
            )
            SpacerLarge()
        }
        item {
            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                value = passwordFieldsState.password,
                onValueChange = onPasswordChange,
                allowAutofill = true,
                allowVisiblePassword = true,
                label = {
                    Text(stringResource(R.string.settings_password_entry))
                },
                onSubmit = { focusRequester.requestFocus() }
            )
            SpacerTiny()
            @Requirement(
                "O.Pass_1#3",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Usage of password strength evaluation to ensure a secure password for change the Password"
            )
            @Requirement(
                "O.Pass_2#3",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Shows password strength within changing the password"
            )
            @Requirement(
                "O.Pass_3#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Change the Password with a confirmation of the new Password"
            )
            PasswordStrength(
                modifier = Modifier.fillMaxWidth(),
                passwordEvaluation = passwordFieldsState.passwordEvaluation
            )
        }
        item {
            ConfirmationPasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                passwordIsValidAndConsistent = passwordFieldsState.passwordIsValidAndConsistent,
                repeatedPasswordHasError = passwordFieldsState.repeatedPasswordHasError,
                value = passwordFieldsState.repeatedPassword,
                onValueChange = onRepeatedPasswordChange,
                onSubmit = {
                    onAuthenticateWithPassword(passwordFieldsState.password)
                }
            )
        }
    }
}

@Composable
fun SettingsSetAppPasswordBottomBar(
    setAppPassword: () -> Unit,
    onBack: () -> Unit,
    passwordFieldsState: PasswordFieldsData
) {
    val emptyDescription = stringResource(id = R.string.set_app_password_button_state_description_empty)
    val notStrongDescription = stringResource(id = R.string.set_app_password_button_state_description_not_strong)
    val hasErrorDescription = stringResource(id = R.string.set_app_password_button_state_description_not_the_same)
    BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.semantics() {
                stateDescription = when {
                    passwordFieldsState.password.isBlank() -> emptyDescription
                    !passwordFieldsState.passwordEvaluation.isStrongEnough -> notStrongDescription
                    passwordFieldsState.repeatedPasswordHasError -> hasErrorDescription
                    passwordFieldsState.passwordIsValidAndConsistent -> ""
                    else -> "" // not reachable
                }
            },
            onClick = {
                setAppPassword()
                onBack()
            },
            enabled = passwordFieldsState.passwordIsValidAndConsistent,
            shape = RoundedCornerShape(PaddingDefaults.Small)
        ) {
            Text(stringResource(R.string.settings_password_save))
        }
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun SettingsSetAppPasswordScreenPreview(
    @PreviewParameter(SetAppPasswordParameterProvider::class) previewData: SetAppPasswordParameter
) {
    PreviewAppTheme {
        SettingsSetAppPasswordScreenScaffold(
            listState = rememberLazyListState(),
            focusRequester = FocusRequester(),
            passwordFieldsState = previewData.passwordFieldsState,
            onBack = {},
            setAppPassword = {},
            onPasswordChange = {},
            onRepeatedPasswordChange = {}
        )
    }
}
