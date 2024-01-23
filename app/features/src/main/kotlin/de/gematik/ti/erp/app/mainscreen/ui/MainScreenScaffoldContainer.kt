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
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberConsentController
import de.gematik.ti.erp.app.pkv.ui.GrantConsentDialog
import de.gematik.ti.erp.app.pkv.ui.HandleConsentErrorState
import de.gematik.ti.erp.app.pkv.ui.HandleConsentState
import de.gematik.ti.erp.app.profiles.presentation.ProfileController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
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
    var startGetConsentBottomSheet by remember { mutableStateOf(false) }
    val dialog = LocalDialog.current
    val snackbar = LocalSnackbar.current

    val profileController = rememberProfileController()
    val activeProfile by profileController.getActiveProfileState()

    val consentController = rememberConsentController(profile = activeProfile)

    val consentState by consentController.consentState.collectAsStateWithLifecycle()
    val consentErrorState by consentController.consentErrorState.collectAsStateWithLifecycle()

    val bottomNavController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val showWelcomeDrawer by mainScreenController.showWelcomeDrawerState
    val showGetConsentDrawer by mainScreenController.showGiveConsentDrawerState

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

    val onClickGoToInvoicesAction = {
        mainNavController.navigate(PkvRoutes.InvoiceListScreen.path(activeProfile.id))
    }

    HandleConsentState(
        consentState = consentState,
        snackbar = snackbar,
        onClickSnackbarAction = onClickGoToInvoicesAction
    )
    HandleConsentErrorState(
        consentErrorState = consentErrorState,
        onRetry = {
            consentController.grantChargeConsent()
        },
        onClickToInvoices = onClickGoToInvoicesAction,
        onShowCardWall = {
            mainNavController.navigate(MainNavigationScreens.CardWall.path(activeProfile.id))
        }
    )

    MainScreenSnackbar(
        mainScreenController = mainScreenController,
        scaffoldState = scaffoldState
    )

    OrderSuccessHandler(mainScreenController)

    if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                when (mainScreenBottomSheetContentState) {
                    is MainScreenBottomSheetContentState.Welcome -> {
                        mainScreenController.welcomeDrawerShown()
                        if (showToolTips) {
                            startToolTips = true
                        }
                    }

                    is MainScreenBottomSheetContentState.GrantConsent -> {
                        mainScreenController.giveConsentDrawerShown(activeProfile.id)
                    }

                    else -> {}
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

    if (showGetConsentDrawer) {
        startGetConsentBottomSheet = true
        mainScreenController.giveConsentDrawerShown(activeProfile.id)
    }

    if (startGetConsentBottomSheet && isInPrescriptionScreen) {
        startGetConsentBottomSheet = false
        mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.GrantConsent()
    }

    LaunchedEffect(Unit) {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager

        if (accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
            .isNotEmpty()
        ) {
            mainScreenController.toolTipsShown()
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
            mainScreenController.toolTipsShown()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = sheetState.isVisible) {
        coroutineScope.launch {
            sheetState.hide()
            mainScreenBottomSheetContentState = null
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
                profileController = profileController,
                infoContentState = mainScreenBottomSheetContentState,
                profileToRename = profileToRename,
                onGrantConsent = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                    dialog.show { dialog ->
                        GrantConsentDialog(
                            onGrantConsent = {
                                consentController.grantChargeConsent()
                                dialog.dismiss()
                            },
                            onCancel = {
                                dialog.dismiss()
                            }
                        )
                    }
                },
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
            profileController = profileController,
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
