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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

// TODO: To be deleted and the ones from ui-components used instead

private const val LIGHT_ALPHA = 0.2F

@Deprecated("Use ui-components version")
@Composable
fun LimitedTextShimmer(
    background: Color = AppTheme.colors.primary200
) {
    Spacer(
        modifier = Modifier
            .height(SizeDefaults.oneHalf)
            .width(SizeDefaults.fifteenfold)
            .background(background)
    )
}

@Deprecated("Use ui-components version")
@Composable
fun StatusChipShimmer(
    background: Color = AppTheme.colors.primary200
) {
    val shape = RoundedCornerShape(SizeDefaults.double)
    Row(
        Modifier
            .background(background, shape)
            .padding(SizeDefaults.eighth)
            .clip(shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TinyTextShimmer()
        SpacerSmall()
        SquareShapeShimmer(size = SizeDefaults.eighth)
    }
}

@Deprecated("Use ui-components version")
@Composable
fun TinyTextShimmer(
    background: Color = AppTheme.colors.primary200
) {
    Spacer(
        modifier = Modifier
            .height(SizeDefaults.oneHalf)
            .width(SizeDefaults.sevenfoldAndHalf)
            .background(background)
    )
}

@Deprecated("Use ui-components version")
@Composable
fun RowTextShimmer(
    modifier: Modifier = Modifier,
    background: Color = AppTheme.colors.primary200
) {
    Spacer(
        modifier = Modifier
            .height(SizeDefaults.oneHalf)
            .fillMaxWidth()
            .then(modifier)
            .background(background)
    )
}

@Deprecated("Use ui-components version")
@Composable
fun CircularShapeShimmer(
    modifier: Modifier = Modifier,
    background: Color = AppTheme.colors.primary200,
    alpha: Float = LIGHT_ALPHA,
    size: Dp = SizeDefaults.fivefold
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .size(size)
            .clip(CircleShape)
            .background(background)
            .alpha(alpha)
    )
}

@Deprecated("Use ui-components version")
@Composable
fun SquareShapeShimmer(
    modifier: Modifier = Modifier,
    background: Color = AppTheme.colors.primary200,
    alpha: Float = LIGHT_ALPHA,
    size: Dp = SizeDefaults.fivefold
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .size(size)
            .clip(RoundedCornerShape(SizeDefaults.one))
            .background(background)
            .alpha(alpha)
    )
}
