/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class DMUtilsTest {
    @Test
    fun `create dm payload from list of urls`() {
        val urls = listOf("ChargeItem/01234?ac=98765", "ChargeItem/98765?ac=01234")
        assertEquals(
            "{\"urls\":[\"ChargeItem/01234?ac=98765\",\"ChargeItem/98765?ac=01234\"]}",
            createDMPayload(urls)
        )
    }
}
