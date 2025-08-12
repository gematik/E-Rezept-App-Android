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

package de.gematik.ti.erp.app.redeem.ui.components

import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.ErrorOnRedeemablePrescriptionDialogParameters
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionDialogMessageState
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionDialogMessageState.Companion.toDialogMessageState
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState.IncompleteOrder
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState.InvalidOrder
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState.OrderCompleted
import io.github.aakira.napier.Napier

object RedeemStateHandler {
    fun BaseRedeemState.handleRedeemedState(
        onShowPrescriptionRedeemAlertDialog: (RedeemPrescriptionDialogMessageState) -> Unit,
        onOrderHasError: (Boolean) -> Unit,
        onIncompleteOrder: (ErrorOnRedeemablePrescriptionDialogParameters) -> Unit,
        onInvalidOrder: (ErrorOnRedeemablePrescriptionDialogParameters) -> Unit
    ) {
        val redeemedState = this
        runCatching {
            when (redeemedState) {
                is IncompleteOrder ->
                    onIncompleteOrder(
                        ErrorOnRedeemablePrescriptionDialogParameters(
                            missingPrescriptionInfos = redeemedState.missingPrescriptionInfos,
                            state = redeemedState.state,
                            isInvalidOrder = false
                        )
                    )

                is InvalidOrder -> onInvalidOrder(
                    ErrorOnRedeemablePrescriptionDialogParameters(
                        missingPrescriptionInfos = redeemedState.missingPrescriptionInfos,
                        state = redeemedState.state,
                        isInvalidOrder = true
                    )
                )

                is OrderCompleted -> {
                    val hasError = redeemedState.results.values.containsError()
                    onOrderHasError(hasError)
                    val dialogMessageState = obtainDialogParameters(redeemedState.results.values)
                    onShowPrescriptionRedeemAlertDialog(dialogMessageState)
                }

                else -> Napier.i { "No action required for state: $redeemedState" }
            }
        }.onFailure { error ->
            Napier.e(error) { "Error while processing redeemed state" }
            onShowPrescriptionRedeemAlertDialog(RedeemPrescriptionDialogMessageState.UNKNOWN)
        }
    }

    private fun obtainDialogParameters(
        results: Collection<BaseRedeemState?>
    ): RedeemPrescriptionDialogMessageState =
        when {
            // case 1: When one prescription is transferred.
            results.size == 1 -> results.firstNotNullOf { it?.toDialogMessageState() }

            // case 2.1: When multiple prescriptions are transferred Successfully.
            results.containsNoError() -> RedeemPrescriptionDialogMessageState.SUCCESS

            // case 2.2: When any multiple prescription are transferred Unsuccessfully. Show a generic error message.
            else -> RedeemPrescriptionDialogMessageState.MULTIPLE_PRESCRIPTIONS_FAILED
        }

    private fun Collection<BaseRedeemState?>.containsError(): Boolean =
        any { it is BaseRedeemState.Error }

    private fun Collection<BaseRedeemState?>.containsNoError(): Boolean =
        any { it !is BaseRedeemState.Error }
}

fun (PharmacyScreenData.OrderOption?).selectVideoSource() = when (this) {
    PharmacyScreenData.OrderOption.Pickup -> R.raw.animation_local
    PharmacyScreenData.OrderOption.Delivery -> R.raw.animation_courier
    PharmacyScreenData.OrderOption.Online -> R.raw.animation_mail
    else -> {
        // show default animation until the order option is not null
        R.raw.animation_local
    }
}
