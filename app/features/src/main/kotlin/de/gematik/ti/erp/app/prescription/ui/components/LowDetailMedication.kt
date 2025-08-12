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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.prescription.ui.SentStatusChip
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.ScannedPrescription
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LowDetailMedication(
    modifier: Modifier = Modifier,
    prescription: ScannedPrescription,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val scannedOn =
        remember {
            prescription.scannedOn
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toJavaLocalDateTime()
                .format(dateFormatter)
        }

    val redeemedOn =
        remember {
            prescription.redeemedOn
                ?.toLocalDateTime(TimeZone.currentSystemDefault())
                ?.toJavaLocalDateTime()
                ?.format(dateFormatter)
        }

    val dateText =
        if (redeemedOn != null) {
            stringResource(R.string.prs_low_detail_redeemed_on, redeemedOn)
        } else {
            stringResource(R.string.prs_low_detail_scanned_on, scannedOn)
        }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
        elevation = 0.dp,
        backgroundColor = AppTheme.colors.neutral050,
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Column(
                modifier =
                Modifier
                    .weight(1f)
            ) {
                Text(
                    prescription.name,
                    style = AppTheme.typography.subtitle1
                )
                SpacerTiny()
                Text(
                    dateText,
                    style = AppTheme.typography.body2l
                )
                SpacerSmall()

                Row {
                    if (prescription.communications.isNotEmpty()) {
                        SentStatusChip()
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = AppTheme.colors.neutral400,
                modifier =
                Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
