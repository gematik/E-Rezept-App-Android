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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkChatRead
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.compose.foundation.layout.systemBarsPadding
import com.google.mlkit.common.sdkinternal.MlKitContext
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.TestTag.Main.Profile.OpenProfileListButton
import de.gematik.ti.erp.app.cardwall.ui.CardWallScreen
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.onboarding.ui.OnboardingNavigationScreens
import de.gematik.ti.erp.app.onboarding.ui.OnboardingScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingSecureAppMethod
import de.gematik.ti.erp.app.onboarding.ui.ReturningUserSecureAppOnboardingScreen
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyNavigation
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailsScreen
import de.gematik.ti.erp.app.prescription.ui.HomeNoHealthCardSignInHint
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionViewModel
import de.gematik.ti.erp.app.prescription.ui.ScanPrescriptionViewModel
import de.gematik.ti.erp.app.prescription.ui.ScanScreen
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.ui.EditProfileScreen
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.ProfileSettingsViewModel
import de.gematik.ti.erp.app.profiles.ui.connectionText
import de.gematik.ti.erp.app.profiles.ui.connectionTextColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.ui.RedeemScreen
import de.gematik.ti.erp.app.settings.ui.AllowBiometryScreen
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.cardunlock.ui.UnlockEgKScreen
import de.gematik.ti.erp.app.debug.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.license.ui.LicenseScreen
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.orders.ui.MessageScreen
import de.gematik.ti.erp.app.orders.ui.OrderScreen
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.PharmacyLicenseScreen
import de.gematik.ti.erp.app.settings.ui.SecureAppWithPassword
import de.gematik.ti.erp.app.utils.compose.BottomNavigation
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.TopAppBarWithContent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.utils.dateTimeShortText
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberViewModel
import java.time.Instant

@Suppress("LongMethod")
@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel
) {
    LaunchedEffect(Unit) {
        mainViewModel.authenticationMethod.collect {
            if (!mainViewModel.showOnboarding && !(it is SettingsData.AuthenticationMode.Password || it == SettingsData.AuthenticationMode.DeviceSecurity)) {
                navController.navigate(MainNavigationScreens.ReturningUserSecureAppOnboarding.path()) {
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
            mainViewModel.showOnboarding -> {
                MainNavigationScreens.Onboarding.route
            }
            else -> {
                MainNavigationScreens.Prescriptions.route
            }
        }

    TrackNavigationChanges(navController)
    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)
    var selectedPrescriptionScreenTab by remember { mutableStateOf(PrescriptionTabs.Redeemable) }
    var secureMethod by rememberSaveable { mutableStateOf<OnboardingSecureAppMethod>(OnboardingSecureAppMethod.None) }
    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(MainNavigationScreens.Onboarding.route) {
            OnboardingScreen(
                mainNavController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(MainNavigationScreens.ReturningUserSecureAppOnboarding.route) {
            ReturningUserSecureAppOnboardingScreen(
                navController,
                secureMethod = secureMethod,
                onSecureMethodChange = { secureMethod = it },
                settingsViewModel = settingsViewModel
            )
        }
        composable(OnboardingNavigationScreens.Biometry.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowBiometryScreen(
                    onBack = { navController.popBackStack() },
                    onNext = { navController.popBackStack() },
                    onSecureMethodChange = { secureMethod = it }
                )
            }
        }
        composable(MainNavigationScreens.DataTermsUpdateScreen.route) {
            val dataProtectionVersionAccepted: Instant? by mainViewModel.dataProtectionVersionAcceptedOn()
                .collectAsState(initial = null)

            dataProtectionVersionAccepted?.let { acceptedOn ->
                DataTermsUpdateScreen(
                    acceptedOn,
                    onClickDataTerms = { navController.navigate(MainNavigationScreens.DataProtection.route) }
                ) {
                    mainViewModel.acceptUpdatedDataTerms()
                    navController.navigate(MainNavigationScreens.Prescriptions.route)
                }
            }
        }
        composable(MainNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { navController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
        composable(
            MainNavigationScreens.Settings.route,
            MainNavigationScreens.Settings.arguments
        ) {
            SettingsScreen(
                mainNavController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(MainNavigationScreens.Camera.route) {
            val scanViewModel by rememberViewModel<ScanPrescriptionViewModel>()
            ScanScreen(mainNavController = navController, scanViewModel = scanViewModel)
        }
        composable(MainNavigationScreens.Prescriptions.route) {
            val mainScreenVM by rememberViewModel<MainScreenViewModel>()
            MainScreenWithScaffold(
                mainNavController = navController,
                selectedTab = selectedPrescriptionScreenTab,
                onSelectedTab = { selectedPrescriptionScreenTab = it },
                mainViewModel = mainViewModel,
                mainScreenViewModel = mainScreenVM,
                settingsViewModel = settingsViewModel
            )
        }

        composable(
            MainNavigationScreens.PrescriptionDetail.route,
            MainNavigationScreens.PrescriptionDetail.arguments
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }
            PrescriptionDetailsScreen(taskId = taskId, mainNavController = navController)
        }
        composable(
            MainNavigationScreens.Pharmacies.route,
            MainNavigationScreens.Pharmacies.arguments
        ) {
            val mainScreenVM by rememberViewModel<MainScreenViewModel>()
            PharmacyNavigation(
                mainNavController = navController,
                mainScreenVM = mainScreenVM
            )
        }
        composable(MainNavigationScreens.InsecureDeviceScreen.route) {
            InsecureDeviceScreen(
                navController,
                mainViewModel,
                stringResource(id = R.string.insecure_device_title),
                painterResource(id = R.drawable.laptop_woman_yellow),
                stringResource(id = R.string.insecure_device_header),
                stringResource(id = R.string.insecure_device_info),
                stringResource(id = R.string.insecure_device_accept)
            )
        }
        composable(MainNavigationScreens.SafetynetNotOkScreen.route) {
            InsecureDeviceScreen(
                navController,
                mainViewModel,
                stringResource(id = R.string.insecure_device_title_safetynet),
                painterResource(id = R.drawable.laptop_woman_pink),
                stringResource(id = R.string.insecure_device_header_safetynet),
                stringResource(id = R.string.insecure_device_info_safetynet),
                stringResource(id = R.string.insecure_device_accept_safetynet),
                pinUseCase = false
            )
        }
        composable(
            MainNavigationScreens.RedeemLocally.route,
            MainNavigationScreens.RedeemLocally.arguments
        ) {
            val taskIds =
                remember { requireNotNull(it.arguments?.getParcelable("taskIds") as? TaskIds) }
            RedeemScreen(
                taskIds = taskIds,
                navController = navController
            )
        }
        composable(
            MainNavigationScreens.Messages.route,
            MainNavigationScreens.Messages.arguments
        ) {
            val orderId =
                remember { it.arguments?.getString("orderId")!! }

            MessageScreen(
                orderId = orderId,
                mainNavController = navController
            )
        }
        composable(
            MainNavigationScreens.CardWall.route,
            MainNavigationScreens.CardWall.arguments
        ) {
            val profileId =
                remember { it.arguments?.getString("profileId")!! }
            CardWallScreen(
                navController,
                onResumeCardWall = {
                    navController.navigate(
                        MainNavigationScreens.Prescriptions.path(),
                        navOptions {
                            popUpTo(MainNavigationScreens.Prescriptions.route) {
                                inclusive = true
                            }
                        }
                    )
                },
                profileId = profileId
            )
        }
        composable(
            MainNavigationScreens.EditProfile.route,
            MainNavigationScreens.EditProfile.arguments
        ) {
            val profileId =
                remember { navController.currentBackStackEntry?.arguments?.getString("profileId")!! }
            EditProfileScreen(
                profileId,
                settingsViewModel,
                profileSettingsViewModel,
                onBack = { navController.popBackStack() },
                mainNavController = navController
            )
        }
        composable(MainNavigationScreens.Debug.route) {
            DebugScreenWrapper(navController)
        }
        composable(MainNavigationScreens.Terms.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    title = stringResource(R.string.onb_terms_of_use),
                    onBack = { navController.popBackStack() },
                    url = URI_TERMS_OF_USE
                )
            }
        }
        composable(MainNavigationScreens.Imprint.route) {
            NavigationAnimation(mode = navigationMode) {
                LegalNoticeWithScaffold(
                    navController
                )
            }
        }
        composable(MainNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { navController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
        composable(MainNavigationScreens.OpenSourceLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                LicenseScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(MainNavigationScreens.AdditionalLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyLicenseScreen {
                    navController.popBackStack()
                }
            }
        }
        composable(MainNavigationScreens.AllowAnalytics.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowAnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    onAllowAnalytics = {
                        if (it) {
                            settingsViewModel.onTrackingAllowed()
                        } else {
                            settingsViewModel.onTrackingDisallowed()
                        }
                    }
                )
            }
        }
        composable(MainNavigationScreens.Password.route) {
            NavigationAnimation(mode = navigationMode) {
                SecureAppWithPassword(
                    navController,
                    settingsViewModel
                )
            }
        }
        composable(MainNavigationScreens.OrderHealthCard.route) {
            HealthCardContactOrderScreen(onBack = { navController.popBackStack() })
        }
        composable(
            MainNavigationScreens.EditProfile.route,
            MainNavigationScreens.EditProfile.arguments
        ) {
            val profileId = remember { it.arguments!!.getString("profileId")!! }

            val state by produceState(SettingsScreen.defaultState) {
                settingsViewModel.screenState().collect {
                    value = it
                }
            }

            state.profileById(profileId)?.let { profile ->
                EditProfileScreen(
                    state,
                    profile,
                    settingsViewModel,
                    profileSettingsViewModel,
                    onRemoveProfile = {
                        settingsViewModel.removeProfile(profile, it)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                    mainNavController = navController
                )
            }
        }
        composable(
            MainNavigationScreens.UnlockEgk.route,
            MainNavigationScreens.UnlockEgk.arguments
        ) {
            val changeSecret = remember { it.arguments!!.getBoolean("changeSecret") }

            NavigationAnimation(mode = navigationMode) {
                UnlockEgKScreen(
                    changeSecret = changeSecret,
                    navController = navController,
                    onClickLearnMore = {
                        navController.navigate(
                            MainNavigationScreens.OrderHealthCard.path()
                        )
                    }
                )
            }
        }
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreenWithScaffold(
    mainNavController: NavController,
    selectedTab: PrescriptionTabs,
    onSelectedTab: (PrescriptionTabs) -> Unit,
    mainViewModel: MainViewModel,
    mainScreenViewModel: MainScreenViewModel,
    settingsViewModel: SettingsViewModel
) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            if (mainViewModel.showDataTermsUpdate.first()) {
                mainNavController.navigate(
                    MainNavigationScreens.DataTermsUpdateScreen.path(),
                    navOptions {
                        launchSingleTop = true
                        popUpTo(MainNavigationScreens.Prescriptions.path()) {
                            inclusive = true
                        }
                    }
                )
            } else if (mainViewModel.showInsecureDevicePrompt.first()) {
                mainNavController.navigate(MainNavigationScreens.InsecureDeviceScreen.path())
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.showSafetynetPrompt.collect {
            if (!it) {
                withContext(Dispatchers.Main) {
                    mainNavController.navigate(MainNavigationScreens.SafetynetNotOkScreen.route)
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    LaunchedEffect(Unit) {
        sheetState.snapTo(ModalBottomSheetValue.Hidden)
    }

    val scaffoldState = rememberScaffoldState()

    MainScreenSnackbar(
        mainScreenViewModel = mainScreenViewModel,
        scaffoldState = scaffoldState
    )

    OrderSuccessDialog(mainScreenViewModel)

    val coroutineScope = rememberCoroutineScope()

    val profileHandler = LocalProfileHandler.current
    val redeemState = rememberRedeemState(profileHandler.activeProfile)
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            RedeemBottomSheetContent(
                redeemState = redeemState,
                onClickLocalRedeem = {
                    mainNavController.navigate(
                        MainNavigationScreens.RedeemLocally.path(
                            TaskIds(it)
                        )
                    )
                },
                onClickOnlineRedeem = {
                    mainNavController.navigate(
                        MainNavigationScreens.Pharmacies.path(
                            TaskIds(it)
                        )
                    )
                }
            )
        }
    ) {
        val bottomNavController = rememberNavController()

        val currentBottomNavigationRoute by bottomNavController
            .currentBackStackEntryFlow
            .collectAsState(null)

        var emptyScreenState by remember { mutableStateOf(PrescriptionScreenData.EmptyActiveScreenState.NotEmpty) }

        val showLogInHint by produceState(false, key1 = emptyScreenState, key2 = selectedTab) {
            bottomNavController.currentBackStackEntryFlow.collect {
                value = emptyScreenState == PrescriptionScreenData.EmptyActiveScreenState.NeverConnected &&
                    selectedTab == PrescriptionTabs.Redeemable &&
                    it.destination.route == MainNavigationScreens.Prescriptions.route
            }
        }

        // TODO: move to general place?
        ExternalAuthenticationDialog()

        var topBarElevated by remember { mutableStateOf(true) }
        Scaffold(
            modifier = Modifier.testTag(TestTag.Main.MainScreen),
            topBar = {
                val isInPrescriptionScreen by derivedStateOf {
                    currentBottomNavigationRoute?.destination?.route == MainNavigationScreens.Prescriptions.route
                }

                if (currentBottomNavigationRoute?.destination?.route != MainNavigationScreens.Settings.route) {
                    MultiProfileTopAppBar(
                        navController = mainNavController,
                        bottomNavController = bottomNavController,
                        title = when (currentBottomNavigationRoute?.destination?.route) {
                            MainNavigationScreens.Prescriptions.route ->
                                stringResource(R.string.pres_bottombar_prescriptions)
                            MainNavigationScreens.Orders.route ->
                                stringResource(R.string.pres_bottombar_orders)
                            else -> ""
                        },
                        elevated = topBarElevated,
                        tabBar = {
                            RedeemAndArchiveTabs(
                                selectedTab = selectedTab,
                                onSelectedTab = onSelectedTab
                            )
                        },
                        scanButtonVisible = isInPrescriptionScreen,
                        tabBarVisible = isInPrescriptionScreen
                    )
                }
            },
            bottomBar = {
                MainScreenBottomNavigation(
                    navController = mainNavController,
                    viewModel = mainScreenViewModel,
                    bottomNavController = bottomNavController,
                    profileId = profileHandler.activeProfile.id,
                    signInHint = if (showLogInHint) {
                        {
                            HomeNoHealthCardSignInHint(
                                onClickAction = {
                                    coroutineScope.launch {
                                        mainNavController.navigate(
                                            MainNavigationScreens.CardWall.path(profileHandler.activeProfile.id)
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        null
                    }
                )
            },
            floatingActionButton = {
                val showRedeemFab by derivedStateOf {
                    redeemState.hasRedeemableTasks && selectedTab == PrescriptionTabs.Redeemable &&
                        currentBottomNavigationRoute?.destination?.route == MainNavigationScreens.Prescriptions.route
                }
                RedeemFloatingActionButton(
                    visible = showRedeemFab,
                    onClick = {
                        coroutineScope.launch {
                            sheetState.show()
                        }
                    }
                )
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
                        val prescriptionViewModel by rememberViewModel<PrescriptionViewModel>()
                        PrescriptionScreen(
                            navController = mainNavController,
                            selectedTab = selectedTab,
                            onEmptyScreenChange = { emptyScreenState = it },
                            prescriptionViewModel = prescriptionViewModel,
                            onElevateTopBar = {
                                topBarElevated = it
                            }
                        )
                    }
                    composable(MainNavigationScreens.Orders.route) {
                        OrderScreen(
                            mainNavController = mainNavController,
                            onElevateTopBar = {
                                topBarElevated = it
                            }
                        )
                    }
                    composable(
                        MainNavigationScreens.Settings.route,
                        MainNavigationScreens.Settings.arguments
                    ) {
                        SettingsScreen(
                            mainNavController = mainNavController,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenBottomNavigation(
    navController: NavController,
    bottomNavController: NavController,
    viewModel: MainScreenViewModel,
    profileId: ProfileIdentifier,
    signInHint: (@Composable () -> Unit)? = null
) {
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadMessagesAvailable by viewModel.unreadMessagesAvailable(profileId)
        .collectAsState(initial = false)

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface,
        extraContent = {
            AnimatedVisibility(
                visible = signInHint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) { signInHint?.invoke() }
        }
    ) {
        MainScreenBottomNavigationItems.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier.testTag(
                    when (screen) {
                        MainNavigationScreens.Prescriptions -> TestTag.BottomNavigation.PrescriptionButton
                        MainNavigationScreens.Orders -> TestTag.BottomNavigation.OrdersButton
                        MainNavigationScreens.Pharmacies -> TestTag.BottomNavigation.PharmaciesButton
                        MainNavigationScreens.Settings -> TestTag.BottomNavigation.SettingsButton
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

                        MainNavigationScreens.Orders -> Icon(
                            if (unreadMessagesAvailable) Icons.Outlined.MarkChatUnread else Icons.Outlined.MarkChatRead,
                            null
                        )

                        MainNavigationScreens.Pharmacies -> Icon(
                            Icons.Outlined.Search,
                            contentDescription = null
                        )

                        MainNavigationScreens.Settings -> Icon(
                            Icons.Outlined.Settings,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(
                            when (screen) {
                                MainNavigationScreens.Prescriptions -> R.string.pres_bottombar_prescriptions
                                MainNavigationScreens.Orders -> R.string.pres_bottombar_orders
                                MainNavigationScreens.Pharmacies -> R.string.pres_bottombar_pharmacies
                                MainNavigationScreens.Settings -> R.string.main_settings_acc
                                else -> R.string.pres_bottombar_prescriptions
                            }
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentRoute == screen.route,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != screen.route) {
                        when (screen.route) {
                            MainNavigationScreens.Pharmacies.route ->
                                navController.navigate(screen.path())
                            else ->
                                bottomNavController.navigate(screen.path())
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TopAppBarMultiUserTitle(
    onClickEdit: (String) -> Unit,
    title: String,
    onClickEditProfiles: () -> Unit
) {
    val profileHandler = LocalProfileHandler.current
    val ssoTokenScope = profileHandler.activeProfile.ssoTokenScope
    val ssoStatusColor = ssoStatusColor(profileHandler.activeProfile, ssoTokenScope)

    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .wrapContentWidth()
            .clip(CircleShape)
            .clickable { expanded = !expanded }
            .padding(PaddingDefaults.Tiny)
            .visualTestTag(OpenProfileListButton)
    ) {
        Avatar(Modifier.size(36.dp), profile = profileHandler.activeProfile, ssoStatusColor)
        SpacerShortMedium()
        SpacerSmall()
        Text(
            text = title,
            style = AppTheme.typography.h6,
            fontWeight = FontWeight.W500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        SpacerMedium()
        if (expanded) {
            val scope = rememberCoroutineScope()

            ProfileSelector(
                onClickEdit = onClickEdit,
                onClickEditProfiles = onClickEditProfiles,
                onClickProfile = {
                    scope.launch { profileHandler.switchActiveProfile(it) }
                },
                userList = profileHandler.profiles,
                onDismiss = { expanded = false }
            )
        }
    }
}

@Composable
fun ssoStatusColor(profile: ProfilesUseCaseData.Profile, ssoTokenScope: IdpData.SingleSignOnTokenScope?) =
    when {
        ssoTokenScope?.token?.isValid() == true -> AppTheme.colors.green400
        profile.lastAuthenticated != null -> AppTheme.colors.red400
        else -> null
    }

@Composable
fun ProfileSelector(
    onClickEdit: (String) -> Unit,
    onClickEditProfiles: () -> Unit,
    onClickProfile: (ProfilesUseCaseData.Profile) -> Unit,
    userList: State<List<ProfilesUseCaseData.Profile>>,
    onDismiss: () -> Unit
) {
    val dismissModifier =
        Modifier.clickable(
            onClick = onDismiss,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            Modifier
                .semantics(false) { }
                .fillMaxSize()
                .then(dismissModifier)
                .background(SolidColor(Color.Black), alpha = 0.5f)
                .systemBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(PaddingDefaults.Medium),
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(28.dp),
                elevation = 8.dp
            ) {
                AnimatedVisibility(
                    visibleState = remember { MutableTransitionState(false) }.apply {
                        targetState = true
                    },
                    enter = expandVertically() + fadeIn(),
                    exit = ExitTransition.None

                ) {
                    Box {
                        Column(modifier = Modifier.padding(bottom = 56.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.select_profile),
                                    style = AppTheme.typography.body2,
                                    color = AppTheme.colors.neutral600,
                                    modifier = Modifier
                                        .padding(
                                            start = PaddingDefaults.Medium,
                                            end = PaddingDefaults.Medium,
                                            top = PaddingDefaults.Medium,
                                            bottom = PaddingDefaults.Medium / 2
                                        )
                                        .weight(1f)
                                        .testTag("ProfileSelector")
                                )
                                IconButton(onClick = { onDismiss() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        tint = AppTheme.colors.primary600,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            val listState = rememberLazyListState()

                            LazyColumn(
                                modifier = Modifier.testTag("profileList"),
                                state = listState
                            ) {
                                userList.value.forEach {
                                    item {
                                        ProfileCard(
                                            profile = it,
                                            onClickEdit = onClickEdit,
                                            onClickProfile = onClickProfile,
                                            onDismiss = onDismiss
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                            Divider(color = AppTheme.colors.neutral300)
                            TextButton(
                                onClick = { onClickEditProfiles() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(text = stringResource(R.string.edit_profiles))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: ProfilesUseCaseData.Profile,
    onClickEdit: (String) -> Unit,
    onClickProfile: (profile: ProfilesUseCaseData.Profile) -> Unit,
    onDismiss: () -> Unit
) {
    val profileSsoTokenScope = profile.ssoTokenScope

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .clickable {
                onClickProfile(profile)
                onDismiss()
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            Avatar(Modifier.size(36.dp), profile, null, active = profile.active)

            SpacerSmall()

            Column {
                Text(
                    profile.name,
                    style = AppTheme.typography.body1
                )

                val lastAuthenticatedDateText =
                    remember(profile.lastAuthenticated) {
                        profile.lastAuthenticated?.let {
                            dateTimeShortText(
                                it
                            )
                        }
                    }
                val connectedText = connectionText(profileSsoTokenScope?.token, lastAuthenticatedDateText)
                val connectedColor = connectionTextColor(profileSsoTokenScope?.token)

                Text(
                    connectedText,
                    style = AppTheme.typography.caption1l,
                    color = connectedColor
                )
            }
        }

        TextButton(
            modifier = Modifier.visualTestTag(TestTag.Main.Profile.ProfileDetailsButton),
            onClick = {
                onClickEdit(profile.id)
            }
        ) {
            Text(text = "Details")
        }

        SpacerTiny()
    }
}

/**
 * The top appbar of the actual main screen.
 */
@Composable
fun MultiProfileTopAppBar(
    navController: NavController,
    bottomNavController: NavController,
    tabBar: @Composable () -> Unit,
    title: String,
    elevated: Boolean,
    scanButtonVisible: Boolean = false,
    tabBarVisible: Boolean = false
) {
    val accScan = stringResource(R.string.main_scan_acc)
    val elevation = remember(elevated) { if (elevated) AppBarDefaults.TopAppBarElevation else 0.dp }
    TopAppBarWithContent(
        title = {
            TopAppBarMultiUserTitle(
                title = title,
                onClickEditProfiles = {
                    bottomNavController.navigate(
                        MainNavigationScreens.Settings.path()
                    )
                },
                onClickEdit = {
                    navController.navigate(MainNavigationScreens.EditProfile.path(it))
                }
            )
        },
        elevation = elevation,
        backgroundColor = MaterialTheme.colors.surface,
        actions = @Composable {
            if (scanButtonVisible) {
                var showMlKitPermissionDialog by remember { mutableStateOf(false) }

                if (showMlKitPermissionDialog) {
                    MlKitPermissionDialog(
                        onAccept = {
                            navController.navigate(MainNavigationScreens.Camera.path())
                            showMlKitPermissionDialog = false
                        },
                        onDecline = {
                            showMlKitPermissionDialog = false
                        }
                    )
                }

                // data matrix code scanner
                IconButton(
                    onClick = {
                        if (!isMlKitInitialized()) {
                            showMlKitPermissionDialog = true
                        } else {
                            navController.navigate(MainNavigationScreens.Camera.path())
                        }
                    },
                    modifier = Modifier
                        .testTag("erx_btn_scn_prescription")
                        .semantics { contentDescription = accScan }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode,
                        contentDescription = null,
                        tint = AppTheme.colors.primary700,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        content = {
            if (tabBarVisible) {
                tabBar()
            }
        }
    )
}

fun isMlKitInitialized() =
    try {
        MlKitContext.getInstance()
        true
    } catch (_: Exception) {
        false
    }

@Composable
fun MlKitPermissionDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    CommonAlertDialog(
        header = stringResource(R.string.cam_accept_mlkit_title),
        info = stringResource(R.string.cam_accept_mlkit_body),
        cancelText = stringResource(R.string.cam_accept_mlkit_decline),
        actionText = stringResource(R.string.cam_accept_mlkit_accept),
        onCancel = onDecline,
        onClickAction = onAccept
    )
}
