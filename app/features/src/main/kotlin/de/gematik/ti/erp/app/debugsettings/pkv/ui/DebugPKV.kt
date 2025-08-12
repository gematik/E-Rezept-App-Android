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

package de.gematik.ti.erp.app.debugsettings.pkv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.debugsettings.pkv.presentation.rememberDebugPkvController
import de.gematik.ti.erp.app.debugsettings.ui.components.DebugCard
import de.gematik.ti.erp.app.debugsettings.ui.components.LoadingButton
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Center
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine

@Suppress("MagicNumber")
@Composable
fun DebugScreenPKV(
    onSaveInvoiceBundle: (String) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val controller = rememberDebugPkvController()

    LaunchedEffect(Unit) {
        controller.checkIsProfilePkv()
    }

    val activeProfile by controller.activeProfile.collectAsStateWithLifecycle()
    val isProfilePKV by controller.isProfilePKV.collectAsStateWithLifecycle()

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        listState = listState,
        topBarTitle = "Private Insurance (PKV)",
        onBack = onBack
    ) { innerPadding ->
        var invoiceBundle by remember { mutableStateOf("") }

        UiStateMachine(state = activeProfile) { activeProfile ->
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
                    DebugCard(title = "Switch Insurance") {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Current Profile: ${activeProfile.name}",
                            textAlign = TextAlign.Center
                        )
                        SpacerMedium()
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = controller::switchToPkv,
                                modifier = Modifier
                                    .height(SizeDefaults.sevenfoldAndHalf)
                                    .weight(0.4f)
                            ) {
                                Center {
                                    Text(
                                        text = "As PKV",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            SpacerMedium()

                            Button(
                                onClick = controller::switchToGkv,
                                modifier = Modifier
                                    .height(SizeDefaults.sevenfoldAndHalf)
                                    .weight(0.4f)
                            ) {
                                Center {
                                    Text(
                                        text = "As GKV",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        SpacerMedium()
                        UiStateMachine(
                            state = isProfilePKV,
                            onLoading = {
                                Center {
                                    CircularProgressIndicator()
                                }
                            },
                            onError = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Cannot check insurance type due to error: ${it.message}",
                                    textAlign = TextAlign.Center
                                )
                            },
                            onContent = { isPkv ->
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = if (isPkv) "PKV Profile" else "GKV Profile",
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
                item {
                    DebugCard(
                        title = "Invoice Bundle"
                    ) {
                        ErezeptOutlineText(
                            modifier = Modifier.fillMaxWidth(),
                            value = invoiceBundle,
                            label = "Bundle",
                            onValueChange = {
                                invoiceBundle = it
                            },
                            maxLines = 1
                        )
                        LoadingButton(
                            onClick = { onSaveInvoiceBundle(invoiceBundle) },
                            text = "Save Invoice"
                        )
                    }
                }
            }
        }
    }
}
