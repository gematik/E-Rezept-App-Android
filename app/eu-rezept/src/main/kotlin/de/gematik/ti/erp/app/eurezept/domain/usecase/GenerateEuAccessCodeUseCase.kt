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
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.eurezept.util.QrCodeGenerator
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for generating an EU access code and its corresponding QR code bitmap for a given profile and country.
 *
 * This class coordinates the creation of the access code, generates the QR code, and returns the result as [EuRedemptionDetails].
 * All operations are performed on the provided [dispatcher] (default: IO).
 *
 * @property euRepository Repository for EU access code creation.
 * @property qrCodeGenerator Utility for generating QR code bitmaps.
 * @property dispatcher Coroutine dispatcher for background operations.
 */
internal class GenerateEuAccessCodeUseCase(
    private val euRepository: EuRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Generates an EU access code and its QR code bitmap for the given [profile], [countryCode], and [relatedTaskIds].
     *
     * @param profile The profile for which to generate the access code.
     * @param countryCode The country code for redemption.
     * @param relatedTaskIds List of related task IDs.
     * @return [Result] containing [EuRedemptionDetails] on success, or an error on failure.
     */
    suspend operator fun invoke(
        profile: ProfilesUseCaseData.Profile,
        countryCode: String,
        relatedTaskIds: List<String>
    ): Result<EuRedemptionDetails> =
        withContext(dispatcher) {
            createEuAccessCode(
                profileIdentifier = profile.id,
                countryCode = countryCode,
                relatedTaskIds = relatedTaskIds
            ).mapCatching { euAccessCode ->
                val qrCode = generateCode(
                    euAccessCode = euAccessCode,
                    insuranceNumber = profile.insurance.insuranceIdentifier
                )
                EuRedemptionDetails(
                    euAccessCode = euAccessCode,
                    insuranceNumber = profile.insurance.insuranceIdentifier,
                    qrCodeBitmap = qrCode
                )
            }
        }

    /**
     * Creates an EU access code for the given [profileIdentifier], [countryCode], and [relatedTaskIds].
     *
     * @param profileIdentifier The profile identifier.
     * @param countryCode The country code for redemption.
     * @param relatedTaskIds List of related task IDs.
     * @return [Result] containing [EuAccessCode] on success, or an error on failure.
     */
    private suspend fun createEuAccessCode(
        profileIdentifier: ProfileIdentifier,
        countryCode: String,
        relatedTaskIds: List<String>
    ): Result<EuAccessCode> =
        euRepository.createEuRedeemAccessCode(
            profileId = profileIdentifier,
            countryCode = countryCode,
            relatedTaskIds = relatedTaskIds
        )

    /**
     * Generates a QR code bitmap for the given [euAccessCode] and [insuranceNumber].
     *
     * @param euAccessCode The EU access code to encode.
     * @param insuranceNumber The insurance number to include in the QR code.
     * @return [ImageBitmap] of the QR code, or null if generation fails.
     */
    private suspend fun generateCode(
        euAccessCode: EuAccessCode,
        insuranceNumber: String
    ): ImageBitmap? = try {
        qrCodeGenerator.generateQrCode(
            insuranceNumber = insuranceNumber,
            code = euAccessCode.accessCode
        )
    } catch (e: Exception) {
        Napier.e { "Error generating QR Code: ${e.message}" }
        null
    }
}
