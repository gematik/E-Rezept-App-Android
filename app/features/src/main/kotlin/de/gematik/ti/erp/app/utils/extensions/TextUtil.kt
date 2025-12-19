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

package de.gematik.ti.erp.app.utils.extensions

import kotlin.streams.asSequence

@Suppress("MagicNumber")
// see https://unicode.org/emoji/charts/full-emoji-list.html
private fun Int.isEmoticon() = this in 0x1F600..0xE007F

private val r = """[\p{L}\p{M}\p{N}\u200d -]""".toRegex()

/**
 * Take the String and map the characters that are characters or emoticon and return the string back
 */
// TODO: Check if we need this fixed
fun String.sanitizeProfileName() =
    codePoints()
        .asSequence()
        .mapNotNull { letter ->
            val s = Character.toChars(letter).concatToString()
            if (letter.isEmoticon() || r.matches(s)) {
                s
            } else {
                null
            }
        }
        .joinToString("")
