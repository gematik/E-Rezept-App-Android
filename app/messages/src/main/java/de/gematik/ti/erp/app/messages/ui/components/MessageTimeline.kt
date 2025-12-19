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
package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny

@Composable
internal fun MessageTimeline(
    drawFilledTop: Boolean,
    drawFilledBottom: Boolean,
    isClickable: Boolean = false,
    onClick: () -> Unit = {},
    timestamp: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    var internalLoading by remember { mutableStateOf(false) }

    // Y-position for placing the timeline circle in the middle of the timestamp row
    val circleYPosition = remember { mutableFloatStateOf(0f) }

    // Fade animation for whole item during load-in
    val alphaAnim = animateFloatAsState(
        targetValue = if (internalLoading) 0.6f else 1f,
        animationSpec = tween(300),
        label = "timeline-alpha"
    )

    // Vertical line animation (center → top/bottom)
    val lineProgress = animateFloatAsState(
        targetValue = if (internalLoading) 0f else 1f,
        animationSpec = tween(
            durationMillis = 450,
            delayMillis = 80
        ),
        label = "timeline-line-progress"
    )

    Row(
        Modifier
            .alpha(alphaAnim.value)
            .semantics(mergeDescendants = true) { hideFromAccessibility() }
            // animate size only when content changes, not skeletons
            .let {
                if (!internalLoading) it.animateContentSize()
                else it
            }
            .drawConnectedLine(
                drawFilledTop = drawFilledTop,
                drawFilledBottom = drawFilledBottom,
                circleYPosition = { circleYPosition.value },
                lineProgress = lineProgress.value
            )
            .then(
                if (isClickable && !internalLoading) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Spacer(Modifier.width(SizeDefaults.triple))

        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            SpacerMedium()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .calculateVerticalCenter(
                        onCenterCalculated = { circleYPosition.value = it }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (internalLoading) {
                    TimestampSkeleton()
                } else {
                    timestamp()
                }
            }

            SpacerTiny()

            if (internalLoading) {
                MessageSkeleton()
            } else {
                content()
            }
        }
    }
}

@Composable
private fun TimestampSkeleton() {
    Box(
        Modifier
            .fillMaxWidth(0.35f)
            .height(SizeDefaults.oneThreeQuarter)
            .clip(RoundedCornerShape(SizeDefaults.half))
            .shimmer()
    )
}

@Composable
private fun MessageSkeleton() {
    Column {
        // Chip placeholder
        Box(
            Modifier
                .fillMaxWidth(0.25f)
                .height(SizeDefaults.doubleQuarter)
                .clip(RoundedCornerShape(SizeDefaults.oneQuarter))
                .shimmer()
        )

        SpacerMedium()

        repeat(3) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(SizeDefaults.oneHalf)
                    .padding(vertical = SizeDefaults.threeSeventyFifth)
                    .clip(RoundedCornerShape(SizeDefaults.half))
                    .shimmer()
            )
        }
    }
}

@LightDarkPreview
@Composable
fun MessageSkeletonPreview() {
    PreviewTheme {
        Column {
            MessageSkeleton()
            MessageTimeline(
                drawFilledTop = false,
                drawFilledBottom = true,
                timestamp = {},
                content = {}
            )
            MessageTimeline(
                drawFilledTop = true,
                drawFilledBottom = true,
                timestamp = {},
                content = {}
            )
            MessageTimeline(
                drawFilledTop = true,
                drawFilledBottom = false,
                timestamp = {},
                content = {}
            )
        }
    }
}
