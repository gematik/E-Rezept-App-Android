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

import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import java.util.UUID
import kotlin.contracts.ExperimentalContracts

internal fun orderID(): UUID = UUID.randomUUID()

sealed class PrescriptionRedeemArguments(
    open val profile: ProfilesUseCaseData.Profile? = null,
    open val orderId: UUID,
    open val prescriptionOrderInfos: List<PharmacyUseCaseData.PrescriptionInOrder>,
    open val redeemOption: PharmacyScreenData.OrderOption,
    open val pharmacy: PharmacyUseCaseData.Pharmacy,
    open val contact: PharmacyUseCaseData.ShippingContact
) {
    @OptIn(ExperimentalContracts::class)
    fun onRedemptionState(
        loggedInUserRedemptionBlock: (LoggedInUserRedemptionArguments) -> Unit
    ) {
        loggedInUserRedemptionBlock(this as LoggedInUserRedemptionArguments)
    }

    // arguments required to redeem a prescription for a logged in user
    data class LoggedInUserRedemptionArguments(
        override val profile: ProfilesUseCaseData.Profile,
        override val orderId: UUID,
        override val prescriptionOrderInfos: List<PharmacyUseCaseData.PrescriptionInOrder>,
        override val redeemOption: PharmacyScreenData.OrderOption,
        override val pharmacy: PharmacyUseCaseData.Pharmacy,
        override val contact: PharmacyUseCaseData.ShippingContact
    ) : PrescriptionRedeemArguments(profile, orderId, prescriptionOrderInfos, redeemOption, pharmacy, contact)

    companion object {
        fun UUID.from(
            profile: ProfilesUseCaseData.Profile,
            order: PharmacyUseCaseData.OrderState,
            redeemOption: PharmacyScreenData.OrderOption,
            pharmacy: PharmacyUseCaseData.Pharmacy
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
