/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.shared.LIGHT_ALPHA
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun RectangularShapeShimmer(
    modifier: Modifier = Modifier,
    background: Color = AppTheme.colors.primary200,
    alpha: Float = LIGHT_ALPHA,
    height: Dp = SizeDefaults.triple,
    width: Dp = SizeDefaults.elevenfold
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .height(height)
            .width(width)
            .clip(RoundedCornerShape(SizeDefaults.one))
            .background(background)
            .alpha(alpha)
    )
}

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun RectangularShapeShimmerPreview() {
    PreviewTheme {
        RectangularShapeShimmer()
    }
}
