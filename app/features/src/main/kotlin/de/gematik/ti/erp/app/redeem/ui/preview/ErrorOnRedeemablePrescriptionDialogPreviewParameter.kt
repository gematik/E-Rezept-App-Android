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
import de.gematik.ti.erp.app.redeem.model.ErrorOnRedeemablePrescriptionDialogParameters
import de.gematik.ti.erp.app.redeem.model.PrescriptionErrorState
import de.gematik.ti.erp.app.redeem.model.PrescriptionReadinessState
import de.gematik.ti.erp.app.redeem.model.RedeemablePrescriptionInfo

data class PreviewErrorOnRedeemablePrescriptionDialogParameter(
    val name: String,
    val params: ErrorOnRedeemablePrescriptionDialogParameters
)

class ErrorOnRedeemablePrescriptionDialogPreviewParameter : PreviewParameterProvider<PreviewErrorOnRedeemablePrescriptionDialogParameter> {
    override val values: Sequence<PreviewErrorOnRedeemablePrescriptionDialogParameter>
        get() = sequenceOf(
            PreviewErrorOnRedeemablePrescriptionDialogParameter(
                name = "Deleted",
                params = ErrorOnRedeemablePrescriptionDialogParameters(
                    missingPrescriptionInfos = listOf(
                        RedeemablePrescriptionInfo(
                            taskId = "001",
                            name = "Ibuprofen 600 mg Filmtabletten N3",
                            state = PrescriptionReadinessState.Deleted
                        )
                    ),
                    state = PrescriptionErrorState.Deleted,
                    isInvalidOrder = false
                )
            ),
            PreviewErrorOnRedeemablePrescriptionDialogParameter(
                name = "NotRedeemable",
                params = ErrorOnRedeemablePrescriptionDialogParameters(
                    missingPrescriptionInfos = listOf(
                        RedeemablePrescriptionInfo(
                            taskId = "002",
                            name = "Paracetamol 500 mg Tabletten",
                            state = PrescriptionReadinessState.NotRedeemable
                        )
                    ),
                    state = PrescriptionErrorState.NotRedeemable,
                    isInvalidOrder = true
                )
            ),
            PreviewErrorOnRedeemablePrescriptionDialogParameter(
                name = "Generic",
                params = ErrorOnRedeemablePrescriptionDialogParameters(
                    missingPrescriptionInfos = listOf(
                        RedeemablePrescriptionInfo(
                            taskId = "003",
                            name = "Metformin 850 mg Tabletten N3",
                            state = PrescriptionReadinessState.NotReady
                        )
                    ),
                    state = PrescriptionErrorState.Generic,
                    isInvalidOrder = false
                )
            ),
            PreviewErrorOnRedeemablePrescriptionDialogParameter(
                name = "MoreThanOnePrescriptionHasIssues",
                params = ErrorOnRedeemablePrescriptionDialogParameters(
                    missingPrescriptionInfos = listOf(
                        RedeemablePrescriptionInfo(
                            taskId = "004",
                            name = "Amoxicillin 1000 mg Filmtabletten",
                            state = PrescriptionReadinessState.NotReady
                        ),
                        RedeemablePrescriptionInfo(
                            taskId = "005",
                            name = "Pantoprazol 40 mg magensaftresistente Tabletten",
                            state = PrescriptionReadinessState.Deleted
                        ),
                        RedeemablePrescriptionInfo(
                            taskId = "006",
                            name = "Eliquis 5 mg Filmtabletten",
                            state = PrescriptionReadinessState.NotRedeemable
                        )
                    ),
                    state = PrescriptionErrorState.MoreThanOnePrescriptionHasIssues,
                    isInvalidOrder = true
                )
            )
        )
}
