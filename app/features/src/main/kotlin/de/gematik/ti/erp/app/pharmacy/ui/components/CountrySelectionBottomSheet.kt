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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToFlag
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToName
import de.gematik.ti.erp.app.eurezept.ui.screens.CountryListItem
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import java.util.Locale

fun getAvailableShippingCountries(): List<Country> {
    val priorityCodes = listOf("DE", "AT", "CH")
    val isoCodes = Locale.getISOCountries()
    val priorityList = priorityCodes.map { code ->
        Country(
            name = countryCodeToName(code),
            code = code,
            flagEmoji = countryCodeToFlag(code)
        )
    }
    val otherList = (isoCodes.map { it.uppercase() } - priorityCodes.toSet())
        .map { code ->
            Country(
                name = countryCodeToName(code),
                code = code,
                flagEmoji = countryCodeToFlag(code)
            )
        }
        .filter { it.name.isNotBlank() }
        .sortedBy { it.name }
    return priorityList + otherList
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountrySelectionBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = AppTheme.colors.neutral000,
            dragHandle = {
                DragHandle(color = AppTheme.colors.neutral700)
            }
        ) {
            CountrySelectionContent(
                onDismissRequest = onDismissRequest,
                onCountrySelected = {
                    onCountrySelected(it)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
fun CountrySelectionContent(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onCountrySelected: (Country) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allCountries = remember { getAvailableShippingCountries() }
    val filteredCountries = remember(searchQuery, allCountries) {
        if (searchQuery.isBlank()) {
            allCountries
        } else {
            allCountries.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(bottom = PaddingDefaults.Large)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.eu_country_selection_country_select_heading),
                style = AppTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.neutral900
            )
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = AppTheme.colors.neutral700
                )
            }
        }

        SpacerSmall()

        ErezeptOutlineText(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = stringResource(R.string.eu_search_countries),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral600
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AppTheme.colors.neutral600
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.a11y_deleted_text),
                            tint = AppTheme.colors.neutral600
                        )
                    }
                }
            } else {
                null
            }
        )

        SpacerMedium()

        if (filteredCountries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PaddingDefaults.XLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.eu_country_not_found_message),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral700,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredCountries, key = { it.code + it.name }) { country ->
                    CountryListItem(
                        country = country,
                        onClick = {
                            onCountrySelected(country)
                        }
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun CountrySelectionContentPreview() {
    PreviewAppTheme {
        CountrySelectionContent(
            onDismissRequest = {},
            onCountrySelected = {}
        )
    }
}
