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

package de.gematik.ti.erp.app.messages.presentation.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import de.gematik.ti.erp.app.extensions.toPx
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

internal fun Modifier.drawConnectedLine(
    drawTop: Boolean,
    drawBottom: Boolean
) = composed {
    val lineColor = AppTheme.colors.neutral300
    val circleBackground = AppTheme.colors.neutral000
    val strokeWidth = SizeDefaults.quarter.toPx()
    val circleRadius = SizeDefaults.onefold.toPx()
    val backgroundRadius = SizeDefaults.threeSeventyFifth.toPx()

    drawBehind {
        val center = Offset(x = SizeDefaults.triple.toPx(), y = size.height / 4f)
        val start = if (drawTop) Offset(x = center.x, y = 0f) else center
        val end = if (drawBottom) Offset(x = center.x, y = size.height) else center

        drawLine(
            color = lineColor,
            strokeWidth = strokeWidth,
            start = start,
            end = end
        )

        drawCircle(color = lineColor, center = center, radius = circleRadius)
        drawCircle(color = circleBackground, center = center, radius = backgroundRadius)
    }
}
