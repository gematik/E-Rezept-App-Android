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

package de.gematik.ti.erp.app.redeem.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.utils.uistate.UiState

data class LocalRedeemPreview(
    val name: String,
    val dmCodes: UiState<List<DMCode>>,
    val showSingleCodes: Boolean
)

const val PAYLOAD = "payload"
const val MEDICATION_NAME_1 = "Medication 1"
const val MEDICATION_NAME_2 = "Medication 2"

class LocalRedeemPreviewParameter : PreviewParameterProvider<LocalRedeemPreview> {

    override val values: Sequence<LocalRedeemPreview>
        get() = sequenceOf(
            LocalRedeemPreview(
                name = "EmptyState",
                dmCodes = UiState.Empty(),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "ErrorState",
                dmCodes = UiState.Error(Throwable("Error")),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "LoadingState",
                dmCodes = UiState.Loading(),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "SingleDataMatrixCodeWithSelfPayerWarning",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 1,
                            name = MEDICATION_NAME_1,
                            selfPayerPrescriptionNames = listOf(MEDICATION_NAME_1),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "MultipleDataMatrixCodeWithSelfPayerWarning_Single_Codes",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 2,
                            name = "$MEDICATION_NAME_1, $MEDICATION_NAME_2",
                            selfPayerPrescriptionNames = listOf(MEDICATION_NAME_1),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = true
            ),
            LocalRedeemPreview(
                name = "MultipleDataMatrixCodeWithOneSelfPayerWarning",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 2,
                            name = "$MEDICATION_NAME_1, $MEDICATION_NAME_2",
                            selfPayerPrescriptionNames = listOf(MEDICATION_NAME_1),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "MultipleDataMatrixCodeWithTwoSelfPayerWarning",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 2,
                            name = "$MEDICATION_NAME_1, $MEDICATION_NAME_2",
                            selfPayerPrescriptionNames = listOf(MEDICATION_NAME_1, MEDICATION_NAME_2),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "SingleDataMatrixCode",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 1,
                            name = MEDICATION_NAME_1,
                            selfPayerPrescriptionNames = listOf(),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "MultipleDataMatrixCode_Single_Codes_false",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 2,
                            name = "$MEDICATION_NAME_1, $MEDICATION_NAME_2",
                            selfPayerPrescriptionNames = listOf(),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = false
            ),
            LocalRedeemPreview(
                name = "MultipleDataMatrixCode_Single_Codes",
                dmCodes = UiState.Data(
                    listOf(
                        DMCode(
                            payload = PAYLOAD,
                            nrOfCodes = 2,
                            name = "$MEDICATION_NAME_1, $MEDICATION_NAME_2",
                            selfPayerPrescriptionNames = listOf(),
                            containsScanned = false
                        )
                    )
                ),
                showSingleCodes = true
            )
        )
}
