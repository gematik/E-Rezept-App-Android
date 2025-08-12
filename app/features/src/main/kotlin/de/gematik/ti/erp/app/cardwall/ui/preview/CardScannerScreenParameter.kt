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

package de.gematik.ti.erp.app.cardwall.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class CardScannerScreenParameter(
    val name: String,
    val detectedCan: String?,
    val isScanning: Boolean,
    val isFlashlightOn: Boolean
)

class CardScannerScreenParameterProvider : PreviewParameterProvider<CardScannerScreenParameter> {
    override val values: Sequence<CardScannerScreenParameter>
        get() = sequenceOf(
            CardScannerScreenParameter(
                name = "Scanning",
                detectedCan = null,
                isScanning = true,
                isFlashlightOn = false
            ),
            CardScannerScreenParameter(
                name = "Scanning with flashlight",
                detectedCan = null,
                isScanning = true,
                isFlashlightOn = true
            ),
            CardScannerScreenParameter(
                name = "CAN detected",
                detectedCan = "123123",
                isScanning = false,
                isFlashlightOn = false
            ),
            CardScannerScreenParameter(
                name = "CAN detected with flashlight",
                detectedCan = "123123",
                isScanning = false,
                isFlashlightOn = true
            )
        )
}
