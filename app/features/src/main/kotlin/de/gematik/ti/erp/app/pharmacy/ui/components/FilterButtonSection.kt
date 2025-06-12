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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.Chip
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun FilterButtonSection(
    modifier: Modifier = Modifier,
    filter: PharmacyUseCaseData.Filter,
    rowState: LazyListState = rememberLazyListState(),
    onClickChip: (Boolean, FilterType) -> Unit,
    onClickFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaddingDefaults.Medium)
            .then(modifier)
    ) {
        SpacerMedium()
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(SizeDefaults.one))
                .clickable {
                    onClickFilter()
                }
                .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(SizeDefaults.one))
                .padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.threeQuarter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Tune, null, Modifier.size(SizeDefaults.double), tint = AppTheme.colors.primary700)
            SpacerSmall()
            Text(
                stringResource(R.string.search_pharmacies_filter),
                style = AppTheme.typography.subtitle2,
                color = AppTheme.colors.primary700
            )
        }
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
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_nearby),
                            modifier = Modifier.semantics { stateDescription = contentDescription },
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(it, FilterType.NEARBY)
                        }
                    }
                }
                if (filter.openNow) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_open_now),
                            modifier = Modifier.semantics { stateDescription = contentDescription },
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(it, FilterType.OPEN_NOW)
                        }
                    }
                }
                if (filter.deliveryService) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_delivery_service),
                            modifier = Modifier.semantics { stateDescription = contentDescription },
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(it, FilterType.DELIVERY_SERVICE)
                        }
                    }
                }
                if (filter.onlineService) {
                    item {
                        Chip(
                            stringResource(R.string.search_pharmacies_filter_online_service),
                            modifier = Modifier.semantics { stateDescription = contentDescription },
                            closable = true,
                            checked = false
                        ) {
                            onClickChip(it, FilterType.ONLINE_SERVICE)
                        }
                    }
                }
                item {
                    SpacerMedium()
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun FilterButtonSectionPreview() {
    PreviewAppTheme {
        FilterButtonSection(
            filter = PharmacyUseCaseData.Filter(
                openNow = true,
                deliveryService = true,
                onlineService = true,
                nearBy = true
            ),
            onClickChip = { _, _ ->
            },
            onClickFilter = {}
        )
    }
}
