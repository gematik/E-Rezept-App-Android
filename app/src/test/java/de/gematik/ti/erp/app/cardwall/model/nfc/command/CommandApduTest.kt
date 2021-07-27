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

package de.gematik.ti.erp.app.cardwall.model.nfc.command

import org.junit.Assert
import org.junit.Test
import java.util.Arrays
import java.util.Random

class CommandApduTest {

    @Test
    fun testCase1Apdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, null)
        Assert.assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04), cApdu.bytes)

        Assert.assertEquals(0x00, cApdu.dataOffset)
        Assert.assertEquals(0x00, cApdu.rawNc)
        Assert.assertNull(cApdu.rawNe)
    }

    @Test
    fun testCase2sApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 127)
        Assert.assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x7F), cApdu.bytes)

        Assert.assertEquals(0x00, cApdu.rawNc)

//        Assert.assertEquals(127, cApdu.rawNe!!)
    }

    @Test
    fun testCase2sExpect256BytesApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 256)
        Assert.assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00), cApdu.bytes)

        Assert.assertEquals(0x00, cApdu.rawNc)

        Assert.assertEquals(256, cApdu.rawNe!!)
    }

    @Test
    fun testCase2sExpectBytesWildcardApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, EXPECTED_LENGTH_WILDCARD_SHORT)
        Assert.assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00), cApdu.bytes)

        Assert.assertEquals(0x00, cApdu.rawNc)

        Assert.assertEquals(EXPECTED_LENGTH_WILDCARD_SHORT, cApdu.rawNe as Int)
    }

    @Test
    fun testCase2eApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 257)
        Assert.assertArrayEquals(
            byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0x01, 0x01),
            cApdu.bytes
        )

        Assert.assertEquals(0x00, cApdu.rawNc)

        Assert.assertEquals(257, cApdu.rawNe!!)
    }

    @Test
    fun testCase2eExpect65535BytesApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 65535)
        Assert.assertArrayEquals(
            byteArrayOf(
                0x01,
                0x02,
                0x03,
                0x04,
                0x00,
                0xff.toByte(),
                0xff.toByte()
            ),
            cApdu.bytes
        )

        Assert.assertEquals(0x00, cApdu.rawNc)

        Assert.assertEquals(65535, cApdu.rawNe!!)
    }

    @Test
    fun testCase2eExpect65536BytesApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 65536)
        Assert.assertArrayEquals(
            byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0x00, 0x00),
            cApdu.bytes
        )

        Assert.assertEquals(0x00, cApdu.rawNc)

        Assert.assertEquals(65536, cApdu.rawNe!!)
    }

    @Test
    fun testCase2eExpectBytesWildcardApdu() {
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, EXPECTED_LENGTH_WILDCARD_EXTENDED)
        Assert.assertArrayEquals(
            byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0x00, 0x00),
            cApdu.bytes
        )

        Assert.assertEquals(0x00, cApdu.rawNc)

        Assert.assertEquals(EXPECTED_LENGTH_WILDCARD_EXTENDED, cApdu.rawNe as Int)
    }

    @Test
    fun testCase3sApdu() {
        val cmdData = byteArrayOf(0x05, 0x06, 0x07, 0x08, 0x09, 0x0a)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, cmdData.size.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(cmdData.size, cApdu.rawNc)

        Assert.assertNull(cApdu.rawNe)
    }

    @Test
    fun testCase3s255BytesDataApdu() {
        val cmdData = ByteArray(255)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(0xff, cApdu.rawNc)

        Assert.assertNull(cApdu.rawNe)
    }

    @Test
    fun testCase3e256BytesDataApdu() {
        val cmdData = ByteArray(256)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0x01, 0x00)
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(256, cApdu.rawNc)

        Assert.assertNull(cApdu.rawNe)
    }

    @Test
    fun testCase3e65535BytesDataApdu() {
        val dataSize = 65535
        val cmdData = ByteArray(dataSize)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
        val header_plus_lc =
            byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, (dataSize shr 8).toByte(), dataSize.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(dataSize, cApdu.rawNc)

        Assert.assertNull(cApdu.rawNe)
    }

    @Test
    fun testCase4sApdu() {
        val cmdData = byteArrayOf(0x05, 0x06, 0x07, 0x08, 0x09, 0x0a)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 127)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, cmdData.size.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 1)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x7F
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(cmdData.size, cApdu.rawNc)

        Assert.assertEquals(127, cApdu.rawNe!!)
    }

    @Test
    fun testCase4s255BytesDataApdu() {
        val cmdData = ByteArray(255)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 256)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 1)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x00.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(0xff, cApdu.rawNc)

        Assert.assertEquals(256, cApdu.rawNe!!)
    }

    @Test
    fun testCase4e256BytesDataApdu() {
        val cmdData = ByteArray(256)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 127)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0x01, 0x00)
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 2)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x00.toByte()
        apdu[header_plus_lc.size + cmdData.size + 1] = 0x7F.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(256, cApdu.rawNc)

        Assert.assertEquals(127, cApdu.rawNe!!)
    }

    @Test
    fun testCase4e255BytesDataExpectedLength257Apdu() {
        val cmdData = ByteArray(255)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 257)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0x00, 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 2)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x01.toByte()
        apdu[header_plus_lc.size + cmdData.size + 1] = 0x01.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(255, cApdu.rawNc)

        Assert.assertEquals(257, cApdu.rawNe!!)
    }

    @Test
    fun testCase4e65535BytesDataExpectedLength65535Apdu() {
        val cmdData = ByteArray(65535)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 65535)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0xFF.toByte(), 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 2)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0xFF.toByte()
        apdu[header_plus_lc.size + cmdData.size + 1] = 0xFF.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(65535, cApdu.rawNc)

        Assert.assertEquals(65535, cApdu.rawNe!!)
    }

    @Test
    fun testCase4e65535BytesDataExpectedLength65536Apdu() {
        val cmdData = ByteArray(65535)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 65536)
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0xFF.toByte(), 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 2)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x00.toByte()
        apdu[header_plus_lc.size + cmdData.size + 1] = 0x00.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(65535, cApdu.rawNc)

        Assert.assertEquals(65536, cApdu.rawNe!!)
    }

    @Test
    fun testCase4e65535BytesDataExpectedLengthWildcardShortApdu() {
        val cmdData = ByteArray(65535)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(
            0x01,
            0x02,
            0x03,
            0x04,
            cmdData,
            EXPECTED_LENGTH_WILDCARD_SHORT
        )
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0xFF.toByte(), 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 2)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x01.toByte()
        apdu[header_plus_lc.size + cmdData.size + 1] = 0x00.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(65535, cApdu.rawNc)

        Assert.assertEquals(EXPECTED_LENGTH_WILDCARD_SHORT, cApdu.rawNe as Int)
    }

    @Test
    fun testCase4e65535BytesDataExpectedLengthWildcardExtendedApdu() {
        val cmdData = ByteArray(65535)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(
            0x01,
            0x02,
            0x03,
            0x04,
            cmdData,
            EXPECTED_LENGTH_WILDCARD_EXTENDED
        )
        val header_plus_lc = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x00, 0xFF.toByte(), 0xFF.toByte())
        val apdu = Arrays.copyOf(header_plus_lc, header_plus_lc.size + cmdData.size + 2)
        System.arraycopy(cmdData, 0, apdu, header_plus_lc.size, cmdData.size)
        apdu[header_plus_lc.size + cmdData.size] = 0x00.toByte()
        apdu[header_plus_lc.size + cmdData.size + 1] = 0x00.toByte()
        Assert.assertArrayEquals(apdu, cApdu.bytes)

        Assert.assertEquals(65535, cApdu.rawNc)

        Assert.assertEquals(EXPECTED_LENGTH_WILDCARD_EXTENDED, cApdu.rawNe as Int)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCmdDataTooBig() {
        val dataSize = 65536
        val cmdData = ByteArray(dataSize)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
    }

    @Test
    fun testCloning() {
        val dataSize = 65535
        val cmdData = ByteArray(dataSize)
        val rnd = Random(System.currentTimeMillis())
        rnd.nextBytes(cmdData)
        val cApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
        val apdu1 = cApdu.bytes
        val apdu2 = cApdu.bytes

        Assert.assertArrayEquals(apdu2, apdu1)
        Assert.assertNotEquals(apdu2, apdu1)
    }
}
