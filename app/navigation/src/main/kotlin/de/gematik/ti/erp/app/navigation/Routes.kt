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

package de.gematik.ti.erp.app.navigation

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType

abstract class UriNavType<T>(override val isNullableAllowed: Boolean) :
    NavType<T>(isNullableAllowed) {
    abstract fun serializeValue(value: T): String
}

@Immutable
open class Routes(private val path: String, vararg arguments: NamedNavArgument) {
    val route = arguments.fold(Uri.Builder().path(path)) { uri, param ->
        uri.appendQueryParameter(param.name, "{${param.name}}")
    }.build().toString()

    val arguments = arguments.toList()

    fun path() = path

    fun path(vararg attributes: Pair<String, Any?>): String {
        require(arguments.size >= attributes.size) { "More attributes specified than arguments." }

        return attributes.fold(Uri.Builder().path(path)) { uri, attr ->
            val arg =
                requireNotNull(arguments.find { it.name == attr.first }) {
                    "`${attr.first}` not specified within arguments."
                }

            when (val v = attr.second) {
                null -> uri
                is CharSequence, is Boolean, is Number, is Enum<*> ->
                    uri.appendQueryParameter(attr.first, v.toString())
                is Parcelable -> {
                    val type =
                        requireNotNull(arg.argument.type as? UriNavType) {
                            "Parcelable types must be accompanied with an `UriNavType` argument."
                        }
                    uri.appendQueryParameter(attr.first, type.serializeValue(attr.second))
                }
                else -> error("Other types not implemented")
            }
        }.build().toString()
    }
}
