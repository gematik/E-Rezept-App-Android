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

package de.gematik.ti.erp.app.mainscreen.ui

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkChatRead
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.CardWallScreen
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.messages.ui.DisplayPickupScreen
import de.gematik.ti.erp.app.messages.ui.MessageScreen
import de.gematik.ti.erp.app.messages.ui.MessageViewModel
import de.gematik.ti.erp.app.onboarding.ui.OnboardingScreen
import de.gematik.ti.erp.app.onboarding.ui.ReturningUserSecureAppOnboardingScreen
import de.gematik.ti.erp.app.pharmacy.ui.PharmacySearchScreenWithNavigation
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailsScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScreen
import de.gematik.ti.erp.app.prescription.ui.ScanScreen
import de.gematik.ti.erp.app.redeem.ui.RedeemScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(navController: NavHostController, mainViewModel: MainViewModel) {

    LaunchedEffect(Unit) {
        mainViewModel.authenticationMethod.collect {
            if (!mainViewModel.isNewUser && !(it == SettingsAuthenticationMethod.Password || it == SettingsAuthenticationMethod.DeviceSecurity)) {
                navController.navigate(MainNavigationScreens.ReturningUserSecureAppOnboardingScreen.path()) {
                    launchSingleTop = true
                    popUpTo(MainNavigationScreens.Prescriptions.path()) {
                        inclusive = true
                    }
                }
            }
        }
    }

    val startDestination =
        when {
            mainViewModel.isNewUser -> {
                MainNavigationScreens.Onboarding.route
            }
            else -> {
                MainNavigationScreens.Prescriptions.route
            }
        }

    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(MainNavigationScreens.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(MainNavigationScreens.ReturningUserSecureAppOnboardingScreen.route) {
            ReturningUserSecureAppOnboardingScreen(navController)
        }
        composable(
            MainNavigationScreens.Settings.route,
            MainNavigationScreens.Settings.arguments
        ) {
            val scrollTo = remember { it.arguments?.get("scrollToSection") as SettingsScrollTo }
            SettingsScreen(scrollTo = scrollTo, navController)
        }
        composable(MainNavigationScreens.Camera.route) {
            ScanScreen(navController)
        }
        composable(MainNavigationScreens.Prescriptions.route) {
            MainScreenWithScaffold(navController)
        }
        composable(
            MainNavigationScreens.PrescriptionDetail.route,
            MainNavigationScreens.PrescriptionDetail.arguments,
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }
            PrescriptionDetailsScreen(taskId, navController)
        }
        composable(
            MainNavigationScreens.PharmacySearch.route,
            MainNavigationScreens.PharmacySearch.arguments,
        ) {
            val taskIds = remember { it.arguments?.getString("taskIds") }
            PharmacySearchScreenWithNavigation(taskIds, navController)
        }
        composable(MainNavigationScreens.InsecureDeviceScreen.route) {
            InsecureDeviceScreen(navController, mainViewModel)
        }
        composable(
            MainNavigationScreens.RedeemLocally.route,
            MainNavigationScreens.RedeemLocally.arguments
        ) {
            val taskIds =
                remember {
                    requireNotNull(
                        navController.currentBackStackEntry?.arguments?.getString(
                            "taskIds"
                        )
                    )
                }
            RedeemScreen(
                taskIds.split(","),
                navController
            )
        }
        composable(
            MainNavigationScreens.PickUpCode.route,
            MainNavigationScreens.PickUpCode.arguments
        ) {
            val pickUpCodeHR =
                remember { navController.currentBackStackEntry?.arguments?.getString("pickUpCodeHR") }
            val pickUpCodeDMC =
                remember { navController.currentBackStackEntry?.arguments?.getString("pickUpCodeDMC") }
            DisplayPickupScreen(
                navController,
                pickupCodeHR = pickUpCodeHR,
                pickupCodeDMC = pickUpCodeDMC
            )
        }
        composable(MainNavigationScreens.CardWall.route) {
            CardWallScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun MainScreenWithScaffold(
    mainNavController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    mainScreenVM: MainScreenViewModel = hiltViewModel(LocalActivity.current),
    messageVM: MessageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        if (mainViewModel.showInsecureDevicePrompt.first()) {
            mainNavController.navigate(MainNavigationScreens.InsecureDeviceScreen.route)
        }
    }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val currentRedeemEvent by produceState(
        RedeemEvent(
            "",
            false
        )
    ) {
        mainScreenVM.onRedeemEvent.collect {
            value = it
            sheetState.show()
        }
    }

    LaunchedEffect(Unit) {
        sheetState.snapTo(ModalBottomSheetValue.Hidden)
    }

    val scaffoldState = rememberScaffoldState()

    val errNetworkNotAvailable = stringResource(R.string.error_message_network_not_available)
    val errServerComFailed = stringResource(R.string.error_message_server_communication_failed)
    val errVau = stringResource(R.string.error_message_vau_error)
    LaunchedEffect(Unit) {
        mainScreenVM.onErrorEvent.collect {
            scaffoldState.snackbarHostState.showSnackbar(
                when (it) {
                    ErrorEvent.NetworkNotAvailable -> errNetworkNotAvailable
                    is ErrorEvent.ServerCommunicationFailedWhileRefreshing -> errServerComFailed.format(
                        it.code
                    )
                    ErrorEvent.FatalTruststoreState -> errVau
                }
            )
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {

            MainScreenBottomSheetAction(
                icon = Icons.Rounded.QrCode,
                title = stringResource(R.string.dialog_redeem_headline),
                info = stringResource(R.string.dialog_redeem_info),
                modifier = Modifier.testId("main/redeemInLocalPharmacyButton")
            ) {
                mainNavController.navigate(
                    MainNavigationScreens.RedeemLocally.path(
                        currentRedeemEvent.taskIds
                    )
                )
            }

            val orderNotPossibleTxt = stringResource(R.string.dialog_order_not_possible)
            val context = LocalContext.current
            MainScreenBottomSheetAction(
                enabled = currentRedeemEvent.isFullDetail,
                icon = Icons.Rounded.ShoppingBag,
                title = stringResource(R.string.dialog_order_headline),
                info = stringResource(R.string.dialog_order_info),
                modifier = Modifier.testId("main/redeemRemoteButton")
            ) {
                if (currentRedeemEvent.isFullDetail) {
                    mainNavController.navigate(
                        MainNavigationScreens.PharmacySearch.path(
                            currentRedeemEvent.taskIds
                        )
                    )
                } else {
                    Toast.makeText(context, orderNotPossibleTxt, Toast.LENGTH_SHORT).show()
                }
            }
        }
    ) {
        val bottomNavController = rememberNavController()

        Scaffold(
            topBar = { MainScreenTopAppBar(mainNavController) },
            bottomBar = {
                MainScreenBottomNavigation(mainNavController, bottomNavController)
            },
            scaffoldState = scaffoldState
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .testTag("main_screen")
            ) {
                NavHost(
                    bottomNavController,
                    startDestination = MainNavigationScreens.Prescriptions.path()
                ) {
                    composable(MainNavigationScreens.Prescriptions.route) {
                        PrescriptionScreen(mainNavController)
                    }
                    composable(MainNavigationScreens.Messages.route) {
                        MessageScreen(mainNavController, messageVM)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScreenBottomNavigation(
    navController: NavController,
    bottomNavController: NavController,
    viewModel: MainScreenViewModel = hiltViewModel(LocalActivity.current)
) {
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadMessagesAvailable by viewModel.unreadMessagesAvailable()
        .collectAsState(initial = false)

    BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
        MainScreenBottomNavigationItems.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier.testTag(
                    when (screen) {
                        MainNavigationScreens.Prescriptions -> "erx_btn_prescriptions"
                        MainNavigationScreens.Messages -> "erx_btn_messages"
                        MainNavigationScreens.PharmacySearch -> "erx_btn_search_pharmacies"
                        else -> ""
                    }
                ),
                selectedContentColor = AppTheme.colors.primary700,
                unselectedContentColor = AppTheme.colors.neutral600,
                icon = {
                    when (screen) {
                        MainNavigationScreens.Prescriptions -> Icon(
                            painterResource(R.drawable.ic_logo_outlined),
                            null,
                            modifier = Modifier.size(24.dp)
                        )
                        MainNavigationScreens.Messages -> Icon(
                            if (unreadMessagesAvailable) Icons.Outlined.MarkChatUnread else Icons.Outlined.MarkChatRead,
                            null
                        )
                        MainNavigationScreens.PharmacySearch -> Icon(
                            Icons.Outlined.Search, contentDescription = null
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(
                            when (screen) {
                                MainNavigationScreens.Prescriptions -> R.string.pres_bottombar_prescriptions
                                MainNavigationScreens.Messages -> R.string.pres_bottombar_messages
                                MainNavigationScreens.PharmacySearch -> R.string.pres_bottombar_pharmacies
                                else -> R.string.pres_bottombar_prescriptions
                            }
                        ),
                        modifier = Modifier.testId(
                            when (screen) {
                                MainNavigationScreens.Prescriptions -> "erx_btn_prescriptions"
                                MainNavigationScreens.Messages -> "erx_btn_messages"
                                MainNavigationScreens.PharmacySearch -> "erx_btn_search_pharmacies"
                                else -> ""
                            }
                        )
                    )
                },
                selected = currentRoute == screen.route,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != screen.route) {
                        if (screen.route == MainNavigationScreens.PharmacySearch.route) {
                            navController.navigate(screen.path())
                        } else {
                            bottomNavController.navigate(screen.path())
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreenBottomSheetAction(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    title: String,
    info: String,
    onClick: () -> Unit,
) {

    val titleColor = if (enabled) {
        Color.Unspecified
    } else {
        AppTheme.typographyColors.subtitle1l
    }

    val textColor = if (enabled) {
        AppTheme.typographyColors.body2l
    } else {
        AppTheme.colors.neutral400
    }

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(
            icon, null,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically),
            tint = textColor
        )
        Spacer16()
        Column {
            Text(
                title,
                style = MaterialTheme.typography.subtitle1,
                color = titleColor

            )
            Text(
                info,
                style = MaterialTheme.typography.body2,
                color = textColor
            )
        }
    }
}

/**
 * The top appbar of the actual main screen.
 */
@Composable
fun MainScreenTopAppBar(
    navController: NavController
) {
    val accSettings = stringResource(R.string.main_settings_acc)
    val accScan = stringResource(R.string.main_scan_acc)

    TopAppBar(
        title = {
            Text(stringResource(R.string.main_nav_title_prescriptions))
        },
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface,
        navigationIcon = @Composable {
            IconButton(
                onClick = {
                    navController.navigate(MainNavigationScreens.Settings.path())
                },
                modifier = Modifier
                    .testId("erx_btn_show_settings")
                    .testTag("erx_btn_show_settings")
                    .semantics {
                        contentDescription = accSettings
                    }
            ) {
                Icon(
                    Icons.Rounded.Menu, null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = @Composable {
            // data matrix code scanner
            IconButton(
                onClick = {
                    navController.navigate(MainNavigationScreens.Camera.path())
                },
                modifier = Modifier
                    .testId("erx_btn_scn_prescription")
                    .semantics { contentDescription = accScan }
            ) {
                Icon(
                    Icons.Rounded.QrCode, null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}
