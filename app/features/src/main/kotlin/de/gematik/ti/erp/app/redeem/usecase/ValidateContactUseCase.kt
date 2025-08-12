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
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.HintRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.MailRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.PhoneRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.PostalCodeRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationRules.TextRegex
import de.gematik.ti.erp.app.redeem.model.ContactValidationState
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyCity
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyMail
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyPhoneNumber
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyPostalCode
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidCity
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidLine1
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidLine2
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidMail
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidPhoneNumber
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidPostalCode

@Requirement(
    "O.Source_1#10", // replacement for O.Source_1#4
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "analyse the user input of shipping contact data"
)
class ValidateContactUseCase {
    operator fun invoke(
        contact: PharmacyUseCaseData.ShippingContact,
        selectedOrderOption: PharmacyScreenData.OrderOption?
    ): ContactValidationState {
        if (selectedOrderOption == PharmacyScreenData.OrderOption.Pickup && !contact.address().isEmpty()) {
            return ContactValidationState.Valid(selectedOrderOption)
        }

        val errors = buildSet {
            validate(contact.name.isEmpty(), EmptyName)
            validate(contact.name.isNotEmpty() && !contact.name.matches(TextRegex), InvalidName)

            validate(contact.line1.isEmpty(), ContactValidationState.Error.EmptyLine1)
            validate(contact.line1.isNotEmpty() && !contact.line1.matches(TextRegex), InvalidLine1)

            validate(contact.line2.isNotEmpty() && !contact.line2.matches(TextRegex), InvalidLine2)

            validate(contact.postalCode.isEmpty(), EmptyPostalCode)
            validate(contact.postalCode.isNotEmpty() && !contact.postalCode.matches(PostalCodeRegex), InvalidPostalCode)

            validate(contact.city.isEmpty(), EmptyCity)
            validate(contact.city.isNotEmpty() && !contact.city.matches(TextRegex), InvalidCity)

            if (selectedOrderOption != PharmacyScreenData.OrderOption.Pickup) {
                validate(contact.telephoneNumber.isEmpty(), EmptyPhoneNumber)
                validate(contact.telephoneNumber.isNotEmpty() && !contact.telephoneNumber.matches(PhoneRegex), InvalidPhoneNumber)

                validate(contact.mail.isEmpty() && contact.telephoneNumber.isEmpty(), EmptyMail)
                validate(contact.mail.isNotEmpty() && !contact.mail.matches(MailRegex), InvalidMail)
            }

            validate(
                contact.deliveryInformation.isNotEmpty() && !contact.deliveryInformation.matches(HintRegex),
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
