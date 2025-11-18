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
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.utils.uistate.UiState

data class EuCountrySelectionPreviewData(
    val uiState: UiState<List<Country>>,
    val searchQuery: String,
    val detectedCountryNotInSupportedList: Boolean = false,
    val filteredCountries: List<Country>,
    val isKeyboardOpen: Boolean
)

class EuCountrySelectionPreviewParameterProvider : PreviewParameterProvider<EuCountrySelectionPreviewData> {

    private val sampleCountries = listOf(
        Country("Spanien", "es", "🇪🇸"),
        Country("Frankreich", "fr", "🇫🇷"),
        Country("Italien", "it", "🇮🇹"),
        Country("Österreich", "at", "🇦🇹"),
        Country("Schweiz", "ch", "🇨🇭"),
        Country("Niederlande", "nl", "🇳🇱"),
        Country("Belgien", "be", "🇧🇪"),
        Country("Polen", "pl", "🇵🇱"),
        Country("Tschechische Republik", "cz", "🇨🇿"),
        Country("Portugal", "pt", "🇵🇹"),
        Country("Griechenland", "gr", "🇬🇷"),
        Country("Schweden", "se", "🇸🇪"),
        Country("Dänemark", "dk", "🇩🇰"),
        Country("Norwegen", "no", "🇳🇴")
    )

    private val searchResultsSpain = listOf(
        Country("Spanien", "es", "🇪🇸")
    )

    override val values: Sequence<EuCountrySelectionPreviewData>
        get() = sequenceOf(

            // Error state - no search
            EuCountrySelectionPreviewData(
                uiState = UiState.Error(Exception("Network connection failed")),
                searchQuery = "",
                filteredCountries = emptyList(),
                isKeyboardOpen = false
            ),

            // Content state - list, no search
            EuCountrySelectionPreviewData(
                uiState = UiState.Data(sampleCountries),
                searchQuery = "",
                filteredCountries = sampleCountries,
                isKeyboardOpen = false
            ),

            // Content state - search with results
            EuCountrySelectionPreviewData(
                uiState = UiState.Data(sampleCountries),
                searchQuery = "",
                filteredCountries = searchResultsSpain,
                isKeyboardOpen = true
            ),

            // DetectedCountryNotInSupportedList state - without search
            EuCountrySelectionPreviewData(
                uiState = UiState.Data(sampleCountries),
                searchQuery = "",
                detectedCountryNotInSupportedList = true,
                filteredCountries = emptyList(),
                isKeyboardOpen = false
            ),
            // Content state - search with results
            EuCountrySelectionPreviewData(
                uiState = UiState.Data(sampleCountries),
                searchQuery = "",
                filteredCountries = searchResultsSpain,
                isKeyboardOpen = true
            ),
            EuCountrySelectionPreviewData(
                uiState = UiState.Empty(),
                searchQuery = "",
                filteredCountries = searchResultsSpain,
                isKeyboardOpen = true
            ),
            EuCountrySelectionPreviewData(
                uiState = UiState.Loading(),
                searchQuery = "",
                filteredCountries = searchResultsSpain,
                isKeyboardOpen = true
            )
        )
}
