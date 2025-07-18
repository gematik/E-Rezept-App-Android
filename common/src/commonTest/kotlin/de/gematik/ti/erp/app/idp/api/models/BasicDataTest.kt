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
