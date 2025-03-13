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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.redeem.model.ErrorOnRedeemablePrescriptionDialogParameters
import de.gematik.ti.erp.app.redeem.model.PrescriptionErrorState
import de.gematik.ti.erp.app.redeem.model.RedeemablePrescriptionInfo
import de.gematik.ti.erp.app.redeem.ui.preview.ErrorOnRedeemablePrescriptionDialogPreviewParameter
import de.gematik.ti.erp.app.redeem.ui.preview.PreviewErrorOnRedeemablePrescriptionDialogParameter
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun ErrorOnRedeemablePrescriptionDialog(
    event: ComposableEvent<ErrorOnRedeemablePrescriptionDialogParameters>,
    dialog: DialogScaffold,
    onClickForInvalidOrder: () -> Unit,
    onClickForIncompleteOrder: (List<String>) -> Unit,
    onClickToCancel: () -> Unit
) {
    event.listen { dialogParams ->
        val taskIds = dialogParams.missingPrescriptionInfos.map { it.taskId }

        dialog.show {
            ErrorOnRedeemablePrescriptionDialog(
                missingPrescriptionInfos = dialogParams.missingPrescriptionInfos,
                isInvalidOrder = dialogParams.isInvalidOrder,
                state = dialogParams.state,
                onClickForInvalidOrder = {
                    onClickForInvalidOrder()
                    it.dismiss()
                },
                onClickForIncompleteOrder = {
                    onClickForIncompleteOrder(taskIds)
                    it.dismiss()
                },
                onClickToCancel = {
                    onClickToCancel()
                    it.dismiss()
                }
            )
        }
    }
}

@Composable
private fun ErrorOnRedeemablePrescriptionDialog(
    missingPrescriptionInfos: List<RedeemablePrescriptionInfo> = emptyList(),
    isInvalidOrder: Boolean,
    state: PrescriptionErrorState,
    onClickForInvalidOrder: () -> Unit,
    onClickForIncompleteOrder: () -> Unit,
    onClickToCancel: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.error_redeemable_prescription_title),
        body = bodyText(
            missingPrescriptionInfos = missingPrescriptionInfos,
            state = state
        ),
        onDismissRequest = onClickToCancel,
        buttons = {
            TextButton(
                onClick = {
                    if (isInvalidOrder) {
                        onClickForInvalidOrder()
                        onClickToCancel()
                    } else {
                        onClickForIncompleteOrder()
                        onClickToCancel()
                    }
                }
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = buttonText(isInvalidOrder)
                )
            }
            TextButton(onClick = onClickToCancel) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.cancel)
                )
            }
        }
    )
}

@Composable
private fun bodyText(
    missingPrescriptionInfos: List<RedeemablePrescriptionInfo> = emptyList(),
    state: PrescriptionErrorState
) = when (state) {
    PrescriptionErrorState.Deleted -> stringResource(
        R.string.error_redeemable_prescription_deleted,
        missingPrescriptionInfos.first().name ?: ""
    )

    PrescriptionErrorState.Generic -> stringResource(
        R.string.error_redeemable_prescription_generic_error,
        missingPrescriptionInfos.firstOrNull()?.name ?: ""
    )

    PrescriptionErrorState.MoreThanOnePrescriptionHasIssues -> {
        val prescriptionNames = missingPrescriptionInfos.map { it.name }.filter { it?.isNotEmpty() == true }
        val jointNames = prescriptionNames.joinToString(separator = "\n• ", prefix = "\n• ")
        stringResource(
            R.string.error_redeemable_prescription_more_than_one_error,
            jointNames
        )
    }

    PrescriptionErrorState.NotRedeemable -> stringResource(
        R.string.error_redeemable_prescription_not_redeemable,
        missingPrescriptionInfos.first().name ?: ""
    )
}

@Composable
private fun buttonText(isInvalidOrder: Boolean) =
    if (isInvalidOrder) {
        stringResource(R.string.error_redeemable_prescription_cancel_order)
    } else {
        stringResource(R.string.error_redeemable_prescription_continue_without)
    }

@LightDarkPreview
@Composable
fun ErrorOnRedeemablePrescriptionDialogPreview(
    @PreviewParameter(ErrorOnRedeemablePrescriptionDialogPreviewParameter::class) previewData: PreviewErrorOnRedeemablePrescriptionDialogParameter
) {
    PreviewAppTheme {
        ErrorOnRedeemablePrescriptionDialog(
            missingPrescriptionInfos = previewData.params.missingPrescriptionInfos,
            state = previewData.params.state,
            isInvalidOrder = previewData.params.isInvalidOrder,
            onClickForInvalidOrder = {},
            onClickForIncompleteOrder = {},
            onClickToCancel = {}
        )
    }
}
