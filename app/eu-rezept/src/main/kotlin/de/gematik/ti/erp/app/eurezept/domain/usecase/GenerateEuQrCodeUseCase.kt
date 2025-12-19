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
package de.gematik.ti.erp.app.eurezept.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.util.QrCodeGenerator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

internal class GenerateEuQrCodeUseCase(
    private val qrCodeGenerator: QrCodeGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Generates a QR code bitmap for the given [euAccessCode] and [insuranceNumber].
     *
     * @param euAccessCode The EU access code to encode.
     * @param insuranceNumber The insurance number to include in the QR code.
     * @return [ImageBitmap] of the QR code, or null if generation fails.
     */
    suspend operator fun invoke(
        euAccessCode: EuAccessCode,
        insuranceNumber: String
    ): Flow<ImageBitmap?> = try {
        withContext(dispatcher) {
            flowOf(
                qrCodeGenerator.generateQrCode(
                    insuranceNumber = insuranceNumber,
                    code = euAccessCode.accessCode
                )
            )
        }
    } catch (e: Exception) {
        Napier.e { "Error generating QR Code: ${e.message}" }
        flowOf(null)
    }
}
