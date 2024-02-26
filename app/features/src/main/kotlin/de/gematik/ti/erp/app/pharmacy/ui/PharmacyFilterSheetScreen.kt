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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.NearByFilterArgument
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.ShowStartSearchButton
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController.FilterType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Chip
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar

class PharmacyFilterSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: PharmacyGraphController
) : PharmacyBottomSheetScreen() {
    @Composable
    override fun Content() {
        val snackbar = LocalSnackbar.current

        val filter by graphController.filterState

        val isNearbyFilter = remember { navBackStackEntry.getNearbyFilter() }

        val showStartButton = remember { navBackStackEntry.getIsStartSearchButtonShown() }

        PharmacyFilterSheetScreenContent(
            filter = filter,
            isNearbyFilter = isNearbyFilter,
            showStartButton = showStartButton,
            onClickFilter = {
                graphController.updateFilter(type = it)
            },
            showToDo = {
                snackbar.show("TODO start search with filters")
            },
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun PharmacyFilterSheetScreenContent(
    filter: PharmacyUseCaseData.Filter,
    isNearbyFilter: Boolean,
    showStartButton: Boolean,
    onClickFilter: (FilterType) -> Unit,
    showToDo: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.padding(PaddingDefaults.Medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.search_pharmacies_filter_header),
                style = AppTheme.typography.h5
            )
            IconButton(
                modifier = Modifier
                    .background(AppTheme.colors.neutral200, CircleShape),
                onClick = onBack
            ) {
                Icon(Icons.Rounded.Close, null)
            }
        }
        SpacerMedium()
        Column(modifier = Modifier.verticalScroll(rememberScrollState(), true)) {
            FlowRow(
                mainAxisSpacing = PaddingDefaults.Small,
                crossAxisSpacing = PaddingDefaults.Small
            ) {
                if (isNearbyFilter) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_nearby),
                        closable = false,
                        checked = filter.nearBy
                    ) {
                        onClickFilter(FilterType.NEARBY)
                    }
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_open_now),
                    closable = false,
                    checked = filter.openNow
                ) {
                    onClickFilter(FilterType.OPEN_NOW)
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_delivery_service),
                    closable = false,
                    checked = filter.deliveryService
                ) {
                    onClickFilter(FilterType.DELIVERY_SERVICE)
                    if (it) {
                        onClickFilter(FilterType.NEARBY)
                    }
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_online_service),
                    closable = false,
                    checked = filter.onlineService
                ) {
                    onClickFilter(FilterType.ONLINE_SERVICE)
                }
            }
            SpacerMedium()
            if (showStartButton) {
                SpacerLarge()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PrimaryButtonSmall(
                        onClick = {
                            onBack()
                            showToDo()
                        }
                    ) {
                        Text(stringResource(R.string.search_pharmacies_start_search))
                    }
                }
                SpacerLarge()
            }
        }
    }
}

private fun NavBackStackEntry.getNearbyFilter(): Boolean =
    arguments?.getBoolean(NearByFilterArgument) ?: false

private fun NavBackStackEntry.getIsStartSearchButtonShown(): Boolean =
    arguments?.getBoolean(ShowStartSearchButton) ?: false

@LightDarkPreview
@Composable
fun PharmacyFilterSheetScreenPreview() {
    PreviewAppTheme {
        PharmacyFilterSheetScreenContent(
            filter = PharmacyUseCaseData.Filter(
                nearBy = true,
                openNow = false,
                deliveryService = false,
                onlineService = true
            ),
            isNearbyFilter = true,
            showStartButton = true,
            onClickFilter = {},
            showToDo = {},
            onBack = {}
        )
    }
}
