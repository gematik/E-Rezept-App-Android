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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.AppModel
import de.gematik.ti.erp.app.core.component1
import de.gematik.ti.erp.app.core.component2
import de.gematik.ti.erp.app.messages.ui.MessageScreen
import de.gematik.ti.erp.app.messages.ui.MessageViewModel
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScreen
import de.gematik.ti.erp.app.redeem.ui.NavArgTaskIds
import de.gematik.ti.erp.app.redeem.ui.RedeemScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val frNavController = AppModel.frNavController

    NavHost(
        navController,
        startDestination = MainNavigationScreens.Main.route
    ) {
        composable(MainNavigationScreens.Main.route) {
            MainScreenWithScaffold(navController, frNavController)
        }
        composable(
            MainNavigationScreens.Redeem.route + "/{$NavArgTaskIds}",
            arguments = listOf(navArgument(NavArgTaskIds) { type = NavType.StringType })
        ) {
            val taskIds =
                requireNotNull(
                    navController.currentBackStackEntry?.arguments?.getString(
                        NavArgTaskIds
                    )
                )
            RedeemScreen(
                taskIds.split(","),
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable(MainNavigationScreens.LegalNotice.route) { LegalNoticeWithScaffold(navigation = navController) }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun MainScreenWithScaffold(mainNavCtr: NavController, frNavController: NavController) {
    val (mainVM: MainScreenViewModel, messageVM: MessageViewModel) = AppModel.viewModels

    val navController = rememberNavController()

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val currentRedeemEvent by produceState(
        RedeemEvent(
            "",
            false
        )
    ) {
        mainVM.onRedeemEvent.collect {
            value = it
            sheetState.show()
        }
    }

    LaunchedEffect(Unit) {
        sheetState.snapTo(ModalBottomSheetValue.Hidden)
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
                mainNavCtr.navigate(MainNavigationScreens.Redeem.route + "/${currentRedeemEvent.taskIds}")
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
                    val taskIds = currentRedeemEvent.taskIds
                    val action =
                        MainScreenFragmentDirections.actionMainScreenFragmentToPharmacySearchFragment(
                            taskIds
                        )
                    frNavController.navigate(action)
                } else {
                    Toast.makeText(context, orderNotPossibleTxt, Toast.LENGTH_SHORT).show()
                }
            }
        }
    ) {
        Scaffold(
            topBar = { MainScreenTopAppBar() },
            bottomBar = {
                MainScreenBottomNavigation(navController, mainVM)
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController,
                    startDestination = MainBottomNavigationScreens.Prescriptions.route
                ) {
                    composable(MainBottomNavigationScreens.Prescriptions.route) {
                        PrescriptionScreen()
                    }
                    composable(MainBottomNavigationScreens.Messages.route) {
                        MessageScreen(frNavController, messageVM)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScreenBottomNavigation(
    navController: NavController,
    viewModel: MainScreenViewModel
) {
    val frNavController = AppModel.frNavController
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadMessagesAvailable by viewModel.unreadMessagesAvailable()
        .collectAsState(initial = false)

    BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
        MainScreenBottomNavigationItems.forEach { screen ->
            BottomNavigationItem(
                selectedContentColor = AppTheme.colors.primary700,
                unselectedContentColor = AppTheme.colors.neutral600,
                icon = {
                    when (screen) {
                        MainBottomNavigationScreens.Prescriptions -> Icon(
                            painterResource(R.drawable.ic_logo_outlined),
                            null,
                            modifier = Modifier.size(24.dp)
                        )
                        MainBottomNavigationScreens.Messages -> Icon(
                            if (unreadMessagesAvailable) Icons.Outlined.MarkChatUnread else Icons.Outlined.MarkChatRead,
                            null
                        )
                        MainBottomNavigationScreens.PharmacySearch -> Icon(
                            Icons.Outlined.Search, contentDescription = null
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(
                            when (screen) {
                                MainBottomNavigationScreens.Prescriptions -> R.string.pres_bottombar_prescriptions
                                MainBottomNavigationScreens.Messages -> R.string.pres_bottombar_messages
                                MainBottomNavigationScreens.PharmacySearch -> R.string.pres_bottombar_pharmacies
                            }
                        ),
                        modifier = Modifier.testId(
                            when (screen) {
                                MainBottomNavigationScreens.Prescriptions -> "erx_btn_prescriptions"
                                MainBottomNavigationScreens.Messages -> "erx_btn_messages"
                                MainBottomNavigationScreens.PharmacySearch -> "erx_btn_search_pharmacies"
                            }
                        )
                    )
                },
                selected = currentRoute == screen.route,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != screen.route) {
                        if (screen.route == MainBottomNavigationScreens.PharmacySearch.route) {
                            val action =
                                MainScreenFragmentDirections.actionMainScreenFragmentToPharmacySearchFragment(
                                    null
                                )
                            frNavController.navigate(action)
                        } else {
                            navController.navigate(screen.route)
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
fun MainScreenTopAppBar() {
    val frNavCtr = AppModel.frNavController
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
                    frNavCtr.navigate(
                        MainScreenFragmentDirections.actionMainScreenFragmentToSettingsFragment(
                            SettingsScrollTo.None
                        )
                    )
                },
                modifier = Modifier
                    .testId("erx_btn_show_settings")
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
                    frNavCtr.navigate(MainScreenFragmentDirections.actionMainScreenFragmentToScanPrescriptionFragment())
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
