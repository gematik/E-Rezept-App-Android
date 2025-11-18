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
 */

package de.gematik.ti.erp.app.eurezept.domain.usecase

import android.content.Context
import android.content.res.Configuration
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.CountrySpecificLabels
import de.gematik.ti.erp.app.localization.CountryCode
import de.gematik.ti.erp.app.localization.LanguageCode
import java.util.Locale

class GetCountryLocaleRedemptionCodeUseCase(
    private val context: Context
) {
    operator fun invoke(countryCode: String?): CountrySpecificLabels {
        val countryFromCode = CountryCode.fromCode(countryCode)
        val supportedLanguages = LanguageCode.entries.map { it.code }.toSet()

        return if (countryFromCode != null && countryFromCode.languageCode in supportedLanguages) {
            createLocalizedLabels(countryFromCode)
        } else {
            createLocalizedLabels(getFallbackCountryCode())
        }
    }

    private fun createLocalizedLabels(countryWithSupportedLanguage: CountryCode): CountrySpecificLabels {
        val locale = Locale(countryWithSupportedLanguage.languageCode)
        val contextForLocale = createLocalizedContext(locale)

        return CountrySpecificLabels(
            codeLabel = contextForLocale.getString(R.string.eu_redemption_code_label),
            insuranceNumberLabel = contextForLocale.getString(R.string.eu_redemption_insurance_number_label)
        )
    }

    private fun getFallbackCountryCode(): CountryCode {
        return CountryCode.UK
    }

    private fun createLocalizedContext(locale: Locale): Context {
        return context.createConfigurationContext(
            Configuration(context.resources.configuration).apply {
                setLocale(locale)
            }
        )
    }

    fun getLocaleForTTS(countryCode: String?): Locale {
        val countryFromCode = CountryCode.fromCode(countryCode) ?: return Locale.ENGLISH
        return LanguageCode.getLocaleForTTS(countryFromCode.languageCode)
    }
}
