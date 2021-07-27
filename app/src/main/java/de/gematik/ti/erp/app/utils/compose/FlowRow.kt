/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import kotlin.math.max
import kotlin.random.Random

private fun runningCalc(
    placeables: List<Placeable>,
    constraints: Constraints,
    spaceBetweenItems: Int,
    spaceBetweenRows: Int,
    place: Placeable.(x: Int, y: Int) -> Unit
): Int {
    var xPosition = 0
    var yPosition = 0
    var minHeightOfRow = 0
    placeables.forEach { placeable ->
        if (xPosition + placeable.measuredWidth > constraints.maxWidth) {
            xPosition = 0
            yPosition += minHeightOfRow + spaceBetweenRows
            minHeightOfRow = placeable.measuredHeight
        } else {
            minHeightOfRow = max(placeable.measuredHeight, minHeightOfRow)
        }

        placeable.place(xPosition, yPosition)

        xPosition += placeable.measuredWidth + spaceBetweenItems
    }
    return yPosition + minHeightOfRow
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    spaceBetweenItems: Dp = 0.dp,
    spaceBetweenRows: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Layout(modifier = Modifier.wrapContentSize(), content = content) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            val requiredHeight = runningCalc(
                placeables,
                constraints,
                spaceBetweenItems.roundToPx(),
                spaceBetweenRows.roundToPx()
            ) { _, _ -> }

            layout(constraints.maxWidth, constraints.constrainHeight(requiredHeight)) {
                runningCalc(
                    placeables,
                    constraints,
                    spaceBetweenItems.roundToPx(),
                    spaceBetweenRows.roundToPx()
                ) { xPosition, yPosition ->
                    place(x = xPosition, y = yPosition)
                }
            }
        }
    }
}

@Preview
@Composable
private fun FlowRowPreview() {
    AppTheme {
        FlowRow(
            spaceBetweenItems = 2.dp,
            spaceBetweenRows = 12.dp,
        ) {
            repeat(20) {
                val r = Random(it).nextInt(0, 10)
                Text("Test ${"|".repeat(r)}")
            }
        }
    }
}
