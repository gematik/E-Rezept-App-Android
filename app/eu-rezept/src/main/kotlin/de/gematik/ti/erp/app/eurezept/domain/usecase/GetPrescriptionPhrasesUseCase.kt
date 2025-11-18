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

package de.gematik.ti.erp.app.eurezept.domain.usecase

import android.content.Context
import android.content.res.Configuration
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.CountryPhrases
import de.gematik.ti.erp.app.localization.CountryCode
import de.gematik.ti.erp.app.localization.LanguageCode
import java.util.Locale

internal class GetPrescriptionPhrasesUseCase(
    private val context: Context
) {
    operator fun invoke(countryCode: CountryCode): CountryPhrases {
        val supportedLanguages = LanguageCode.entries.map { it.code }.toSet()

        return if (countryCode.languageCode in supportedLanguages) {
            createLocalizedPhrases(countryCode)
        } else {
            createLocalizedPhrases(getFallbackCountryCode())
        }
    }

    private fun createLocalizedPhrases(countryInfo: CountryCode): CountryPhrases {
        val locale = Locale(countryInfo.languageCode)
        val localizedContext = createLocalizedContext(locale)

        return CountryPhrases(
            flagEmoji = countryInfo.flagEmoji,
            redeemPrescriptionPhrase = localizedContext.getString(R.string.prescription_phrase_generic),
            thankYouPhrase = localizedContext.getString(R.string.thank_you_phrase_generic)
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
}
