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

package de.gematik.ti.erp.app.cardwall

import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallLoadNfcPositionUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.mlkitscanner.usecase.CalculateScanRegionUseCase
import de.gematik.ti.erp.app.mlkitscanner.usecase.CardScannerUseCase
import de.gematik.ti.erp.app.mlkitscanner.usecase.SetupCameraUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val cardWallModule = DI.Module("cardWallModule") {
    bindProvider { AuthenticationUseCase(instance()) }
    bindProvider { CardWallLoadNfcPositionUseCase(instance()) }
    bindProvider { CardWallUseCase(instance(), instance()) }
    bindSingleton<TextRecognizer> {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    bindSingleton { CardScannerUseCase(instance()) }
    bindProvider { SetupCameraUseCase() }
    bindProvider { CalculateScanRegionUseCase() }
}
