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

package de.gematik.ti.erp.app.shippingInfo.model

import org.junit.Test
import kotlin.test.assertEquals

class ShippingInfoErpModelTest {

    @Test
    fun `splitFullName splits three or more names into first word as firstname and rest as lastname`() {
        val (first, last) = ShippingInfoErpModel.splitFullName("Hans muller schmidth")
        assertEquals("Hans", first)
        assertEquals("muller schmidth", last)
    }

    @Test
    fun `splitFullName splits two names normally`() {
        val (first, last) = ShippingInfoErpModel.splitFullName("Erika Mustermann")
        assertEquals("Erika", first)
        assertEquals("Mustermann", last)
    }

    @Test
    fun `splitFullName with single word has empty lastname`() {
        val (first, last) = ShippingInfoErpModel.splitFullName("Cher")
        assertEquals("Cher", first)
        assertEquals("", last)
    }

    @Test
    fun `splitFullName handles multiple whitespaces`() {
        val (first, last) = ShippingInfoErpModel.splitFullName("  Hans   muller   schmidth  ")
        assertEquals("Hans", first)
        assertEquals("muller   schmidth", last)
    }

    @Test
    fun `splitFullName handles empty string`() {
        val (first, last) = ShippingInfoErpModel.splitFullName("")
        assertEquals("", first)
        assertEquals("", last)
    }

    @Test
    fun `resolvedFirstname and resolvedLastname use splitFullName when fields are empty`() {
        val model = ShippingInfoErpModel(name = "Hans muller schmidth")
        assertEquals("Hans", model.resolvedFirstname())
        assertEquals("muller schmidth", model.resolvedLastname())
    }

    @Test
    fun `resolvedFirstname and resolvedLastname prioritize explicit fields`() {
        val model = ShippingInfoErpModel(
            name = "Hans muller schmidth",
            firstname = "Johann",
            lastname = "Schmidt"
        )
        assertEquals("Johann", model.resolvedFirstname())
        assertEquals("Schmidt", model.resolvedLastname())
    }
}
