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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R

enum class CountryCode(
    val code: String,
    val flagEmoji: String,
    val languageCode: String,
    val resource: Int
) {
    AT("at", "🇦🇹", "de", R.string.country_selection_at),
    BE("be", "🇧🇪", "fr", R.string.country_selection_be),
    CZ("cz", "🇨🇿", "cs", R.string.country_selection_cz),
    DE("de", "🇩🇪", "de", R.string.country_selection_de),
    DK("dk", "🇩🇰", "da", R.string.country_selection_dk),
    EE("ee", "🇪🇪", "et", R.string.country_selection_ee),
    FI("fi", "🇫🇮", "fi", R.string.country_selection_fi),
    FR("fr", "🇫🇷", "fr", R.string.country_selection_fr),
    HR("hr", "🇭🇷", "hr", R.string.country_selection_hr),
    HU("hu", "🇭🇺", "hu", R.string.country_selection_hu),
    IT("it", "🇮🇹", "it", R.string.country_selection_it),
    LU("lu", "🇱🇺", "fr", R.string.country_selection_lu),
    NL("nl", "🇳🇱", "nl", R.string.country_selection_nl),
    PL("pl", "🇵🇱", "pl", R.string.country_selection_pl),
    PT("pt", "🇵🇹", "pt", R.string.country_selection_pt),
    SE("se", "🇸🇪", "sv", R.string.country_selection_se),
    ES("es", "🇪🇸", "es", R.string.country_selection_es),
    LI("li", "🇱🇮", "de", R.string.country_selection_li),
    UK("uk", "🇬🇧", "en", R.string.country_selection_uk)
    ;

    @Composable
    fun mapToName() = stringResource(resource)

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull {
            it.code.equals(code, ignoreCase = true)
        }
    }
}
