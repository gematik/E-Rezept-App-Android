/*
 * Copyright (c) 2022 gematik GmbH
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.navigationBarsPadding
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import java.net.URI
import kotlinx.coroutines.launch

@Composable
private fun DebugCard(
    modifier: Modifier = Modifier,
    title: String,
    onReset: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) =
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        backgroundColor = AppTheme.colors.neutral100,
        elevation = 0.dp,
        border = null
    ) {
        Box {
            Column(Modifier.padding(PaddingDefaults.Medium), verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
                Text(
                    title,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                SpacerMedium()
                content()
            }
            onReset?.run {
                IconButton(modifier = Modifier.align(Alignment.TopEnd).padding(PaddingDefaults.Small), onClick = onReset) {
                    Icon(Icons.Rounded.Refresh, null)
                }
            }
        }
    }

@Composable
fun EditablePathComponentSetButton(
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
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
                .padding(end = PaddingDefaults.Medium)
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
    modifier: Modifier = Modifier,
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
                .padding(end = PaddingDefaults.Medium)
        )

        content { onValueChange(textFieldValue, it) }
    }
}

@Composable
fun DebugScreen(navigation: NavController, viewModel: DebugSettingsViewModel = hiltViewModel()) {
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                title = "Debug Settings"
            ) { navigation.popBackStack() }
        }
    ) { innerPadding ->

        LaunchedEffect(key1 = Unit) {
            viewModel.state()
        }

        LazyColumn(
            modifier = Modifier.padding(innerPadding).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                DebugCard(
                    title = "General"
                ) {
                    Button(
                        onClick = { viewModel.restartWithOnboarding() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Restart with Onboarding")
                    }
                    Button(
                        onClick = { viewModel.refreshPrescriptions() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Trigger Prescription Refresh")
                    }
                    TextWithResetButtonComponent(
                        label = "UI Hints",
                        onClick = { viewModel.resetHints() },
                        active = false
                    )
                }
            }
            item {
                DebugCard(
                    title = "Card Wall"
                ) {
                    TextWithResetButtonComponent(
                        label = "Card Wall Intro",
                        onClick = { viewModel.resetCardWallIntro() },
                        active = !viewModel.debugSettingsData.cardWallIntroIsAccepted
                    )

                    TextWithResetButtonComponent(
                        label = "Card Access Number",
                        onClick = {
                            viewModel.resetCardAccessNumber()
                        },
                        active = !viewModel.debugSettingsData.cardAccessNumberIsSet
                    )

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Fake NFC Capability",
                            modifier = Modifier
                                .weight(1f)
                        )
                        Switch(
                            checked = viewModel.debugSettingsData.fakeNFCCapabilities,
                            onCheckedChange = { viewModel.allowNfc(it) }
                        )
                    }
                }
            }
            item {
                DebugCard(
                    title = "Authentication"
                ) {
                    EditablePathComponentSetButton(
                        label = "Bearer Token",
                        text = viewModel.debugSettingsData.bearerToken,
                        active = viewModel.debugSettingsData.bearerTokenIsSet,
                        onValueChange = { text, _ ->
                            viewModel.updateState(
                                viewModel.debugSettingsData.copy(
                                    bearerToken = text,
                                    bearerTokenIsSet = false
                                )
                            )
                        },
                        onClick = {
                            viewModel.changeBearerToken(viewModel.debugSettingsData.activeProfileName)
                        }
                    )
                    Button(
                        onClick = { viewModel.breakSSOToken() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Break SSO Token")
                    }
                }
            }
            item {
                DebugCard(
                    title = "Service URLs"
                ) {
                    EditablePathComponentCheckable(
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Speichern und Neustarten")
                    }
                }
            }
            item {
                VirtualHealthCard(viewModel = viewModel)
            }
            item {
                FeatureToggles(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun VirtualHealthCard(modifier: Modifier = Modifier, viewModel: DebugSettingsViewModel) {
    var virtualHealthCardLoading by remember { mutableStateOf(false) }
    var virtualHealthCardError by remember { mutableStateOf<String?>(null) }
    virtualHealthCardError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                virtualHealthCardError = null
            },
            buttons = {
                Button(onClick = { virtualHealthCardError = null }) {
                    Text("OK")
                }
            },
            text = {
                Text(error)
            }
        )
    }

    DebugCard(modifier, title = "Virtual Health Card", onReset = viewModel::onResetVirtualHealthCard) {
        val scope = rememberCoroutineScope()

        OutlinedTextField(
            modifier = Modifier.heightIn(max = 144.dp).fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardCert,
            onValueChange = {
                viewModel.onSetVirtualHealthCardCertificate(it)
            },
            label = { Text("Certificate in Base64") }
        )

        val subjectInfo =
            remember(viewModel.debugSettingsData.virtualHealthCardCert) { viewModel.getVirtualHealthCardCertificateSubjectInfo() }
        Text(subjectInfo, style = AppTheme.typography.captionl)

        OutlinedTextField(
            modifier = Modifier.heightIn(max = 144.dp).fillMaxWidth(),
            value = viewModel.debugSettingsData.virtualHealthCardPrivateKey,
            onValueChange = {
                viewModel.onSetVirtualHealthCardPrivateKey(it)
            },
            label = { Text("Private Key in Base64") }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                virtualHealthCardLoading = true
                scope.launch {
                    try {
                        viewModel.onTriggerVirtualHealthCard(
                            certificateBase64 = viewModel.debugSettingsData.virtualHealthCardCert,
                            privateKeyBase64 = viewModel.debugSettingsData.virtualHealthCardPrivateKey
                        )
                    } catch (e: Exception) {
                        virtualHealthCardError = e.message
                    } finally {
                        virtualHealthCardLoading = false
                    }
                }
            },
            enabled = !virtualHealthCardLoading
        ) {
            if (virtualHealthCardLoading) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = AppTheme.colors.neutral600)
                SpacerSmall()
            }
            Text("Set Virtual Health Card for Active Profile", textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FeatureToggles(modifier: Modifier = Modifier, viewModel: DebugSettingsViewModel) {
    val featuresState by produceState(initialValue = mutableMapOf<String, Boolean>()) {
        viewModel.featuresState().collect {
            value = it
        }
    }
    DebugCard(modifier, title = "Feature Toggles") {
        for (feature in viewModel.features()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature.featureName,
                    modifier = Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.body1
                )
                Switch(
                    checked = featuresState[feature.featureName] ?: false,
                    onCheckedChange = { viewModel.toggleFeature(feature) }
                )
            }
        }
    }
}
