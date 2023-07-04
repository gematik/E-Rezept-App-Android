/*
 * Copyright (c) 2023 gematik GmbH
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel

@Composable
fun DebugScreenPKV(onBack: () -> Unit) {
    val viewModel by rememberViewModel<DebugSettingsViewModel>()
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        topBarTitle = "Debug PKV",
        onBack = onBack
    ) { innerPadding ->
        var invoiceBundle by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                DebugCard(title = "Login state") {
                    val profileHandler = LocalProfileHandler.current
                    val activeProfile = profileHandler.activeProfile
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                scope.launch {
                                    profileHandler.switchProfileToPKV(activeProfile.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Set User with ${activeProfile.name} as PKV", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            item {
                DebugCard(
                    title = "Invoice Bundle"
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = invoiceBundle,
                        label = { Text("Bundle") },
                        onValueChange = {
                            invoiceBundle = it
                        },
                        maxLines = 1
                    )
                    DebugLoadingButton(
                        onClick = { viewModel.saveInvoice(invoiceBundle) },
                        text = "Save Invoice"
                    )
                }
            }
        }
    }
}
