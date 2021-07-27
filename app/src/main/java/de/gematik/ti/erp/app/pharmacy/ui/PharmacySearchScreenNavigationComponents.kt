/*
 * Copyright (c) 2021 gematik GmbH
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.core.LocalFragmentNavController
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.navigationModeState

@Composable
fun PharmacySearchScreenWithNavigation(
    viewModel: PharmacySearchViewModel = viewModel(),
    taskIds: String?
) {
    val fragmentNavController = LocalFragmentNavController.current
    val navController = rememberNavController()
    val selectedPharmacy = rememberSaveable {
        mutableStateOf<PharmacyUseCaseData.Pharmacy?>(null)
    }

    val navigationMode by navController.navigationModeState(PharmacyNavigationScreens.SearchResults.route)

    NavHost(
        navController,
        startDestination = PharmacyNavigationScreens.SearchResults.route
    ) {
        composable(PharmacyNavigationScreens.SearchResults.route) {
            NavigationAnimation(navigationMode) {
                PharmacySearchScreen(
                    viewModel = viewModel,
                    navController,
                    selectedPharmacy
                )
            }
        }
        composable(PharmacyNavigationScreens.PharmacyDetails.route) {
            selectedPharmacy.value?.let { pharmacy ->
                NavigationAnimation(navigationMode) {
                    PharmacyDetailsScreen(
                        navController,
                        pharmacy,
                        showRedeemOptions = !taskIds.isNullOrEmpty()
                    )
                }
            }
        }
        composable(PharmacyNavigationScreens.ReserveInPharmacy.route) {
            selectedPharmacy.value?.let { pharmacy ->
                NavigationAnimation(navigationMode) {
                    ReserveForPickupInPharmacy(
                        navController = navController,
                        viewModel,
                        taskIds,
                        pharmacy.name,
                        pharmacy.telematikId
                    )
                }
            }
        }
        composable(PharmacyNavigationScreens.CourierDelivery.route) {
            selectedPharmacy.value?.let { pharmacy ->
                NavigationAnimation(navigationMode) {
                    CourierDelivery(
                        navigation = navController,
                        viewModel,
                        taskIds,
                        pharmacy.name,
                        pharmacy.telematikId,
                        pharmacy.contacts.phone
                    )
                }
            }
        }
        composable(PharmacyNavigationScreens.MailDelivery.route) {
            selectedPharmacy.value?.let { pharmacy ->
                NavigationAnimation(navigationMode) {
                    MailDelivery(
                        navigation = navController,
                        viewModel,
                        taskIds,
                        pharmacy.name,
                        pharmacy.telematikId
                    )
                }
            }
        }
        composable(
            "uploadStatus/{redeemOption}",
            arguments = listOf(navArgument("redeemOption") { type = NavType.IntType })
        ) { navBackStackEntry ->
            NavigationAnimation(navigationMode) {
                RedeemOnlineSuccess(
                    navBackStackEntry.arguments?.getInt("redeemOption"),
                    fragmentNavController
                )
            }
        }
    }
}
