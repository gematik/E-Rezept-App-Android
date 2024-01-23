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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.mainscreen.ui.rememberMainScreenController
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("LongMethod")
@Composable
fun PharmacyNavigation(
    orderState: PharmacyOrderState = rememberPharmacyOrderState(),
    isNestedNavigation: Boolean = false,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pharmacySearchController = rememberPharmacySearchController()
    var searchFilter by remember(pharmacySearchController.searchState.filter) {
        mutableStateOf(pharmacySearchController.searchState.filter)
    }
    val hasRedeemableTasks by orderState.hasRedeemableTasks

    LaunchedEffect(Unit) {
        searchFilter = searchFilter.copy(directRedeem = false)
        if (orderState.profile.lastAuthenticated == null && hasRedeemableTasks
        ) {
            searchFilter = searchFilter.copy(directRedeem = true)
        }
    }

    var showNoLocationDialog by remember { mutableStateOf(false) }

    val searchAgainFn = { nearBy: Boolean ->
        scope.launch {
            pharmacySearchController.search(
                name = "",
                filter = searchFilter.copy(nearBy = nearBy)
            )
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.any { it }) {
                searchAgainFn(true)
            } else {
                showNoLocationDialog = true
            }
        }

    if (showNoLocationDialog) {
        NoLocationDialog(
            onAccept = {
                searchAgainFn(false)
                showNoLocationDialog = false
            }
        )
    }

    var showNoLocationServicesDialog by remember { mutableStateOf(false) }
    if (showNoLocationServicesDialog) {
        NoLocationServicesDialog(
            onClose = {
                searchAgainFn(false)
                showNoLocationServicesDialog = false
            }
        )
    }

    val navController = rememberNavController()
    val navigationMode by navController
        .navigationModeState(PharmacyNavigationScreens.StartSearch.route) { prev, curr ->
            when {
                isNestedNavigation && prev == null ->
                    NavigationMode.Forward

                prev == PharmacyNavigationScreens.StartSearch.route &&
                    (curr == PharmacyNavigationScreens.Maps.route || curr == PharmacyNavigationScreens.List.route) ->
                    NavigationMode.Open

                (prev == PharmacyNavigationScreens.Maps.route || prev == PharmacyNavigationScreens.List.route) &&
                    curr == PharmacyNavigationScreens.StartSearch.route ->
                    NavigationMode.Closed

                else -> null
            }
        }

    var previousNavEntry by remember { mutableStateOf("pharmacySearch") }
    TrackNavigationChanges(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })

    val handleSearchResultFn = { searchResult: PharmacySearchController.SearchQueryResult ->
        when (searchResult) {
            PharmacySearchController.SearchQueryResult.NoLocationPermission -> {
                locationPermissionLauncher.launch(locationPermissions)
            }

            PharmacySearchController.SearchQueryResult.NoLocationServicesEnabled -> {
                showNoLocationServicesDialog = true
            }

            else -> {}
        }
    }

    NavHost(
        navController,
        startDestination = PharmacyNavigationScreens.StartSearch.route
    ) {
        composable(PharmacyNavigationScreens.StartSearch.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyOverviewScreen(
                    isNestedNavigation = isNestedNavigation,
                    orderState = orderState,
                    onBack = onBack,
                    navController = navController,
                    onFilterChange = { searchFilter = it },
                    filter = searchFilter,
                    onStartSearch = {
                        scope.launch(Dispatchers.Main) {
                            navController.navigate(PharmacyNavigationScreens.List.path())
                            handleSearchResultFn(
                                pharmacySearchController.search(name = "", filter = searchFilter)
                            )
                        }
                    },
                    onShowMaps = {
                        scope.launch(Dispatchers.Main) {
                            navController.navigate(PharmacyNavigationScreens.Maps.path())
                            handleSearchResultFn(
                                pharmacySearchController.search(
                                    name = "",
                                    filter = searchFilter.copy(nearBy = true)
                                )
                            )
                        }
                    },
                    onSelectPharmacy = { pharmacy, orderOption ->
                        scope.launch(Dispatchers.Main) {
                            orderState.onSelectPharmacy(pharmacy, orderOption)
                            navController.navigate(PharmacyNavigationScreens.OrderOverview.path())
                        }
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.List.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacySearchResultScreen(
                    orderState = orderState,
                    navController = navController,
                    searchController = pharmacySearchController,
                    onBack = {
                        orderState.onResetPharmacySelection()
                        navController.navigate(PharmacyNavigationScreens.StartSearch.path())
                    },
                    onClickMaps = {
                        scope.launch(Dispatchers.Main) {
                            navController.navigate(PharmacyNavigationScreens.Maps.path())
                            handleSearchResultFn(
                                pharmacySearchController.search(
                                    name = "",
                                    filter = searchFilter.copy(nearBy = true)
                                )
                            )
                        }
                    },
                    onSelectPharmacy = { pharmacy, orderOption ->
                        scope.launch(Dispatchers.Main) {
                            orderState.onSelectPharmacy(pharmacy, orderOption)
                            navController.navigate(PharmacyNavigationScreens.OrderOverview.path())
                        }
                    }
                )
            }
        }

        composable(PharmacyNavigationScreens.Maps.route) {
            NavigationAnimation(mode = navigationMode) {
                MapsOverview(
                    searchController = pharmacySearchController,
                    orderState = orderState,
                    navController = navController,
                    onBack = {
                        orderState.onResetPharmacySelection()
                        navController.popBackStack()
                    },
                    onSelectPharmacy = { pharmacy, orderOption ->
                        scope.launch {
                            orderState.onSelectPharmacy(pharmacy, orderOption)
                            navController.navigate(PharmacyNavigationScreens.OrderOverview.path())
                        }
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.OrderOverview.route) {
            NavigationAnimation(mode = navigationMode) {
                val mainScreenController = rememberMainScreenController()
                OrderOverview(
                    orderState = orderState,
                    onClickContacts = {
                        navController.navigate(PharmacyNavigationScreens.EditShippingContact.path())
                    },
                    onBack = { navController.popBackStack() },
                    onSelectPrescriptions = {
                        navController.navigate(PharmacyNavigationScreens.PrescriptionSelection.path())
                    },
                    onFinish = { hasError ->
                        mainScreenController.onOrdered(hasError = hasError)
                        onFinish()
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.EditShippingContact.route) {
            NavigationAnimation(mode = navigationMode) {
                EditShippingContactScreen(
                    orderState = orderState,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.PrescriptionSelection.route) {
            NavigationAnimation(mode = navigationMode) {
                PrescriptionSelection(
                    orderState = orderState,
                    onFinishSelection = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
