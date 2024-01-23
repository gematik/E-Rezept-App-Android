/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

private const val LIGHT_ALPHA = 0.2F

@Composable
fun InvoiceLoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = AppTheme.colors.neutral000,
        elevation = 0.dp,
        border = BorderStroke(1.dp, AppTheme.colors.primary300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium)
                .shimmer(),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            verticalAlignment = Alignment.Top
        ) {
            CircularShape()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                TitleRow()
                SpacerSmall()
                BodyRow()
            }
        }
    }
}

@Composable
private fun CircularShape(
    background: Color = AppTheme.colors.primary200,
    alpha: Float = LIGHT_ALPHA,
    size: Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .alpha(alpha)
    )
}

@Composable
private fun TitleRow(
    background: Color = AppTheme.colors.primary200
) {
    Spacer(
        modifier = Modifier
            .height(12.dp)
            .width(120.dp)
            .background(background)
    )
}

@Composable
private fun BodyRow(
    background: Color = AppTheme.colors.primary200
) {
    Spacer(
        modifier = Modifier
            .height(12.dp)
            .fillMaxWidth()
            .background(background)
    )
}

@LightDarkPreview
@Composable
fun InvoiceShimmerCardPreview() {
    PreviewAppTheme {
        InvoiceLoadingCard()
    }
}
