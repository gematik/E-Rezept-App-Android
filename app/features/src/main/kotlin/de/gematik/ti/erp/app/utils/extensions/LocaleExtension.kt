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

package de.gematik.ti.erp.app.utils.extensions

import java.util.Locale

private fun getLanguageCode(): String {
    Locale.getDefault().let {
        return it.language.trim()
    }
}

fun getUriDataTerms(): String {
    return when (getLanguageCode()) {
        "de" -> URI_DATA_TERMS_DE
        "en" -> URI_DATA_TERMS_EN
        "pl" -> URI_DATA_TERMS_PL
        "nl" -> URI_DATA_TERMS_NL
        else -> URI_DATA_TERMS_DE
    }
}

private const val URI_DATA_TERMS_DE = "file:///android_asset/data_terms_de.html"
private const val URI_DATA_TERMS_EN = "file:///android_asset/data_terms_en.html"
private const val URI_DATA_TERMS_PL = "file:///android_asset/data_terms_pl.html"
private const val URI_DATA_TERMS_NL = "file:///android_asset/data_terms_nl.html"
