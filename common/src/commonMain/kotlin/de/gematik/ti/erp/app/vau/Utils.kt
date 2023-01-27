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

package de.gematik.ti.erp.app.vau

// hex

private fun hexMap(index: Int) =
    when (index) {
        in 0..9 -> (index + 48).toByte()
        in 10..15 -> (index + 97 - 10).toByte()
        else -> error("wrong hex")
    }

/**
 * Converts the bytes into an hex representation as bytes.
 *
 * E.g. `byteArrayOf(0, 5, 2).toLowerCaseHex()` result in `[48, 48, 48, 53, 48, 50]`.
 */
fun ByteArray.toLowerCaseHex(): ByteArray {
    val buffer = ByteArray(this.size * 2)
    for (i in this.indices) {
        (this[i].toInt() and 0xFF).let {
            buffer[i * 2] = hexMap((it / 16) % 16)
            buffer[i * 2 + 1] = hexMap(it % 16)
        }
    }
    return buffer
}

/**
 * Searches [other] within [this] array of bytes.
 */
fun ByteArray.contains(other: ByteArray): Boolean {
    if (this.isEmpty() || other.isEmpty() || other.size > this.size) {
        return false
    }

    for (i in 0..(this.size - other.size)) {
        if (this[i] == other[0] && this.size - other.size - i >= 0) {
            var found = true
            for (j in other.indices) {
                if (this[i + j] != other[j]) {
                    found = false
                    break
                }
            }
            if (found) {
                return true
            }
        }
    }
    return false
}
