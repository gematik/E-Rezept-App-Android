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

import android.graphics.Bitmap
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
import androidx.compose.material.Text
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import kotlin.math.max
import kotlin.math.roundToInt

private const val BitmapMinSize = 10
private const val ScaleOutValue = 0.7f
private const val ScaleInValue = 1f

@Composable
fun DataMatrix(
    modifier: Modifier,
    matrix: BitMatrix,
    codeName: String? = null
) {
    var isZoomedOut by remember { mutableStateOf(false) }

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
                    .scale(scale)
                    .drawDataMatrix(matrix)
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            SpacerMedium()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (codeName != null) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = codeName,
                        style = AppTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = AppTheme.colors.neutral999
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                SpacerMedium()
                TertiaryButton(
                    onClick = { isZoomedOut = !isZoomedOut }
                ) {
                    Crossfade(targetState = isZoomedOut) { isZoomedOut ->
                        when {
                            isZoomedOut -> Icon(Icons.Rounded.ZoomIn, null)
                            else -> Icon(Icons.Rounded.ZoomOut, null)
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.drawDataMatrix(matrix: BitMatrix) =
    drawWithCache {
        val bitmap = Bitmap.createScaledBitmap(
            matrix.toBitmap(),
            max(size.width.roundToInt(), BitmapMinSize),
            max(size.height.roundToInt(), BitmapMinSize),
            false
        )

        onDrawBehind {
            drawImage(bitmap.asImageBitmap())
        }
    }

fun createBitMatrix(data: String): BitMatrix =
    // width & height is unused in the underlying implementation
    DataMatrixWriter().encode(data, BarcodeFormat.DATA_MATRIX, 1, 1)

fun BitMatrix.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x,
                y,
                if (get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}
