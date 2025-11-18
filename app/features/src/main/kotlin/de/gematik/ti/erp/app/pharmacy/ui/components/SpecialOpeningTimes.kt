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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.pharmacy.model.SpecialOpeningTimeMetadata
import de.gematik.ti.erp.app.fhir.pharmacy.model.sortedByPeriodStart
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun SpecialOpeningTimes(
    specialOpeningTimes: List<SpecialOpeningTimeMetadata>
) {
    val sorted = specialOpeningTimes.sortedByPeriodStart()
    val upcomingOrActive = sorted.filter { !it.isInPast }
    if (upcomingOrActive.isEmpty()) {
        return
    }

    ErezeptText.Title(text = stringResource(id = R.string.pharm_detail_emergency_service_opening_times))
    SpacerSmall()

    upcomingOrActive.forEachIndexed { index, specialTime ->
        SpecialOpeningTime(specialTime)
        if (index < upcomingOrActive.lastIndex) {
            SpacerMedium()
        }
    }
}

@Composable
private fun SpecialOpeningTime(
    specialTimeMetadata: SpecialOpeningTimeMetadata
) {
    val specialTime = specialTimeMetadata.erpModel

    if (!specialTimeMetadata.isInPast) {
        val currentColor = when {
            specialTimeMetadata.isActive -> AppTheme.colors.green700
            else -> AppTheme.colors.neutral600
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isNightTime = (specialTime.period?.start as? FhirTemporal.Instant)?.let { dateTime ->
                val hour = dateTime.value.toLocalDateTime(TimeZone.currentSystemDefault()).hour
                hour !in 8..16
            } ?: false

            Image(
                painter = painterResource(
                    id = if (isNightTime) R.drawable.moon else R.drawable.bell
                ),
                contentDescription = null,
                modifier = Modifier.size(SizeDefaults.doubleHalf)
            )

            SpacerMedium()

            letNotNull(
                specialTime.period?.start,
                specialTime.period?.end
            ) { start, end ->
                PeriodTimeText(
                    start = start,
                    end = end,
                    isActive = specialTimeMetadata.isActive,
                    color = currentColor
                )
            }
        }
    }
}
