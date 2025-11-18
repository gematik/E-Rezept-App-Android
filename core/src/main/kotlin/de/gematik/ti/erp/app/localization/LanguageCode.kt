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
import java.util.Locale

/**
 * Enum representing all supported language codes in the application.
 * Each language code maps to its corresponding string resource for display.
 */
enum class LanguageCode(val code: String, val resource: Int) {
    DE("de", R.string.language_selection_de), // default language
    AR("ar", R.string.language_selection_ar),
    BG("bg", R.string.language_selection_bg),
    CS("cs", R.string.language_selection_cs),
    DA("da", R.string.language_selection_da),
    EN("en", R.string.language_selection_en),
    FR("fr", R.string.language_selection_fr),
    IW("iw", R.string.language_selection_he),
    IT("it", R.string.language_selection_it),
    NL("nl", R.string.language_selection_nl),
    PL("pl", R.string.language_selection_pl),
    RO("ro", R.string.language_selection_ro),
    RU("ru", R.string.language_selection_ru),
    TR("tr", R.string.language_selection_tr),
    UK("uk", R.string.language_selection_uk),
    ES("es", R.string.language_selection_es),
    GA("ga", R.string.language_selection_ga)
    ;

    @Composable
    fun mapToName() = stringResource(resource)

    /**
     * Convert language code to Java Locale for TTS and localization
     */
    fun toLocale(): Locale {
        return Locale(code)
    }

    companion object {
        private val ADDITIONAL_TTS_LANGUAGES = mapOf(
            "et" to Locale("et"), // Estonian
            "fi" to Locale("fi"), // Finnish
            "hr" to Locale("hr"), // Croatian
            "hu" to Locale("hu"), // Hungarian
            "pt" to Locale("pt"), // Portuguese
            "sv" to Locale("sv") // Swedish
        )

        fun fromCode(code: String) = entries.firstOrNull { it.code == code }

        fun getLocaleForTTS(code: String): Locale {
            // First check if it's a supported UI language
            fromCode(code)?.let { return it.toLocale() }

            // Then check additional TTS-only languages
            return ADDITIONAL_TTS_LANGUAGES[code] ?: Locale.ENGLISH
        }
    }
}
