/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.model.nfc

import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.PaceInfo
import okio.ByteString.Companion.decodeHex
import org.junit.Assert
import org.junit.Test

class PaceInfoTest {

    @Test
    fun testPaceInfoExtraction() {
        val cardAccessBytes: ByteArray = "31143012060A04007F0007020204020202010202010D".decodeHex().toByteArray()
        val expectedProtocolId = "0.4.0.127.0.7.2.2.4.2.2"
        val expectedPaceInfoProtocolBytes: ByteArray = "04007F00070202040202".decodeHex().toByteArray()
        val paceInfo = PaceInfo(cardAccessBytes)
        val protocolId = paceInfo.protocolID
        Assert.assertEquals(expectedProtocolId, protocolId)
        val paceInfoProtocolBytes = paceInfo.paceInfoProtocolBytes
        Assert.assertArrayEquals(
            expectedPaceInfoProtocolBytes,
            paceInfoProtocolBytes
        )
    }
}
