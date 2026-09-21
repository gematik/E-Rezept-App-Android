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

package de.gematik.ti.erp.app.pharmacy.model

import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import java.util.UUID
import kotlin.contracts.ExperimentalContracts

internal fun orderID(): UUID = UUID.randomUUID()

sealed class PrescriptionRedeemArguments(
    open val profile: ProfileErpModel? = null,
    open val orderId: UUID,
    open val prescriptionOrderInfos: List<PrescriptionInOrderErpModel>,
    open val redeemOption: OrderOptionErpModel,
    open val pharmacy: PharmacyDetailsErpModel,
    open val contact: ShippingInfoErpModel
) {
    @OptIn(ExperimentalContracts::class)
    fun onRedemptionState(
        loggedInUserRedemptionBlock: (LoggedInUserRedemptionArguments) -> Unit
    ) {
        loggedInUserRedemptionBlock(this as LoggedInUserRedemptionArguments)
    }

    // arguments required to redeem a prescription for a logged in user
    data class LoggedInUserRedemptionArguments(
        override val profile: ProfileErpModel,
        override val orderId: UUID,
        override val prescriptionOrderInfos: List<PrescriptionInOrderErpModel>,
        override val redeemOption: OrderOptionErpModel,
        override val pharmacy: PharmacyDetailsErpModel,
        override val contact: ShippingInfoErpModel
    ) : PrescriptionRedeemArguments(profile, orderId, prescriptionOrderInfos, redeemOption, pharmacy, contact)

    companion object {
        fun UUID.from(
            profile: ProfileErpModel,
            order: OrderStateErpModel,
            redeemOption: OrderOptionErpModel,
            pharmacy: PharmacyDetailsErpModel
        ): PrescriptionRedeemArguments =
            LoggedInUserRedemptionArguments(
                profile = profile,
                orderId = this,
                prescriptionOrderInfos = order.prescriptionsInOrder,
                redeemOption = redeemOption,
                pharmacy = pharmacy,
                contact = order.contact
            )
    }
}
