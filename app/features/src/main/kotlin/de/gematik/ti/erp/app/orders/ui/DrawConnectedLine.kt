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

package de.gematik.ti.erp.app.orders.ui

import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme

internal fun Modifier.drawConnectedLine(
    drawTop: Boolean,
    drawBottom: Boolean,
    topDashed: Boolean = false
) = composed {
    val lineColor = AppTheme.colors.neutral300
    val circleBackground = AppTheme.colors.neutral000
    val strokeWidth = 2.dp.toPx()
    val circleRadius = 8.dp.toPx()
    val backgroundRadius = 3.dp.toPx()

    drawBehind {
        val center = Offset(x = 24.dp.toPx(), y = size.height / 2)
        val start = if (drawTop) Offset(x = center.x, y = 0f) else center
        val end = if (drawBottom) Offset(x = center.x, y = size.height) else center

        drawLine(
            color = lineColor,
            strokeWidth = strokeWidth,
            start = start,
            end = end,
            pathEffect = if (topDashed) {
                PathEffect.dashPathEffect(
                    floatArrayOf(
                        5.dp.toPx(),
                        2.dp.toPx()
                    )
                )
            } else {
                null
            }
        )

        drawCircle(color = lineColor, center = center, radius = circleRadius)
        drawCircle(color = circleBackground, center = center, radius = backgroundRadius)
    }
}

private fun Dp.toPx(): Float {
    val density = Resources.getSystem().displayMetrics.density
    return this.value * density
}
