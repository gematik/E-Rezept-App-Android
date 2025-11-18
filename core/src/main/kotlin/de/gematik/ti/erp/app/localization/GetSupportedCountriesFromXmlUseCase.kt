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

package de.gematik.ti.erp.app.localization

import io.github.aakira.napier.Napier
import org.xmlpull.v1.XmlPullParser

class GetSupportedCountriesFromXmlUseCase(
    private val parser: XmlResourceParserWrapper
) {
    operator fun invoke(): List<CountryCode> {
        return parseCountries(parser)
    }
}

@Suppress("NestedBlockDepth")
private fun parseCountries(
    parser: XmlResourceParserWrapper
): List<CountryCode> {
    val countryCodes = mutableListOf<CountryCode>()
    try {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (isCountryTag(parser)) {
                parseCountryCode(parser)?.let { countryCodes.add(it) }
            }
        }
    } catch (e: Exception) {
        Napier.e("Error while parsing countries_config.xml: $e")
    }
    return countryCodes
}

private fun isCountryTag(
    parser: XmlResourceParserWrapper
): Boolean {
    return parser.getEventType() == XmlPullParser.START_TAG && parser.name() == "country"
}

private fun parseCountryCode(
    parser: XmlResourceParserWrapper
): CountryCode? {
    val country = parser.getAttributeValue() ?: return null
    return CountryCode.fromCode(country)
}
