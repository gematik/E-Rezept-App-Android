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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.extensions.toPx
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

/**
 * A custom Modifier extension function to draw a connected line with a circular marker at a specific
 * position in a message list. The function supports rendering dotted or solid lines above and below the circle
 * based on the `drawFilledTop` and `drawFilledBottom` flags.
 *
 * @param drawFilledTop A flag indicating whether to draw a filled line above the circle.
 *                      If false, a dotted line is drawn instead.
 * @param drawFilledBottom A flag indicating whether to draw a filled line below the circle.
 * @param circleYPosition A lambda function that dynamically provides the vertical position (Y-coordinate)
 *                        of the circle relative to its parent container.
 *
 * ### Drawing Details:
 * - If `drawFilledTop` is false, a dotted line is drawn above the circle.
 * - If `drawFilledBottom` is true, a filled line is drawn below the circle.
 * - A smaller filled circle and a larger background circle are drawn at the center position.
 *
 * This function is designed for use in a message list to visually connect items with lines and markers.
 */
@Suppress("MagicNumber")
internal fun Modifier.drawConnectedLine(
    drawFilledTop: Boolean, // there are messages above this in the list
    drawFilledBottom: Boolean, // if there are messages below this in the list
    circleYPosition: () -> Float // Use a lambda to dynamically fetch the value
) = composed {
    val lineColor = AppTheme.colors.neutral300
    val circleBackground = AppTheme.colors.neutral000

    drawBehind {
        val textYPosition = circleYPosition()

        val center = Offset(x = SizeDefaults.triple.toPx(), y = textYPosition)
        val start = if (drawFilledTop) Offset(x = center.x, y = 0f) else center
        val end = if (drawFilledBottom) Offset(x = center.x, y = size.height) else center

        // Draw the dotted line above the circle if it's the first message (drawTop = false)
        if (!drawFilledTop) {
            drawDottedTop(
                center = center,
                textYPosition = textYPosition,
                lineColor = lineColor
            )
        }

        drawFixedLine(
            start = start,
            end = end,
            lineColor = lineColor
        )

        drawSmallerCircle(
            center = center,
            lineColor = lineColor
        )

        drawBiggerCircleAroundSmaller(
            center = center,
            circleBackground = circleBackground
        )
    }
}

@Suppress("MagicNumber")
private fun DrawScope.drawDottedTop(
    circleRadius: Float = SizeDefaults.onefold.toPx(),
    strokeWidth: Float = SizeDefaults.quarter.toPx(),
    textYPosition: Float,
    lineColor: Color,
    center: Offset
) {
    val dotHeight = SizeDefaults.half.toPx() // Height of each dot
    val gapHeight = SizeDefaults.quarter.toPx() // Gap between dots
    val initialY = textYPosition - circleRadius // Starting position (touching the circle)

    // Draw the first (largest) dot
    drawRect(
        color = lineColor,
        topLeft = Offset(center.x - strokeWidth / 2, initialY - dotHeight),
        size = Size(strokeWidth, dotHeight)
    )

    // Draw the second (smaller) dot
    val secondDotHeight = dotHeight * 0.7f // Make it 70% of the first dot's height
    val secondDotY = initialY - dotHeight - gapHeight
    drawRect(
        color = lineColor,
        topLeft = Offset(center.x - strokeWidth / 2, secondDotY - secondDotHeight),
        size = Size(strokeWidth, secondDotHeight)
    )

    // Draw the third (smallest) dot
    val thirdDotHeight = dotHeight * 0.5f // Make it 50% of the first dot's height
    val thirdDotY = secondDotY - secondDotHeight - gapHeight
    drawRect(
        color = lineColor,
        topLeft = Offset(center.x - strokeWidth / 2, thirdDotY - thirdDotHeight),
        size = Size(strokeWidth, thirdDotHeight)
    )
}

private fun DrawScope.drawFixedLine(
    strokeWidth: Float = SizeDefaults.quarter.toPx(),
    start: Offset,
    end: Offset,
    lineColor: Color
) {
    drawLine(
        color = lineColor,
        strokeWidth = strokeWidth,
        start = start,
        end = end
    )
}

private fun DrawScope.drawSmallerCircle(
    circleRadius: Float = SizeDefaults.onefold.toPx(),
    center: Offset,
    lineColor: Color
) {
    drawCircle(color = lineColor, center = center, radius = circleRadius)
}

private fun DrawScope.drawBiggerCircleAroundSmaller(
    backgroundRadius: Float = SizeDefaults.threeSeventyFifth.toPx(),
    center: Offset,
    circleBackground: Color
) {
    drawCircle(color = circleBackground, center = center, radius = backgroundRadius)
}

/**
 * A Modifier extension function that calculates the vertical center position of a Composable
 * relative to its parent and passes the result as a Float to the given callback.
 *
 * @param onCenterCalculated A callback that receives the calculated vertical center (in pixels)
 *                           of the Composable as a [Float].
 * @param offset An optional [Dp] value that adjusts the calculated center position.
 *               Useful for fine-tuning the alignment (default is 0.dp).
 *
 * ### Usage Example:
 * ```
 * var circleYPosition by remember { mutableStateOf(0f) }
 *
 * Text(
 *     text = "Hello World",
 *     modifier = Modifier.calculateVerticalCenter(
 *         onCenterCalculated = { centerY ->
 *             circleYPosition = centerY // Update the state with the calculated center
 *         },
 *         offset = 8.dp // Apply a custom offset if needed
 *     )
 * )
 * ```
 *
 * ### Key Details:
 * - The calculation includes the Composable's position within its parent using `positionInParent`.
 * - Adds half the height of the Composable to get the vertical center.
 * - Optionally applies an offset to adjust the calculated center.
 *
 * This function is useful when aligning UI elements (e.g., placing a circle beside a Text)
 * that require precise positioning relative to the center of another Composable.
 */
internal fun Modifier.calculateVerticalCenter(
    onCenterCalculated: (Float) -> Unit,
    offset: Dp = SizeDefaults.oneThreeQuarter // Optional offset to fine-tune the position
): Modifier = this.onGloballyPositioned { layoutCoordinates ->
    val positionInParent = layoutCoordinates.positionInParent()
    val centerY = positionInParent.y + layoutCoordinates.size.height / 2
    onCenterCalculated(centerY + offset.toPx())
}
