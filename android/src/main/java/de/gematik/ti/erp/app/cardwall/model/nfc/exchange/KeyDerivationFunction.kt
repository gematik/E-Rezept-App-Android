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

package de.gematik.ti.erp.app.cardwall.model.nfc.exchange

import org.bouncycastle.crypto.digests.SHA1Digest

private const val CHECKSUMLENGTH = 20
private const val AES128LENGTH = 16
private const val OFFSETLENGTH = 4
private const val ENCLASTBYTE = 1
private const val MACLASTBYTE = 2
private const val PASSWORDLASTBYTE = 3

/**
 * This class provides functionality to derive AES-128 keys.
 */
object KeyDerivationFunction {
    /**
     * derive AES-128 key
     *
     * @param sharedSecretK byte array with shared secret value.
     * @param mode key derivation for ENC, MAC or derivation from password
     * @return byte array with AES-128 key
     */
    fun getAES128Key(sharedSecretK: ByteArray, mode: Mode): ByteArray {
        val checksum = ByteArray(CHECKSUMLENGTH)
        val data = replaceLastKeyByte(sharedSecretK, mode)
        SHA1Digest().apply {
            update(data, 0, data.size)
            doFinal(checksum, 0)
        }
        return checksum.copyOf(AES128LENGTH)
    }

    private fun replaceLastKeyByte(key: ByteArray, mode: Mode): ByteArray =
        ByteArray(key.size + OFFSETLENGTH).apply {
            key.copyInto(this)
            this[this.size - 1] = when (mode) {
                Mode.ENC -> ENCLASTBYTE.toByte()
                Mode.MAC -> MACLASTBYTE.toByte()
                Mode.PASSWORD -> PASSWORDLASTBYTE.toByte()
            }
        }

    enum class Mode {
        ENC, // key for encryption/decryption
        MAC, // key for MAC
        PASSWORD // encryption keys from a password
    }
}
