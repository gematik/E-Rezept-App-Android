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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.nfc

import de.gematik.ti.erp.app.card.model.card.PaceKey
import de.gematik.ti.erp.app.card.model.card.SecureMessaging
import de.gematik.ti.erp.app.card.model.command.CommandApdu
import de.gematik.ti.erp.app.card.model.command.ResponseApdu
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert
import kotlin.test.Test

class SecureMessagingTest {
    private val keyEnc: ByteArray = Hex.decode("68406B4162100563D9C901A6154D2901")
    private val keyMac: ByteArray = Hex.decode("73FF268784F72AF833FDC9464049AFC9")
    private val paceKey = PaceKey(keyEnc, keyMac)
    private val secureMessaging = SecureMessaging(paceKey)

    // test Case 1: |CLA|INS|P1|P2|
    @Test
    fun testEncryptionCase1() {
        val commandApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, null)
        val expectedEncryptedApdu = Hex.decode("0D0203040A8E08D92B4FDDC2BBED8C00")
        val encryptedCommandApdu = secureMessaging.encrypt(commandApdu)
        Assert.assertArrayEquals(
            expectedEncryptedApdu,
            encryptedCommandApdu.bytes
        )
        try {
            secureMessaging.encrypt(encryptedCommandApdu)
            Assert.fail("Encrypting an already encrypted Apdu should give an error.")
        } catch (e: Exception) {
            // expected
        }
    }

    // test Case 2s: |CLA|INS|P1|P2|LE|
    @Test
    fun testEncryptionCase2s() {
        val secureMessaging = SecureMessaging(paceKey)
        val commandApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 127)
        val expectedEncryptedApdu = Hex.decode("0D02030400000D97017F8E0871D8E0418DAE20F30000")
        val encryptedCommandApdu = secureMessaging.encrypt(commandApdu)
        Assert.assertArrayEquals(
            expectedEncryptedApdu,
            encryptedCommandApdu.bytes
        )
    }

    // test Case 2e: |CLA|INS|P1|P2|EXTLE|
    @Test
    fun testEncryptionCase2e() {
        val secureMessaging = SecureMessaging(paceKey)
        val commandApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, 257)
        val expectedEncryptedApdu = Hex.decode("0D02030400000E970201018E089F3EDDFBB1D3971D0000")
        val encryptedCommandApdu = secureMessaging.encrypt(commandApdu)
        Assert.assertArrayEquals(
            expectedEncryptedApdu,
            encryptedCommandApdu.bytes
        )
    }

    // test Case 3s. : |CLA|INS|P1|P2|LC|DATA|
    @Test
    fun testEncryptionCase3s() {
        val cmdData = byteArrayOf(0x05, 0x06, 0x07, 0x08, 0x09, 0x0a)
        val secureMessaging = SecureMessaging(paceKey)
        val commandApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, null)
        val expectedEncryptedApdu =
            Hex.decode("0D0203041D871101496C26D36306679609665A385C54DB378E08E7AAD918F260D8EF00")
        val encryptedCommandApdu = secureMessaging.encrypt(commandApdu)
        Assert.assertArrayEquals(
            expectedEncryptedApdu,
            encryptedCommandApdu.bytes
        )
    }

    // test Case 4s. : |CLA|INS|P1|P2|LC|DATA|LE|
    @Test
    fun testEncryptionCase4s() {
        val cmdData = byteArrayOf(0x05, 0x06, 0x07, 0x08, 0x09, 0x0a)
        val commandApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 127)
        val expectedEncryptedApdu =
            Hex.decode("0D020304000020871101496C26D36306679609665A385C54DB3797017F8E0863D541F262BD445A0000")
        val encryptedCommandApdu = secureMessaging.encrypt(commandApdu)
        Assert.assertArrayEquals(
            expectedEncryptedApdu,
            encryptedCommandApdu.bytes
        )
    }

    // test Case 4e: |CLA|INS|P1|P2|EXT('00')|LC|DATA|LE|
    @Test
    fun testEncryptionCase4e() {
        val cmdData = ByteArray(256)
        val commandApdu = CommandApdu.ofOptions(0x01, 0x02, 0x03, 0x04, cmdData, 127)
        val expectedEncryptedApdu =
            (
                Hex.decode(
                    "0D02030400012287820111013297D4AA774AB26AF8AD539C0A829BCA4D222D3EE2DB100CF86D7DB5A1FAC12B7623328DEFE3F6FDD41A993A" +
                        "C917BC17B364C3DD24740079DE60A3D0231A7185D36A77D37E147025913ADA00CD07736CFDE0DB2E0BB09B75C5773607E54A9D84181A" +
                        "CBC6F7726762A8BCE324C0B330548114154A13EDDBFF6DCBC3773DCA9A8494404BE4A5654273F9C2B9EBE1BD615CB39FFD0D3F2A0EEA" +
                        "29AA10B810D53EDB550FB741A68CC6B0BDF928F9EB6BC238416AACB4CF3002E865D486CF42D762C86EEBE6A2B25DECE2E88D569854A0" +
                        "7D3F146BC134BAF08B6EDCBEBDFF47EBA6AC7B441A1642B03253B588C49B69ABBEC92BA1723B7260DE8AD6158873141AFA7C70CFCF12" +
                        "5BA1DF77CA48025D049FCEE497017F8E0856332C83EABDF93C0000"
                )
                )
        val encryptedCommandApdu = secureMessaging.encrypt(commandApdu)
        Assert.assertArrayEquals(
            expectedEncryptedApdu,
            encryptedCommandApdu.bytes
        )
    }

    // test Case 1: DO99|DO8E|SW1SW2
    @Test
    fun shouldDecryptDo99Apdu() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt = ResponseApdu(Hex.decode("990290008E08087631D746F872729000"))
        val decryptedApdu: ResponseApdu = secureMessaging.decrypt(apduToDecrypt)
        val expectedDecryptedApdu = ResponseApdu(byteArrayOf(0x90.toByte(), 0x00))
        Assert.assertArrayEquals(
            expectedDecryptedApdu.bytes,
            decryptedApdu.bytes
        )
    }

    // test Case 2: DO87|DO99|DO8E|SW1SW2
    @Test
    fun shouldDecryptDo87Apdu() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt =
            ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290008E08B7E9ED2A0C89FB3A9000"))
        val decryptedApdu: ResponseApdu = secureMessaging.decrypt(apduToDecrypt)
        val expectedDecryptedApdu = ResponseApdu(Hex.decode("05060708090a9000"))
        Assert.assertArrayEquals(
            expectedDecryptedApdu.bytes,
            decryptedApdu.bytes
        )
    }

    @Test
    fun decryptShouldFailWithMissingStatusBytes() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt =
            ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db378E08B7E9ED2A0C89FB3A9000"))
        try {
            secureMessaging.decrypt(apduToDecrypt)
            Assert.fail("Decrypting an APDU without DO99 should fail.")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun decryptShouldFailWithMissingStatus() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt =
            ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290008E08B7E9ED2A0C89FB3A"))
        try {
            secureMessaging.decrypt(apduToDecrypt)
            Assert.fail("Decrypting an APDU with missing status should fail.")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun decryptShouldFailWithWrongCCS() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt =
            ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290008E08A7E9ED2A0C89FB3A9000"))
        try {
            secureMessaging.decrypt(apduToDecrypt)
            Assert.fail("Decrypting an APDU without wrong DO8E should fail.")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun decryptShouldFailWithMissingCCS() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt =
            ResponseApdu(Hex.decode("871101496c26d36306679609665a385c54db37990290009000"))
        try {
            secureMessaging.decrypt(apduToDecrypt)
            Assert.fail("Decrypting an APDU without DO8E should fail.")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    fun decryptShouldFailWithNotEncryptedApdu() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt = ResponseApdu(byteArrayOf(0x90.toByte(), 0x00))
        try {
            secureMessaging.decrypt(apduToDecrypt)
            Assert.fail("Decrypting an unencrypted APDU should fail.")
        } catch (e: Exception) {
            // expected
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDecryption() {
        val secureMessaging = SecureMessaging(paceKey)
        val apduToDecrypt = ResponseApdu(Hex.decode("990290008E08087631D746F872729000"))
        val decryptedAPDU: ResponseApdu = secureMessaging.decrypt(apduToDecrypt)
        val expectedDecryptedAPDU = byteArrayOf(0x90.toByte(), 0x00)
        Assert.assertArrayEquals(
            expectedDecryptedAPDU,
            decryptedAPDU.bytes
        )
    }
}
