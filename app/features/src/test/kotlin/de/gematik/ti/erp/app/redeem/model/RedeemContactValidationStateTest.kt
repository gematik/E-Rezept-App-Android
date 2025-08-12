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

import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData.OrderOption.Delivery
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData.OrderOption.Online
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData.OrderOption.Pickup
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Companion.redeemValidationState
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyName
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.EmptyPhoneNumber
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidDeliveryInformation
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Error.InvalidLine2
import de.gematik.ti.erp.app.redeem.model.RedeemContactValidationState.MissingPersonalInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class RedeemContactValidationStateTest {

    @Test
    fun `returns MissingOrderOption when selectedOrderOption is null`() {
        val state = ContactValidationState.Invalid(null, setOf(EmptyName))
        assertEquals(RedeemContactValidationState.MissingOrderOption, state.redeemValidationState())
    }

    @Test
    fun `returns NoError for valid state`() {
        val state = ContactValidationState.Valid(Delivery)
        assertEquals(RedeemContactValidationState.NoError, state.redeemValidationState())
    }

    @Test
    fun `returns NoError for pickup with no name`() {
        val state = ContactValidationState.Invalid(Pickup, setOf(EmptyName))
        assertEquals(RedeemContactValidationState.NoError, state.redeemValidationState())
    }

    @Test
    fun `returns NoError for pickup with non-personal error`() {
        val state = ContactValidationState.Invalid(Pickup, setOf(InvalidDeliveryInformation))
        assertEquals(RedeemContactValidationState.NoError, state.redeemValidationState())
    }

    @Test
    fun `returns MissingPhone for delivery with only phone error`() {
        val state = ContactValidationState.Invalid(Delivery, setOf(EmptyPhoneNumber))
        assertEquals(RedeemContactValidationState.MissingPhone, state.redeemValidationState())
    }

    @Test
    fun `returns MissingPersonalInfo for delivery with personal info error`() {
        val state = ContactValidationState.Invalid(Delivery, setOf(EmptyName))
        assertEquals(MissingPersonalInfo, state.redeemValidationState())
    }

    @Test
    fun `returns MissingDeliveryInfo for delivery with only delivery info error`() {
        val state = ContactValidationState.Invalid(Delivery, setOf(InvalidDeliveryInformation))
        assertEquals(RedeemContactValidationState.MissingDeliveryInfo, state.redeemValidationState())
    }

    @Test
    fun `returns highest priority error in delivery`() {
        val state = ContactValidationState.Invalid(Delivery, setOf(EmptyPhoneNumber, EmptyName, InvalidDeliveryInformation))
        // MissingPersonalInfo should have priority
        assertEquals(MissingPersonalInfo, state.redeemValidationState())
    }

    @Test
    fun `returns MissingPhone for online order with phone error`() {
        val state = ContactValidationState.Invalid(Online, setOf(EmptyPhoneNumber))
        assertEquals(RedeemContactValidationState.MissingPhone, state.redeemValidationState())
    }

    @Test
    fun `returns MissingDeliveryInfo for online order with delivery info error`() {
        val state = ContactValidationState.Invalid(Online, setOf(InvalidDeliveryInformation))
        assertEquals(RedeemContactValidationState.MissingDeliveryInfo, state.redeemValidationState())
    }

    @Test
    fun `returns NoError for invalid state with unrelated error`() {
        val state = ContactValidationState.Invalid(Online, setOf(InvalidLine2))
        assertEquals(RedeemContactValidationState.NoError, state.redeemValidationState())
    }
}
