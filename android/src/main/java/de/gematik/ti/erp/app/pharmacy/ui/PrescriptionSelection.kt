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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.prescriptionIds
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.dateTimeShortText

@Composable
fun PrescriptionSelection(
    orderState: PharmacyOrderState,
    showNextButton: Boolean = false,
    backIsFinish: Boolean = true,
    onFinishSelection: () -> Unit,
    onBack: () -> Unit
) {
    val prescriptions by orderState.prescriptions
    val order by orderState.order

    var showNoSelectedRxDialog by remember { mutableStateOf(false) }
    BackHandler(backIsFinish && order.prescriptions.isEmpty()) {
        showNoSelectedRxDialog = true
    }

    val onFinishFn = {
        if (order.prescriptions.isEmpty()) {
            showNoSelectedRxDialog = true
        } else {
            onFinishSelection()
        }
    }

    if (showNoSelectedRxDialog) {
        AcceptDialog(
            header = stringResource(R.string.pharmacy_order_no_selected_prescriptions_title),
            info = stringResource(R.string.pharmacy_order_no_selected_prescriptions_desc),
            acceptText = stringResource(R.string.ok),
            onClickAccept = {
                showNoSelectedRxDialog = false
            }
        )
    }

    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Screen),
        topBarTitle = stringResource(R.string.pharmacy_order_select_prescriptions),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        onBack = if (backIsFinish) onFinishFn else onBack
    ) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Content)
                    .semantics {
                        prescriptionIds = prescriptions.map { it.taskId }
                    },
                state = listState
            ) {
                prescriptions.forEach { prescription ->
                    item(key = "prescription-${prescription.taskId}") {
                        PrescriptionItem(
                            modifier = Modifier,
                            prescription = prescription,
                            checked = remember(prescription, order) { prescription in order.prescriptions },
                            onCheckedChange = {
                                if (it) {
                                    orderState.onSelectPrescription(prescription)
                                } else {
                                    orderState.onDeselectPrescription(prescription)
                                }
                            }
                        )
                    }
                }
            }
            if (showNextButton) {
                NextButton(
                    onNext = onFinishFn
                )
            }
        }
    }
}

@Composable
private fun PrescriptionItem(
    modifier: Modifier,
    prescription: PharmacyUseCaseData.PrescriptionOrder,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val scannedRxTxt = stringResource(R.string.pres_details_scanned_prescription)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .padding(PaddingDefaults.Medium)
            .semantics {
                selected = checked
                prescriptionId = prescription.taskId
            }
    ) {
        val dt = remember(prescription) { dateTimeShortText(prescription.timestamp) }
        Column(Modifier.weight(1f)) {
            Text(prescription.title ?: scannedRxTxt, style = AppTheme.typography.body1)
            Text(dt, style = AppTheme.typography.body2l)
        }
        SpacerMedium()
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    null,
                    tint = AppTheme.colors.primary600
                )
            } else {
                Icon(
                    Icons.Rounded.RadioButtonUnchecked,
                    null,
                    tint = AppTheme.colors.neutral400
                )
            }
        }
    }
}

@Composable
private fun NextButton(
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(Modifier.navigationBarsPadding()) {
            SpacerMedium()
            PrimaryButtonLarge(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton),
                onClick = onNext
            ) {
                Text(stringResource(R.string.rx_selection_next))
            }
        }
    }
}
