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

import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import io.github.aakira.napier.Napier

fun UriHandler.openUriWhenValid(uri: String) {
    if (isValidUri(uri)) {
        runCatching {
            openUri(uri)
        }.onFailure { exception ->
            Napier.e(exception) { "URI $uri is not valid: ${exception.message}" }
        }
    }
}

private fun isValidUri(uriString: String): Boolean {
    return try {
        val uri = uriString.toUri()
        uri.scheme != null // can still be an invalid uri schema
    } catch (e: Throwable) {
        Napier.e { "uri $uriString is not valid ${e.stackTraceToString()}" }
        false
    }
}
