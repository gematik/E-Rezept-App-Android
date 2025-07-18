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

package de.gematik.ti.erp.app.vau

import org.junit.Assert.assertEquals
import kotlin.test.Test

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
