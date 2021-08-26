/*
 * Copyright (c) 2021 gematik GmbH
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
import androidx.navigation.compose.NamedNavArgument
import javax.annotation.concurrent.Immutable

@Immutable
open class Route(private val path: String, vararg arguments: NamedNavArgument) {
    val route = arguments.fold(Uri.Builder().path(path)) { uri, param ->
        uri.appendQueryParameter(param.name, "{${param.name}}")
    }.build().toString()

    val arguments = arguments.toList()

    fun path() = path

    fun path(vararg attributes: Pair<String, Any?>): String =
        attributes.fold(Uri.Builder().path(path)) { uri, attr ->
            when (val v = attr.second) {
                is CharSequence, is Boolean, is Number, is Enum<*> ->
                    uri.appendQueryParameter(attr.first, v.toString())
                else -> error("Other types not implemented")
            }
        }.build().toString()
}
