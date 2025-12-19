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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import kotlin.math.max
import kotlin.math.roundToInt

private const val BitmapMinSize = 10

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
