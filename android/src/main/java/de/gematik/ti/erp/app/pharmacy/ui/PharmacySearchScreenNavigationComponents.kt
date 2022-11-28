/*
 * Copyright (c) 2022 gematik GmbH
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

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.mainscreen.ui.ActionEvent
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenViewModel
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel

private const val AnimationOffset = 9

private enum class PharmacyOverviewScreen {
    Overview,
    List
}

@Suppress("LongMethod")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PharmacyNavigation(
    mainNavController: NavController,
    mainScreenVM: MainScreenViewModel
) {
    val viewModel by rememberViewModel<PharmacySearchViewModel>()
    val scope = rememberCoroutineScope()
    val pharmacySearchController = rememberPharmacySearchController()
    var searchFilter by remember(pharmacySearchController.searchState.filter) {
        mutableStateOf(pharmacySearchController.searchState.filter)
    }
    var screen by remember { mutableStateOf(PharmacyOverviewScreen.Overview) }
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
    val navigationMode by navController.navigationModeState(PharmacyNavigationScreens.SearchResults.route)
    val startDestination = PharmacyNavigationScreens.StartSearch.route

    TrackNavigationChanges(navController)

    val hasRedeemableTasks by produceState(false) {
        viewModel.hasRedeemableTasks().collect { value = it }
    }

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
        startDestination = startDestination
    ) {
        composable(PharmacyNavigationScreens.StartSearch.route) {
            BackHandler(screen == PharmacyOverviewScreen.List) {
                screen = PharmacyOverviewScreen.Overview
            }

            NavigationAnimation(mode = navigationMode) {
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        if (screen != PharmacyOverviewScreen.Overview) {
                            slideInVertically(initialOffsetY = { it / AnimationOffset }) + fadeIn() with
                                fadeOut()
                        } else {
                            fadeIn(tween(durationMillis = 550)) with fadeOut(tween(durationMillis = 550))
                        }
                    }
                ) {
                    when (it) {
                        PharmacyOverviewScreen.Overview -> {
                            PharmacyOverviewScreen(
                                onBack = { mainNavController.popBackStack() },
                                onFilterChange = { searchFilter = it },
                                filter = searchFilter,
                                onStartSearch = {
                                    scope.launch {
                                        screen = PharmacyOverviewScreen.List
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
                                                filter = searchFilter.copy(nearBy = true, ready = true)
                                            )
                                        )
                                    }
                                },
                                onSelectPharmacy = {
                                    scope.launch(Dispatchers.Main) {
                                        viewModel.onSelectPharmacy(it)
                                        navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                                    }
                                }
                            )
                        }

                        PharmacyOverviewScreen.List -> {
                            PharmacySearchResultScreen(
                                pharmacySearchController = pharmacySearchController,
                                onBack = { screen = PharmacyOverviewScreen.Overview },
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
                                onSelectPharmacy = {
                                    scope.launch(Dispatchers.Main) {
                                        viewModel.onSelectPharmacy(it)
                                        navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        composable(PharmacyNavigationScreens.Maps.route) {
            NavigationAnimation(mode = navigationMode) {
                MapsOverview(
                    pharmacySearchController = pharmacySearchController,
                    hasRedeemableTasks = hasRedeemableTasks,
                    onBack = { navController.popBackStack() },
                    onSelectPharmacy = { pharmacy, orderOption ->
                        scope.launch {
                            viewModel.onSelectPharmacy(pharmacy)
                            if (orderOption != null) {
                                viewModel.onSelectOrderOption(orderOption)
                                navController.navigate(PharmacyNavigationScreens.OrderPrescription.path())
                            } else {
                                navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                            }
                        }
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.PharmacyDetails.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyDetailsScreen(
                    navController = navController,
                    viewModel = viewModel,
                    hasRedeemableTasks = hasRedeemableTasks,
                    onClickFavoriteStar = { pharmacy, markAsFavorite ->
                        scope.launch {
                            if (markAsFavorite) {
                                viewModel.saveOrUpdateFavoritePharmacy(pharmacy)
                            } else {
                                viewModel.deleteFavoritePharmacy(pharmacy)
                            }
                        }
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.OrderPrescription.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyOrderScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onSuccessfullyOrdered = {
                        mainScreenVM.onSuccessfullyOrdered(ActionEvent.ReturnFromPharmacyOrder(it))
                        mainNavController.popBackStack(MainNavigationScreens.Prescriptions.path(), false)
                    }
                )
            }
        }
        composable(PharmacyNavigationScreens.EditShippingContact.route) {
            NavigationAnimation(mode = navigationMode) {
                EditShippingContactScreen(
                    navController,
                    viewModel
                )
            }
        }
    }
}
