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

package de.gematik.ti.erp.app.eurezept.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class QrCodeGenerator(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    suspend fun generateQrCode(insuranceNumber: String, code: String): ImageBitmap? {
        return withContext(dispatcher) {
            generateQrCodeInternal(insuranceNumber, code)
        }
    }

    companion object {
        // Putting QR logic to companion object to support both suspend (actual app) and sync (preview) usage
        internal fun generateQrCodeInternal(insuranceNumber: String, code: String): ImageBitmap? {
            return try {
                val qrContent = "Insurance: $insuranceNumber\nCode: $code"
                val bitMatrix = QRCodeWriter().encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
                bitMatrixToBitmap(bitMatrix).asImageBitmap()
            } catch (e: Exception) {
                Napier.e { "Error generating QR Code: ${e.message}" }
                null
            }
        }

        private fun bitMatrixToBitmap(
            bitMatrix: BitMatrix,
            foreground: Int = Color.BLACK,
            background: Int = Color.WHITE
        ): Bitmap {
            val pixels = IntArray(bitMatrix.width * bitMatrix.height) { index ->
                val x = index % bitMatrix.width
                val y = index / bitMatrix.width
                if (bitMatrix.get(x, y)) foreground else background
            }
            return Bitmap.createBitmap(pixels, bitMatrix.width, bitMatrix.height, Bitmap.Config.RGB_565)
        }
    }
}
