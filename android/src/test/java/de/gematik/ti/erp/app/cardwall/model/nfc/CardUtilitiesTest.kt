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

package de.gematik.ti.erp.app.cardwall.model.nfc

import de.gematik.ti.erp.app.cardwall.model.nfc.CardUtilities.byteArrayToECPoint
import okio.ByteString.Companion.decodeHex
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class CardUtilitiesTest {
    private val byteArray: ByteArray = "044E2778F6AAEF54CB42865A3C30C753495AF4E53121400802D0AB1ACD665E9C774C2FAE1687E9DAA36C64570C909F93176F01EEAFCB45F9C08E49805F127D94EF".decodeHex().toByteArray()
    private val ecNamedCurveParameterSpec: ECNamedCurveParameterSpec =
        ECNamedCurveTable.getParameterSpec("BrainpoolP256r1")
    private val expectedECPoint =
        "(4e2778f6aaef54cb42865a3c30c753495af4e53121400802d0ab1acd665e9c77,4c2fae1687e9daa36c64570c909f93176f01eeafcb45f9c08e49805f127d94ef,1,7d5a0975fc2c3057eef67530417affe7fb8055c126dc5c6ce94a4b44f330b5d9)"
    private val curve: ECCurve.Fp = ecNamedCurveParameterSpec.getCurve() as ECCurve.Fp

    @Test
    fun shouldCreateValidEcPointFromByteArray() {
        val point: ECPoint = byteArrayToECPoint(byteArray, curve)
        Assert.assertEquals(expectedECPoint, point.toString())
    }

    @Test
    @Throws(IOException::class)
    fun shouldEncodeAsn1KeyObject() {
        val asn1InputArray: ByteArray =
            "7C438341041B05278F276BD92E6B0EE3478BD3A93B03FE8E4C35556F0D6C13C89C504F91C065E85C1D289B306F61BE2CECCED4E7532BF0925A4907F246DF7A69C8D69ED24F".decodeHex().toByteArray()
        val expectedKeyArray: ByteArray =
            "041B05278F276BD92E6B0EE3478BD3A93B03FE8E4C35556F0D6C13C89C504F91C065E85C1D289B306F61BE2CECCED4E7532BF0925A4907F246DF7A69C8D69ED24F".decodeHex().toByteArray()
        val keyArray: ByteArray = CardUtilities.extractKeyObjectEncoded(asn1InputArray)
        Assert.assertArrayEquals(expectedKeyArray, keyArray)
    }
}
