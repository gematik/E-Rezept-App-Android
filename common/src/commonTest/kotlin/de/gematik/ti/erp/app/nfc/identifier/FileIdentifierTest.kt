/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier
import org.junit.Assert
import kotlin.test.Test

class FileIdentifierTest {
    @Test
    fun shouldConstructEqualObjects() {
        val fi1 = FileIdentifier(FID_INTEGER)
        val fi2 = FileIdentifier(FID_HEX_STRING)
        Assert.assertArrayEquals(fi1.getFid(), fi2.getFid())
    }

    @Test
    fun shouldConvertToEqualObject() {
        val fi1 = FileIdentifier(FID_HEX_STRING)
        val fi2 = FileIdentifier(fi1.getFid())
        Assert.assertArrayEquals(fi1.getFid(), fi2.getFid())
    }

    companion object {
        private const val FID_HEX_STRING = "011C"
        private const val FID_INTEGER = 0x011C
    }
}
