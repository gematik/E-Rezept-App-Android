/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.utils.extensions

import android.net.Uri
import androidx.compose.ui.platform.UriHandler
import io.github.aakira.napier.Napier

fun UriHandler.openUriWhenValid(uri: String) {
    if (isValidUri(uri)) {
        openUri(uri)
    }
}

private fun isValidUri(uriString: String): Boolean {
    return try {
        val uri = Uri.parse(uriString)
        uri != null && uri.scheme != null
    } catch (e: Throwable) {
        Napier.e { "uri $uriString is not valid ${e.stackTraceToString()}" }
        false
    }
}
