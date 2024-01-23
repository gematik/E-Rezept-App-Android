/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.utils

import kotlin.streams.asSequence

// see https://unicode.org/emoji/charts/full-emoji-list.html
private fun Int.isEmoticon() = this in 0x1F600..0xE007F

private val r = """[\p{L}\p{M}\p{N}\u200d -]""".toRegex()

/**
 * Take the String and map the characters that are characters or emoticon and return the string back
 */
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
