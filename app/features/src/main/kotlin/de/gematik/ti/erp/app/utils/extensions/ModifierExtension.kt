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

package de.gematik.ti.erp.app.utils.extensions

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.navigation.oneSecondInfiniteTween
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

fun Modifier.sectionPadding() =
    this.padding(
        start = PaddingDefaults.Medium,
        end = PaddingDefaults.Medium,
        bottom = PaddingDefaults.Small,
        top = PaddingDefaults.Medium
    )

@Composable
fun Modifier.imeHeight(): Modifier {
    val density = LocalDensity.current
    val insets = WindowInsets.ime
    val height = remember(insets.getBottom(density)) {
        with(density) { insets.getBottom(density).toDp() }
    }
    return this.padding(bottom = height)
}

@Composable
fun Modifier.greyCircularBorder(): Modifier = this.then(
    Modifier.border(SizeDefaults.eighth, AppTheme.colors.neutral300, CircleShape)
)

@Composable
fun Modifier.circularBorder(
    color: Color,
    width: Dp = SizeDefaults.eighth
): Modifier = this.then(
    Modifier.border(
        color = color,
        width = width,
        shape = CircleShape
    )
)

@Composable
fun Modifier.animatedCircularBorder(
    animate: Boolean,
    startColor: Color = AppTheme.colors.neutral400,
    endColor: Color = AppTheme.colors.neutral600
): Modifier {
    return if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
        val width by infiniteTransition.animateValue(
            initialValue = SizeDefaults.zero,
            targetValue = SizeDefaults.half,
            typeConverter = Dp.VectorConverter,
            animationSpec = oneSecondInfiniteTween(),
            label = "width"
        )
        val color by infiniteTransition.animateColor(
            initialValue = startColor,
            targetValue = endColor,
            animationSpec = oneSecondInfiniteTween(),
            label = "color"
        )
        circularBorder(
            color = color,
            width = width
        )
    } else {
        Modifier
    }
}

fun Modifier.disableCopyPasteFromKeyboard() = this.then(
    Modifier.onPreviewKeyEvent { keyEvent ->
        if ((keyEvent.key == Key.C && keyEvent.isCtrlPressed) ||
            (keyEvent.key == Key.X && keyEvent.isCtrlPressed) ||
            (keyEvent.key == Key.V && keyEvent.isCtrlPressed)
        ) {
            // Intercept copy, cut, and paste actions
            return@onPreviewKeyEvent true
        }
        false
    }
)
