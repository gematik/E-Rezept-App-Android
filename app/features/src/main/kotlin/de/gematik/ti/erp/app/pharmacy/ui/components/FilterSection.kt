/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("FunctionName")

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Moped
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

internal fun LazyListScope.FilterSection(
    filter: PharmacyUseCaseData.Filter,
    onSelectFilter: (PharmacyUseCaseData.Filter) -> Unit,
    onClickFilter: () -> Unit
) {
    item {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.search_pharmacies_filter_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier
                    .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
                    .padding(horizontal = PaddingDefaults.Medium),
                textAlign = TextAlign.Start
            )
            FilterButton(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.search_pharmacies_filter_open_now_and_local),
                icon = Icons.Outlined.LocationOn,
                onClick = {
                    onSelectFilter(
                        filter.copy(
                            nearBy = true,
                            openNow = true,
                            deliveryService = false,
                            onlineService = false
                        )
                    )
                }
            )
            FilterButton(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.search_pharmacies_filter_delivery_service),
                icon = Icons.Outlined.Moped,
                onClick = {
                    onSelectFilter(
                        filter.copy(
                            nearBy = true,
                            deliveryService = true,
                            onlineService = false,
                            openNow = false
                        )
                    )
                }
            )
            FilterButton(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.search_pharmacies_filter_online_service),
                icon = Icons.Outlined.LocalShipping,
                onClick = {
                    onSelectFilter(
                        filter.copy(
                            nearBy = false,
                            onlineService = true,
                            deliveryService = false,
                            openNow = false
                        )
                    )
                }
            )
            FilterButton(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.search_pharmacies_filter_by),
                icon = Icons.Outlined.Tune,
                onClick = onClickFilter
            )
        }
    }
}
