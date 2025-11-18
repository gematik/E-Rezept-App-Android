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

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.pharmacy.mapper.toFhirReadableDateTime
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.extensions.toAccessibleString
import kotlinx.datetime.TimeZone

/**
 * Displays a formatted period time range with appropriate styling and accessibility support for special closing and opening pharmacy time.
 *
 * @param start The start time as a [FhirTemporal] instance
 * @param end The end time as a [FhirTemporal] instance
 * @param isActive Whether the period is currently active (affects bold styling)
 * @param color The color to apply to the text
 * @param zone The time zone for converting temporal values. Defaults to system default.
 */
@Composable
internal fun PeriodTimeText(
    start: FhirTemporal,
    end: FhirTemporal,
    isActive: Boolean,
    color: Color,
    zone: TimeZone = TimeZone.currentSystemDefault()
) {
    val context = LocalContext.current

    val displayStart = start.toFhirReadableDateTime(zone) ?: ""
    val displayEnd = end.toFhirReadableDateTime(zone) ?: ""

    val displayText = if (hasTimeComponent(displayStart) && hasTimeComponent(displayEnd)) {
        "$displayStart -\n$displayEnd"
    } else {
        "$displayStart - $displayEnd"
    }

    Text(
        text = displayText,
        style = AppTheme.typography.body1.merge(
            if (isActive) TextStyle(fontWeight = FontWeight.Bold)
            else TextStyle()
        ),
        color = color,
        modifier = Modifier.semantics {
            this.contentDescription = "${start.toAccessibleString(context, zone)} ${end.toAccessibleString(context, zone)}"
        }
    )
}

private fun hasTimeComponent(dateTimeString: String): Boolean = dateTimeString.contains(":")
