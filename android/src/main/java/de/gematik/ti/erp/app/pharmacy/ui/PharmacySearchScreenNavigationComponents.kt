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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.mainscreen.ui.ActionEvent
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenViewModel
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.tracking.TrackNavigationChanges
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.navigationModeState

@Composable
fun PharmacySearchScreenWithNavigation(
    taskIds: List<String>,
    mainNavController: NavController,
    viewModel: PharmacySearchViewModel = hiltViewModel(),
    mainScreenVM: MainScreenViewModel = hiltViewModel(LocalActivity.current)
) {
    val navController = rememberNavController()
    val navigationMode by navController.navigationModeState(PharmacyNavigationScreens.SearchResults.route)

    TrackNavigationChanges(navController)

    NavHost(
        navController,
        startDestination = PharmacyNavigationScreens.SearchResults.route
    ) {
        composable(PharmacyNavigationScreens.SearchResults.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacySearchScreen(
                    mainNavController = mainNavController,
                    onSelectPharmacy = {
                        viewModel.onSelectPharmacy(it)
                        navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                    },
                    viewModel,
                )
            }
        }
        composable(PharmacyNavigationScreens.PharmacyDetails.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyDetailsScreen(
                    navController,
                    showRedeemOptions = taskIds.isNotEmpty(),
                    viewModel
                )
            }
        }
        composable(PharmacyNavigationScreens.OrderPrescription.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyOrderScreen(
                    navController,
                    taskIds,
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
                    taskIds,
                    viewModel
                )
            }
        }
    }
}
