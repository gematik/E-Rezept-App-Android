package de.gematik.ti.erp.app.common.strings

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

sealed class Translatable {
    abstract operator fun invoke(count: Int = 1): String
    abstract operator fun invoke(count: Int = 1, vararg args: Any?): String
    abstract operator fun invoke(count: Int = 1, vararg args: AnnotatedString): AnnotatedString
}

data class Singular(val value: String) : Translatable() {
    override fun invoke(count: Int): String {
        return value
    }

    override fun invoke(count: Int, vararg args: Any?): String {
        return value.format(*args)
    }

    override fun invoke(count: Int, vararg args: AnnotatedString): AnnotatedString =
        buildAnnotatedString {
            value.split("%s").forEachIndexed { index, s ->
                append(s)
                if (index < args.size) {
                    append(args[index])
                }
            }
        }
}

data class Plurals(val values: Map<Type, String>) : Translatable() {
    enum class Type {
        Zero, One, Two, Few, Many, Other
    }

    private fun countMapping(count: Int): Type =
        when (count) {
            0 -> if (values.containsKey(Type.Zero)) Type.Zero else countMapping(count + 1)
            1 -> if (values.containsKey(Type.One)) Type.One else countMapping(count + 1)
            2 -> if (values.containsKey(Type.Two)) Type.Two else countMapping(count + 1)
            in 3..4 -> if (values.containsKey(Type.Few)) Type.Few else countMapping(count + 3)
            in 11..99 -> if (values.containsKey(Type.Many)) Type.Many else Type.Other
            else -> Type.Other
        }

    override fun invoke(count: Int): String {
        return requireNotNull(values[countMapping(count)]) { "Couldn't find any plural matching count $count" }
    }

    override fun invoke(count: Int, vararg args: Any?): String {
        return invoke(count).format(*args)
    }

    override fun invoke(count: Int, vararg args: AnnotatedString): AnnotatedString =
        buildAnnotatedString {
            invoke(count).split("%s").forEachIndexed { index, s ->
                append(s)
                if (index < args.size) {
                    append(args[index])
                }
            }
        }
}

val LocalStrings = staticCompositionLocalOf<Strings> {
    error("No Strings provided")
}
