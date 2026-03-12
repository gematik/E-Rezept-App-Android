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

package de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.ripple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.checkbox.LabeledCheckbox
import de.gematik.ti.erp.app.core.ClipBoardCopy
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.presentation.PharmacyBackendServiceSelectionViewModel
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.presentation.pharmacyBackendServiceSelectionViewModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.capitalizeFirstChar
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton

private const val EQUAL_WEIGHTS = 0.5f

@Composable
fun PharmacyServiceSelectionScreen(
    viewModel: PharmacyBackendServiceSelectionViewModel = pharmacyBackendServiceSelectionViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val snackbarScaffold = LocalSnackbarScaffold.current
    val listState = rememberLazyListState()

    val selectedService by viewModel.selectedService.collectAsStateWithLifecycle()
    val originalSearchAccessToken by viewModel.searchAccessToken.collectAsStateWithLifecycle(null)
    val showTelematikId by viewModel.showTelematikId.collectAsStateWithLifecycle(false)
    var searchAccessToken by remember { mutableStateOf(originalSearchAccessToken) }

    // Load the current selection when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadPharmacyBackendService()
    }

    AnimatedElevationScaffold(
        topBarTitle = "Pharmacy Backend Service",
        onBack = onBack,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        listState = listState,
        actions = {}
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            item { SpacerMedium() }

            // Service Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SizeDefaults.double),
                    elevation = SizeDefaults.half,
                    backgroundColor = AppTheme.colors.neutral050,
                    border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral200)
                ) {
                    Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                        Text(
                            text = "Backend Service",
                            style = AppTheme.typography.subtitle1,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.neutral900
                        )
                        SpacerSmall()
                        Text(
                            text = "Select which pharmacy backend service to use for search.",
                            style = MaterialTheme.typography.body2,
                            color = AppTheme.colors.neutral600,
                            lineHeight = 20.sp
                        )

                        SpacerMedium()
                        HorizontalDivider(color = AppTheme.colors.neutral200)
                        SpacerMedium()

                        // Radio buttons for pharmacy services
                        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
                            PharmacyVzdService.entries.forEach { service ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = ripple(),
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            viewModel.savePharmacyBackendService(service)
                                            snackbarScaffold.showWithDismissButton(
                                                message = "Backend service updated to ${service.name.lowercase().capitalizeFirstChar()}",
                                                actionLabel = "Close",
                                                scope = uiScope
                                            )
                                        }
                                        .padding(vertical = PaddingDefaults.Small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedService == service,
                                        onClick = {
                                            viewModel.savePharmacyBackendService(service)
                                            snackbarScaffold.showWithDismissButton(
                                                message = "Backend service updated to ${service.name.lowercase().capitalizeFirstChar()}",
                                                actionLabel = "Close",
                                                scope = uiScope
                                            )
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = AppTheme.colors.primary600,
                                            unselectedColor = AppTheme.colors.neutral400
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(PaddingDefaults.Small))
                                    Text(
                                        text = service.name.lowercase().capitalizeFirstChar(),
                                        style = AppTheme.typography.body1,
                                        color = if (selectedService == service) {
                                            AppTheme.colors.primary900
                                        } else {
                                            AppTheme.colors.neutral900
                                        },
                                        fontWeight = if (selectedService == service) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Access Token Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SizeDefaults.double),
                    elevation = SizeDefaults.half,
                    backgroundColor = AppTheme.colors.neutral050,
                    border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral200)
                ) {
                    Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                        Text(
                            text = "Search Access Token",
                            style = AppTheme.typography.subtitle1,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.neutral900
                        )
                        SpacerSmall()
                        Text(
                            text = "Override the search access token for testing purposes.",
                            style = MaterialTheme.typography.body2,
                            color = AppTheme.colors.neutral600,
                            lineHeight = 20.sp
                        )

                        SpacerMedium()

                        ErezeptOutlineText(
                            modifier = Modifier.fillMaxWidth(),
                            value = searchAccessToken ?: "",
                            onValueChange = { searchAccessToken = it },
                            placeholder = { Text("Enter or paste token") }
                        )

                        SpacerSmall()

                        // Current token display
                        Text(
                            text = "Current Token:",
                            style = MaterialTheme.typography.body2,
                            color = AppTheme.colors.neutral600,
                            fontWeight = FontWeight.Medium
                        )
                        SpacerTiny()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ClipBoardCopy.copyToClipboard(
                                        context = context,
                                        text = originalSearchAccessToken ?: ""
                                    )
                                    snackbarScaffold.showWithDismissButton(
                                        message = "Token copied to clipboard",
                                        actionLabel = "Close",
                                        scope = uiScope
                                    )
                                },
                            shape = RoundedCornerShape(SizeDefaults.one),
                            color = AppTheme.colors.neutral100,
                            border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300)
                        ) {
                            Row(
                                modifier = Modifier.padding(PaddingDefaults.Medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = originalSearchAccessToken?.take(40)?.plus("...") ?: "No token",
                                    style = MaterialTheme.typography.body2,
                                    color = AppTheme.colors.neutral900,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = AppTheme.colors.primary600
                                )
                            }
                        }

                        SpacerMedium()

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        searchAccessToken?.let { viewModel.saveNewSearchAccessToken(it) }
                                        snackbarScaffold.showWithDismissButton(
                                            message = "Token updated successfully",
                                            actionLabel = "Close",
                                            scope = uiScope
                                        )
                                    },
                                shape = RoundedCornerShape(SizeDefaults.oneHalf),
                                color = AppTheme.colors.primary600
                            ) {
                                Text(
                                    text = "Update",
                                    style = AppTheme.typography.body1,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(PaddingDefaults.Medium),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.clearSearchAccessToken()
                                        searchAccessToken = null
                                        snackbarScaffold.showWithDismissButton(
                                            message = "Token cleared. New one will be downloaded.",
                                            actionLabel = "Close",
                                            scope = uiScope
                                        )
                                    },
                                shape = RoundedCornerShape(SizeDefaults.oneHalf),
                                color = Color.Transparent,
                                border = BorderStroke(SizeDefaults.quarter, AppTheme.colors.primary600)
                            ) {
                                Text(
                                    text = "Clear",
                                    style = AppTheme.typography.body1,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.colors.primary600,
                                    modifier = Modifier.padding(PaddingDefaults.Medium),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Telematik ID Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SizeDefaults.double),
                    elevation = SizeDefaults.half,
                    backgroundColor = AppTheme.colors.neutral050,
                    border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral200)
                ) {
                    Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                        Text(
                            text = "Display Settings",
                            style = AppTheme.typography.subtitle1,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.neutral900
                        )
                        SpacerSmall()

                        LabeledCheckbox(
                            label = "Show Telematik ID",
                            checked = showTelematikId,
                            onCheckedChange = {
                                viewModel.toggleShowTelematikIdVisibility(it)
                                snackbarScaffold.showWithDismissButton(
                                    message = "Telematik ID visibility: ${if (it) "Shown" else "Hidden"}",
                                    actionLabel = "Close",
                                    scope = uiScope
                                )
                            }
                        )
                    }
                }
            }

            item { SpacerMedium() }
        }
    }
}
