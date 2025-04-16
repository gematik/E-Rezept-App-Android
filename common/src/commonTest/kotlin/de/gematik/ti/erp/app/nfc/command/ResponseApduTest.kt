/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.nfc.command

import de.gematik.ti.erp.app.card.model.command.ResponseApdu
import org.junit.Assert
import org.junit.Test
import java.util.Arrays
import java.util.Random

class ResponseApduTest {
    @Test
    fun testEmptyDataResponse9000() {
        val rApdu = ResponseApdu(byteArrayOf(0x90.toByte(), 0x00))
        Assert.assertEquals(0x90, rApdu.sw1)
        Assert.assertEquals(0x00, rApdu.sw2)
        Assert.assertEquals(0x9000, rApdu.sw)
        Assert.assertEquals(0, rApdu.nr)
        Assert.assertEquals(0, rApdu.data.size)
        Assert.assertArrayEquals(byteArrayOf(0x90.toByte(), 0x00), rApdu.bytes)
        Assert.assertEquals(ResponseApdu(byteArrayOf(0x90.toByte(), 0x00)), rApdu)
    }

    @Test
    fun testEmptyDataResponse6383() {
        val rApdu = ResponseApdu(byteArrayOf(0x63, 0x83.toByte()))
        Assert.assertEquals(0x63, rApdu.sw1)
        Assert.assertEquals(0x83, rApdu.sw2)
        Assert.assertEquals(0x6383, rApdu.sw)
        Assert.assertEquals(0, rApdu.nr)
        Assert.assertEquals(0, rApdu.data.size)
        Assert.assertArrayEquals(byteArrayOf(0x63, 0x83.toByte()), rApdu.bytes)
        Assert.assertEquals(ResponseApdu(byteArrayOf(0x63.toByte(), 0x83.toByte())), rApdu)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testShortApdu() {
        ResponseApdu(byteArrayOf(0x63))
    }

    @Test
    fun tesResponseWithData() {
        val rApdu1 = ResponseApdu(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x90.toByte(), 0x09))
        Assert.assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05), rApdu1.data)
        Assert.assertEquals(0x90, rApdu1.sw1)
        Assert.assertEquals(0x09, rApdu1.sw2)
        val rApdu2 = ResponseApdu(byteArrayOf(0x00, 0x90.toByte(), 0x09))
        Assert.assertArrayEquals(byteArrayOf(0x00), rApdu2.data)
        Assert.assertEquals(0x90, rApdu2.sw1)
        Assert.assertEquals(0x09, rApdu2.sw2)
        val data = ByteArray(65536)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(data)
        val apdu = Arrays.copyOf(data, data.size + 2)
        apdu[data.size] = 0x90.toByte()
        val rApdu3 = ResponseApdu(apdu)
        Assert.assertArrayEquals(data, rApdu3.data)
        Assert.assertEquals(0x90, rApdu3.sw1)
        Assert.assertEquals(0x00, rApdu3.sw2)
    }

    @Test
    fun testCloning() {
        val rApdu = ResponseApdu(byteArrayOf(0x90.toByte(), 0x00, 0x01, 0x02, 0x03, 0x04, 0x05))
        val apduCopy1 = rApdu.bytes
        val apduCopy2 = rApdu.bytes
        Assert.assertNotEquals(apduCopy2, apduCopy1)
        val data1 = rApdu.data
        val data2 = rApdu.data
        Assert.assertNotEquals(data2, data1)
    }

    @Test
    fun testEquality() {
        val rApdu1 = ResponseApdu(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x90.toByte(), 0x00))
        val rApdu2 = ResponseApdu(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x90.toByte(), 0x00))
        val rApdu3 = ResponseApdu(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x90.toByte(), 0x01))
        val rApdu4 = ResponseApdu(byteArrayOf(0x00, 0x02, 0x03, 0x04, 0x05, 0x90.toByte(), 0x00))
        Assert.assertEquals(rApdu2, rApdu1)
        Assert.assertNotEquals(rApdu3, rApdu1)
        Assert.assertNotEquals(rApdu4, rApdu1)
    }
}
