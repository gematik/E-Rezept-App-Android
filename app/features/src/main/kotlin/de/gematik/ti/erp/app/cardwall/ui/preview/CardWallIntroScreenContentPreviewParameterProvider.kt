/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.cardwall.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class CardWallIntroScreenPreviewData(
    val name: String,
    val isNfcAvailable: Boolean,
    val isNfcEnabled: Boolean,
    val isDomainVerified: Boolean
)

class CardWallIntroScreenContentPreviewParameterProvider : PreviewParameterProvider<CardWallIntroScreenPreviewData> {
    override val values = sequenceOf(nfcNotAvailable, nfcNotEnabled, nfcEnabled)

    companion object {
        val nfcNotAvailable = CardWallIntroScreenPreviewData(
            name = "nfcNotAvailable",
            isNfcAvailable = false,
            isNfcEnabled = false,
            isDomainVerified = false
        )

        val nfcNotEnabled = CardWallIntroScreenPreviewData(
            name = "nfcNotEnabled",
            isNfcAvailable = true,
            isNfcEnabled = false,
            isDomainVerified = false
        )

        val nfcEnabled = CardWallIntroScreenPreviewData(
            name = "nfcEnabled",
            isNfcAvailable = true,
            isNfcEnabled = true,
            isDomainVerified = false
        )
    }
}
