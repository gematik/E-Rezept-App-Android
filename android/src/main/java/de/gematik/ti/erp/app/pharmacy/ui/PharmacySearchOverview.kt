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

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Moped
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackPharmacySearchPopUps
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ModalBottomSheet
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.launch

private const val LastUsedPharmaciesListLength = 5

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PharmacyOverviewScreen(
    isNestedNavigation: Boolean,
    orderState: PharmacyOrderState,
    navController: NavHostController,
    onBack: () -> Unit,
    onStartSearch: () -> Unit,
    onShowMaps: () -> Unit,
    filter: PharmacyUseCaseData.Filter,
    onFilterChange: (PharmacyUseCaseData.Filter) -> Unit,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberPharmacySheetState()

    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            analytics.trackPharmacySearchPopUps(sheetState.content)
        } else {
            analytics.onPopUpClosed()
            val route = Uri.parse(navController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
        }
    }

    Box {
        AnimatedElevationScaffold(
            modifier = Modifier.testTag(TestTag.PharmacySearch.OverviewScreen),
            listState = listState,
            topBarTitle = stringResource(R.string.redeem_header),
            navigationMode = if (isNestedNavigation) NavigationBarMode.Back else NavigationBarMode.Close,
            onBack = onBack
        ) {
            val pharmacySearchController = rememberPharmacySearchController()
            OverviewContent(
                onSelectPharmacy = {
                    sheetState.show(PharmacySearchSheetContentState.PharmacySelected(it))
                },
                listState = listState,
                onFilterChange = onFilterChange,
                searchFilter = filter,
                onStartSearch = onStartSearch,
                pharmacySearchController = pharmacySearchController,
                onShowFilter = {
                    sheetState.show(PharmacySearchSheetContentState.FilterSelected())
                },
                onShowMaps = onShowMaps
            )
        }

        ModalBottomSheet(
            sheetState = sheetState,
            sheetContent = {
                when (sheetState.content) {
                    is PharmacySearchSheetContentState.FilterSelected ->
                        FilterSheetContent(
                            modifier = Modifier.navigationBarsPadding(),
                            filter = filter,
                            onClickChip = onFilterChange,
                            onClickClose = { scope.launch { sheetState.animateTo(ModalBottomSheetValue.Hidden) } },
                            extraContent = {
                                SpacerLarge()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    PrimaryButtonSmall(
                                        onClick = {
                                            scope.launch { sheetState.animateTo(ModalBottomSheetValue.Hidden) }
                                            onStartSearch()
                                        }
                                    ) {
                                        Text(stringResource(R.string.search_pharmacies_start_search))
                                    }
                                }
                                SpacerLarge()
                            }
                        )

                    is PharmacySearchSheetContentState.PharmacySelected ->
                        PharmacyBottomSheetDetails(
                            orderState = orderState,
                            pharmacy = (sheetState.content as PharmacySearchSheetContentState.PharmacySelected)
                                .pharmacy,
                            onClickOrder = { pharmacy, orderOption ->
                                onSelectPharmacy(pharmacy, orderOption)
                            }
                        )
                }
            },
            sheetShape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) }
        )
    }
}

@Composable
private fun OverviewContent(
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    listState: LazyListState,
    searchFilter: PharmacyUseCaseData.Filter,
    onFilterChange: (PharmacyUseCaseData.Filter) -> Unit,
    onStartSearch: () -> Unit,
    pharmacySearchController: PharmacySearchController,
    onShowFilter: () -> Unit,
    onShowMaps: () -> Unit
) {
    val pharmacySearchState by pharmacySearchController.pharmacySearchOverviewState
    val overviewPharmacyList = pharmacySearchState.overviewPharmacies

    val contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
        .add(WindowInsets(top = PaddingDefaults.Medium, bottom = PaddingDefaults.Medium)).asPaddingValues()

    val context = LocalContext.current
    val isGoogleApiAvailable by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.PharmacySearch.OverviewContent),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPadding
    ) {
        item {
            PharmacySearchButton(
                modifier = Modifier
                    .padding(horizontal = PaddingDefaults.Medium)
                    .testTag(TestTag.PharmacySearch.TextSearchButton)
            ) {
                onFilterChange(
                    searchFilter.copy(
                        onlineService = false,
                        deliveryService = false,
                        openNow = false
                    )
                )
                onStartSearch()
            }
        }
        if (isGoogleApiAvailable) {
            item {
                MapsSection(onShowMaps = onShowMaps)
            }
        }
        item {
            FilterSection(
                filter = searchFilter,
                onClick = onFilterChange,
                onClickFilter = onShowFilter,
                onStartSearch = onStartSearch
            )
        }
        item {
            OverviewPharmacies(
                oftenUsedPharmacyList = overviewPharmacyList,
                onSelectPharmacy = onSelectPharmacy,
                pharmacySearchController = pharmacySearchController
            )
        }
    }
}

@Composable
private fun MapsSection(
    onShowMaps: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.pharmacy_maps_header),
            style = AppTheme.typography.subtitle1,
            modifier = Modifier
                .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
                .padding(horizontal = PaddingDefaults.Medium)
        )
    }
    MapsOverviewSmall(
        modifier = Modifier
            .fillMaxWidth()
            .height(186.dp)
            .padding(horizontal = PaddingDefaults.Medium),
        onClick = onShowMaps
    )
}

@Composable
private fun OverviewPharmacies(
    oftenUsedPharmacyList: List<OverviewPharmacyData.OverviewPharmacy>,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    pharmacySearchController: PharmacySearchController
) {
    if (oftenUsedPharmacyList.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium)
        ) {
            Text(
                stringResource(R.string.pharmacy_my_pharmacies_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier.padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium),
                textAlign = TextAlign.Start
            )
            val shortOftenUsedPharmacyList = remember(oftenUsedPharmacyList) {
                oftenUsedPharmacyList.take(LastUsedPharmaciesListLength)
            }
            Column {
                for (oftenUsedPharmacy in shortOftenUsedPharmacyList) {
                    FavoritePharmacyCard(
                        overviewPharmacy = oftenUsedPharmacy,
                        onSelectPharmacy = onSelectPharmacy,
                        pharmacySearchController = pharmacySearchController
                    )
                    SpacerMedium()
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    filter: PharmacyUseCaseData.Filter,
    onClick: (PharmacyUseCaseData.Filter) -> Unit,
    onStartSearch: () -> Unit,
    onClickFilter: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                onClick(
                    filter.copy(
                        nearBy = true,
                        openNow = true,
                        deliveryService = false,
                        onlineService = false
                    )
                )
                onStartSearch()
            }
        )
        FilterButton(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.search_pharmacies_filter_delivery_service),
            icon = Icons.Outlined.Moped,
            onClick = {
                onClick(
                    filter.copy(
                        nearBy = true,
                        deliveryService = true,
                        onlineService = false,
                        openNow = false
                    )
                )
                onStartSearch()
            }
        )
        FilterButton(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.search_pharmacies_filter_online_service),
            icon = Icons.Outlined.LocalShipping,
            onClick = {
                onClick(
                    filter.copy(
                        nearBy = false,
                        onlineService = true,
                        deliveryService = false,
                        openNow = false
                    )
                )
                onStartSearch()
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

@Composable
private fun FilterButton(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onClick() }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            icon,
            null,
            tint = AppTheme.colors.neutral600
        )
        SpacerMedium()
        Text(
            text,
            modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
            color = AppTheme.colors.neutral900,
            style = AppTheme.typography.body1,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
private fun PharmacySearchButton(
    modifier: Modifier,
    onStartSearch: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) { onStartSearch() }
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.ShortMedium)
    ) {
        Icon(
            Icons.Rounded.Search,
            tint = AppTheme.colors.neutral600,
            contentDescription = null
        )
        SpacerSmall()
        Text(
            text = stringResource(R.string.pharmacy_start_search_text),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f),
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral600
        )
    }
}

@Composable
fun PharmacyImagePlaceholder(modifier: Modifier) {
    Image(
        painterResource(R.drawable.ic_green_cross),
        null,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .size(64.dp)
    )
}
