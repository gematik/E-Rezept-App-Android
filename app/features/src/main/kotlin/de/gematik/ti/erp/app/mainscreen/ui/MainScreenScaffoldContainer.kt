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

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
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
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackMainScreenBottomPopUps
import de.gematik.ti.erp.app.analytics.trackNavigationChangesAsync
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.profiles.presentation.ProfilesController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.presentation.rememberProfilesController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Suppress("LongMethod")
@Composable
internal fun MainScreenScaffoldContainer(
    mainNavController: NavController,
    mainScreenController: MainScreenController,
    onClickAddPrescription: () -> Unit
) {
    val context = LocalContext.current

    val showToolTips by mainScreenController.canStartToolTipsState
    var startToolTips by remember { mutableStateOf(false) }

    val profilesController = rememberProfilesController()
    val bottomNavController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val showWelcomeDrawer by mainScreenController.showWelcomeDrawerState

    var mainScreenBottomSheetContentState: MainScreenBottomSheetContentState? by remember { mutableStateOf(null) }

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val currentBottomNavigationRoute by bottomNavController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)

    var previousNavEntry by remember { mutableStateOf("main") }

    trackNavigationChangesAsync(bottomNavController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })

    val isInPrescriptionScreen by remember {
        derivedStateOf {
            currentBottomNavigationRoute?.destination?.route == MainNavigationScreens.Prescriptions.route
        }
    }

    MainScreenSnackbar(
        mainScreenController = mainScreenController,
        scaffoldState = scaffoldState
    )

    OrderSuccessHandler(mainScreenController)

    if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                mainScreenController.welcomeDrawerShown()
                if (showToolTips) {
                    startToolTips = true
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

    if (showWelcomeDrawer) {
        mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.Welcome()
        mainScreenController.welcomeDrawerShown()
    }

    LaunchedEffect(Unit) {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager

        if (accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
            .isNotEmpty()
        ) {
            mainScreenController.toolTippsShown()
        }
    }
    var profileToRename by remember {
        mutableStateOf(DEFAULT_EMPTY_PROFILE)
    }
    val toolTipBounds = remember {
        mutableStateOf<Map<Int, Rect>>(emptyMap())
    }
    if (startToolTips) {
        ToolTips(
            isInPrescriptionScreen,
            toolTipBounds
        ) {
            startToolTips = false
            mainScreenController.toolTippsShown()
        }
    }

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
                mainScreenController = mainScreenController,
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
            profilesController = profilesController,
            mainNavController = mainNavController,
            bottomNavController = bottomNavController,
            showToolTipps = startToolTips,
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
            onClickAddPrescription = onClickAddPrescription,
            scaffoldState = scaffoldState
        )
    }
}
