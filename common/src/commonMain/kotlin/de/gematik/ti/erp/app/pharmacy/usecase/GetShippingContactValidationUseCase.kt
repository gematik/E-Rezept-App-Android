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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pharmacy.model.OrderOptionErpModel
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel

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
        data object EmptyFirstName : ShippingContactError
        data object InvalidFirstName : ShippingContactError
        data object EmptyLastName : ShippingContactError
        data object InvalidLastName : ShippingContactError
        data object EmptyCountry : ShippingContactError
        data object InvalidCountry : ShippingContactError
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

@Deprecated("Use ValidateContactUseCase")
class GetShippingContactValidationUseCase {
    @Suppress("TooManyFunctions")
    companion object {
        private const val MAX_TEXT_LENGTH = 100
        private const val MAX_NAME_PART_LENGTH = 45
        private const val MAX_HINT_TEXT_LENGTH = 100
        private const val MAX_PHONE_LENGTH = 31
        private const val MIN_PHONE_LENGTH = 1
        private const val MAX_MAIL_LENGTH = 70

        private const val MIN_STREET_LENGTH = 3
        private const val MAX_STREET_LENGTH = 100

        private const val MIN_CITY_LENGTH = 2
        private const val MAX_CITY_LENGTH = 100

        private const val MIN_POSTAL_CODE_LENGTH = 3
        private const val MAX_POSTAL_CODE_LENGTH = 10

        // allows letters from any language, numbers and some restricted symbols
        val textRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{1,$MAX_TEXT_LENGTH}$")
        val streetRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{$MIN_STREET_LENGTH,$MAX_STREET_LENGTH}$")
        val cityRegex = Regex("[\\p{L}0-9\\-.,:!@_%+'/\"\\s]{$MIN_CITY_LENGTH,$MAX_CITY_LENGTH}$")
        private val hintRegex = Regex("[\\p{L}0-9\\-.,:!@_%+?'/\"\\s]{1,$MAX_HINT_TEXT_LENGTH}$")

        val postalCodeRegex = Regex("^[a-zA-Z0-9\\s\\-]{$MIN_POSTAL_CODE_LENGTH,$MAX_POSTAL_CODE_LENGTH}$")
        val phoneNumberRegex = Regex("^\\+?[0-9\\-'/\"\\s]{$MIN_PHONE_LENGTH,$MAX_PHONE_LENGTH}$")
        val countryRegex = Regex("^[A-Z]{2}$")
        val namePartRegex = Regex("^[\\p{L}0-9\\-.,'\\s]{1,$MAX_NAME_PART_LENGTH}$")

        val mailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,}\$"
        )

        fun ShippingContactState.isEmptyPhoneNumber(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.EmptyPhoneNumber)

        fun ShippingContactState.isInvalidPhoneNumber(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidPhoneNumber)

        fun ShippingContactState.isEmptyFirstName(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.EmptyFirstName)

        fun ShippingContactState.isInvalidFirstName(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidFirstName)

        fun ShippingContactState.isEmptyLastName(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.EmptyLastName)

        fun ShippingContactState.isInvalidLastName(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidLastName)

        fun ShippingContactState.isEmptyCountry(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.EmptyCountry)

        fun ShippingContactState.isInvalidCountry(): Boolean =
            this is ShippingContactState.InvalidShippingContactState &&
                this.errorList.contains(ShippingContactState.ShippingContactError.InvalidCountry)

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
            this.isEmptyFirstName() ||
            this.isEmptyLastName() ||
            this.isEmptyCountry() ||
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
        contact: ShippingInfoErpModel,
        selectedOrderOption: OrderOptionErpModel?,
        isCommResV3: Boolean = true
    ): ShippingContactState {
        val errors = mutableListOf<ShippingContactState.ShippingContactError>()
        val isPickup = selectedOrderOption == OrderOptionErpModel.Pickup
        val isPhoneMandatory = isCommResV3 || !isPickup

        if (!isCommResV3 && isPickup && contact.isEmpty()) {
            return ShippingContactState.ValidShippingContactState.OK
        } else {
            if (contact.firstname.isNotEmpty() || contact.lastname.isNotEmpty()) {
                checkFirstName(
                    contact.firstname,
                    onFirstNameIsEmpty = { if (!isPickup) errors.add(it) },
                    onFirstNameIsInvalid = { errors.add(it) }
                )
                checkLastName(
                    contact.lastname,
                    onLastNameIsEmpty = { if (!isPickup) errors.add(it) },
                    onLastNameIsInvalid = { errors.add(it) }
                )
            } else {
                checkContactName(
                    contact.name,
                    onNameIsEmpty = { if (!isPickup) errors.add(it) },
                    onNameIsInvalid = { errors.add(it) }
                )
            }
            if (contact.country.isNotEmpty()) {
                checkCountry(contact.country, onCountryIsEmpty = { errors.add(it) }, onCountryIsInvalid = { errors.add(it) })
            }
            checkContactLine1(
                contact.street,
                onLine1IsEmpty = { if (!isPickup) errors.add(it) },
                onLine1IsInvalid = { errors.add(it) }
            )
            checkContactLine2(contact.addressDetail, onLine2IsInvalid = { errors.add(it) })
            checkContactPostalCode(
                contact.zip,
                onPostalCodeIsEmpty = { if (!isPickup) errors.add(it) },
                onPostalCodeIsInvalid = { errors.add(it) }
            )
            checkContactCity(
                contact.city,
                onCityIsEmpty = { if (!isPickup) errors.add(it) },
                onCityIsInvalid = { errors.add(it) }
            )
            val cleanPhone = contact.phone.filterNot { it.isWhitespace() }
            val isPhoneEmpty = cleanPhone.isBlank() || cleanPhone.matches(Regex("^\\+\\d{1,4}$"))

            checkPhoneNumber(
                contact.phone,
                isMandatory = isPhoneMandatory && (!isPickup || contact.mail.isEmpty()),
                onPhoneNumberIsEmpty = { errors.add(it) },
                onPhoneNumberIsInvalid = { errors.add(it) }
            )

            checkMailAddress(
                contact.mail,
                isPickupServiceSelected = isPickup,
                onMailIsEmpty = {
                    if (isPhoneEmpty && !isPickup) {
                        errors.add(it)
                    }
                },
                onMailIsInvalid = { errors.add(it) }
            )
            checkDeliveryInformation(
                contact.deliveryInfo,
                onDeliveryInformationIsInvalid = { errors.add(it) }
            )
            return if (errors.isEmpty()) {
                ShippingContactState.ValidShippingContactState.OK
            } else {
                ShippingContactState.InvalidShippingContactState(errors.toList())
            }
        }
    }

    private fun checkFirstName(
        firstName: String,
        onFirstNameIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onFirstNameIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = firstName.trim()
        when {
            t.isEmpty() -> onFirstNameIsEmpty(ShippingContactState.ShippingContactError.EmptyFirstName)
            t.length > MAX_NAME_PART_LENGTH || !t.matches(namePartRegex) ->
                onFirstNameIsInvalid(ShippingContactState.ShippingContactError.InvalidFirstName)
        }
    }

    private fun checkLastName(
        lastName: String,
        onLastNameIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onLastNameIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = lastName.trim()
        when {
            t.isEmpty() -> onLastNameIsEmpty(ShippingContactState.ShippingContactError.EmptyLastName)
            t.length > MAX_NAME_PART_LENGTH || !t.matches(namePartRegex) ->
                onLastNameIsInvalid(ShippingContactState.ShippingContactError.InvalidLastName)
        }
    }

    private fun checkCountry(
        country: String,
        onCountryIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onCountryIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = country.trim()
        when {
            t.isEmpty() -> onCountryIsEmpty(ShippingContactState.ShippingContactError.EmptyCountry)
            !t.matches(countryRegex) -> onCountryIsInvalid(ShippingContactState.ShippingContactError.InvalidCountry)
        }
    }

    private fun checkContactName(
        name: String,
        onNameIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onNameIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = name.trim()
        when {
            t.isEmpty() -> onNameIsEmpty(ShippingContactState.ShippingContactError.EmptyName)
            t.length > MAX_TEXT_LENGTH || !t.matches(textRegex) -> onNameIsInvalid(ShippingContactState.ShippingContactError.InvalidName)
        }
    }

    private fun checkContactLine1(
        line1: String,
        onLine1IsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onLine1IsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = line1.trim()
        when {
            t.isEmpty() -> onLine1IsEmpty(ShippingContactState.ShippingContactError.EmptyLine1)
            t.length < MIN_STREET_LENGTH || t.length > MAX_STREET_LENGTH || !t.matches(streetRegex) ->
                onLine1IsInvalid(ShippingContactState.ShippingContactError.InvalidLine1)
        }
    }

    private fun checkContactLine2(
        line2: String,
        onLine2IsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = line2.trim()
        if (t.isNotEmpty() && (t.length > MAX_TEXT_LENGTH || !t.matches(textRegex))) {
            onLine2IsInvalid(ShippingContactState.ShippingContactError.InvalidLine2)
        }
    }

    private fun checkContactPostalCode(
        postalCode: String,
        onPostalCodeIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onPostalCodeIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = postalCode.trim()
        when {
            t.isEmpty() -> onPostalCodeIsEmpty(ShippingContactState.ShippingContactError.EmptyPostalCode)
            t.length < MIN_POSTAL_CODE_LENGTH || t.length > MAX_POSTAL_CODE_LENGTH ||
                !t.matches(postalCodeRegex) -> onPostalCodeIsInvalid(
                ShippingContactState.ShippingContactError.InvalidPostalCode
            )
        }
    }

    private fun checkContactCity(
        city: String,
        onCityIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onCityIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = city.trim()
        when {
            t.isEmpty() -> onCityIsEmpty(ShippingContactState.ShippingContactError.EmptyCity)
            t.length < MIN_CITY_LENGTH || t.length > MAX_CITY_LENGTH || !t.matches(cityRegex) ->
                onCityIsInvalid(ShippingContactState.ShippingContactError.InvalidCity)
        }
    }

    private fun checkPhoneNumber(
        phoneNumber: String,
        isMandatory: Boolean,
        onPhoneNumberIsEmpty: (ShippingContactState.ShippingContactError) -> Unit,
        onPhoneNumberIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val cleanPhone = phoneNumber.filterNot { it.isWhitespace() }
        val isOnlyCallingCode = cleanPhone.isBlank() || cleanPhone.matches(Regex("^\\+\\d{1,4}$"))
        when {
            (phoneNumber.isEmpty() || isOnlyCallingCode) && isMandatory -> onPhoneNumberIsEmpty(
                ShippingContactState.ShippingContactError.EmptyPhoneNumber
            )

            phoneNumber.isNotEmpty() && !isOnlyCallingCode &&
                (cleanPhone.length > MAX_PHONE_LENGTH || !phoneNumber.matches(phoneNumberRegex)) -> onPhoneNumberIsInvalid(
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
        val t = mail.trim()
        when {
            t.isEmpty() && !isPickupServiceSelected -> onMailIsEmpty(
                ShippingContactState.ShippingContactError.EmptyMail
            )

            t.isNotEmpty() && (t.length > MAX_MAIL_LENGTH || !t.matches(mailRegex)) -> onMailIsInvalid(
                ShippingContactState.ShippingContactError.InvalidMail
            )
        }
    }

    private fun checkDeliveryInformation(
        deliveryInformation: String,
        onDeliveryInformationIsInvalid: (ShippingContactState.ShippingContactError) -> Unit
    ) {
        val t = deliveryInformation.trim()
        if (t.isNotEmpty() && t.length > MAX_HINT_TEXT_LENGTH) {
            onDeliveryInformationIsInvalid(ShippingContactState.ShippingContactError.InvalidDeliveryInformation)
        }
    }
}
