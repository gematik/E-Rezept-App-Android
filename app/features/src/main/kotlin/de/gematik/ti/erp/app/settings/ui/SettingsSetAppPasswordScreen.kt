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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.presentation.rememberPasswordSettingsController
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PasswordStrength
import de.gematik.ti.erp.app.utils.compose.PasswordTextField
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.validatePasswordScore

class SettingsSetAppPasswordScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        var password by remember { mutableStateOf("") }
        var repeatedPassword by remember { mutableStateOf("") }
        var passwordScore by remember { mutableIntStateOf(0) }
        val focusRequester = FocusRequester.Default
        val settingsController = rememberPasswordSettingsController()
        val listState = rememberLazyListState()

        AnimatedElevationScaffold(
            topBarTitle = stringResource(R.string.settings_password_headline),
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            onBack = navController::popBackStack,
            bottomBar = {
                BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            settingsController.selectPasswordAsAuthenticationMode(password)
                            navController.popBackStack()
                        },
                        enabled = validatePassword(
                            password = password,
                            repeatedPassword = repeatedPassword,
                            score = passwordScore
                        ),
                        shape = RoundedCornerShape(PaddingDefaults.Small)
                    ) {
                        Text(stringResource(R.string.settings_password_save))
                    }
                    SpacerMedium()
                }
            }
        ) { innerPadding ->
            SettingsSetAppPasswordScreenContent(
                innerPadding,
                listState,
                focusRequester,
                password,
                repeatedPassword,
                passwordScore,
                onAuthenticateWithPassword = {
                    settingsController.selectPasswordAsAuthenticationMode(password = it)
                    navController.popBackStack()
                },
                onPasswortChange = {
                    repeatedPassword = ""
                    password = it
                },
                onRepeatedPasswortChange = {
                    repeatedPassword = it
                },
                onScoreChange = {
                    passwordScore = it
                }
            )
        }
    }
}

@Composable
private fun SettingsSetAppPasswordScreenContent(
    innerPadding: PaddingValues,
    listState: LazyListState,
    focusRequester: FocusRequester,
    password: String,
    repeatedPassword: String,
    passwordScore: Int,
    onAuthenticateWithPassword: (String) -> Unit,
    onPasswortChange: (String) -> Unit,
    onRepeatedPasswortChange: (String) -> Unit,
    onScoreChange: (Int) -> Unit
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
            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswortChange,
                allowAutofill = true,
                allowVisiblePassword = true,
                label = {
                    Text(stringResource(R.string.settings_password_enter))
                },
                onSubmit = { focusRequester.requestFocus() }
            )
            SpacerTiny()
            PasswordStrength(
                modifier = Modifier.fillMaxWidth(),
                password = password,
                onScoreChange = onScoreChange
            )
        }
        item {
            ConfirmationPasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                password = password,
                passwordScore = passwordScore,
                value = repeatedPassword,
                onValueChange = onRepeatedPasswortChange,
                onSubmit = {
                    if (
                        validatePassword(
                            password = password,
                            repeatedPassword = repeatedPassword,
                            score = passwordScore
                        )
                    ) {
                        onAuthenticateWithPassword(password)
                    }
                }
            )
        }
    }
}

fun validatePassword(password: String, repeatedPassword: String, score: Int): Boolean =
    password.isNotBlank() && password == repeatedPassword && validatePasswordScore(score)
