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

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackMainScreenBottomPopUps
import de.gematik.ti.erp.app.analytics.trackNavigationChanges
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.mainscreen.presentation.rememberMainScreenController
import de.gematik.ti.erp.app.profiles.presentation.ProfilesController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.presentation.rememberProfilesController
import de.gematik.ti.erp.app.settings.ui.rememberSettingsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Suppress("LongMethod")
@Composable
internal fun MainScreenScaffoldContainer(
    mainNavController: NavController,
    onDeviceIsInsecure: () -> Unit
) {
    val mainScreenController = rememberMainScreenController()
    val settingsController = rememberSettingsController()
    val profilesController = rememberProfilesController()

    val context = LocalContext.current
    val bottomNavController = rememberNavController()
    val currentBottomNavigationRoute by bottomNavController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    var previousNavEntry by remember { mutableStateOf("main") }
    trackNavigationChanges(bottomNavController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })
    val isInPrescriptionScreen by remember {
        derivedStateOf {
            currentBottomNavigationRoute?.destination?.route == MainNavigationScreens.Prescriptions.route
        }
    }
    @Requirement(
        "O.Plat_1#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Check for insecure Devices on MainScreen."
    )
    CheckInsecureDevice(onDeviceIsInsecure)
    @Requirement(
        "O.Arch_6#2",
        "O.Resi_2#2",
        "O.Resi_3#2",
        "O.Resi_4#2",
        "O.Resi_5#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Check device integrity."
    )
    CheckDeviceIntegrity(mainScreenController, mainNavController)
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    MainScreenSnackbar(
        mainScreenController = mainScreenController,
        scaffoldState = scaffoldState
    )
    OrderSuccessHandler(mainScreenController)
    var mainScreenBottomSheetContentState: MainScreenBottomSheetContentState? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                scope.launch {
                    settingsController.welcomeDrawerShown()
                }
            }
        }
    }
    LaunchedEffect(mainScreenBottomSheetContentState) {
        if (mainScreenBottomSheetContentState != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            mainScreenBottomSheetContentState?.let { analytics.trackMainScreenBottomPopUps(it) }
        } else {
            analytics.onPopUpClosed()
            val route = Uri.parse(mainNavController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
        }
    }
    LaunchedEffect(Unit) {
        if (settingsController.showWelcomeDrawer.first()) {
            mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.Welcome()
        }
    }
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.targetValue == ModalBottomSheetValue.Hidden) {
            if (mainScreenBottomSheetContentState == MainScreenBottomSheetContentState.Welcome()) {
                settingsController.welcomeDrawerShown()
            }
            mainScreenBottomSheetContentState = null
        }
    }
    LaunchedEffect(Unit) {
        if (settingsController.talkbackEnabled(context)) {
            settingsController.mainScreenTooltipsShown()
        }
    }
    var profileToRename by remember {
        mutableStateOf(DEFAULT_EMPTY_PROFILE)
    }
    val toolTipBounds = remember {
        mutableStateOf<Map<Int, Rect>>(emptyMap())
    }
    ToolTips(settingsController, isInPrescriptionScreen, toolTipBounds)
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = sheetState.isVisible) {
        coroutineScope.launch {
            sheetState.hide()
        }
    }
    ModalBottomSheetLayout(
        sheetState = sheetState,
        modifier = Modifier
            .imePadding()
            .testTag(TestTag.Main.MainScreenBottomSheet.Modal),
        sheetShape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) },
        sheetContent = {
            MainScreenBottomSheetContentState(
                mainNavController = mainNavController,
                profilesController = profilesController,
                infoContentState = mainScreenBottomSheetContentState,
                profileToRename = profileToRename,
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            )
        }
    ) {
        // TODO: move to general place?
        ExternalAuthenticationDialog()
        MainScreenScaffold(
            mainScreenController = mainScreenController,
            settingsController = settingsController,
            profilesController = profilesController,
            mainNavController = mainNavController,
            bottomNavController = bottomNavController,
            tooltipBounds = toolTipBounds,
            onClickAddProfile = {
                mainScreenBottomSheetContentState =
                    MainScreenBottomSheetContentState.AddProfile()
            },
            onClickChangeProfileName = { profile ->
                profileToRename = profile
                mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.EditProfileName()
            },
            onClickAvatar = {
                mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.EditProfilePicture()
            },
            scaffoldState = scaffoldState
        )
    }
}

@Composable
private fun CheckInsecureDevice(onDeviceIsInsecure: () -> Unit) {
    val settingsController = rememberSettingsController()
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            return@LaunchedEffect
        }
        @Requirement(
            "O.Plat_1#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Navigate to insecure Devices warning."
        )
        (
            withContext(Dispatchers.Main) {
                if (settingsController.showInsecureDevicePrompt.first()) {
                    onDeviceIsInsecure()
                }
            }
            )
    }
}

@Composable
private fun CheckDeviceIntegrity(
    mainScreenController: MainScreenController,
    mainNavController: NavController
) {
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            return@LaunchedEffect
        }
        if (mainScreenController.checkDeviceIntegrity().first()) {
            withContext(Dispatchers.Main) {
                mainNavController.navigate(MainNavigationScreens.IntegrityNotOkScreen.route)
                navOptions {
                    launchSingleTop = true
                    popUpTo(MainNavigationScreens.IntegrityNotOkScreen.path()) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
