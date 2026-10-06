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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
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
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCallingCodeBottomSheet(
    visible: Boolean,
    selectedCallingCode: CountryCallingCode,
    onDismissRequest: () -> Unit,
    onCallingCodeSelected: (CountryCallingCode) -> Unit,
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
            CountryCallingCodeSelectionContent(
                selectedCallingCode = selectedCallingCode,
                onDismissRequest = onDismissRequest,
                onCallingCodeSelected = {
                    onCallingCodeSelected(it)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
fun CountryCallingCodeSelectionContent(
    selectedCallingCode: CountryCallingCode,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onCallingCodeSelected: (CountryCallingCode) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allCallingCodes = remember { getAvailableCountryCallingCodes() }
    val filteredCallingCodes = remember(searchQuery, allCallingCodes) {
        if (searchQuery.isBlank()) {
            allCallingCodes
        } else {
            val query = searchQuery.trim().removePrefix("+")
            allCallingCodes.filter {
                it.countryName.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.countryCode.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.callingCode.toString().contains(query)
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
                text = stringResource(R.string.edit_shipping_contact_calling_code_title),
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
                    text = stringResource(R.string.edit_shipping_contact_calling_code_search),
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

        SpacerSmall()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            items(
                items = filteredCallingCodes,
                key = { "${it.countryCode}_${it.callingCode}" }
            ) { item ->
                val isSelected = item.countryCode == selectedCallingCode.countryCode &&
                    item.callingCode == selectedCallingCode.callingCode
                CountryCallingCodeListItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onCallingCodeSelected(item) }
                )
            }
        }
    }
}

@Composable
fun CountryCallingCodeListItem(
    item: CountryCallingCode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PaddingDefaults.Small, horizontal = PaddingDefaults.Tiny),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Text(
            text = item.flagEmoji,
            style = AppTheme.typography.h5
        )
        Text(
            text = "${item.countryName} (${item.countryCode})",
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral900,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = item.displayCallingCode,
            style = AppTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) AppTheme.colors.primary700 else AppTheme.colors.neutral700
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AppTheme.colors.primary700
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CountryCallingCodeSelectionPreview() {
    PreviewAppTheme {
        CountryCallingCodeSelectionContent(
            selectedCallingCode = CountryCallingCode("DE", "Deutschland", 49, "🇩🇪"),
            onDismissRequest = {},
            onCallingCodeSelected = {}
        )
    }
}
