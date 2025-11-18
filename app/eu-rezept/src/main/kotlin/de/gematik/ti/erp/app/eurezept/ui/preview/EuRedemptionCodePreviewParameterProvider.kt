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

package de.gematik.ti.erp.app.eurezept.ui.preview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.util.QrCodeGenerator
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

data class EuRedemptionCodePreviewParameter(
    val name: String,
    val uiState: UiState<EuRedemptionDetails>,
    val isQrCodeVisible: Boolean = true
)

class EuRedemptionCodePreviewParameterProvider : PreviewParameterProvider<EuRedemptionCodePreviewParameter> {

    override val values = sequenceOf(
        EuRedemptionCodePreviewParameter(
            name = "Loading",
            uiState = UiState.Loading()
        ),
        EuRedemptionCodePreviewParameter(
            name = "Error",
            uiState = UiState.Error(Throwable())
        ),
        EuRedemptionCodePreviewParameter(
            name = "Data - Qr Code",
            uiState = UiState.Data(
                EuRedemptionDetails(
                    euAccessCode = EuAccessCode(
                        countryCode = "IT",
                        accessCode = "123456",
                        validUntil = Instant.DISTANT_FUTURE,
                        createdAt = Instant.DISTANT_FUTURE,
                        profileIdentifier = "profile1"
                    ),
                    insuranceNumber = "KVNR123",
                    qrCodeBitmap = createDummyQrCode()
                )
            )
        ),
        EuRedemptionCodePreviewParameter(
            name = "Data - Code",
            uiState = UiState.Data(
                EuRedemptionDetails(
                    euAccessCode = EuAccessCode(
                        countryCode = "IT",
                        accessCode = "123456",
                        validUntil = Instant.DISTANT_FUTURE,
                        createdAt = Instant.DISTANT_FUTURE,
                        profileIdentifier = "profile1"
                    ),
                    insuranceNumber = "KVNR123",
                    qrCodeBitmap = createDummyQrCode()
                )
            ),
            isQrCodeVisible = false
        ),
        EuRedemptionCodePreviewParameter(
            name = "Data - Code Expired",
            uiState = UiState.Data(
                EuRedemptionDetails(
                    euAccessCode = EuAccessCode(
                        countryCode = "IT",
                        accessCode = "123456",
                        validUntil = Instant.DISTANT_PAST,
                        createdAt = Instant.DISTANT_PAST,
                        profileIdentifier = "profile1"
                    ),
                    insuranceNumber = "KVNR123",
                    qrCodeBitmap = createDummyQrCode()
                )
            ),
            isQrCodeVisible = false
        ),
        EuRedemptionCodePreviewParameter(
            name = "Data - QR Code Expired",
            uiState = UiState.Data(
                EuRedemptionDetails(
                    euAccessCode = EuAccessCode(
                        countryCode = "IT",
                        accessCode = "123456",
                        validUntil = Instant.DISTANT_PAST,
                        createdAt = Instant.DISTANT_PAST,
                        profileIdentifier = "profile1"
                    ),
                    insuranceNumber = "KVNR123",
                    qrCodeBitmap = createDummyQrCode()
                )
            )
        )

    )
}

private fun createDummyQrCode(): ImageBitmap? {
    return QrCodeGenerator.generateQrCodeInternal(
        insuranceNumber = "M 1 2 3 4 5 6 7 8 9",
        code = "A 1 b 2 C 3"
    )
}
