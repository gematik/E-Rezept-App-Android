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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.shimmer.RectangularShapeShimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall

@Composable
fun DigaPrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaddingDefaults.Medium)
    ) {
        PrimaryButtonSmall(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.XLarge)
                .wrapContentHeight(),
            enabled = enabled,
            onClick = { onClick() }
        ) {
            Text(text = text, style = AppTheme.typography.button)
        }
    }
}

@Composable
fun DigaPrimaryButtonLoading() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer()
            .padding(PaddingDefaults.Medium)
    ) {
        RectangularShapeShimmer()
    }
}

@LightDarkPreview
@Composable
internal fun DigaPrimaryButtonLoadingPreview() {
    PreviewTheme {
        DigaPrimaryButtonLoading()
    }
}
