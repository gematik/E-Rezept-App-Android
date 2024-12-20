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

package de.gematik.ti.erp.app.pkv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.shimmer.LimitedTextShimmer
import de.gematik.ti.erp.app.shimmer.RectangularShapeShimmer
import de.gematik.ti.erp.app.shimmer.TinyTextShimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLargeMedium
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("MagicNumber")
@Composable
private fun InvoiceListLoadingItem() {
    Column(
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        LimitedTextShimmer(width = SizeDefaults.twentythreefold)
        LimitedTextShimmer()
        Row {
            Column(
                modifier = Modifier.weight(0.55f)
            ) {
                TinyTextShimmer()
                SpacerSmall()
                RectangularShapeShimmer()
            }
            Spacer(Modifier.weight(0.3f))
            Icon(
                modifier = Modifier.weight(0.15f),
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.colors.neutral400
            )
        }

        SpacerLarge()
    }
}

@Suppress("MagicNumber")
@Composable
fun InvoiceListLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
            .shimmer()
    ) {
        SpacerXXLargeMedium()
        repeat(10) {
            InvoiceListLoadingItem()
        }
    }
}

@LightDarkPreview
@Composable
fun InvoiceListLoadingPreview() {
    PreviewAppTheme {
        InvoiceListLoading()
    }
}
