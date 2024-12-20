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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.CircularShapeShimmer
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LimitedTextShimmer
import de.gematik.ti.erp.app.utils.compose.RowTextShimmer
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

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
            CircularShapeShimmer()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                LimitedTextShimmer()
                SpacerSmall()
                RowTextShimmer()
            }
        }
    }
}

@LightDarkPreview
@Composable
fun InvoiceShimmerCardPreview() {
    PreviewAppTheme {
        InvoiceLoadingCard()
    }
}
