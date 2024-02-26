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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData.OverviewPharmacy
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.ui.components.FavouritePharmacies
import de.gematik.ti.erp.app.pharmacy.ui.components.FilterSection
import de.gematik.ti.erp.app.pharmacy.ui.components.MapsTile
import de.gematik.ti.erp.app.pharmacy.ui.components.MapsTitle
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacySearchButton
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.extensions.capitalizeFirstChar
import de.gematik.ti.erp.app.utils.extensions.isGooglePlayServiceAvailable
import kotlinx.datetime.Instant

class PharmacyStartScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: PharmacyGraphController
) : PharmacyScreen() {
    @Composable
    override fun Content() {
        val snackbar = LocalSnackbar.current

        val context = LocalContext.current

        val filter by graphController.filterState

        val favouritePharmacies by graphController.favouritePharmaciesState

        val isGooglePlayServicesAvailable = context.isGooglePlayServiceAvailable()

        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            graphController.init()
            graphController.updateIsDirectRedeemEnabledOnFilter()
        }

        PharmacyStartScreenContent(
            filter = filter,
            pharmacies = favouritePharmacies,
            listState = listState,
            isGooglePlayServicesAvailable = isGooglePlayServicesAvailable,
            onBack = { navController.popBackStack() },
            onClickPharmacySearch = {
                snackbar.show("TODO open pharmacy search")
            },
            onClickMapsSearch = {
                snackbar.show("TODO open pharmacy maps")
            },
            onClickQuickFilterSearch = {
                snackbar.show("TODO open pharmacy search with filters")
            },
            onClickFilter = {
                navController.navigate(
                    PharmacyRoutes.PharmacyFilterSheetScreen.path(
                        showNearbyFilter = true,
                        showButton = true
                    )
                )
            },
            onClickFavouritePharmacy = {
                snackbar.show("TODO open pharmacy detail bottomsheet")
            }
        )
    }
}

@Composable
private fun PharmacyStartScreenContent(
    filter: PharmacyUseCaseData.Filter = PharmacyUseCaseData.Filter(),
    pharmacies: List<OverviewPharmacy> = emptyList(),
    listState: LazyListState,
    isGooglePlayServicesAvailable: Boolean = true,
    onClickQuickFilterSearch: (PharmacyUseCaseData.Filter) -> Unit,
    onClickFavouritePharmacy: (OverviewPharmacy) -> Unit,
    onClickPharmacySearch: () -> Unit,
    onClickMapsSearch: () -> Unit,
    onClickFilter: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.redeem_header).capitalizeFirstChar(),
        listState = listState,
        actions = {},
        onBack = onBack,
        content = { paddingValues ->
            PharmacyStartScreenBody(
                paddingValues = paddingValues,
                filter = filter,
                pharmacies = pharmacies,
                listState = listState,
                isGooglePlayServicesAvailable = isGooglePlayServicesAvailable,
                onClickPharmacySearch = onClickPharmacySearch,
                onClickMapsSearch = onClickMapsSearch,
                onClickQuickFilterSearch = onClickQuickFilterSearch,
                onClickFilter = onClickFilter,
                onClickFavouritePharmacy = onClickFavouritePharmacy
            )
        }
    )
}

@Composable
private fun PharmacyStartScreenBody(
    paddingValues: PaddingValues,
    filter: PharmacyUseCaseData.Filter,
    pharmacies: List<OverviewPharmacy>,
    listState: LazyListState,
    isGooglePlayServicesAvailable: Boolean,
    onClickPharmacySearch: () -> Unit,
    onClickMapsSearch: () -> Unit,
    onClickQuickFilterSearch: (PharmacyUseCaseData.Filter) -> Unit,
    onClickFilter: () -> Unit,
    onClickFavouritePharmacy: (OverviewPharmacy) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        contentPadding = paddingValues
    ) {
        item {
            SpacerLarge()
        }
        PharmacySearchButton(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium),
            onStartSearch = onClickPharmacySearch
        )
        if (isGooglePlayServicesAvailable) {
            MapsTitle()
            MapsTile { onClickMapsSearch() }
        }
        FilterSection(
            filter = filter,
            onClickFilter = onClickFilter,
            onSelectFilter = onClickQuickFilterSearch
        )
        if (pharmacies.isNotEmpty()) {
            FavouritePharmacies(
                modifier = Modifier
                    .padding(horizontal = PaddingDefaults.Medium)
                    .fillMaxWidth(),
                pharmacies = pharmacies
            ) {
                onClickFavouritePharmacy(it)
            }
            item {
                SpacerLarge()
            }
        }
    }
}

@LightDarkPreview
@Composable
fun PharmacyStartScreenPreview() {
    val time = Instant.parse("2022-01-01T00:00:00Z")
    PreviewAppTheme {
        Column {
            PharmacyStartScreenContent(
                listState = rememberLazyListState(),
                filter = PharmacyUseCaseData.Filter(),
                pharmacies = listOf(
                    OverviewPharmacy(
                        lastUsed = time,
                        isFavorite = false,
                        usageCount = 1,
                        telematikId = "123456789",
                        pharmacyName = "Berlin Apotheke",
                        address = "BerlinStr, 12345 Berlin"
                    ),
                    OverviewPharmacy(
                        lastUsed = time,
                        isFavorite = true,
                        usageCount = 1,
                        telematikId = "123456788",
                        pharmacyName = "Stuttgart Apotheke",
                        address = "StuttgartStr, 12345 Stuttgart"
                    )
                ),
                isGooglePlayServicesAvailable = true,
                onClickQuickFilterSearch = {},
                onClickFavouritePharmacy = {},
                onClickPharmacySearch = {},
                onClickMapsSearch = {},
                onClickFilter = {},
                onBack = {}
            )
        }
    }
}
