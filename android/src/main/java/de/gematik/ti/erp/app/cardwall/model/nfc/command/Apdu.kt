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

import java.io.ByteArrayOutputStream

/**
 * Value for when wildcardShort for expected length encoding is needed
 */
const val EXPECTED_LENGTH_WILDCARD_EXTENDED: Int = 65536
const val EXPECTED_LENGTH_WILDCARD_SHORT: Int = 256

private fun encodeDataLengthExtended(nc: Int): ByteArray =
    byteArrayOf(0x0, (nc shr 8).toByte(), (nc and 0xFF).toByte())

private fun encodeDataLengthShort(nc: Int): ByteArray =
    byteArrayOf(nc.toByte())

private fun encodeExpectedLengthExtended(ne: Int): ByteArray =
    if (ne != EXPECTED_LENGTH_WILDCARD_EXTENDED) { // == 65536
        byteArrayOf((ne shr 8).toByte(), (ne and 0xFF).toByte()) // l1, l2
    } else {
        byteArrayOf(0x0, 0x0)
    }

private fun encodeExpectedLengthShort(ne: Int): ByteArray =
    byteArrayOf(
        if (ne != EXPECTED_LENGTH_WILDCARD_EXTENDED) {
            ne.toByte()
        } else {
            0x0
        }
    )

/**
 * An APDU (Application Protocol Data Unit) Command per ISO/IEC 7816-4.
 * Command APDU encoding options:
 *
 * ```
 * case 1:  |CLA|INS|P1 |P2 |                                 len = 4
 * case 2s: |CLA|INS|P1 |P2 |LE |                             len = 5
 * case 3s: |CLA|INS|P1 |P2 |LC |...BODY...|                  len = 6..260
 * case 4s: |CLA|INS|P1 |P2 |LC |...BODY...|LE |              len = 7..261
 * case 2e: |CLA|INS|P1 |P2 |00 |LE1|LE2|                     len = 7
 * case 3e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|          len = 8..65542
 * case 4e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|LE1|LE2|  len =10..65544
 *
 * LE, LE1, LE2 may be 0x00.
 * LC must not be 0x00 and LC1|LC2 must not be 0x00|0x00
 * ```
 */
class CommandApdu(
    apduBytes: ByteArray,
    val rawNc: Int,
    val rawNe: Int?,
    val dataOffset: Int
) {
    private val _apduBytes = apduBytes.copyOf()
    val bytes
        get() = _apduBytes.copyOf()

    companion object {
        fun ofOptions(
            cla: Int,
            ins: Int,
            p1: Int,
            p2: Int,
            ne: Int?
        ) = ofOptions(cla = cla, ins = ins, p1 = p1, p2 = p2, data = null, ne = ne)

        fun ofOptions(
            cla: Int,
            ins: Int,
            p1: Int,
            p2: Int,
            data: ByteArray?,
            ne: Int?
        ): CommandApdu {
            require(!(cla < 0 || ins < 0 || p1 < 0 || p2 < 0)) { "APDU header fields must not be less than 0" }
            require(!(cla > 0xFF || ins > 0xFF || p1 > 0xFF || p2 > 0xFF)) { "APDU header fields must not be greater than 255 (0xFF)" }
            ne?.let { require(ne <= EXPECTED_LENGTH_WILDCARD_EXTENDED || ne >= 0) { "APDU response length is out of bounds [0, 65536]" } }

            val bytes = ByteArrayOutputStream()
            // write header |CLA|INS|P1 |P2 |
            bytes.write(byteArrayOf(cla.toByte(), ins.toByte(), p1.toByte(), p2.toByte()))

            return if (data != null) {
                val nc = data.size
                require(nc <= 65535) { "ADPU cmd data length must not exceed 65535 bytes" }

                var dataOffset: Int
                var le: Int? // le1, le2
                if (ne != null) {
                    le = ne
                    // case 4s or 4e
                    if (nc <= 255 && ne <= EXPECTED_LENGTH_WILDCARD_SHORT) {
                        // case 4s
                        dataOffset = 5
                        bytes.write(encodeDataLengthShort(nc))
                        bytes.write(data)
                        bytes.write(encodeExpectedLengthShort(ne))
                    } else {
                        // case 4e
                        dataOffset = 7
                        bytes.write(encodeDataLengthExtended(nc))
                        bytes.write(data)
                        bytes.write(encodeExpectedLengthExtended(ne))
                    }
                } else {
                    // case 3s or 3e
                    le = null
                    if (nc <= 255) {
                        // case 3s
                        dataOffset = 5
                        bytes.write(encodeDataLengthShort(nc))
                    } else {
                        // case 3e
                        dataOffset = 7
                        bytes.write(encodeDataLengthExtended(nc))
                    }
                    bytes.write(data)
                }

                CommandApdu(
                    apduBytes = bytes.toByteArray(),
                    rawNc = nc,
                    rawNe = le,
                    dataOffset = dataOffset
                )
            } else {
                // data empty
                if (ne != null) {
                    // case 2s or 2e
                    if (ne <= EXPECTED_LENGTH_WILDCARD_SHORT) {
                        // case 2s
                        // 256 is encoded 0x0
                        bytes.write(encodeExpectedLengthShort(ne))
                    } else {
                        // case 2e
                        bytes.write(0x0)
                        bytes.write(encodeExpectedLengthExtended(ne))
                    }

                    CommandApdu(
                        apduBytes = bytes.toByteArray(),
                        rawNc = 0,
                        rawNe = ne,
                        dataOffset = 0
                    )
                } else {
                    // case 1
                    CommandApdu(
                        apduBytes = bytes.toByteArray(),
                        rawNc = 0,
                        rawNe = null,
                        dataOffset = 0
                    )
                }
            }
        }
    }
}

/**
 * APDU Response
 */
class ResponseApdu(apdu: ByteArray) {
    init {
        require(apdu.size >= 2) { "Response APDU must not have less than 2 bytes (status bytes SW1, SW2)" }
    }

    private val apdu = apdu.copyOf()

    val nr: Int
        get() = apdu.size - 2

    val data: ByteArray
        get() = apdu.copyOfRange(0, apdu.size - 2)

    val sw1: Int
        get() = apdu[apdu.size - 2].toInt() and 0xFF

    val sw2: Int
        get() = apdu[apdu.size - 1].toInt() and 0xFF

    val sw: Int
        get() = sw1 shl 8 or sw2

    val bytes: ByteArray
        get() = apdu.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseApdu

        if (!apdu.contentEquals(other.apdu)) return false

        return true
    }

    override fun hashCode(): Int {
        return apdu.contentHashCode()
    }
}
