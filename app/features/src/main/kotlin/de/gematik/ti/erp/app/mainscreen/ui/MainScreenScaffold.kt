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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenContentNavHost
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.profiles.presentation.ProfilesController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

@Composable
internal fun MainScreenScaffold(
    modifier: Modifier = Modifier,
    mainScreenController: MainScreenController,
    profilesController: ProfilesController,
    mainNavController: NavController,
    bottomNavController: NavHostController,
    showToolTipps: Boolean,
    tooltipBounds: MutableState<Map<Int, Rect>>,
    onClickAddProfile: () -> Unit,
    onClickChangeProfileName: (ProfilesUseCaseData.Profile) -> Unit,
    onClickAvatar: () -> Unit,
    onClickAddPrescription: () -> Unit,
    scaffoldState: ScaffoldState
) {
    val currentBottomNavigationRoute by bottomNavController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    val activeProfile by profilesController.getActiveProfileState()

    val isInPrescriptionScreen by remember {
        derivedStateOf {
            currentBottomNavigationRoute?.destination?.route == MainNavigationScreens.Prescriptions.route
        }
    }
    var topBarElevated by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.testTag(TestTag.Main.MainScreen).then(modifier),
        topBar = {
            if (currentBottomNavigationRoute?.destination?.route != MainNavigationScreens.Settings.route) {
                MultiProfileTopAppBar(
                    mainScreenController = mainScreenController,
                    profilesController = profilesController,
                    isInPrescriptionScreen = isInPrescriptionScreen,
                    showToolTipps = showToolTipps,
                    tooltipBounds = tooltipBounds,
                    elevated = topBarElevated,
                    onClickAddProfile = onClickAddProfile,
                    onClickChangeProfileName = onClickChangeProfileName,
                    onClickAddPrescription = onClickAddPrescription
                )
            }
        },
        bottomBar = {
            MainScreenBottomBar(
                navController = mainNavController,
                mainScreenController = mainScreenController,
                profilesController = profilesController,
                bottomNavController = bottomNavController
            )
        },
        floatingActionButton = {
            if (isInPrescriptionScreen) {
                RedeemFloatingActionButton(
                    activeProfile = activeProfile,
                    onClick = {
                        mainNavController.navigate(MainNavigationScreens.Redeem.path())
                    }
                )
            }
        },
        scaffoldState = scaffoldState
    ) { innerPadding ->

        MainScreenContentNavHost(
            modifier = Modifier.padding(innerPadding),
            mainScreenController = mainScreenController,
            mainNavController = mainNavController,
            bottomNavController = bottomNavController,
            onClickAvatar = onClickAvatar,
            onElevateTopBar = {
                topBarElevated = it
            },
            onClickArchive = { mainNavController.navigate(MainNavigationScreens.Archive.path()) }
        )
    }
}
