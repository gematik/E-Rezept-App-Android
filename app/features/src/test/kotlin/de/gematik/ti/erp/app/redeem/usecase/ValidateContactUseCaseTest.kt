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

import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.model.ContactValidationState
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateContactUseCaseTest {
    private val useCase = ValidateContactUseCase()

    private fun validContact() = PharmacyUseCaseData.ShippingContact(
        name = "John Doe",
        line1 = "Main Street 123",
        line2 = "2nd Floor",
        postalCode = "12345",
        city = "Berlin",
        telephoneNumber = "0301234567",
        mail = "john.doe@example.com",
        deliveryInformation = "Please ring the bell"
    )

    @Test
    fun `valid contact with delivery should return Valid`() {
        val result = useCase(validContact(), PharmacyScreenData.OrderOption.Delivery)
        assertEquals(ContactValidationState.Valid(PharmacyScreenData.OrderOption.Delivery), result)
    }

    @Test
    fun `pickup order skips contact validation if address exists`() {
        val contact = validContact()
        val result = useCase(contact, PharmacyScreenData.OrderOption.Pickup)
        assertEquals(ContactValidationState.Valid(PharmacyScreenData.OrderOption.Pickup), result)
    }

    @Test
    fun `invalid contact with multiple errors returns Invalid with proper set`() {
        val contact = PharmacyUseCaseData.ShippingContact(
            name = "",
            line1 = "",
            line2 = "#@!",
            postalCode = "12",
            city = "",
            telephoneNumber = "abc",
            mail = "invalid-email",
            deliveryInformation = "!"
        )

        val result = useCase(contact, PharmacyScreenData.OrderOption.Delivery)

        val expectedErrors = setOf(
            ContactValidationState.Error.EmptyName,
            ContactValidationState.Error.EmptyLine1,
            ContactValidationState.Error.InvalidLine2,
            ContactValidationState.Error.InvalidPostalCode,
            ContactValidationState.Error.EmptyCity,
            ContactValidationState.Error.InvalidPhoneNumber,
            ContactValidationState.Error.InvalidMail
        )

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assertEquals(expectedErrors, errors)
    }

    @Test
    fun `contact with only delivery information invalid returns only delivery info error`() {
        val contact = validContact().copy(deliveryInformation = "!@#")

        val result = useCase(contact, PharmacyScreenData.OrderOption.Delivery)

        val expectedErrors = setOf(ContactValidationState.Error.InvalidDeliveryInformation)
        assert(result is ContactValidationState.Invalid)
        assertEquals(expectedErrors, (result as ContactValidationState.Invalid).errors)
    }
}
