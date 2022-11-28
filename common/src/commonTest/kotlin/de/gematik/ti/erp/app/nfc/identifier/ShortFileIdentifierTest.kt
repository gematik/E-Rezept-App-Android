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

package de.gematik.ti.erp.app.nfc.identifier

import de.gematik.ti.erp.app.card.model.identifier.ShortFileIdentifier
import org.junit.Assert
import kotlin.test.Test

class ShortFileIdentifierTest {
    @Test
    fun shouldConstructEqualObjects() {
        val sfi2 = ShortFileIdentifier(0x1C)
        val sfi1 = ShortFileIdentifier("1C")
        Assert.assertEquals(sfi1.sfId.toLong(), sfi2.sfId.toLong())
    }
}
