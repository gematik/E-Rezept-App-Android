/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.settings.usecase

import android.content.res.Resources
import de.gematik.ti.erp.app.settings.presentation.LanguageCode
import io.github.aakira.napier.Napier
import org.xmlpull.v1.XmlPullParser

class GetSupportedLanguagesFromXmlUseCase(
    private val parser: XmlResourceParserWrapper,
    val resources: Resources
) {
    operator fun invoke(): List<LanguageCode> {
        val languageCodes = parseLanguages(parser)
        return sortLanguageCodes(languageCodes, resources)
    }
}

@Suppress("NestedBlockDepth")
private fun parseLanguages(
    parser: XmlResourceParserWrapper
): List<LanguageCode> {
    val languageCodes = mutableListOf<LanguageCode>()
    try {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (isLocaleTag(parser)) {
                parseLanguageCode(parser)?.let { languageCodes.add(it) }
            }
        }
    } catch (e: Exception) {
        Napier.e("Error while parsing locales_config.xml: $e")
    }
    return languageCodes
}

private fun isLocaleTag(
    parser: XmlResourceParserWrapper
): Boolean {
    return parser.getEventType() == XmlPullParser.START_TAG && parser.name() == "locale"
}

private fun parseLanguageCode(
    parser: XmlResourceParserWrapper
): LanguageCode? {
    val language = parser.getAttributeValue() ?: return null
    return LanguageCode.fromCode(language)
}

private fun sortLanguageCodes(
    languageCodes: List<LanguageCode>,
    resources: Resources
): List<LanguageCode> {
    val sortedCodes = languageCodes
        .filterNot { it == LanguageCode.DE } // Remove German temporarily
        .sortedBy { resources.getString(it.resource) }
    return listOf(LanguageCode.DE) + sortedCodes // Add German at the beginning, so it's always top
}
