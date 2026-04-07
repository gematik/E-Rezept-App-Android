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

@file:Suppress("FunctionName")

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.ui.model.QuickFilter
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

internal fun LazyListScope.FilterSection(
    onSelectFilter: (QuickFilter) -> Unit,
    onClickFilter: () -> Unit
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
                .padding(horizontal = PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.search_pharmacies_popular_filter_header),
                style = AppTheme.typography.h6,
                modifier = Modifier.semanticsHeading(),
                textAlign = TextAlign.Start,
                color = AppTheme.colors.neutral900
            )
            Row(
                modifier = Modifier
                    .clickable(role = Role.Button) { onClickFilter() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                Text(
                    text = stringResource(R.string.search_pharmacies_filter_all),
                    style = AppTheme.typography.body1,
                    color = AppTheme.colors.primary700
                )
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700
                )
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            FilterChip(
                text = stringResource(R.string.search_pharmacies_filter_delivery_service),
                onClick = { onSelectFilter(QuickFilter.DeliveryNearby) }
            )
            FilterChip(
                text = stringResource(R.string.search_pharmacies_filter_online_service),
                onClick = { onSelectFilter(QuickFilter.Online) }
            )
            FilterChip(
                text = stringResource(R.string.search_pharmacies_filter_open_now_and_local),
                onClick = { onSelectFilter(QuickFilter.OpenNowNearby) }
            )
        }
        SpacerSmall()
    }
}

@Preview
@Composable
private fun FilterSectionPreview() {
    PreviewAppTheme {
        LazyColumn {
            FilterSection(
                onSelectFilter = {},
                onClickFilter = {}
            )
        }
    }
}
