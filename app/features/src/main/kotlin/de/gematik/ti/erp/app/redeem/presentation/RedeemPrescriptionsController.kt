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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments.DirectRedemptionArguments
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments.LoggedInUserRedemptionArguments
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnDirectUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnLoggedInUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class RedeemPrescriptionsController(
    private val redeemPrescriptionsOnLoggedInUseCase: RedeemPrescriptionsOnLoggedInUseCase,
    private val redeemPrescriptionsOnDirectUseCase: RedeemPrescriptionsOnDirectUseCase
) : Controller() {

    val onProcessStartEvent: ComposableEvent<Unit> = ComposableEvent()
    val onProcessEndEvent: ComposableEvent<Unit> = ComposableEvent()

    private var _redeemedState = MutableStateFlow<RedeemedPrescriptionState>(
        RedeemedPrescriptionState.Init
    )
    val redeemedState = _redeemedState.asStateFlow()

    // process prescription redemptions and update the state
    fun processPrescriptionRedemptions(
        arguments: PrescriptionRedeemArguments
    ) {
        arguments.onRedemptionState(
            directRedemptionBlock = {
                controllerScope.launch {
                    processPrescriptionForDirectRedemption(it)
                }
            },
            loggedInUserRedemptionBlock = {
                controllerScope.launch {
                    processPrescriptionRedemptionsForLoggedInUser(it)
                }
            }
        )
    }

    private suspend fun processPrescriptionRedemptionsForLoggedInUser(
        arguments: LoggedInUserRedemptionArguments
    ) {
        redeemPrescriptionsOnLoggedInUseCase.invoke(
            orderId = arguments.orderId,
            profileId = arguments.profile.id,
            redeemOption = arguments.redeemOption,
            prescriptionOrderInfos = arguments.prescriptionOrderInfos,
            contact = arguments.contact,
            pharmacy = arguments.pharmacy,
            onProcessStart = { onProcessStartEvent.trigger() },
            onProcessEnd = { onProcessEndEvent.trigger() }
        ).collectLatest { value ->
            _redeemedState.value = value
        }
    }

    @Requirement(
        "A_22778-01#1",
        "A_22779-01#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Start Redeem without TI (Controller)."
    )
    private suspend fun processPrescriptionForDirectRedemption(
        arguments: DirectRedemptionArguments
    ) {
        redeemPrescriptionsOnDirectUseCase.invoke(
            orderId = arguments.orderId,
            redeemOption = arguments.redeemOption,
            prescriptionOrderInfos = arguments.prescriptionOrderInfos,
            contact = arguments.contact,
            pharmacy = arguments.pharmacy,
            onProcessStart = { onProcessStartEvent.trigger() },
            onProcessEnd = { onProcessEndEvent.trigger() }
        ).collectLatest {
            _redeemedState.value = it
        }
    }
}

@Composable
fun rememberRedeemPrescriptionsController(): RedeemPrescriptionsController {
    val redeemPrescriptionsOnLoggedInUseCase by rememberInstance<RedeemPrescriptionsOnLoggedInUseCase>()
    val redeemPrescriptionsOnDirectUseCase by rememberInstance<RedeemPrescriptionsOnDirectUseCase>()

    return remember {
        RedeemPrescriptionsController(
            redeemPrescriptionsOnLoggedInUseCase = redeemPrescriptionsOnLoggedInUseCase,
            redeemPrescriptionsOnDirectUseCase = redeemPrescriptionsOnDirectUseCase
        )
    }
}
