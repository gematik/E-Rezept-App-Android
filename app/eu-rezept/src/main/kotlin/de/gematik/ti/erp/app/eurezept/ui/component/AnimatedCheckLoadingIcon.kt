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
package de.gematik.ti.erp.app.eurezept.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

internal enum class AnimatedCheckLoadingIconState {
    Loading,
    Error,
    Success
}

@Composable
internal fun AnimatedCheckLoadingIcon(
    state: AnimatedCheckLoadingIconState,
    size: Dp = SizeDefaults.sixfold,
    loadingColor: Color = AppTheme.colors.primary600,
    successColor: Color = AppTheme.colors.green600,
    errorColor: Color = AppTheme.colors.red600,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(state, label = "checkLoadingTransition")

    // Rotation for loading
    val rotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = LinearEasing) },
        label = "rotation"
    ) { target ->
        if (target == AnimatedCheckLoadingIconState.Loading) 360f else 0f
    }

    // Loader arc visibility
    val loaderAlpha by transition.animateFloat(
        transitionSpec = { tween(300) },
        label = "loaderAlpha"
    ) { target ->
        if (target == AnimatedCheckLoadingIconState.Loading) 1f else 0f
    }

    // Outline circle visibility
    val circleAlpha by transition.animateFloat(
        transitionSpec = { tween(250, delayMillis = 120) },
        label = "circleAlpha"
    ) { target ->
        when (target) {
            AnimatedCheckLoadingIconState.Success,
            AnimatedCheckLoadingIconState.Error -> 1f
            else -> 0f
        }
    }

    // Symbol (check or X) visibility
    val symbolAlpha by transition.animateFloat(
        transitionSpec = { tween(250, delayMillis = 200) },
        label = "symbolAlpha"
    ) { target ->
        when (target) {
            AnimatedCheckLoadingIconState.Success,
            AnimatedCheckLoadingIconState.Error -> 1f
            else -> 0f
        }
    }

    // Pop-in scale animation
    val symbolScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "symbolScale"
    ) { target ->
        if (target == AnimatedCheckLoadingIconState.Loading) 0.7f else 1f
    }

    val resultSize = if (state == AnimatedCheckLoadingIconState.Loading) SizeDefaults.fourfold else size

    Canvas(
        modifier = modifier
            .padding(PaddingDefaults.Tiny)
            .size(resultSize)
            .graphicsLayer { rotationZ = rotation }
    ) {
        val width = size.toPx()
        val radius = width / 2f
        val strokeWidth = width * 0.10f

        // 1) LOADER ARC (blue)
        if (loaderAlpha > 0f) {
            drawArc(
                color = loadingColor,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                alpha = loaderAlpha,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 2) OUTLINE CIRCLE (SUCCESS or ERROR)
        if (circleAlpha > 0f) {
            val circleColor = when (state) {
                AnimatedCheckLoadingIconState.Success -> successColor
                AnimatedCheckLoadingIconState.Error -> errorColor
                else -> Color.Transparent
            }

            drawCircle(
                color = circleColor,
                radius = radius - strokeWidth / 2f,
                alpha = circleAlpha,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 3) SYMBOL PATH (Check or X)
        if (symbolAlpha > 0f) {
            val symbolColor = when (state) {
                AnimatedCheckLoadingIconState.Success -> successColor
                AnimatedCheckLoadingIconState.Error -> errorColor
                else -> Color.Transparent
            }

            val symbolPath = Path().apply {
                if (state == AnimatedCheckLoadingIconState.Success) {
                    // CHECKMARK
                    moveTo(width * 0.28f, width * 0.55f)
                    lineTo(width * 0.42f, width * 0.70f)
                    lineTo(width * 0.72f, width * 0.35f)
                } else {
                    // ERROR "X"
                    moveTo(width * 0.30f, width * 0.30f)
                    lineTo(width * 0.70f, width * 0.70f)
                    moveTo(width * 0.70f, width * 0.30f)
                    lineTo(width * 0.30f, width * 0.70f)
                }
            }

            withTransform({
                scale(symbolScale, symbolScale, pivot = center)
            }) {
                drawPath(
                    path = symbolPath,
                    color = symbolColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    alpha = symbolAlpha
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun AnimatedCheckLoadingPreview_Loading() {
    PreviewTheme {
        AnimatedCheckLoadingIcon(state = AnimatedCheckLoadingIconState.Loading)
    }
}

@LightDarkPreview
@Composable
private fun AnimatedCheckLoadingPreview_Success() {
    PreviewTheme {
        AnimatedCheckLoadingIcon(state = AnimatedCheckLoadingIconState.Success)
    }
}

@LightDarkPreview
@Composable
private fun AnimatedCheckLoadingPreview_Error() {
    PreviewTheme {
        AnimatedCheckLoadingIcon(state = AnimatedCheckLoadingIconState.Error)
    }
}
