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

package de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.checkbox.LabeledCheckbox
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.presentation.PharmacyBackendServiceSelectionViewModel
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.presentation.pharmacyBackendServiceSelectionViewModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.capitalizeFirstChar
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import kotlinx.coroutines.launch

private const val EQUAL_WEIGHTS = 0.5f

@Composable
fun PharmacyServiceSelectionScreen(
    viewModel: PharmacyBackendServiceSelectionViewModel = pharmacyBackendServiceSelectionViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val snackbarScaffold = LocalSnackbarScaffold.current

    val selectedService by viewModel.selectedService.collectAsStateWithLifecycle()
    val originalSearchAccessToken by viewModel.searchAccessToken.collectAsStateWithLifecycle(null)
    val showTelematikId by viewModel.showTelematikId.collectAsStateWithLifecycle(false)
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    var searchAccessToken by remember { mutableStateOf(originalSearchAccessToken) }

    // Load the current selection when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadPharmacyBackendService()
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Column(modifier = Modifier.padding(SizeDefaults.double)) {
                Text(
                    text = "Select Pharmacy Backend Service"
                )
                // List of available pharmacy services
                PharmacyVzdService.entries.forEach { service ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SizeDefaults.one)
                            .clickable {
                                viewModel.savePharmacyBackendService(service)
                                uiScope.launch { sheetState.hide() }
                            }
                    ) {
                        RadioButton(
                            selected = selectedService == service,
                            onClick = {
                                viewModel.savePharmacyBackendService(service)
                                uiScope.launch { sheetState.hide() }
                            }
                        )
                        Spacer(modifier = Modifier.width(SizeDefaults.one))
                        Text(
                            text = service.name.lowercase().capitalizeFirstChar(),
                            style = AppTheme.typography.subtitle1
                        )
                    }
                }
            }
        },
        content = {
            AnimatedElevationScaffold(
                topBarTitle = "Pharmacy Backend Service",
                onBack = onBack,
                bottomBar = {
                    Row {
                        PrimaryButton(
                            modifier = Modifier
                                .padding(SizeDefaults.triple)
                                .weight(EQUAL_WEIGHTS),
                            onClick = {
                                searchAccessToken?.let { viewModel.saveNewSearchAccessToken(it) }
                                snackbarScaffold.showWithDismissButton(
                                    message = "Search access token updated. Will be used, if valid.",
                                    actionLabel = "Close",
                                    scope = uiScope
                                )
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = SizeDefaults.one),
                                text = "Update",
                                style = AppTheme.typography.caption2
                            )
                        }

                        PrimaryButton(
                            modifier = Modifier
                                .padding(SizeDefaults.triple)
                                .weight(EQUAL_WEIGHTS),
                            onClick = {
                                viewModel.clearSearchAccessToken()
                                snackbarScaffold.showWithDismissButton(
                                    message = "Search access token deleted. New one will be downloaded.",
                                    actionLabel = "Close",
                                    scope = uiScope
                                )
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = SizeDefaults.one),
                                text = "Clear",
                                style = AppTheme.typography.caption2
                            )
                        }
                    }
                },
                content = {
                    Column {
                        Spacer(modifier = Modifier.width(SizeDefaults.one))

                        Text(
                            modifier = Modifier.padding(horizontal = SizeDefaults.one),
                            text = "Search Access Token Override",
                            style = AppTheme.typography.caption2
                        )
                        Spacer(modifier = Modifier.height(SizeDefaults.one))

                        ErezeptOutlineText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SizeDefaults.one),
                            value = searchAccessToken ?: "",
                            onValueChange = {
                                searchAccessToken = it
                            },
                            placeholder = { Text("Enter or paste search access token") }
                        )
                        Spacer(modifier = Modifier.height(SizeDefaults.half))
                        Text(
                            modifier = Modifier.padding(horizontal = SizeDefaults.one),
                            text = "Current token",
                            style = AppTheme.typography.caption2
                        )
                        Spacer(modifier = Modifier.height(SizeDefaults.half))
                        Text(
                            modifier = Modifier
                                .padding(horizontal = SizeDefaults.double)
                                .clickable {
                                    ClipBoardCopy.copyToClipboard(
                                        context = context,
                                        text = originalSearchAccessToken ?: ""
                                    )
                                },
                            text = "$originalSearchAccessToken",
                            style = AppTheme.typography.caption2
                        )

                        PrimaryButton(
                            modifier = Modifier
                                .padding(SizeDefaults.double)
                                .fillMaxWidth(),
                            onClick = {
                                uiScope.launch { sheetState.show() }
                            }
                        ) {
                            Text(
                                text = "Select Pharmacy Backend Service",
                                style = AppTheme.typography.subtitle1
                            )
                        }

                        Spacer(modifier = Modifier.height(SizeDefaults.double))

                        LabeledCheckbox(
                            label = "Show Telematik ID",
                            checked = showTelematikId,
                            onCheckedChange = {
                                snackbarScaffold.showWithDismissButton(
                                    message = "Telematik-id visibility changed to $it",
                                    actionLabel = "Close",
                                    scope = uiScope
                                )
                                viewModel.toggleShowTelematikIdVisibility(it)
                            }
                        )

                        Spacer(modifier = Modifier.height(SizeDefaults.double))
                    }
                }
            )
        }
    )
}
