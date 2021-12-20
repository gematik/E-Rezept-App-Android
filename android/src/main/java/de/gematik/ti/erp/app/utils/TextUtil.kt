package de.gematik.ti.erp.app.utils

fun firstCharOfForeNameSurName(name: String): String {
    val names = name.split(" ")
    return when {
        names.size > 1 && names.first().isNotBlank() && names.last().isNotBlank() -> {
            val letterFirst = names.first().codePointAt(0)
            val letterLast = names.last().codePointAt(0)
            if (letterFirst.isEmoticon()) {
                Character.toChars(letterFirst).concatToString()
            } else {
                (Character.toChars(letterFirst) + Character.toChars(letterLast)).concatToString().uppercase()
            }
        }
        names.size == 1 && names.first().isNotBlank() -> {
            val letter = names.first().codePointAt(0)
            Character.toChars(letter).concatToString().uppercase()
        }
        else -> ""
    }
}

// see https://unicode.org/emoji/charts/full-emoji-list.html
private fun Int.isEmoticon() = this in 0x1F600..0xE007F
