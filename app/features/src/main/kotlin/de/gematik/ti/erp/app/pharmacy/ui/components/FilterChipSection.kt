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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun FilterChipSection(
    modifier: Modifier = Modifier,
    filter: PharmacyUseCaseData.Filter,
    rowState: LazyListState = rememberLazyListState(),
    onFilterToggle: (Boolean, FilterType) -> Unit,
    onRemoveOnSiteFeature: (PharmacyOnSiteFeatureOption) -> Unit = {},
    onRemoveAvailableService: (PharmacyFilterServiceOption) -> Unit = {},
    onClickFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaddingDefaults.Small)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpacerSmall()
        AssistChip(
            onClick = onClickFilter,
            label = {
                Text(
                    text = stringResource(R.string.search_pharmacies_filter),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral700,
                    modifier = Modifier.padding(vertical = PaddingDefaults.Small)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(SizeDefaults.doubleQuarter),
                    tint = AppTheme.colors.neutral700
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = AppTheme.colors.neutral000,
                labelColor = AppTheme.colors.primary900,
                leadingIconContentColor = AppTheme.colors.primary900
            ),
            border = AssistChipDefaults.assistChipBorder(
                enabled = true,
                borderColor = AppTheme.colors.primary900,
                borderWidth = SizeDefaults.eighth
            ),
            shape = CircleShape
        )
        if (filter.isAnySet()) {
            SpacerSmall()
            val contentDescription = stringResource(id = R.string.pharmacy_search_active_filter)
            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (filter.nearBy) {
                    item {
                        ActiveFilterChip(
                            text = stringResource(R.string.search_pharmacies_filter_nearby),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onFilterToggle(false, FilterType.NEARBY) }
                    }
                }
                if (filter.openNow) {
                    item {
                        ActiveFilterChip(
                            text = stringResource(R.string.search_pharmacies_filter_open_now),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onFilterToggle(false, FilterType.OPEN_NOW) }
                    }
                }
                if (filter.deliveryService) {
                    item {
                        ActiveFilterChip(
                            text = stringResource(R.string.search_pharmacies_filter_delivery_service),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onFilterToggle(false, FilterType.DELIVERY_SERVICE) }
                    }
                }
                if (filter.onlineService) {
                    item {
                        ActiveFilterChip(
                            text = stringResource(R.string.search_pharmacies_filter_online_service),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onFilterToggle(false, FilterType.ONLINE_SERVICE) }
                    }
                }
                if (filter.pickup) {
                    item {
                        ActiveFilterChip(
                            text = stringResource(R.string.search_pharmacies_filter_pickup),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onFilterToggle(false, FilterType.PICKUP) }
                    }
                }
                if (filter.recentlyUsed) {
                    item {
                        ActiveFilterChip(
                            text = stringResource(R.string.search_pharmacies_filter_recently_used),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onFilterToggle(false, FilterType.RECENTLY_USED) }
                    }
                }
                filter.onSiteFeatures.forEach { code ->
                    val option = PharmacyOnSiteFeatureOption.entries.find { it.code == code }
                        ?: return@forEach
                    item {
                        ActiveFilterChip(
                            text = stringResource(option.label),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onRemoveOnSiteFeature(option) }
                    }
                }
                filter.availableServices.forEach { code ->
                    val option = PharmacyFilterServiceOption.entries.find { it.code == code }
                        ?: return@forEach
                    item {
                        ActiveFilterChip(
                            text = stringResource(option.title),
                            modifier = Modifier.semantics { stateDescription = contentDescription }
                        ) { onRemoveAvailableService(option) }
                    }
                }
                item { SpacerMedium() }
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    text: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    InputChip(
        selected = true,
        onClick = onClose,
        label = {
            Text(
                text = text.trim().trimEnd('*').trimEnd(),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.primary900,
                modifier = modifier.padding(vertical = PaddingDefaults.Small)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                tint = AppTheme.colors.primary900,
                modifier = modifier.size(SizeDefaults.double)
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = AppTheme.colors.primary200,
            selectedLabelColor = AppTheme.colors.primary900,
            selectedTrailingIconColor = AppTheme.colors.primary900
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = true,
            selectedBorderColor = AppTheme.colors.primary900,
            selectedBorderWidth = SizeDefaults.eighth
        ),
        shape = CircleShape
    )
}

@LightDarkPreview
@Composable
fun FilterChipSectionPreview() {
    PreviewAppTheme {
        FilterChipSection(
            filter = PharmacyUseCaseData.Filter(
                openNow = true,
                pickup = true,
                deliveryService = true,
                onlineService = true,
                nearBy = true
            ),
            onFilterToggle = { _, _ -> },
            onClickFilter = {}
        )
    }
}
