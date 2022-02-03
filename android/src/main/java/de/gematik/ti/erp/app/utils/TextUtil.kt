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

package de.gematik.ti.erp.app.utils

import kotlin.streams.asSequence
import kotlin.streams.toList

fun firstCharOfForeNameSurName(name: String): String {
    val names = name.split(" ", "-").filter {
        it.isNotBlank()
    }
    return when {
        names.size > 1 -> {
            val letterFirst = names.first().codePointAt(0)
            val letterLast = names.last().codePointAt(0)
            if (letterFirst.isEmoticon()) {
                joinCombinedEmoji(names.first())
            } else {
                (Character.toChars(letterFirst) + Character.toChars(letterLast)).concatToString().uppercase()
            }
        }
        names.size == 1 -> {
            val letter = names.first().codePointAt(0)
            if (letter.isEmoticon()) {
                joinCombinedEmoji(names.first())
            } else {
                Character.toChars(letter).concatToString().uppercase()
            }
        }
        else -> ""
    }
}

// Combined emojis use https://en.wikipedia.org/wiki/Zero-width_joiner
private fun joinCombinedEmoji(value: String): String {
    var lastLetter = 0
    var outString = ""
    for (letter in value.codePoints().toList()) {
        outString += when {
            (lastLetter == 0 || lastLetter == 0x200d) && letter.isEmoticon() ->
                Character.toChars(letter).concatToString()
            letter == 0x200d ->
                Character.toChars(letter).concatToString()
            else -> break
        }
        lastLetter = letter
    }
    return outString
}

// see https://unicode.org/emoji/charts/full-emoji-list.html
private fun Int.isEmoticon() = this in 0x1F600..0xE007F

private val r = """[\p{L}\p{M}\p{N}\u200d -]""".toRegex()

fun sanitizeProfileName(name: String): String =
    name.codePoints()
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
