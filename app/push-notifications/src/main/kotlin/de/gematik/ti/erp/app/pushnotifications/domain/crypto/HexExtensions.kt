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

package de.gematik.ti.erp.app.pushnotifications.domain.crypto

private const val HEX_CHARS = "0123456789abcdef"

private fun Char.hexNibble(): Int = when (this) {
    in '0'..'9' -> this - '0'
    in 'a'..'f' -> this - 'a' + 10
    in 'A'..'F' -> this - 'A' + 10
    else -> throw IllegalArgumentException("Non-hex character: '$this'")
}

// Decodes a hex string into a ByteArray. Requires an even length, valid hex string.
fun String.hexToByteArray(): ByteArray {
    require(isNotEmpty()) { "Hex string must not be empty" }
    require(length % 2 == 0) { "Hex string must have even length" }
    return ByteArray(length / 2) { i -> ((this[i * 2].hexNibble() shl 4) or this[i * 2 + 1].hexNibble()).toByte() }
}

// Encodes a ByteArray into a hex string.
fun ByteArray.toHexString(): String = buildString(size * 2) {
    this@toHexString.forEach { byte ->
        val value = byte.toInt() and 0xFF
        append(HEX_CHARS[value ushr 4])
        append(HEX_CHARS[value and 0x0F])
    }
}
