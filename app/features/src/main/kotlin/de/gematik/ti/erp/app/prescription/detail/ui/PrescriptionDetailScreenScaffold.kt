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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.pkv.presentation.model.InvoiceCardUiState
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailBottomSheetNavigationData
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.PrescriptionType
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Suppress("LongParameterList", "FunctionNaming")
@Composable
fun PrescriptionDetailScreenScaffold(
    activeProfile: ProfilesUseCaseData.Profile,
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    prescription: PrescriptionData.Prescription?,
    medicationSchedule: MedicationSchedule?,
    invoiceCardState: InvoiceCardUiState,
    onShowInfoBottomSheet: PrescriptionDetailBottomSheetNavigationData,
    now: Instant = Clock.System.now(),
    isMedicationPlanEnabled: Boolean,
    onSwitchRedeemed: (Boolean) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onChangePrescriptionName: (String) -> Unit,
    onGrantConsent: () -> Unit,
    onClickRedeemLocal: () -> Unit,
    onClickRedeemOnline: () -> Unit,
    onClickTechnicalInformation: () -> Unit,
    onClickDeletePrescription: () -> Unit,
    onClickMedicationPlan: (PrescriptionType) -> Unit,
    onSharePrescription: () -> Unit,
    onShowHowLongValidBottomSheet: () -> Unit,
    onClickInvoice: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onBack,
        topBarTitle = stringResource(R.string.prescription_details),
        navigationMode = NavigationBarMode.Close,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
        actions = {
            letNotNull(
                prescription,
                prescription?.taskId,
                prescription?.accessCode
            ) { actualPrescription, taskId, accessCode ->
                if (accessCode.isNotEmpty()) { // not for direct assignment
                    IconButton(onClick = {
                        onSharePrescription()
                    }) {
                        Icon(Icons.Rounded.Share, null, tint = AppTheme.colors.primary700)
                    }
                }

                PrescriptionDetailsDropdownMenu(
                    isDeletable = (actualPrescription as? PrescriptionData.Synced)?.isDeletable ?: true,
                    onClickDelete = onClickDeletePrescription
                )
            }
        }
    ) {
        when (prescription) {
            is PrescriptionData.Synced ->
                SyncedPrescriptionOverview(
                    invoiceCardState = invoiceCardState,
                    onGrantConsent = onGrantConsent,
                    activeProfile = activeProfile,
                    listState = listState,
                    prescription = prescription,
                    now = now,
                    isMedicationPlanEnabled = isMedicationPlanEnabled,
                    onClickInvoice = onClickInvoice,
                    medicationSchedule = medicationSchedule,
                    onClickMedication = onClickMedication,
                    onNavigateToRoute = onNavigateToRoute,
                    onClickRedeemLocal = onClickRedeemLocal,
                    onClickRedeemOnline = onClickRedeemOnline,
                    onShowInfoBottomSheet = onShowInfoBottomSheet,
                    onShowHowLongValidBottomSheet = onShowHowLongValidBottomSheet,
                    onClickMedicationPlan = { onClickMedicationPlan(PrescriptionType.SyncedTask) }
                )

            is PrescriptionData.Scanned ->
                ScannedPrescriptionOverview(
                    listState = listState,
                    prescription = prescription,
                    medicationSchedule = medicationSchedule,
                    isMedicationPlanEnabled = isMedicationPlanEnabled,
                    onSwitchRedeemed = {
                        onSwitchRedeemed(it)
                    },
                    onClickRedeemLocal = onClickRedeemLocal,
                    onClickRedeemOnline = onClickRedeemOnline,
                    onChangePrescriptionName = onChangePrescriptionName,
                    onClickTechnicalInformation = onClickTechnicalInformation,
                    onClickMedicationPlan = { onClickMedicationPlan(PrescriptionType.ScannedTask) },
                    onShowScannedPrescriptionBottomSheet = onShowInfoBottomSheet.scannedPrescriptionBottomSheet
                )

            else -> {
                // do nothing
            }
        }
    }
}

@Composable
private fun PrescriptionDetailsDropdownMenu(
    isDeletable: Boolean,
    onClickDelete: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { dropdownExpanded = true },
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.MoreButton)
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        offset = DpOffset(SizeDefaults.triple, SizeDefaults.zero)
    ) {
        DropdownMenuItem(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.DeleteButton),
            enabled = isDeletable,
            onClick = {
                dropdownExpanded = false
                onClickDelete()
            }
        ) {
            Text(
                text = stringResource(R.string.pres_detail_dropdown_delete),
                color =
                if (isDeletable) {
                    AppTheme.colors.red600
                } else {
                    AppTheme.colors.neutral400
                }
            )
        }
    }
}
