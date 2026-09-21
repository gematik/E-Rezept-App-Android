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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.pkv.presentation.model.InvoiceCardUiState
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.PrescriptionDetailBottomSheetNavigationData
import de.gematik.ti.erp.app.prescription.model.PrescriptionType
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
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
    activeProfile: ProfileErpModel,
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    isDemoMode: Boolean,
    medicationScheduleErpModel: MedicationScheduleErpModel?,
    prescription: TaskErpModel?,
    invoiceCardState: InvoiceCardUiState,
    onShowInfoBottomSheet: PrescriptionDetailBottomSheetNavigationData,
    euRedeemFeatureFlag: Boolean,
    now: Instant = Clock.System.now(),
    onSwitchRedeemed: (Boolean) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onClickMedication: (PrescriptionData.Medication) -> Unit, // TODO (dinesh) use erp-model
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
    onClickRedeemInEuAbroad: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onBack,
        topBarTitle = stringResource(R.string.prescription_details),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
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
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.a11y_prescription_details_share),
                            tint = AppTheme.colors.primary700
                        )
                    }
                }

                PrescriptionDetailsDropdownMenu(
                    isDeletable = (actualPrescription as? TaskErpModel.Synced.Prescription)?.isDeletable() ?: true,
                    isEuRedeemable = actualPrescription.isEuRedeemable && actualPrescription.isReady(),
                    isActive = actualPrescription.isActive(),
                    euRedeemFeatureFlag = euRedeemFeatureFlag,
                    onClickDelete = onClickDeletePrescription,
                    onClickRedeemInEuAbroad = onClickRedeemInEuAbroad
                )
            }
        }
    ) {
        when (prescription) {
            is TaskErpModel.Synced.Prescription ->
                SyncedPrescriptionOverview(
                    invoiceCardState = invoiceCardState,
                    onGrantConsent = onGrantConsent,
                    activeProfile = activeProfile,
                    listState = listState,
                    prescription = prescription,
                    now = now,
                    isDemoMode = isDemoMode,
                    onClickInvoice = onClickInvoice,
                    medicationScheduleErpModel = medicationScheduleErpModel,
                    onClickMedication = onClickMedication,
                    onNavigateToRoute = onNavigateToRoute,
                    onClickRedeemLocal = onClickRedeemLocal,
                    onClickRedeemOnline = onClickRedeemOnline,
                    onShowInfoBottomSheet = onShowInfoBottomSheet,
                    onShowHowLongValidBottomSheet = onShowHowLongValidBottomSheet,
                    onClickMedicationPlan = { onClickMedicationPlan(PrescriptionType.SyncedTask) }
                )

            is TaskErpModel.Scanned ->
                ScannedPrescriptionOverview(
                    listState = listState,
                    prescription = prescription,
                    medicationScheduleErpModel = medicationScheduleErpModel,
                    isDemoMode = isDemoMode,
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
    isEuRedeemable: Boolean,
    isActive: Boolean,
    euRedeemFeatureFlag: Boolean,
    onClickDelete: () -> Unit,
    onClickRedeemInEuAbroad: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.a11y_prescription_more_option)
    val closeHint = stringResource(R.string.a11y_three_dot_menu_options_hint)
    IconButton(
        onClick = { dropdownExpanded = true },
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.MoreButton).semantics {
            contentDescription = description
            stateDescription = closeHint
        }
    ) {
        Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = AppTheme.colors.neutral700)
    }
    DropdownMenu(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        offset = DpOffset(SizeDefaults.triple, SizeDefaults.zero)
    ) {
        // EU Redemption menu item (only show when prescription is EU redeemable)
        if (euRedeemFeatureFlag && isEuRedeemable && isActive) {
            DropdownMenuItem(
                onClick = {
                    dropdownExpanded = false
                    onClickRedeemInEuAbroad()
                }
            ) {
                Text(
                    text = stringResource(R.string.pres_detail_dropdown_redeem_in_eu_abroad),
                    color = AppTheme.colors.primary700
                )
            }
        }

        // Delete menu item
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
                    AppTheme.colors.red700
                } else {
                    AppTheme.colors.neutral700
                }
            )
        }
    }
}
