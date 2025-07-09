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

package de.gematik.ti.erp.app.animated

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Suppress("MagicNumber")
@Composable
fun LoadingIndicatorLine(isLoading: Boolean) {
    if (!isLoading) return

    val transition = rememberInfiniteTransition()

    // the moving blue line's offset
    val offsetX by transition.animateValue(
        initialValue = SizeDefaults.zero,
        targetValue = SizeDefaults.fortyfold,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // the grey line
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SizeDefaults.half) // Adjust thickness
            .background(AppTheme.colors.neutral600.copy(alpha = 0.3f))
    ) {
        // the moving blue line
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(SizeDefaults.tenfold) // Width of the moving bar
                .height(SizeDefaults.half)
                .background(AppTheme.colors.primary600, shape = RoundedCornerShape(50))
        )
    }
}

@Preview
@Composable
fun LoadingIndicatorLinePreview() {
    PreviewTheme {
        LoadingIndicatorLine(isLoading = true)
    }
}
