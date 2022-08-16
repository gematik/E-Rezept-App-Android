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
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel

private const val AnimationOffset = 9

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
    var searchFilter by remember { mutableStateOf(PharmacyUseCaseData.Filter()) }
    var showPharmacySearchResult by remember { mutableStateOf(false) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            scope.launch {
                pharmacySearchController.search(
                    name = "",
                    filter = searchFilter.copy(nearBy = permissions.values.any { it })
                )
                showPharmacySearchResult = true
            }
        }

    var showEnableLocationDialog by remember { mutableStateOf(false) }
    if (showEnableLocationDialog) {
        EnableLocationDialog(
            onCancel = {
                searchFilter = searchFilter.copy(nearBy = false)
                showEnableLocationDialog = false
            },
            onAccept = {
                locationPermissionLauncher.launch(locationPermissions)
                showEnableLocationDialog = false
            }
        )
    }

    val navController = rememberNavController()
    val navigationMode by navController.navigationModeState(PharmacyNavigationScreens.SearchResults.route)
    val startDestination = PharmacyNavigationScreens.StartSearch.route

    TrackNavigationChanges(navController)

    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(PharmacyNavigationScreens.StartSearch.route) {
            NavigationAnimation(mode = navigationMode) {
                AnimatedContent(
                    targetState = showPharmacySearchResult,
                    transitionSpec = {
                        if (showPharmacySearchResult) {
                            slideInVertically(initialOffsetY = { it / AnimationOffset }) + fadeIn() with
                                fadeOut()
                        } else {
                            fadeIn(tween(durationMillis = 550)) with fadeOut(tween(durationMillis = 550))
                        }
                    }
                ) {
                    if (!it) {
                        PharmacyOverviewScreen(
                            onBack = { mainNavController.popBackStack() },
                            onFilterChange = { searchFilter = it },
                            filter = searchFilter,
                            onStartSearch = {
                                scope.launch {
                                    when (pharmacySearchController.search(name = "", filter = searchFilter)) {
                                        PharmacySearchController.SearchQueryResult.Send -> {
                                            showPharmacySearchResult = true
                                        }
                                        PharmacySearchController.SearchQueryResult.NoLocationPermission -> {
                                            showEnableLocationDialog = true
                                        }
                                        PharmacySearchController.SearchQueryResult.NoLocationFound -> {
                                            pharmacySearchController.search(
                                                name = "",
                                                filter = searchFilter.copy(nearBy = false)
                                            )
                                            showPharmacySearchResult = true
                                        }
                                    }
                                }
                            },
                            onSelectPharmacy = {
                                viewModel.onSelectPharmacy(it)
                                navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                            }
                        )
                    } else {
                        PharmacySearchResultScreen(
                            pharmacySearchController = pharmacySearchController,
                            onBack = { showPharmacySearchResult = false },
                            onSelectPharmacy = {
                                viewModel.onSelectPharmacy(it)
                                navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                            }
                        )

                        BackHandler {
                            showPharmacySearchResult = false
                        }
                    }
                }
            }
        }
        composable(PharmacyNavigationScreens.PharmacyDetails.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyDetailsScreen(
                    navController,
                    viewModel
                )
            }
        }
        composable(PharmacyNavigationScreens.OrderPrescription.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyOrderScreen(
                    navController,
                    viewModel,
                    onSuccessfullyOrdered = {
                        mainScreenVM.onAction(ActionEvent.ReturnFromPharmacyOrder(it))
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
