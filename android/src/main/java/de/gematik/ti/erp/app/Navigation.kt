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

package de.gematik.ti.erp.app

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import de.gematik.ti.erp.app.mainscreen.ui.TaskIds
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class UriNavType<T>(override val isNullableAllowed: Boolean) :
    NavType<T>(isNullableAllowed) {
    abstract fun serializeValue(value: T): String
}

object AppNavTypes {
    val TaskIdsType = object : UriNavType<TaskIds>(false) {
        override fun put(bundle: Bundle, key: String, value: TaskIds) {
            bundle.putParcelable(key, value)
        }

        override fun get(bundle: Bundle, key: String): TaskIds {
            return bundle.getParcelable(key)!!
        }

        override fun parseValue(value: String): TaskIds {
            return Json.decodeFromString(value)
        }

        override fun serializeValue(value: TaskIds): String {
            return Json.encodeToString(value)
        }

        override val name: String
            get() = "taskIds"
    }
}

@Immutable
open class Route(private val path: String, vararg arguments: NamedNavArgument, val badgeCount: Int = 1) {
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
