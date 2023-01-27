/*
 * Copyright (c) 2023 gematik GmbH
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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkChatRead
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.ui.UnlockEgKScreen
import de.gematik.ti.erp.app.cardwall.ui.CardWallScreen
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.debug.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.license.ui.LicenseScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingNavigationScreens
import de.gematik.ti.erp.app.onboarding.ui.OnboardingScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingSecureAppMethod
import de.gematik.ti.erp.app.onboarding.ui.ReturningUserSecureAppOnboardingScreen
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.orders.ui.MessageScreen
import de.gematik.ti.erp.app.orders.ui.OrderScreen
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyNavigation
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailsScreen
import de.gematik.ti.erp.app.prescription.ui.ArchiveScreen
import de.gematik.ti.erp.app.prescription.ui.MlKitInformationScreen
import de.gematik.ti.erp.app.prescription.ui.MlKitIntroScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionViewModel
import de.gematik.ti.erp.app.prescription.ui.ScanPrescriptionViewModel
import de.gematik.ti.erp.app.prescription.ui.ScanScreen
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.DefaultProfile
import de.gematik.ti.erp.app.profiles.ui.EditProfileScreen
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.ProfileImageCropper
import de.gematik.ti.erp.app.profiles.ui.ProfileSettingsViewModel
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.ui.RedeemNavigation
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.AllowBiometryScreen
import de.gematik.ti.erp.app.settings.ui.PharmacyLicenseScreen
import de.gematik.ti.erp.app.settings.ui.SecureAppWithPassword
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.BottomNavigation
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.TopAppBarWithContent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberViewModel
import java.time.Instant

@Suppress("LongMethod")
@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    mainScreenViewModel: MainScreenViewModel,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel
) {
    LaunchedEffect(Unit) {
        mainViewModel.authenticationMethod.collect {
            if (!mainViewModel.showOnboarding && !(
                it is SettingsData.AuthenticationMode.Password ||
                    it == SettingsData.AuthenticationMode.DeviceSecurity
                )
            ) {
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
            val dataProtectionVersionAccepted: Instant? by mainViewModel
                .dataProtectionVersionAcceptedOn()
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
            MainScreenWithScaffold(
                mainNavController = navController,
                mainViewModel = mainViewModel,
                mainScreenViewModel = mainScreenViewModel,
                settingsViewModel = settingsViewModel,
                profileSettingsViewModel = profileSettingsViewModel
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
            PharmacyNavigation(
                mainScreenViewModel = mainScreenViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onFinish = {
                    runBlocking(Dispatchers.Main) {
                        navController.popBackStack(MainNavigationScreens.Prescriptions.route, false)
                    }
                }
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
        composable(MainNavigationScreens.MlKitIntroScreen.route) {
            MlKitIntroScreen(
                navController,
                mainViewModel
            )
        }
        composable(MainNavigationScreens.MlKitInformationScreen.route) {
            MlKitInformationScreen(
                navController
            )
        }
        composable(MainNavigationScreens.IntegrityNotOkScreen.route) {
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
            MainNavigationScreens.Redeem.route,
            MainNavigationScreens.Redeem.arguments
        ) {
            RedeemNavigation(
                mainScreenViewModel = mainScreenViewModel,
                onFinish = {
                    navController.popBackStack(MainNavigationScreens.Prescriptions.route, false)
                }
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
            MainNavigationScreens.ProfileImageCropper.route,
            MainNavigationScreens.ProfileImageCropper.arguments
        ) {
            val profileId = remember { it.arguments!!.getString("profileId")!! }

            ProfileImageCropper(
                onSaveCroppedImage = {
                    profileSettingsViewModel.savePersonalizedProfileImage(profileId, it)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            MainNavigationScreens.UnlockEgk.route,
            MainNavigationScreens.UnlockEgk.arguments
        ) {
            val unlockMethod = remember { it.arguments!!.getString("unlockMethod") }

            NavigationAnimation(mode = navigationMode) {
                UnlockEgKScreen(
                    unlockMethod = when (unlockMethod) {
                        UnlockMethod.ChangeReferenceData.name -> UnlockMethod.ChangeReferenceData
                        UnlockMethod.ResetRetryCounter.name -> UnlockMethod.ResetRetryCounter
                        UnlockMethod.ResetRetryCounterWithNewSecret.name -> UnlockMethod.ResetRetryCounterWithNewSecret
                        else -> UnlockMethod.None
                    },
                    navController = navController,
                    onClickLearnMore = {
                        navController.navigate(
                            MainNavigationScreens.OrderHealthCard.path()
                        )
                    }
                )
            }
        }

        composable(
            MainNavigationScreens.Archive.route
        ) {
            val prescriptionViewModel by rememberViewModel<PrescriptionViewModel>()

            NavigationAnimation(mode = navigationMode) {
                ArchiveScreen(prescriptionViewModel = prescriptionViewModel, navController = navController) {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Suppress("LongMethod", "ComplexMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreenWithScaffold(
    mainNavController: NavController,
    mainViewModel: MainViewModel,
    mainScreenViewModel: MainScreenViewModel,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel
) {
    val profileHandler = LocalProfileHandler.current
    val bottomNavController = rememberNavController()

    val currentBottomNavigationRoute by bottomNavController.currentBackStackEntryFlow.collectAsState(null)

    val isInPrescriptionScreen by remember {
        derivedStateOf {
            currentBottomNavigationRoute?.destination?.route == MainNavigationScreens.Prescriptions.route
        }
    }

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
        if (BuildConfig.DEBUG) {
            return@LaunchedEffect
        }
        if (!mainViewModel.checkDeviceIntegrity().first()) {
            withContext(Dispatchers.Main) {
                mainNavController.navigate(MainNavigationScreens.IntegrityNotOkScreen.route)
            }
        }
    }

    val scaffoldState = rememberScaffoldState()

    MainScreenSnackbar(
        mainScreenViewModel = mainScreenViewModel,
        scaffoldState = scaffoldState
    )

    OrderSuccessHandler(mainScreenViewModel)

    var mainScreenBottomSheetContentState: MainScreenBottomSheetContentState? by remember { mutableStateOf(null) }

    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = {
            it != ModalBottomSheetValue.HalfExpanded
        }
    )
    LaunchedEffect(Unit) {
        sheetState.snapTo(ModalBottomSheetValue.Hidden)
    }

    LaunchedEffect(mainScreenBottomSheetContentState) {
        if (mainScreenBottomSheetContentState != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    LaunchedEffect(Unit) {
        if (mainViewModel.showWelcomeDrawer.first()) {
            mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.Connect
        }
    }

    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.targetValue == ModalBottomSheetValue.Hidden) {
            if (mainScreenBottomSheetContentState == MainScreenBottomSheetContentState.Connect) {
                mainViewModel.welcomeDrawerShown()
            }
            mainScreenBottomSheetContentState = null
        }
    }

    var profileToRename by remember {
        mutableStateOf(DefaultProfile)
    }

    val toolTipBounds = remember {
        mutableStateOf<Map<Int, Rect>>(emptyMap())
    }

    ToolTips(mainViewModel, isInPrescriptionScreen, toolTipBounds)

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
                settingsViewModel = settingsViewModel,
                mainViewModel = mainViewModel,
                profileSettingsViewModel = profileSettingsViewModel,
                infoContentState = mainScreenBottomSheetContentState,
                mainNavController = mainNavController,
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

        var topBarElevated by remember { mutableStateOf(true) }

        Scaffold(
            modifier = Modifier.testTag(TestTag.Main.MainScreen),
            topBar = {
                if (currentBottomNavigationRoute?.destination?.route != MainNavigationScreens.Settings.route) {
                    MultiProfileTopAppBar(
                        navController = mainNavController,
                        elevated = topBarElevated,
                        mainScreenViewModel = mainScreenViewModel,
                        mainViewModel = mainViewModel,
                        isInPrescriptionScreen = isInPrescriptionScreen,
                        onClickAddProfile = {
                            mainScreenBottomSheetContentState =
                                MainScreenBottomSheetContentState.EditOrAddProfileName(addProfile = true)
                        },
                        onClickChangeProfileName = { profile ->
                            profileToRename = profile
                            mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.EditOrAddProfileName()
                        },
                        tooltipBounds = toolTipBounds
                    )
                }
            },
            bottomBar = {
                MainScreenBottomNavigation(
                    navController = mainNavController,
                    viewModel = mainScreenViewModel,
                    bottomNavController = bottomNavController,
                    profileId = profileHandler.activeProfile.id
                )
            },
            floatingActionButton = {
                if (isInPrescriptionScreen) {
                    RedeemFloatingActionButton(
                        onClick = {
                            mainNavController.navigate(MainNavigationScreens.Redeem.path())
                        }
                    )
                }
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
                            onClickAvatar = {
                                mainScreenBottomSheetContentState = MainScreenBottomSheetContentState.EditProfile
                            },
                            prescriptionViewModel = prescriptionViewModel,
                            mainScreenViewModel = mainScreenViewModel,
                            onElevateTopBar = {
                                topBarElevated = it
                            },
                            onClickArchive = { mainNavController.navigate(MainNavigationScreens.Archive.path()) }
                        )
                    }
                    composable(MainNavigationScreens.Orders.route) {
                        OrderScreen(
                            mainNavController = mainNavController,
                            mainScreenViewModel = mainScreenViewModel,
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
    profileId: ProfileIdentifier
) {
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadMessagesAvailable by viewModel.unreadMessagesAvailable(profileId)
        .collectAsState(initial = false)

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface,
        extraContent = {}
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
fun MainScreenTopBarTitle(isInPrescriptionScreen: Boolean) {
    val text = if (isInPrescriptionScreen) {
        stringResource(R.string.pres_bottombar_prescriptions)
    } else {
        stringResource(R.string.orders_title)
    }
    Text(
        text = text,
        style = AppTheme.typography.h5,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ProfilesChipBar(
    mainScreenViewModel: MainScreenViewModel,
    onClickAddProfile: () -> Unit,
    onClickChangeProfileName: (profile: ProfilesUseCaseData.Profile) -> Unit,
    tooltipBounds: MutableState<Map<Int, Rect>>,
    toolTipBoundsRequired: Boolean
) {
    val profileHandler = LocalProfileHandler.current
    val profiles = profileHandler.profiles.value
    val scope = rememberCoroutineScope()
    val rowState = rememberLazyListState()

    var indexOfActiveProfile by remember { mutableStateOf(0) }

    LaunchedEffect(indexOfActiveProfile) {
        delay(timeMillis = 300L)
        rowState.animateScrollToItem(indexOfActiveProfile)
    }

    LazyRow(
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaddingDefaults.Medium, bottom = PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            SpacerSmall()
        }
        profiles.forEachIndexed { index, profile ->
            if (profile.id == profileHandler.activeProfile.id) {
                indexOfActiveProfile = index + 1
            }

            item {
                ProfileChip(
                    profile = profile,
                    mainScreenViewModel = mainScreenViewModel,
                    selected = profile.id == profileHandler.activeProfile.id,
                    onClickChip = { scope.launch { profileHandler.switchActiveProfile(profile) } },
                    onClickChangeProfileName = onClickChangeProfileName,
                    tooltipBounds = tooltipBounds,
                    toolTipBoundsRequired = toolTipBoundsRequired
                )
                SpacerSmall()
            }
        }
        item {
            AddProfileChip(
                onClickAddProfile = onClickAddProfile,
                tooltipBounds = tooltipBounds,
                toolTipBoundsRequired = toolTipBoundsRequired
            )
            SpacerMedium()
        }
    }
}

@Composable
fun ssoStatusColor(profile: ProfilesUseCaseData.Profile, ssoTokenScope: IdpData.SingleSignOnTokenScope?) =
    when {
        ssoTokenScope?.token?.isValid() == true -> AppTheme.colors.green400
        profile.lastAuthenticated != null -> AppTheme.colors.neutral400
        else -> null
    }

/**
 * The top appbar of the actual main screen.
 */
@Composable
fun MultiProfileTopAppBar(
    navController: NavController,
    mainScreenViewModel: MainScreenViewModel,
    mainViewModel: MainViewModel,
    isInPrescriptionScreen: Boolean,
    elevated: Boolean,
    onClickAddProfile: () -> Unit,
    onClickChangeProfileName: (profile: ProfilesUseCaseData.Profile) -> Unit,
    tooltipBounds: MutableState<Map<Int, Rect>>
) {
    val accScan = stringResource(R.string.main_scan_acc)
    val elevation = remember(elevated) { if (elevated) AppBarDefaults.TopAppBarElevation else 0.dp }

    val toolTipBoundsRequired by produceState(initialValue = false) {
        mainViewModel.showMainScreenToolTips().collect {
            value = it
        }
    }

    val scope = rememberCoroutineScope()

    TopAppBarWithContent(
        title = {
            MainScreenTopBarTitle(isInPrescriptionScreen)
        },
        elevation = elevation,
        backgroundColor = AppTheme.colors.neutral025,
        actions = @Composable {
            if (isInPrescriptionScreen) {
                // data matrix code scanner
                IconButton(
                    onClick = {
                        scope.launch {
                            if (mainViewModel.mlKitNotAccepted().first()) {
                                navController.navigate(MainNavigationScreens.MlKitIntroScreen.path())
                            } else {
                                navController.navigate(MainNavigationScreens.Camera.path())
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("erx_btn_scn_prescription")
                        .semantics { contentDescription = accScan }
                        .onGloballyPositioned { coordinates ->
                            if (toolTipBoundsRequired) {
                                tooltipBounds.value += Pair(0, coordinates.boundsInRoot())
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircle,
                        contentDescription = null,
                        tint = AppTheme.colors.primary700,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        content = {
            ProfilesChipBar(
                mainScreenViewModel = mainScreenViewModel,
                onClickAddProfile = onClickAddProfile,
                onClickChangeProfileName = onClickChangeProfileName,
                tooltipBounds = tooltipBounds,
                toolTipBoundsRequired = toolTipBoundsRequired
            )
        }
    )
}
