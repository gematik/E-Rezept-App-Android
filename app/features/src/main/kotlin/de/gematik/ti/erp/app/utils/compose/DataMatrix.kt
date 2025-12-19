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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.google.zxing.common.BitMatrix
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.core.R

private const val ScaleOutValue = 0.7f
private const val ScaleInValue = 1f

@Composable
fun DataMatrix(
    modifier: Modifier,
    matrix: BitMatrix
) {
    var isZoomedOut by remember { mutableStateOf(false) }
    val dataMatrixCodeDescription = stringResource(R.string.a11y_datamatrix_code_description)
    // Animating the scale factor
    val scale by animateFloatAsState(targetValue = if (isZoomedOut) ScaleOutValue else ScaleInValue)

    AppTheme(darkTheme = false) {
        val shape = RoundedCornerShape(SizeDefaults.double)

        Column(
            modifier = modifier
                .background(AppTheme.colors.neutral000, shape)
                .border(SizeDefaults.eighth, AppTheme.colors.neutral300, shape)
                .padding(PaddingDefaults.Medium)
        ) {
            Box(
                modifier = Modifier
                    .semantics() {
                        contentDescription = dataMatrixCodeDescription
                    }
                    .scale(scale)
                    .drawDataMatrix(matrix)
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            SpacerMedium()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                TertiaryButton(
                    onClick = { isZoomedOut = !isZoomedOut }
                ) {
                    Crossfade(targetState = isZoomedOut) { isZoomedOut ->
                        when {
                            isZoomedOut -> Icon(Icons.Rounded.ZoomIn, stringResource(R.string.a11y_datamatrix_code_zoom_in_button_description))
                            else -> Icon(Icons.Rounded.ZoomOut, stringResource(R.string.a11y_datamatrix_code_zoom_out_button_description))
                        }
                    }
                }
            }
        }
    }
}
