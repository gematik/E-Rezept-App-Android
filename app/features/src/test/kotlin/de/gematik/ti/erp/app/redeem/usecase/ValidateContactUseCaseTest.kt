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

import de.gematik.ti.erp.app.pharmacy.model.OrderOptionErpModel
import de.gematik.ti.erp.app.redeem.model.ContactValidationState
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateContactUseCaseTest {
    private val useCase = ValidateContactUseCase()

    private fun validContact() = ShippingInfoErpModel(
        name = "John Doe",
        firstname = "John",
        lastname = "Doe",
        street = "Main Street 123",
        addressDetail = "2nd Floor",
        zip = "12345",
        city = "Berlin",
        country = "DE",
        phone = "+49301234567",
        mail = "john.doe@example.com",
        deliveryInfo = "Please ring the bell"
    )

    @Test
    fun `valid contact with delivery should return Valid`() {
        val result = useCase(validContact(), OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result)
    }

    @Test
    fun `pickup order skips contact validation if address exists`() {
        val contact = validContact()
        val result = useCase(contact, OrderOptionErpModel.Pickup)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Pickup), result)
    }

    @Test
    fun `invalid contact with multiple errors returns Invalid with proper set`() {
        val contact = ShippingInfoErpModel(
            name = "",
            firstname = "",
            lastname = "",
            street = "",
            addressDetail = "#@!",
            zip = "12",
            city = "",
            country = "",
            phone = "abc",
            mail = "invalid-email",
            deliveryInfo = "!"
        )

        val result = useCase(contact, OrderOptionErpModel.Delivery)

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
    fun `phone without plus prefix returns InvalidPhoneNumber`() {
        val contact = validContact().copy(phone = "0301234567")
        val result = useCase(contact, OrderOptionErpModel.Delivery)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.InvalidPhoneNumber in errors)
    }

    @Test
    fun `valid international phone number with different country calling code returns Valid`() {
        val contact = validContact().copy(phone = "+4312345678")
        val result = useCase(contact, OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result)
    }

    @Test
    fun `phone with only country calling code returns EmptyPhoneNumber`() {
        val contact = validContact().copy(phone = "+49")
        val result = useCase(contact, OrderOptionErpModel.Delivery)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.EmptyPhoneNumber in errors)
    }

    @Test
    fun `invalid country code returns InvalidCountry`() {
        val contact = validContact().copy(country = "GER")
        val result = useCase(contact, OrderOptionErpModel.Delivery)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.InvalidCountry in errors)
    }

    @Test
    fun `separate firstname and lastname validated correctly`() {
        val contact = validContact().copy(name = "", firstname = "Erika", lastname = "")
        val result = useCase(contact, OrderOptionErpModel.Delivery)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.EmptyLastName in errors)
    }

    @Test
    fun `contact with only delivery information invalid returns only delivery info error`() {
        val contact = validContact().copy(deliveryInfo = "!@#")

        val result = useCase(contact, OrderOptionErpModel.Delivery)

        val expectedErrors = setOf(ContactValidationState.Error.InvalidDeliveryInformation)
        assert(result is ContactValidationState.Invalid)
        assertEquals(expectedErrors, (result as ContactValidationState.Invalid).errors)
    }

    @Test
    fun `pickup order requires phone number or mail in commResV3 mode`() {
        val contact = validContact().copy(phone = "", mail = "")
        val result = useCase(contact, OrderOptionErpModel.Pickup, isCommResV3 = true)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.EmptyPhoneNumber in errors)
    }

    @Test
    fun `phone with spaces is valid`() {
        val contact = validContact().copy(phone = "+49 171 1234567")
        val result = useCase(contact, OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result)
    }

    @Test
    fun `street less than 3 chars returns InvalidLine1`() {
        val contact = validContact().copy(street = "AB")
        val result = useCase(contact, OrderOptionErpModel.Delivery)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.InvalidLine1 in errors)
    }

    @Test
    fun `street up to 100 chars is valid`() {
        val contact = validContact().copy(street = "A".repeat(100))
        val result = useCase(contact, OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result)
    }

    @Test
    fun `postal code between 3 and 10 chars is valid`() {
        val contact = validContact().copy(zip = "1234567890")
        val result = useCase(contact, OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result)
    }

    @Test
    fun `pickup order with only telephone number is valid`() {
        val contact = ShippingInfoErpModel(
            name = "",
            street = "",
            zip = "",
            city = "",
            country = "",
            phone = "+491701234567",
            mail = "",
            deliveryInfo = ""
        )
        val result = useCase(contact, OrderOptionErpModel.Pickup)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Pickup), result)
    }

    @Test
    fun `city between 2 and 100 chars is valid`() {
        val contactShort = validContact().copy(city = "A")
        val resultShort = useCase(contactShort, OrderOptionErpModel.Delivery)
        assert(resultShort is ContactValidationState.Invalid)

        val contact100 = validContact().copy(city = "A".repeat(100))
        val result100 = useCase(contact100, OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result100)
    }

    @Test
    fun `delivery info longer than 100 chars returns InvalidDeliveryInformation`() {
        val contact = validContact().copy(deliveryInfo = "A".repeat(101))
        val result = useCase(contact, OrderOptionErpModel.Delivery)

        assert(result is ContactValidationState.Invalid)
        val errors = (result as ContactValidationState.Invalid).errors
        assert(ContactValidationState.Error.InvalidDeliveryInformation in errors)
    }

    @Test
    fun `phone number up to 32 chars is valid`() {
        val contact = validContact().copy(phone = "+4912345678901234567890123456789")
        val result = useCase(contact, OrderOptionErpModel.Delivery)
        assertEquals(ContactValidationState.Valid(OrderOptionErpModel.Delivery), result)
    }

    @Test
    fun `empty phone number or spaces returns EmptyPhoneNumber`() {
        val emptyContact = validContact().copy(phone = "")
        val emptyResult = useCase(emptyContact, OrderOptionErpModel.Delivery)
        assert(emptyResult is ContactValidationState.Invalid)
        assert(ContactValidationState.Error.EmptyPhoneNumber in (emptyResult as ContactValidationState.Invalid).errors)

        val spacesContact = validContact().copy(phone = "   ")
        val spacesResult = useCase(spacesContact, OrderOptionErpModel.Delivery)
        assert(spacesResult is ContactValidationState.Invalid)
        assert(ContactValidationState.Error.EmptyPhoneNumber in (spacesResult as ContactValidationState.Invalid).errors)

        val callingCodeOnlyContact = validContact().copy(phone = "+49 ")
        val callingCodeOnlyResult = useCase(callingCodeOnlyContact, OrderOptionErpModel.Delivery)
        assert(callingCodeOnlyResult is ContactValidationState.Invalid)
        assert(ContactValidationState.Error.EmptyPhoneNumber in (callingCodeOnlyResult as ContactValidationState.Invalid).errors)
    }
}
