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

package de.gematik.ti.erp.app.idp.extension

import de.gematik.ti.erp.app.utils.toPair
import java.net.URI

fun URI.extractRequiredQueryParameter(key: String): String {
    val entries = getQueryPairs()
    val extract = entries.find {
        it?.first == key.lowercase() && it.second.isNotBlank()
    }?.second
    return requireNotNull(extract) {
        "no parameter for key: $key"
    }
}

fun URI.extractNullableQueryParameter(key: String): String? {
    val entries = getQueryPairs()
    val extract = entries.find {
        it?.first == key.lowercase() && it.second.isNotBlank()
    }?.second
    return extract
}

fun URI.getQueryPairs(): List<Pair<String, String>?> {
    val entries = query.split("&").map { queries ->
        queries.split("=", limit = 2).toPair().takeIf {
            it != null && it.first.isNotBlank() && it.second.isNotBlank()
        }
    }
    return entries
}
