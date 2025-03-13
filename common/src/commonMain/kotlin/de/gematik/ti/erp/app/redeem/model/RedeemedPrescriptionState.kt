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

package de.gematik.ti.erp.app.redeem.model

import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.model.RedeemablePrescriptionInfo.Companion.getPrescriptionErrorStateForDialog
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
sealed class RedeemedPrescriptionState {

    data object Init : RedeemedPrescriptionState() // no orders have been processed

    data class OrderCompleted(
        val orderId: String,
        val results: Map<PharmacyUseCaseData.PrescriptionInOrder, RedeemedPrescriptionState>
    ) : RedeemedPrescriptionState() // everything is processed and this contains the internal states of every prescription once they are processed

    data object Success : RedeemedPrescriptionState()

    data class IncompleteOrder(
        val missingPrescriptionInfos: List<RedeemablePrescriptionInfo>
    ) : RedeemedPrescriptionState() {
        val state: PrescriptionErrorState = missingPrescriptionInfos.getPrescriptionErrorStateForDialog()
    }

    data class InvalidOrder(
        val missingPrescriptionInfos: List<RedeemablePrescriptionInfo>
    ) : RedeemedPrescriptionState() {
        val state: PrescriptionErrorState = missingPrescriptionInfos.getPrescriptionErrorStateForDialog()
    }

    data class Error(val errorState: HttpErrorState) : RedeemedPrescriptionState()
}

sealed interface PrescriptionErrorState {
    data object Deleted : PrescriptionErrorState
    data object Generic : PrescriptionErrorState
    data object NotRedeemable : PrescriptionErrorState
    data object MoreThanOnePrescriptionHasIssues : PrescriptionErrorState
}
