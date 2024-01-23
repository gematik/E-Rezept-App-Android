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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge

@Composable
fun MedicationOverviewScreen(
    prescription: PrescriptionData.Synced,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onBack: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onBack,
        topBarTitle = stringResource(R.string.synced_medication_detail_header),
        navigationMode = NavigationBarMode.Back,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
        actions = {}
    ) { innerPadding ->
        prescription.medicationRequest.medication?.let { med ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
            ) {
                item {
                    SpacerMedium()
                    Text(
                        stringResource(R.string.medication_overview_prescribed_header),
                        style = AppTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                    )
                    SpacerMedium()
                    Label(
                        text = med.name(),
                        label = null,
                        onClick = {
                            onClickMedication(PrescriptionData.Medication.Request(prescription.medicationRequest))
                        }
                    )
                }
                item {
                    SpacerXXLarge()
                    Text(
                        stringResource(R.string.medication_overview_dispenses_header),
                        style = AppTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                    )
                    SpacerMedium()
                }

                prescription.medicationDispenses.forEach { dispense ->
                    // TODO: add tracking event (with dispenseId + performer) in case of medication is null
                    dispense.medication?.let {
                        item {
                            Label(
                                text = it.name(),
                                label = null,
                                onClick = {
                                    onClickMedication(PrescriptionData.Medication.Dispense(dispense))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
