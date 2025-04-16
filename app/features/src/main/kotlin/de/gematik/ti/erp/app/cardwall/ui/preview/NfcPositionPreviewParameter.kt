/*
 * Copyright 2025, gematik GmbH
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
import de.gematik.ti.erp.app.cardwall.usecase.model.NfcPositionUseCaseData

data class NfcPositionPreview(
    val name: String,
    val nfcPos: NfcPositionUseCaseData.NfcPos
)

class NfcPositionPreviewParameter : PreviewParameterProvider<NfcPositionPreview> {
    override val values: Sequence<NfcPositionPreview>
        get() = sequenceOf(
            NfcPositionPreview(
                "top-left",
                NfcPositionUseCaseData.NfcPos(
                    x0 = 0.0,
                    y0 = 0.0,
                    x1 = 0.0,
                    y1 = 0.0
                )
            ),
            NfcPositionPreview(
                "top-mid",
                NfcPositionUseCaseData.NfcPos(
                    x0 = 0.5,
                    y0 = 0.0,
                    x1 = 0.5,
                    y1 = 0.0
                )
            ),
            NfcPositionPreview(
                "top-right",
                NfcPositionUseCaseData.NfcPos(
                    x0 = 1.0,
                    y0 = 0.0,
                    x1 = 1.0,
                    y1 = 0.0
                )
            ),
            NfcPositionPreview(
                "center-mid",
                NfcPositionUseCaseData.NfcPos(
                    x0 = 0.5,
                    y0 = 0.5,
                    x1 = 0.5,
                    y1 = 0.5
                )
            ),
            NfcPositionPreview(
                "bottom-right",
                NfcPositionUseCaseData.NfcPos(
                    x0 = 1.0,
                    y0 = 1.0,
                    x1 = 1.0,
                    y1 = 1.0
                )
            )
        )
}
