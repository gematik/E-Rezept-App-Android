/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.utils

import java.math.BigInteger

object Bytes {
    private const val PAD = 0x80.toByte()

    /**
     * Padding the data with [PAD].
     *
     * @param data byte array with data
     * @param blockSize int
     * @return byte array with padded data
     */
    fun padData(data: ByteArray, blockSize: Int): ByteArray =
        ByteArray(data.size + (blockSize - data.size % blockSize)).apply {
            data.copyInto(this)
            this[data.size] = PAD
        }

    /**
     * Unpadding the data.
     *
     * @param paddedData byte array with padded data
     * @return byte array with data
     */
    fun unPadData(paddedData: ByteArray): ByteArray {
        for (i in paddedData.indices.reversed()) {
            if (paddedData[i] == PAD) {
                return paddedData.copyOfRange(0, i)
            }
        }
        return paddedData
    }

    /**
     * Converts a BigInteger into a ByteArray. A leading byte with the value 0 is truncated.
     *
     * @param bigInteger The BigInteger object to convert.
     * @return The ByteArray without leading 0-byte
     */
    fun bigIntToByteArray(bigInteger: BigInteger): ByteArray {
        val bigIntArray = bigInteger.toByteArray()
        return if (bigIntArray[0] == 0.toByte()) {
            bigIntArray.copyOfRange(1, bigIntArray.size)
        } else {
            bigIntArray
        }
    }
}
