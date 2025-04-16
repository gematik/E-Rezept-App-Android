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

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.usecase.GetSupportedLanguagesFromXmlUseCase
import org.kodein.di.compose.rememberInstance

class SettingsLanguageScreenController(
    val getSupportedLanguagesFromXmlUseCase: GetSupportedLanguagesFromXmlUseCase
) : Controller() {

    val languageList by lazy {
        getSupportedLanguagesFromXmlUseCase()
    }
}

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
    GA("ga", R.string.language_selection_ga);

    @Composable
    fun mapToName() = stringResource(resource)

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code }
    }
}

@Composable
fun rememberSettingsLanguageScreenController(): SettingsLanguageScreenController {
    val getSupportedLanguagesFromXmlUseCase by rememberInstance<GetSupportedLanguagesFromXmlUseCase>()
    return remember {
        SettingsLanguageScreenController(getSupportedLanguagesFromXmlUseCase)
    }
}
