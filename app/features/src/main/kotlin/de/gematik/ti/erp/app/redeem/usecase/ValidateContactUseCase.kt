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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pharmacy.model.OrderOptionErpModel
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.CityRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.CountryRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.HintRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.MAX_MAIL_LENGTH
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.MailRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.NamePartRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.PostalCodeRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.StreetRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.TextRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.isValidE164Phone
import de.gematik.ti.erp.app.redeem.model.ContactValidationState
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyCity
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyFirstName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyLastName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyMail
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyPhoneNumber
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyPostalCode
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidCity
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidCountry
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidFirstName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidLastName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidLine1
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidLine2
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidMail
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidPhoneNumber
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidPostalCode
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel

@Requirement(
    "O.Source_1#10", // replacement for O.Source_1#4
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "analyse the user input of shipping contact data"
)
class ValidateContactUseCase {
    operator fun invoke(
        contact: ShippingInfoErpModel,
        selectedOrderOption: OrderOptionErpModel?,
        isCommResV3: Boolean = true
    ): ContactValidationState {
        val isPickup = selectedOrderOption == OrderOptionErpModel.Pickup
        val isPhoneMandatory = isCommResV3 || !isPickup

        if (!isCommResV3 && isPickup && !contact.address().isEmpty()) {
            return ContactValidationState.Valid(selectedOrderOption)
        }

        val errors = buildSet {
            val trimmedFirstname = contact.firstname.trim()
            val trimmedLastname = contact.lastname.trim()
            val trimmedName = contact.name.trim()
            val trimmedCountry = contact.country.trim()
            val trimmedStreet = contact.street.trim()
            val trimmedAddressDetail = contact.addressDetail.trim()
            val trimmedZip = contact.zip.trim()
            val trimmedCity = contact.city.trim()
            val trimmedMail = contact.mail.trim()
            val trimmedDeliveryInfo = contact.deliveryInfo.trim()

            if (trimmedFirstname.isNotEmpty() || trimmedLastname.isNotEmpty()) {
                if (!isPickup) {
                    validate(trimmedFirstname.isEmpty(), EmptyFirstName)
                    validate(trimmedLastname.isEmpty(), EmptyLastName)
                }
                validate(trimmedFirstname.isNotEmpty() && !trimmedFirstname.matches(NamePartRegex), InvalidFirstName)
                validate(trimmedLastname.isNotEmpty() && !trimmedLastname.matches(NamePartRegex), InvalidLastName)
            } else {
                if (!isPickup) {
                    validate(trimmedName.isEmpty(), EmptyName)
                }
                validate(trimmedName.isNotEmpty() && !trimmedName.matches(TextRegex), InvalidName)
            }

            if (trimmedCountry.isNotEmpty()) {
                validate(!trimmedCountry.matches(CountryRegex), InvalidCountry)
            }

            validate(trimmedStreet.isEmpty() && !isPickup, ContactValidationState.Error.EmptyLine1)
            validate(trimmedStreet.isNotEmpty() && !trimmedStreet.matches(StreetRegex), InvalidLine1)

            validate(trimmedAddressDetail.isNotEmpty() && !trimmedAddressDetail.matches(TextRegex), InvalidLine2)

            validate(trimmedZip.isEmpty() && !isPickup, EmptyPostalCode)
            validate(trimmedZip.isNotEmpty() && !trimmedZip.matches(PostalCodeRegex), InvalidPostalCode)

            validate(trimmedCity.isEmpty() && !isPickup, EmptyCity)
            validate(trimmedCity.isNotEmpty() && !trimmedCity.matches(CityRegex), InvalidCity)

            val cleanPhone = contact.phone.filterNot { it.isWhitespace() }
            val isPhoneEmpty = cleanPhone.isBlank() || cleanPhone.matches(Regex("^\\+\\d{1,4}$"))

            if (isPhoneMandatory) {
                if (isPickup) {
                    val isMailEmpty = trimmedMail.isEmpty()
                    validate(isPhoneEmpty && isMailEmpty, EmptyPhoneNumber)
                    validate(!isPhoneEmpty && !isValidE164Phone(contact.phone), InvalidPhoneNumber)
                    validate(!isMailEmpty && (trimmedMail.length > MAX_MAIL_LENGTH || !trimmedMail.matches(MailRegex)), InvalidMail)
                } else {
                    validate(isPhoneEmpty, EmptyPhoneNumber)
                    validate(!isPhoneEmpty && !isValidE164Phone(contact.phone), InvalidPhoneNumber)

                    validate(trimmedMail.isEmpty() && isPhoneEmpty, EmptyMail)
                    validate(trimmedMail.isNotEmpty() && (trimmedMail.length > MAX_MAIL_LENGTH || !trimmedMail.matches(MailRegex)), InvalidMail)
                }
            } else {
                validate(!isPhoneEmpty && !isValidE164Phone(contact.phone), InvalidPhoneNumber)
                validate(trimmedMail.isNotEmpty() && (trimmedMail.length > MAX_MAIL_LENGTH || !trimmedMail.matches(MailRegex)), InvalidMail)
            }

            validate(
                trimmedDeliveryInfo.isNotEmpty() && !trimmedDeliveryInfo.matches(HintRegex),
                ContactValidationState.Error.InvalidDeliveryInformation
            )
        }

        return if (errors.isEmpty()) {
            ContactValidationState.Valid(selectedOrderOption)
        } else {
            ContactValidationState.Invalid(selectedOrderOption, errors)
        }
    }

    private fun MutableSet<ContactValidationState.Error>.validate(
        condition: Boolean,
        error: ContactValidationState.Error
    ) {
        if (condition) add(error)
    }
}
