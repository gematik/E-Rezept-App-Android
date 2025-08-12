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

package de.gematik.ti.erp.app.redeem.model

import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData.OrderOption

sealed class ContactValidationState(open val selectedOrderOption: OrderOption?) {
    data class Valid(override val selectedOrderOption: OrderOption?) : ContactValidationState(selectedOrderOption)
    data class Invalid(override val selectedOrderOption: OrderOption?, val errors: Set<Error>) : ContactValidationState(selectedOrderOption)

    data class NoOrderOption(override val selectedOrderOption: OrderOption?) : ContactValidationState(selectedOrderOption)

    enum class Error {
        EmptyName, InvalidName,
        EmptyLine1, InvalidLine1,
        InvalidLine2,
        EmptyPostalCode, InvalidPostalCode,
        EmptyCity, InvalidCity,
        EmptyPhoneNumber, InvalidPhoneNumber,
        EmptyMail, InvalidMail,
        InvalidDeliveryInformation
    }

    fun hasError(error: Error): Boolean =
        this is Invalid && error in this.errors

    fun isValid(): Boolean = this is Valid

    fun isPersonalInformationMissing(): Boolean = this is Invalid && errors.any {
        it in setOf(
            Error.EmptyName,
            Error.EmptyLine1,
            Error.EmptyPostalCode,
            Error.EmptyCity
        )
    }

    fun isContactPhoneInformationMissing(): Boolean = this is Invalid && errors.any {
        it in setOf(
            Error.EmptyPhoneNumber,
            Error.InvalidPhoneNumber
        )
    }

    fun isContactMailInformationMissing(): Boolean = this is Invalid && errors.any {
        it in setOf(
            Error.EmptyMail,
            Error.InvalidMail
        )
    }

    fun isDeliveryInformationMissing(): Boolean = this is Invalid && errors.any {
        it in setOf(Error.InvalidDeliveryInformation)
    }

    companion object {
        // a subset of the [ContactValidationState] to show on the redeem screen
        fun ContactValidationState.redeemValidationState(): RedeemContactValidationState {
            val option = selectedOrderOption ?: return RedeemContactValidationState.MissingOrderOption

            if (this is Valid) {
                return RedeemContactValidationState.NoError
            }

            return when (option) {
                OrderOption.Pickup -> RedeemContactValidationState.NoError

                OrderOption.Delivery, OrderOption.Online -> when {
                    isPersonalInformationMissing() -> RedeemContactValidationState.MissingPersonalInfo
                    isContactPhoneInformationMissing() -> RedeemContactValidationState.MissingPhone
                    isDeliveryInformationMissing() -> RedeemContactValidationState.MissingDeliveryInfo
                    else -> RedeemContactValidationState.NoError
                }
            }
        }
    }
}
