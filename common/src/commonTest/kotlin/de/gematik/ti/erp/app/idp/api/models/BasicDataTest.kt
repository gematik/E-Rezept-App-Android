/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.idp.api.models

import org.junit.Assert
import org.junit.Test

class BasicDataTest {

    @Test
    fun `generateRandomUrlSafeStringSecure - expected length works`() {
        Assert.assertEquals(1, generateRandomUrlSafeStringSecure(1).length)
        Assert.assertEquals(32, generateRandomUrlSafeStringSecure(32).length)
        Assert.assertEquals(64, generateRandomUrlSafeStringSecure(64).length)
        Assert.assertEquals(77, generateRandomUrlSafeStringSecure(77).length)
        Assert.assertEquals(111, generateRandomUrlSafeStringSecure(111).length)
        Assert.assertEquals(12345, generateRandomUrlSafeStringSecure(12345).length)
    }

    @Test
    fun `generateRandomUrlSafeStringSecure - base 64 url safe charset only`() {
        Assert.assertTrue("""^[A-Za-z0-9_-]+$""".toRegex().matches(generateRandomUrlSafeStringSecure(12345)))
    }
}
