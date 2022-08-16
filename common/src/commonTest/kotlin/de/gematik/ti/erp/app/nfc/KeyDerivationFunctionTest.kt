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

package de.gematik.ti.erp.app.nfc

import de.gematik.ti.erp.app.card.model.exchange.KeyDerivationFunction
import de.gematik.ti.erp.app.card.model.exchange.KeyDerivationFunction.getAES128Key
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert
import kotlin.test.Test

class KeyDerivationFunctionTest {
    private val secretK: ByteArray =
        Hex.decode("2ECA74E72CD6C1E0DA235093569984987C34A9F4D34E4E60FB0AD87B983CDC62")

    @Test
    fun shouldReturnValidAES128KeyModeEnc() {
        val validAes128Key: ByteArray = Hex.decode("AB5541629D18E5F33EE2B13DBDCDBE84")
        val aes128Key = getAES128Key(secretK, KeyDerivationFunction.Mode.ENC)
        Assert.assertArrayEquals(aes128Key, validAes128Key)
    }

    @Test
    fun shouldReturnValidAES128KeyModeMac() {
        val validAes128Key: ByteArray = Hex.decode("E13D3757C7D9073794A3D7CA94B22D30")
        val aes128Key = getAES128Key(secretK, KeyDerivationFunction.Mode.MAC)
        Assert.assertArrayEquals(aes128Key, validAes128Key)
    }

    @Test
    fun shouldReturnValidAES128KeyModePassword() {
        val validAes128Key: ByteArray = Hex.decode("74C1F5E712B53BAAA3B02B182E0961B9")
        val aes128Key = getAES128Key(secretK, KeyDerivationFunction.Mode.PASSWORD)
        Assert.assertArrayEquals(aes128Key, validAes128Key)
    }
}
