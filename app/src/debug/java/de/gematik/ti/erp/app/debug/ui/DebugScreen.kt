/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.debug.ui

import android.security.NetworkSecurityPolicy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import java.net.URI

@Composable
fun EditablePathComponentSetButton(
    modifier: Modifier,
    label: String,
    text: String,
    active: Boolean,
    onValueChange: (String, Boolean) -> Unit,
    onClick: () -> Unit
) {

    val color = if (active) Color.Green else Color.Red
    val buttonText = if (active) "SAVED" else "SET"
    EditablePathComponentWithControl(
        modifier = modifier,
        label = label,
        textFieldValue = text,
        onValueChange = onValueChange,
        content = {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = color),
                enabled = !active

            ) {
                Text(text = buttonText)
            }
        }
    )
}

@Composable
fun TextWithResetButtonComponent(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit,
    active: Boolean

) {
    val color = if (active) Color.Green else Color.Red
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
        val text = if (active) "UNSET" else "RESET"
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = color),
            enabled = !active
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun EditablePathComponentCheckable(
    modifier: Modifier,
    label: String,
    textFieldValue: String,
    checked: Boolean,
    onValueChange: (String, Boolean) -> Unit
) {
    EditablePathComponentWithControl(
        modifier = modifier,
        label = label,
        textFieldValue = textFieldValue,
        onValueChange = onValueChange,
        content = { onChange ->
            Checkbox(
                checked = checked,
                onCheckedChange = onChange
            )
        }
    )
}

@Composable
fun EditablePathComponentWithControl(
    modifier: Modifier,
    label: String,
    textFieldValue: String,
    onValueChange: (String, Boolean) -> Unit,
    content: @Composable ((Boolean) -> Unit) -> Unit
) {

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {

        TextField(
            value = textFieldValue,
            onValueChange = { onValueChange(it, false) },
            label = { Text(label) },
            maxLines = 3,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )

        content { onValueChange(textFieldValue, it) }
    }
}

@Composable
fun DebugScreen(navigation: NavController, viewModel: DebugSettingsViewModel = hiltViewModel()) {
    val header = "Debug Settings"

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                header
            ) { navigation.popBackStack() }
        }
    ) { innerPadding ->

        val elementDistance = 16.dp
        val modifier = Modifier.padding(
            start = elementDistance,
            end = elementDistance,
            bottom = elementDistance
        )

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(elementDistance))

            Button(
                onClick = { viewModel.restartWithOnboarding() },
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text = "Neustart mit aktiviertem Onboarding")
            }

            Button(
                onClick = { viewModel.refreshPrescriptions() },
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text = "Trigger prescription refresh")
            }

            Card(
                modifier = Modifier.padding(horizontal = 2.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = 2.dp,
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column {

                    Spacer(modifier = modifier)

                    Text(
                        text = "Card Wall", modifier = modifier, style = MaterialTheme.typography.h6
                    )

                    TextWithResetButtonComponent(
                        modifier = modifier,
                        label = "Card Wall Intro",
                        onClick = { viewModel.resetCardWallIntro() },
                        active = !viewModel.debugSettingsData.cardWallIntroIsAccepted
                    )
                    TextWithResetButtonComponent(
                        modifier = modifier,
                        label = "Card Access Number",
                        onClick = { viewModel.resetCardAccessNumber() },
                        active = !viewModel.debugSettingsData.cardAccessNumberIsSet
                    )

                    Row(modifier = modifier.fillMaxWidth()) {
                        Text(
                            text = "So tun als hätte das Handy NFC ",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)

                        )
                        Switch(
                            checked = viewModel.debugSettingsData.fakeNFCCapabilities,
                            onCheckedChange = { viewModel.allowNfc(it) }
                        )
                    }
                }
            }

            Spacer(modifier = modifier)

            TextWithResetButtonComponent(
                modifier = modifier,
                label = "UI Hints",
                onClick = { viewModel.resetHints() },
                active = false
            )

            Spacer(modifier = modifier)

            EditablePathComponentSetButton(
                modifier = modifier,
                label = "Bearer Token",
                text = viewModel.debugSettingsData.bearerToken,
                active = viewModel.debugSettingsData.bearerTokenIsSet,
                onValueChange = { text, active ->
                    viewModel.updateState(
                        viewModel.debugSettingsData.copy(
                            bearerToken = text,
                            bearerTokenIsSet = false
                        )
                    )
                },
                onClick = {
                    viewModel.changeBearerToken()
                }
            )

            Spacer(modifier = modifier)

            Button(
                onClick = { viewModel.breakSSOToken() },
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text = "Break SSO Token")
            }

            Card(
                modifier = Modifier.padding(horizontal = 2.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = 2.dp,
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column {

                    Spacer(modifier = modifier)

                    Text(
                        text = "Service URL's",
                        modifier = modifier,
                        style = MaterialTheme.typography.h6
                    )

                    val clearTextAllowed =
                        !NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted
                    Text(
                        text = "Ist Cleartext Traffic erlaubt: $clearTextAllowed",
                        modifier = modifier.align(Alignment.Start)
                    )

                    EditablePathComponentCheckable(
                        modifier = modifier,
                        label = "ERezept Fachdienst Base URL",
                        textFieldValue = viewModel.debugSettingsData.eRezeptServiceURL,
                        checked = viewModel.debugSettingsData.eRezeptActive,
                        onValueChange = { text, checked ->
                            runCatching { URI(text) }.getOrNull()?.run {
                                viewModel.updateState(
                                    viewModel.debugSettingsData.copy(
                                        eRezeptServiceURL = text,
                                        eRezeptActive = checked
                                    )
                                )
                            }
                        }
                    )
                    EditablePathComponentCheckable(
                        modifier = modifier,
                        label = "IDP Service Base URL",
                        textFieldValue = viewModel.debugSettingsData.idpUrl,
                        checked = viewModel.debugSettingsData.idpActive,
                        onValueChange = { text, checked ->
                            runCatching { URI(text) }.getOrNull()?.run {
                                viewModel.updateState(
                                    viewModel.debugSettingsData.copy(
                                        idpUrl = text,
                                        idpActive = checked
                                    )
                                )
                            }
                        }
                    )

                    Button(
                        onClick = { viewModel.saveAndRestartApp() },
                        modifier = modifier.fillMaxWidth()
                    ) {
                        Text(text = "Speichern und Neustarten")
                    }
                }
            }
        }
    }
}
