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
