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

package de.gematik.ti.erp.app.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

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

@Suppress("UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun StatusChipShimmerPreview() {
    PreviewTheme {
        StatusChipShimmer()
    }
}