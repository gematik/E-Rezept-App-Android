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

package de.gematik.ti.erp.app.mainscreen.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.orders.ui.OrderScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionsScreen
import de.gematik.ti.erp.app.prescription.ui.rememberPrescriptionsController
import de.gematik.ti.erp.app.profiles.presentation.rememberProfilesController
import de.gematik.ti.erp.app.settings.ui.SettingsScreen

@Composable
internal fun MainScreenContentNavHost(
    modifier: Modifier,
    mainScreenController: MainScreenController,
    mainNavController: NavController,
    bottomNavController: NavHostController,
    onElevateTopBar: (Boolean) -> Unit,
    onClickAvatar: () -> Unit,
    onClickArchive: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag("main_screen")
    ) {
        NavHost(
            bottomNavController,
            startDestination = MainNavigationScreens.Prescriptions.path()
        ) {
            composable(MainNavigationScreens.Prescriptions.route) {
                val prescriptionsController = rememberPrescriptionsController()
                val profilesController = rememberProfilesController()
                val activeProfile by profilesController.getActiveProfileState()
                PrescriptionsScreen(
                    controller = prescriptionsController,
                    mainScreenController = mainScreenController,
                    activeProfile = activeProfile,
                    onElevateTopBar = onElevateTopBar,
                    onClickArchive = onClickArchive,
                    onClickAvatar = onClickAvatar,
                    onShowCardWall = {
                        mainNavController.navigate(
                            MainNavigationScreens.CardWall.path(activeProfile.id)
                        )
                    },
                    onClickPrescription = { taskId ->
                        mainNavController.navigate(
                            MainNavigationScreens.PrescriptionDetail.path(
                                taskId = taskId
                            )
                        )
                    }
                )
            }
            composable(MainNavigationScreens.Orders.route) {
                OrderScreen(
                    mainNavController = mainNavController,
                    mainScreenController = mainScreenController,
                    onElevateTopBar = onElevateTopBar
                )
            }
            composable(
                MainNavigationScreens.Settings.route,
                MainNavigationScreens.Settings.arguments
            ) {
                SettingsScreen(
                    mainNavController = mainNavController
                )
            }
        }
    }
}
