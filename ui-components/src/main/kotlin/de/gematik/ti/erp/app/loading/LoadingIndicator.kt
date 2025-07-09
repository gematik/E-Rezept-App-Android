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

package de.gematik.ti.erp.app.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import kotlin.math.floor

@Composable
fun LoadingIndicator() {
    Center {
        SmartCircularProgressIndicator()
    }
}

@Composable
fun SmartCircularProgressIndicator(
    colorCycleDurationMillis: Int = 1000,
    spinSpeedMillis: Int = 800 // lower = faster spin
) {
    val colors = listOf(
        AppTheme.colors.primary100,
        AppTheme.colors.primary200,
        AppTheme.colors.primary300,
        AppTheme.colors.primary400,
        AppTheme.colors.primary500,
        AppTheme.colors.primary600,
        AppTheme.colors.primary800,
        AppTheme.colors.primary900,
        AppTheme.colors.primary800,
        AppTheme.colors.primary700,
        AppTheme.colors.primary600,
        AppTheme.colors.primary500,
        AppTheme.colors.primary400,
        AppTheme.colors.primary300,
        AppTheme.colors.primary200
    )

    // Spinning animation
    val infiniteTransition = rememberInfiniteTransition(label = "Spinner")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = spinSpeedMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val colorAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = colors.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = colorCycleDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorCycle"
    )

    // Determine current color index
    val colorIndex = floor(colorAnimation).toInt() % colors.size
    val currentColor = colors[colorIndex]

    Box(
        modifier = Modifier
            .size(SizeDefaults.doubleHalf)
            .rotate(rotation)
    ) {
        CircularProgressIndicator(
            strokeWidth = SizeDefaults.half,
            color = currentColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}
