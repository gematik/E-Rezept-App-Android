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

package de.gematik.ti.erp.app.eurezept.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.domain.model.CountryPhrases
import de.gematik.ti.erp.app.localization.CountryCode

class EuInstructionsPreviewParameterProvider : PreviewParameterProvider<EuInstructionsPreviewData> {

    override val values = sequenceOf(
        EuInstructionsPreviewData(
            name = "France",
            countryPhrases = createPhrasesFromCountryCode(CountryCode.FR)
        ),
        EuInstructionsPreviewData(
            name = "Spain",
            countryPhrases = createPhrasesFromCountryCode(CountryCode.ES)
        ),
        EuInstructionsPreviewData(
            name = "Liechtenstein",
            countryPhrases = createPhrasesFromCountryCode(CountryCode.LI)
        ),
        EuInstructionsPreviewData(
            name = "Unsupported Country - Fallback to English",
            countryPhrases = createPhrasesFromCountryCode(CountryCode.UK)
        )
    )

    // Mirrors same logic in GetPrescriptionPhrasesUseCase. Simplified for previews
    private fun createPhrasesFromCountryCode(countryCode: CountryCode): CountryPhrases {
        return CountryPhrases(
            flagEmoji = countryCode.flagEmoji,
            redeemPrescriptionPhrase = getLocalizedRedeemPhrase(countryCode),
            thankYouPhrase = getLocalizedThankYouPhrase(countryCode)
        )
    }

    private fun getLocalizedRedeemPhrase(countryCode: CountryCode): String {
        return when (countryCode.languageCode) {
            "de" -> "Ich möchte ein deutsches Rezept einlösen"
            "fr" -> "Je voudrais échanger une ordonnance allemande"
            "es" -> "Quiero canjear una receta alemana"
            else -> "I would like to redeem a German prescription"
        }
    }

    private fun getLocalizedThankYouPhrase(countryCode: CountryCode): String {
        return when (countryCode.languageCode) {
            "de" -> "Danke"
            "fr" -> "Merci"
            "es" -> "Gracias"
            else -> "Thank you"
        }
    }
}

data class EuInstructionsPreviewData(
    val name: String,
    val countryPhrases: CountryPhrases
)
