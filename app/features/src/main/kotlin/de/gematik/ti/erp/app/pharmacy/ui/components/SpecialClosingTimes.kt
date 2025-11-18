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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.pharmacy.model.NotAvailablePeriodMetadata
import de.gematik.ti.erp.app.fhir.pharmacy.model.sortedByPeriodStart
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.letNotNull

@Composable
internal fun SpecialClosingTimes(
    specialClosingTimes: List<NotAvailablePeriodMetadata>
) {
    // Use shared model-layer sorting (stable & deterministic)
    val sorted = specialClosingTimes.sortedByPeriodStart()
    // Filter out items that are in the past
    val upcomingOrActive = sorted.filter { !it.isInPast }
    if (upcomingOrActive.isEmpty()) {
        // Nothing relevant to show (all entries are in the past) -> render nothing
        return
    }

    ErezeptText.Title(text = stringResource(id = R.string.pharm_detail_special_closing_times))
    SpacerSmall()

    upcomingOrActive.forEachIndexed { index, specialClosingTime ->
        SpecialClosingTime(specialClosingTime)
        if (index < upcomingOrActive.lastIndex) {
            SpacerMedium()
        }
    }
}

@Composable
private fun SpecialClosingTime(
    specialClosingTime: NotAvailablePeriodMetadata
) {
    if (!specialClosingTime.isInPast) {
        val currentColor = when {
            specialClosingTime.isActive -> AppTheme.colors.green700
            else -> AppTheme.colors.neutral600
        }

        letNotNull(
            specialClosingTime.erpModel.period.start,
            specialClosingTime.erpModel.period.end
        ) { start, end ->
            Text(
                text = specialClosingTime.erpModel.description ?: "",
                color = currentColor,
                fontSize = SizeDefaults.oneHalf.value.sp,
                fontStyle = FontStyle.Italic
            )
            Row {
                PeriodTimeText(
                    start = start,
                    end = end,
                    isActive = specialClosingTime.isActive,
                    color = currentColor
                )
            }
        }
    }
}
