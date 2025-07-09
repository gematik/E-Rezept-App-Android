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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LimitedTextShimmer
import de.gematik.ti.erp.app.utils.compose.RowTextShimmer
import de.gematik.ti.erp.app.utils.compose.SquareShapeShimmer
import de.gematik.ti.erp.app.utils.compose.TinyTextShimmer
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Suppress("FunctionName")
internal fun LazyListScope.PharmacySearchLoading(
    modifier: Modifier = Modifier
) {
    item {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium)
                .shimmer(),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            verticalAlignment = Alignment.Top
        ) {
            SquareShapeShimmer(
                size = SizeDefaults.eightfold
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                RowTextShimmer()
                LimitedTextShimmer()
                TinyTextShimmer()
            }
        }
    }
}

@Suppress("FunctionName")
internal fun LazyListScope.PharmacyFullScreenSearchLoading() {
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
    PharmacySearchLoading()
}

@LightDarkPreview
@Composable
fun PharmacySearchLoadingPreview() {
    PreviewAppTheme {
        LazyColumn {
            PharmacyFullScreenSearchLoading()
        }
    }
}
