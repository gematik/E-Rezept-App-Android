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

package de.gematik.ti.erp.app.vau

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun `find sub byte array within byte array`() {
        assertEquals(true, byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf(2, 3)))
        assertEquals(true, byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf(3)))

        assertEquals(false, byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf()))
        assertEquals(false, byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf(4, 6)))

        assertEquals(
            true,
            byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf(2, 3, 1, 5, 6, 9, 0))
        )
        assertEquals(
            false,
            byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf(0, 3, 1, 5, 6, 9, 0))
        )

        assertEquals(
            false,
            byteArrayOf(2, 3, 1, 5, 6, 9, 0).contains(byteArrayOf(1, 3, 4, 0, 3, 1, 5, 6, 9, 0))
        )

        assertEquals(false, byteArrayOf().contains(byteArrayOf(6, 9, 0)))
    }
}
