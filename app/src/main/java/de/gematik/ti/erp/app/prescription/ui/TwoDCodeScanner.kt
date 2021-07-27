/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.ui

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_SCAN_TIME = 250L

class TwoDCodeScanner @Inject constructor() : ImageAnalysis.Analyzer {
    data class Batch(
        val matrixCodes: List<Barcode>,
        val cameraSize: Size = Size(0, 0),
        val cameraRotation: Int = 0,
        val averageScanTime: Long = DEFAULT_SCAN_TIME
    )

    val defaultBatch = Batch(listOf())

    var batch = MutableStateFlow(defaultBatch)
        private set

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_DATA_MATRIX)
            .build()
    )

    private val countLock = Any()
    private var averageTime = DEFAULT_SCAN_TIME

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.also {
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)

            try {
                val t0 = System.currentTimeMillis()
                scanner.process(image)
                    .addOnSuccessListener { matrixCodes ->
                        synchronized(countLock) {
                            averageTime =
                                ((System.currentTimeMillis() - t0) + averageTime) / 2
                        }

                        if (matrixCodes.isNotEmpty()) {
                            batch.value =
                                Batch(
                                    matrixCodes = matrixCodes,
                                    cameraSize = Size(image.width, image.height),
                                    cameraRotation = image.rotationDegrees
                                )
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }
}
