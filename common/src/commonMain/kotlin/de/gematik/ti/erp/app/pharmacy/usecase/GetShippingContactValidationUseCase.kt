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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData

sealed interface ShippingContactState {
    sealed interface ValidShippingContactState : ShippingContactState {
        data object OK : ValidShippingContactState
    }

    data class InvalidShippingContactState(
        val errorList: List<ShippingContactError>
    ) : ShippingContactState

    sealed interface ShippingContactError {
        data object EmptyName : ShippingContactError
        data object InvalidName : ShippingContactError
        data object EmptyLine1 : ShippingContactError
        data object InvalidLine1 : ShippingContactError
        data object InvalidLine2 : ShippingContactError
        data object EmptyPostalCode : ShippingContactError
        data object InvalidPostalCode : ShippingContactError
        data object EmptyCity : ShippingContactError
        data object InvalidCity : ShippingContactError
        data object EmptyPhoneNumber : ShippingContactError
        data object InvalidPhoneNumber : ShippingContactError
        data object EmptyMail : ShippingContactError
        data object InvalidMail : ShippingContactError
        data object InvalidDeliveryInformation : ShippingContactError
    }
}
class GetShippingContactValidationUseCase {
    @Suppress("TooManyFunctions")
    companion object {
        private const val MAX_TEXT_LENGTH = 50
        private const val MAX_HINT_TEXT_LENGTH = 500
        private const val MAX_PHONE_LENGTH = 25
        private const val MIN_PHONE_LENGTH = 4

        // allows letters from any language, numbers and some restricted symbols
        val textRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_TEXT_LENGTH}")
        private val hintRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_HINT_TEXT_LENGTH}")

        val postalCodeRegex = Regex("^\\d{5}$")
        val phoneNumberRegex = Regex("[0-9\\-+'/\"\\s]{$MIN_PHONE_LENGTH,$MAX_PHONE_LENGTH}")

        val mailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,}\$"
        )

        fun ShippingContactState.isEmptyPhoneNumber(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.EmptyPhoneNumber)

        fun ShippingContactState.isInvalidPhoneNumber(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidPhoneNumber)

        fun ShippingContactState.isEmptyMail(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.EmptyPhoneNumber) &&
            this.errorList.contains(ShippingContactState.ShippingContactError.EmptyMail)

        fun ShippingContactState.isInvalidMail(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.InvalidMail)

        fun ShippingContactState.isEmptyName(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.EmptyName)

        fun ShippingContactState.isInvalidName(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.InvalidName)

        fun ShippingContactState.isEmptyLine1(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.EmptyLine1)

        fun ShippingContactState.isInvalidLine1(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.InvalidLine1)

        fun ShippingContactState.isInvalidLine2(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.InvalidLine2)

        fun ShippingContactState.isEmptyPostalCode(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.EmptyPostalCode)

        fun ShippingContactState.isInvalidPostalCode(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidPostalCode)

        fun ShippingContactState.isEmptyCity(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.EmptyCity)

        fun ShippingContactState.isInvalidCity(): Boolean = this is ShippingContactState.InvalidShippingContactState &&
            this.errorList.contains(ShippingContactState.ShippingContactError.InvalidCity)

        fun ShippingContactState.isInvalidDeliveryInformation(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidDeliveryInformation)

        fun ShippingContactState.isContactInformationMissing(): Boolean = this.isEmptyPhoneNumber() ||
            this.isEmptyName() ||
            this.isEmptyLine1() ||
            this.isEmptyPostalCode() ||
            this.isEmptyCity()

        fun ShippingContactState.isValid(): Boolean = this == ShippingContactState.ValidShippingContactState.OK
    }

    @Requirement(
        "O.Source_1#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "analyse the user input of shipping contact data"
    )
    operator fun invoke(
        contact: PharmacyUseCaseData.ShippingContact,
        selectedOrderOption: PharmacyScreenData.OrderOption?
    ): ShippingContactState {
        val errors = mutableListOf<ShippingContactState.ShippingContactError>()
        if (selectedOrderOption == PharmacyScreenData.OrderOption.PickupService &&
            contact.isEmpty()
        ) {
            return ShippingContactState.ValidShippingContactState.OK
        } else {
            checkContactName(
                contact.name,
                onNameIsEmpty = { errors.add(it) },
                onNameIsInvalid = { errors.add(it) }
            )
            checkContactLine1(
                contact.line1,
                onLine1IsEmpty = { errors.add(it) },
                onLine1IsInvalid = { errors.add(it) }
            )
            checkContactLine2(contact.line2, onLine2IsInvalid = { errors.add(it) })
            checkContactPostalCode(
                contact.postalCode,
                onPostalCodeIsEmpty = { errors.add(it) },
                onPostalCodeIsInvalid = { errors.add(it) }
            )
            checkContactCity(
                contact.city,
                onCityIsEmpty = { errors.add(it) },
                onCityIsInvalid = { errors.add(it) }
            )
            checkPhoneNumber(
                contact.telephoneNumber,
                selectedOrderOption == PharmacyScreenData.OrderOption.PickupService,
                onPhoneNumberIsEmpty = { errors.add(it) },
                onPhoneNumberIsInvalid = { errors.add(it) }
            )

            checkMailAddress(
                contact.mail,
                selectedOrderOption == PharmacyScreenData.OrderOption.PickupService,
                onMailIsEmpty = {
                    if (contact.telephoneNumber.isEmpty()) {
                        errors.add(it)
                    }
                },
                onMailIsInvalid = { errors.add(it) }
            )
            checkDeliveryInformation(
                contact.deliveryInformation,
                onDeliveryInformationIsInvalid = { errors.add(it) }
            )
            return if (errors.isEmpty()) {
                ShippingContactState.ValidShippingContactState.OK
            } else {
                ShippingContactState.InvalidShippingContactState(errors.toList())
            }
        }
    }

    private fun checkContactName(
        name: String,
        onNameIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onNameIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        when {
            name.isEmpty() -> onNameIsEmpty(ShippingContactState.ShippingContactError.EmptyName)
            !name.matches(textRegex) -> onNameIsInvalid(ShippingContactState.ShippingContactError.InvalidName)
        }
    }

    private fun checkContactLine1(
        line1: String,
        onLine1IsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onLine1IsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        when {
            line1.isEmpty() -> onLine1IsEmpty(ShippingContactState.ShippingContactError.EmptyLine1)
            !line1.matches(textRegex) -> onLine1IsInvalid(ShippingContactState.ShippingContactError.InvalidLine1)
        }
    }

    private fun checkContactLine2(
        line2: String,
        onLine2IsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        if (line2.isNotEmpty() && !line2.matches(textRegex)) {
            onLine2IsInvalid(ShippingContactState.ShippingContactError.InvalidLine2)
        }
    }

    private fun checkContactPostalCode(
        postalCode: String,
        onPostalCodeIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onPostalCodeIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        when {
            postalCode.isEmpty() -> onPostalCodeIsEmpty(ShippingContactState.ShippingContactError.EmptyPostalCode)
            !postalCode.matches(postalCodeRegex) -> onPostalCodeIsInvalid(
                ShippingContactState.ShippingContactError.InvalidPostalCode
            )
        }
    }

    private fun checkContactCity(
        city: String,
        onCityIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onCityIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        when {
            city.isEmpty() -> onCityIsEmpty(ShippingContactState.ShippingContactError.EmptyCity)
            !city.matches(textRegex) -> onCityIsInvalid(ShippingContactState.ShippingContactError.InvalidCity)
        }
    }

    private fun checkPhoneNumber(
        phoneNumber: String,
        isPickupServiceSelected: Boolean,
        onPhoneNumberIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onPhoneNumberIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        when {
            phoneNumber.isEmpty() && !isPickupServiceSelected -> onPhoneNumberIsEmpty(
                ShippingContactState.ShippingContactError.EmptyPhoneNumber
            )
            phoneNumber.isNotEmpty() &&
                !phoneNumber.matches(phoneNumberRegex) -> onPhoneNumberIsInvalid(
                ShippingContactState.ShippingContactError.InvalidPhoneNumber
            )
        }
    }

    private fun checkMailAddress(
        mail: String,
        isPickupServiceSelected: Boolean,
        onMailIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onMailIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        when {
            mail.isEmpty() && !isPickupServiceSelected -> onMailIsEmpty(
                ShippingContactState.ShippingContactError.EmptyMail
            )
            mail.isNotEmpty() && !mail.matches(mailRegex) -> onMailIsInvalid(
                ShippingContactState.ShippingContactError.InvalidMail
            )
        }
    }

    private fun checkDeliveryInformation(
        deliveryInformation: String,
        onDeliveryInformationIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        if (deliveryInformation.isNotEmpty() && !deliveryInformation.matches(hintRegex)) {
            onDeliveryInformationIsInvalid(ShippingContactState.ShippingContactError.InvalidDeliveryInformation)
        }
    }
}
