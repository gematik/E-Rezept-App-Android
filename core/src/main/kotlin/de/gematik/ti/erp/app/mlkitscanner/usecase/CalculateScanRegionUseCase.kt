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

package de.gematik.ti.erp.app.mlkitscanner.usecase

import de.gematik.ti.erp.app.mlkitscanner.model.CardScannerData.ScanRegion
import de.gematik.ti.erp.app.mlkitscanner.model.CardScannerData.ScanRegionParams
import de.gematik.ti.erp.app.theme.SizeDefaults
import io.github.aakira.napier.Napier

class CalculateScanRegionUseCase {

    companion object {
        private const val PADDING_PERCENT = 0.1f
    }

    operator fun invoke(params: ScanRegionParams): ScanRegion? {
        return try {
            with(params.density) {
                val previewWidth = params.previewView.width
                val previewHeight = params.previewView.height

                if (previewWidth <= 0 || previewHeight <= 0) {
                    return null
                }

                val baseRegion = calculateBaseScanRegion(
                    params = params,
                    containerWidth = previewWidth,
                    containerHeight = previewHeight
                )

                applyPaddingToRegion(baseRegion)
            }
        } catch (e: Exception) {
            Napier.e("CalculateScanRegionUseCase error: ${e.message}")
            null
        }
    }

    fun calculateWithScreenCoordinates(
        params: ScanRegionParams,
        screenWidth: Int,
        screenHeight: Int
    ): ScanRegion? {
        return try {
            if (screenWidth <= 0 || screenHeight <= 0) {
                return null
            }

            calculateBaseScanRegion(
                params = params,
                containerWidth = screenWidth,
                containerHeight = screenHeight
            )
        } catch (e: Exception) {
            Napier.e("CalculateScanRegionUseCase error: ${e.message}")
            null
        }
    }

    private fun calculateBaseScanRegion(
        params: ScanRegionParams,
        containerWidth: Int,
        containerHeight: Int
    ): ScanRegion {
        return with(params.density) {
            val cardWidth = SizeDefaults.fortyfold.toPx()
            val cardHeight = SizeDefaults.twentyfivefold.toPx()

            val cardLeft = (containerWidth - cardWidth) / 2
            val cardTop = (containerHeight - cardHeight) / 2

            val imageWidth = params.imageProxy.width
            val imageHeight = params.imageProxy.height

            val scaleX = imageWidth.toFloat() / containerWidth.toFloat()
            val scaleY = imageHeight.toFloat() / containerHeight.toFloat()

            val scaledLeft = (cardLeft * scaleX).toInt()
            val scaledTop = (cardTop * scaleY).toInt()
            val scaledWidth = (cardWidth * scaleX).toInt()
            val scaledHeight = (cardHeight * scaleY).toInt()

            val clampedLeft = maxOf(0, scaledLeft)
            val clampedTop = maxOf(0, scaledTop)

            ScanRegion(
                left = clampedLeft,
                top = clampedTop,
                width = minOf(scaledWidth, imageWidth - clampedLeft),
                height = minOf(scaledHeight, imageHeight - clampedTop)
            )
        }
    }

    private fun applyPaddingToRegion(region: ScanRegion): ScanRegion {
        val widthPadding = (region.width * PADDING_PERCENT).toInt()
        val heightPadding = (region.height * PADDING_PERCENT).toInt()

        return ScanRegion(
            left = region.left + widthPadding,
            top = region.top + heightPadding,
            width = region.width - (widthPadding * 2),
            height = region.height - (heightPadding * 2)
        )
    }
}
