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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.component.Label
import de.gematik.ti.erp.app.medicationplan.components.MedicationPlanLineItem
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.ui.SentStatusChip
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.EditableHeaderTextField
import de.gematik.ti.erp.app.utils.compose.HealthPortalLink
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString

@Composable
fun ScannedPrescriptionOverview(
    listState: LazyListState,
    prescription: PrescriptionData.Scanned,
    medicationSchedule: MedicationSchedule?,
    isMedicationPlanEnabled: Boolean,
    onSwitchRedeemed: (redeemed: Boolean) -> Unit,
    onChangePrescriptionName: (String) -> Unit,
    onClickTechnicalInformation: () -> Unit,
    onClickRedeemLocal: () -> Unit,
    onShowScannedPrescriptionBottomSheet: () -> Unit,
    onClickRedeemOnline: () -> Unit,
    onClickMedicationPlan: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier =
        Modifier.fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(PaddingDefaults.Medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EditableHeaderTextField(
                    text = prescription.name,
                    onSaveText = { onChangePrescriptionName(it) }
                )

                SpacerShortMedium()
                Row(
                    modifier =
                    Modifier.clickable {
                        onShowScannedPrescriptionBottomSheet()
                    },
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val date = dateWithIntroductionString(R.string.prs_low_detail_scanned_on, prescription.scannedOn)
                    Text(date, style = AppTheme.typography.body2l)
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = AppTheme.colors.primary700)
                }
                if (prescription.task.communications.isNotEmpty()) {
                    SpacerShortMedium()
                    SentStatusChip()
                }
            }
        }

        if (!prescription.isRedeemed) {
            item {
                RedeemFromDetailSection(
                    onClickRedeemLocal = onClickRedeemLocal,
                    onClickRedeemOnline = onClickRedeemOnline
                )
            }
        }

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Medium)
            ) {
                SpacerXLarge()
                RedeemedButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    redeemed = prescription.isRedeemed,
                    onSwitchRedeemed = onSwitchRedeemed
                )
                SpacerXXLarge()
            }
        }
        if (isMedicationPlanEnabled) {
            item {
                MedicationPlanLineItem(medicationSchedule, onClickMedicationPlan)
            }
        }

        item {
            Label(
                text = stringResource(R.string.pres_detail_technical_information),
                onClick = onClickTechnicalInformation
            )
        }

        item {
            HealthPortalLink(Modifier.padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.XXLarge))
        }
    }
}

@Composable
private fun RedeemedButton(
    modifier: Modifier,
    redeemed: Boolean,
    onSwitchRedeemed: (redeemed: Boolean) -> Unit
) {
    val buttonText =
        if (redeemed) {
            stringResource(R.string.scanned_prescription_details_mark_as_unredeemed)
        } else {
            stringResource(R.string.scanned_prescription_details_mark_as_redeemed)
        }

    PrimaryButtonSmall(
        onClick = {
            onSwitchRedeemed(!redeemed)
        },
        modifier = modifier
    ) {
        Text(buttonText)
    }
}
