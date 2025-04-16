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

import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyCity
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyLine1
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyMail
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyName
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyPhoneNumber
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyPostalCode
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidCity
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidDeliveryInformation
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidLine1
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidLine2
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidMail
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidName
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidPhoneNumber
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidPostalCode
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetShippingContactValidationUseCaseTest {

    private lateinit var getShippingContactValidationUseCase: GetShippingContactValidationUseCase

    private val emptyShippingContact = PharmacyUseCaseData.ShippingContact(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    @Before
    fun setup() {
        getShippingContactValidationUseCase = GetShippingContactValidationUseCase()
    }

    @Test
    fun `validate empty shipping contact on direct redeem should contain OK`() = runTest {
        val validationState = getShippingContactValidationUseCase(
            contact = emptyShippingContact,
            selectedOrderOption = PharmacyScreenData.OrderOption.PickupService
        )
        assertTrue { validationState == ShippingContactState.ValidShippingContactState.OK }
    }

    @Test
    fun `validate empty shipping contact on CourierDelivery and MailDelivery should return several Errors`() = runTest {
        validateEmptyShippingContactWith(PharmacyScreenData.OrderOption.CourierDelivery)
        validateEmptyShippingContactWith(PharmacyScreenData.OrderOption.MailDelivery)
    }

    private fun validateEmptyShippingContactWith(option: PharmacyScreenData.OrderOption) = runTest {
        val validationState = getShippingContactValidationUseCase(
            contact = emptyShippingContact,
            selectedOrderOption = option
        )
        assertTrue {
            validationState.isEmptyName() &&
                validationState.isEmptyLine1() &&
                validationState.isEmptyCity() &&
                validationState.isEmptyPostalCode() &&
                validationState.isEmptyPhoneNumber() &&
                validationState.isEmptyMail()
        }
    }

    @Test
    fun `validate shipping contact with valid mail`() = runTest {
        val validMailAddresses = listOf(
            "user@example.com",
            "john.doe123@gmail.com",
            "alice_smith@company.net",
            "support@website.org",
            "info@my-domain.com",
            "user123@provider.co.uk"
        )

        validMailAddresses.forEach {
            val shippingContact = emptyShippingContact.copy(mail = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )
            assertFalse {
                validationState.isEmptyMail() ||
                    validationState.isInvalidMail()
            }
        }
    }

    @Test
    fun `validate shipping contact with invalid mail`() = runTest {
        val invalidMailAddresses = listOf(
            "user@example@domain.com", // Double "@" symbol
            "user.example.com", // Missing "@" symbol
            "user@exam_ple.com", // Underscore in the domain
            "user@exam ple.com", // Space in the domain
            "user@.com", // Empty domain name after the "@"
            "user@domain" // Missing top-level domain
        )

        invalidMailAddresses.forEach {
            val shippingContact = emptyShippingContact.copy(mail = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertTrue {
                validationState.isInvalidMail()
            }
        }
    }

    @Test
    fun `validate shipping contact with valid texts (Name, Line1, Line2, City)`() = runTest {
        val validtexts = listOf(
            "Lorem ipsum dolor sit amet, consectetur.",
            "1234 This is a valid text with numbers.",
            "Café au lait is a popular beverage.",
            "Special characters: !@_%+'/\"",
            "info@my-domain.com",
            generateRandomString(50),
            "Alice-Müller"
        )

        validtexts.forEach {
            val shippingContact = emptyShippingContact.copy(name = it, line1 = it, line2 = it, city = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )
            assertFalse {
                validationState.isEmptyName() || validationState.isInvalidName() ||
                    validationState.isEmptyLine1() || validationState.isInvalidLine1() ||
                    validationState.isInvalidLine2() ||
                    validationState.isEmptyCity() || validationState.isInvalidCity()
            }
        }
    }

    @Test
    fun `validate shipping contact with invalid texts (Name, Line1, Line2, City)`() = runTest {
        val invalidTexts = listOf(
            "This text contains a special character like: #",
            "This text contains a special character like: <",
            "This text contains a special character like: >",
            "The cost of the item is $50.",
            generateRandomString(51),
            "This text contains a prohibited character like $ within quotes.",
            "Mathematical formula: x² + y² = r²"
        )

        invalidTexts.forEach {
            val shippingContact = emptyShippingContact.copy(name = it, line1 = it, line2 = it, city = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertTrue {
                validationState.isInvalidName() && validationState.isInvalidLine1() &&
                    validationState.isInvalidLine2() && validationState.isInvalidCity()
            }
        }
    }

    @Test
    fun `validate postal code contact with valid text`() = runTest {
        val shippingContact = emptyShippingContact.copy(postalCode = "12345")

        val validationState = getShippingContactValidationUseCase(
            contact = shippingContact,
            selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
        )

        assertFalse {
            validationState.isEmptyPostalCode() || validationState.isInvalidPostalCode()
        }
    }

    @Test
    fun `validate postal code with invalid text`() = runTest {
        val invalidTexts = listOf(
            "0",
            "-1234",
            "123",
            "1234",
            "ABCDE"
        )

        invalidTexts.forEach {
            val shippingContact = emptyShippingContact.copy(postalCode = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertTrue {
                validationState.isInvalidPostalCode()
            }
        }
    }

    @Test
    fun `validate phone number with valid numbers`() = runTest {
        val validNumbers = listOf(
            "1234",
            "0049 12345",
            "+49 123 45 678",
            "+491234 123 45 678",
            "0123 - 1345678",
            "030 623462492"
        )

        validNumbers.forEach {
            val shippingContact = emptyShippingContact.copy(telephoneNumber = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertFalse {
                validationState.isEmptyPhoneNumber() || validationState.isInvalidPhoneNumber()
            }
        }
    }

    @Test
    fun `validate phone number with invalid numbers`() = runTest {
        val invalidNumbers = listOf(
            "0",
            "-1234%",
            "123",
            "1234 1234 - 123 000 / + 000",
            "ABCDE"
        )

        invalidNumbers.forEach {
            val shippingContact = emptyShippingContact.copy(telephoneNumber = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertTrue {
                validationState.isInvalidPhoneNumber()
            }
        }
    }

    @Test
    fun `validate delivery contact with valid texts`() = runTest {
        val validtexts = listOf(
            "Lorem ipsum dolor sit amet, consectetur.",
            "1234 This is a valid text with numbers.",
            "Café au lait is a popular beverage.",
            "Special characters: !@_%+'/\"",
            "info@my-domain.com",
            "It's raining cats and dogs",
            "Alice-Müller",
            generateRandomString(500)
        )

        validtexts.forEach {
            val shippingContact = emptyShippingContact.copy(deliveryInformation = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertFalse {
                validationState.isInvalidDeliveryInformation()
            }
        }
    }

    @Test
    fun `validate delivery contact with invalid texts`() = runTest {
        val invalidTexts = listOf(
            "This text contains a special character like: #",
            "This text contains a special character like: <",
            "This text contains a special character like: >",
            "The cost of the item is $50.",
            generateRandomString(501),
            "Mathematical formula: x² + y² = r²"
        )

        invalidTexts.forEach {
            val shippingContact = emptyShippingContact.copy(deliveryInformation = it)

            val validationState = getShippingContactValidationUseCase(
                contact = shippingContact,
                selectedOrderOption = PharmacyScreenData.OrderOption.MailDelivery
            )

            assertTrue {
                validationState.isInvalidDeliveryInformation()
            }
        }
    }

    private fun generateRandomString(maxLength: Int): String {
        val allowedCharacters = ('a'..'z') + ('A'..'Z') + ('0'..'9') +
            listOf('-', '.', ',', ':', '!', '@', '_', '%', '+', '/', '"', ' ', '\'')

        val stringBuilder = StringBuilder()

        repeat(maxLength) {
            val randomChar = allowedCharacters.random()
            stringBuilder.append(randomChar)
        }

        return stringBuilder.toString()
    }
}
