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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import de.gematik.ti.erp.app.utils.compose.BottomNavigation
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
import de.gematik.ti.erp.app.utils.compose.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkChatRead
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.systemBarsPadding
import com.google.mlkit.common.sdkinternal.MlKitContext
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.CardWallScreen
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.db.entities.ProfileColors
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.messages.ui.DisplayPickupScreen
import de.gematik.ti.erp.app.messages.ui.MessageScreen
import de.gematik.ti.erp.app.messages.ui.MessageViewModel
import de.gematik.ti.erp.app.onboarding.ui.OnboardingProfile
import de.gematik.ti.erp.app.onboarding.ui.OnboardingScreen
import de.gematik.ti.erp.app.onboarding.ui.ReturningUserSecureAppOnboardingScreen
import de.gematik.ti.erp.app.pharmacy.ui.PharmacySearchScreenWithNavigation
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailsScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScreen
import de.gematik.ti.erp.app.prescription.ui.ScanScreen
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.ui.EditProfileScreen
import de.gematik.ti.erp.app.profiles.ui.connectionTextColor
import de.gematik.ti.erp.app.profiles.ui.connectionText
import de.gematik.ti.erp.app.profiles.ui.profileColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.ui.RedeemScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.tracking.TrackNavigationChanges
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.testId
import de.gematik.ti.erp.app.utils.dateTimeShortText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        mainViewModel.authenticationMethod.collect {
            if (!mainViewModel.isNewUser && !(it == SettingsAuthenticationMethod.Password || it == SettingsAuthenticationMethod.DeviceSecurity)) {
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
            mainViewModel.isNewUser -> {
                MainNavigationScreens.Onboarding.route
            }
            else -> {
                MainNavigationScreens.Prescriptions.route
            }
        }

    TrackNavigationChanges(navController)

    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(MainNavigationScreens.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(MainNavigationScreens.ReturningUserSecureAppOnboarding.route) {
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
        composable(MainNavigationScreens.ProfileSetup.route) {
            OnboardingProfile(isReturningUser = true) {
                mainViewModel.overwriteDefaultProfile(it)
                navController.popBackStack()
            }
        }
        composable(
            MainNavigationScreens.PrescriptionDetail.route,
            MainNavigationScreens.PrescriptionDetail.arguments,
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }
            PrescriptionDetailsScreen(taskId, navController)
        }
        composable(
            MainNavigationScreens.Pharmacies.route,
            MainNavigationScreens.Pharmacies.arguments,
        ) {
            val taskIds =
                remember { requireNotNull(it.arguments?.getParcelable("taskIds") as? TaskIds) }
            PharmacySearchScreenWithNavigation(taskIds, navController)
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
                taskIds,
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
        composable(
            MainNavigationScreens.CardWall.route,
            MainNavigationScreens.CardWall.arguments,
        ) {
            val canAvailable = remember {
                navController.currentBackStackEntry?.arguments?.getBoolean("can") ?: false
            }
            CardWallScreen(onFinishedCardWall = {
                navController.navigate(
                    MainNavigationScreens.Prescriptions.path(),
                    navOptions { popUpTo(MainNavigationScreens.Prescriptions.route) }
                )
            }, canAvailable)
        }
        composable(
            MainNavigationScreens.EditProfile.route,
            MainNavigationScreens.EditProfile.arguments,
        ) {
            val profileId =
                remember { navController.currentBackStackEntry?.arguments?.getInt("profileId")!! }
            EditProfileScreen(
                profileId,
                settingsViewModel,
                onRemoveProfile = {
                    settingsViewModel.removeProfile(profileId, it)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                mainNavController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreenWithScaffold(
    mainNavController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(LocalActivity.current),
    mainScreenVM: MainScreenViewModel = hiltViewModel(LocalActivity.current),
    messageVM: MessageViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        if (mainViewModel.showInsecureDevicePrompt.first()) {
            mainNavController.navigate(MainNavigationScreens.InsecureDeviceScreen.route)
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.showSafetynetPrompt.collect {
            if (!it) {
                mainNavController.navigate(MainNavigationScreens.SafetynetNotOkScreen.route)
            }
        }
    }

    val multiProfile by produceState(initialValue = false) {
        mainViewModel.profilesOn().collect {
            value = it
        }
    }

    if (multiProfile) {
        LaunchedEffect(Unit) {
            mainViewModel.showProfileSetupPrompt.collect {
                if (!it) {
                    mainNavController.navigate(MainNavigationScreens.ProfileSetup.route)
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val currentRedeemEvent by produceState(
        RedeemEvent(
            emptyList(),
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

    val refreshEvent by mainScreenVM.onRefreshEvent.collectAsState(initial = null)

    val refreshEventText = refreshEvent.let {
        when (it) {
            RefreshEvent.NetworkNotAvailable -> errNetworkNotAvailable
            is RefreshEvent.ServerCommunicationFailedWhileRefreshing -> errServerComFailed.format(
                it.code
            )
            RefreshEvent.FatalTruststoreState -> errVau
            is RefreshEvent.NewPrescriptionsEvent -> {
                if (it.nrOfNewPrescriptions == 0) {
                    stringResource(R.string.zero_prescriptions_updatet)
                } else {
                    annotatedPluralsResource(
                        R.plurals.prescriptions_updated,
                        quantity = it.nrOfNewPrescriptions,
                        AnnotatedString(it.nrOfNewPrescriptions.toString())
                    )
                }
            }
            else -> null
        }
    }

    LaunchedEffect(refreshEvent, refreshEventText) {

        if (refreshEventText != null) {
            scaffoldState.snackbarHostState.showSnackbar(
                refreshEventText.toString()
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
                        TaskIds(currentRedeemEvent.taskIds)
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
                        MainNavigationScreens.Pharmacies.path(
                            TaskIds(currentRedeemEvent.taskIds)
                        )
                    )
                } else {
                    Toast.makeText(context, orderNotPossibleTxt, Toast.LENGTH_SHORT).show()
                }
            }

            Box(Modifier.navigationBarsHeight())
        }
    ) {
        val bottomNavController = rememberNavController()

        Scaffold(
            topBar = { MultiProfileTopAppBar(mainNavController, mainScreenVM) },
            bottomBar = { MainScreenBottomNavigation(mainNavController, bottomNavController) },
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
                        MainNavigationScreens.Pharmacies -> "erx_btn_search_pharmacies"
                        MainNavigationScreens.Settings -> "erx_btn_settings"
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
                        MainNavigationScreens.Pharmacies -> Icon(
                            Icons.Outlined.Search, contentDescription = null
                        )
                        MainNavigationScreens.Settings -> Icon(
                            Icons.Outlined.Settings, contentDescription = null
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(
                            when (screen) {
                                MainNavigationScreens.Prescriptions -> R.string.pres_bottombar_prescriptions
                                MainNavigationScreens.Messages -> R.string.pres_bottombar_messages
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
                        if (screen.route == MainNavigationScreens.Pharmacies.route ||
                            screen.route == MainNavigationScreens.Settings.route
                        ) {
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

@Preview(showBackground = true)
@Composable
fun TopAppBarMultiUserPreview() {
    AppTheme {
        TopAppBarMultiUser(mainScreenViewModel = hiltViewModel(LocalActivity.current), {}, {})
    }
}

@Composable
fun TopAppBarMultiUser(
    mainScreenViewModel: MainScreenViewModel,
    onClickEdit: (Int) -> Unit,
    onClickEditProfiles: () -> Unit
) {

    val profileList by produceState(
        initialValue = listOf(
            ProfilesUseCaseData.Profile(
                id = 0,
                name = "",
                active = true,
                color = ProfileColors.SPRING_GRAY,
            )
        )
    ) {
        mainScreenViewModel.profileUiState().collect { value = it }
    }

    val activeProfile = profileList.find {
        it.active
    }!!

    val lastAuthenticatedDate = remember(activeProfile) {
        activeProfile.lastAuthenticated?.let {
            dateTimeShortText(it)
        }
    }

    val activeProfileName = activeProfile.name
    val activeProfileColor = profileColor(activeProfile.color)
    val ssoToken = activeProfile.ssoToken
    val ssoText = connectionText(ssoToken, lastAuthenticatedDate)
    val ssoTextColor = connectionTextColor(profileSsoToken = ssoToken)
    val ssoStatusColor = ssoStatusColor(activeProfile, ssoToken)

    var expanded by remember { mutableStateOf(false) }

    val onClickUser = { profileName: String ->
        mainScreenViewModel.saveActiveProfile(profileName)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .wrapContentWidth()
            .clip(CircleShape)
            .clickable { expanded = !expanded }
            .padding(PaddingDefaults.Tiny)
    ) {
        Avatar(activeProfileName, activeProfileColor, ssoStatusColor)
        Column(
            Modifier.padding(
                start = PaddingDefaults.Small + PaddingDefaults.Tiny,
                end = PaddingDefaults.Medium
            )
        ) {

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeProfileName,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = ssoText,
                color = ssoTextColor,
                style = AppTheme.typography.captionl
            )

            if (expanded) {
                ProfileSelector(
                    onClickEdit = onClickEdit,
                    onClickEditProfiles = onClickEditProfiles,
                    onClickUser = onClickUser,
                    userList = profileList,
                    onDismiss = { expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ssoStatusColor(profile: ProfilesUseCaseData.Profile, ssoToken: SingleSignOnToken?) =
    when {
        ssoToken?.isValid() == true -> AppTheme.colors.green400
        profile.lastAuthenticated != null -> AppTheme.colors.red400
        else -> null
    }

@Composable
private fun ProfileSelector(
    onClickEdit: (Int) -> Unit,
    onClickEditProfiles: () -> Unit,
    onClickUser: (String) -> Unit,
    userList: List<ProfilesUseCaseData.Profile>,
    onDismiss: () -> Unit
) {

    val dismissModifier =
        Modifier.clickable(
            onClick = onDismiss,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )

    Dialog(
        onDismissRequest = onDismiss,
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
                    Box() {
                        Column(modifier = Modifier.padding(bottom = 56.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.select_profile),
                                    style = MaterialTheme.typography.body2,
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
                                userList.forEach {
                                    item {
                                        ProfileCard(it, onClickEdit, onClickUser, onDismiss)
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
    onClickEdit: (Int) -> Unit,
    onClickUser: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = profileColor(profileColors = profile.color)
    val profileSsoToken = profile.ssoToken

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .clickable {
                onClickUser(profile.name)
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
            Avatar(profile.name, colors, null, active = profile.active)

            SpacerSmall()

            Column {
                Text(
                    profile.name, style = MaterialTheme.typography.body1,
                )

                val lastAuthenticatedDateText =
                    remember(profile.lastAuthenticated) {
                        profile.lastAuthenticated?.let {
                            dateTimeShortText(
                                it
                            )
                        }
                    }
                val connectedText = connectionText(profileSsoToken, lastAuthenticatedDateText)
                val connectedColor = connectionTextColor(profileSsoToken)

                Text(
                    connectedText, style = AppTheme.typography.captionl,
                    color = connectedColor,
                )
            }
        }

        TextButton(
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
    mainScreenVieModel: MainScreenViewModel
) {
    val accScan = stringResource(R.string.main_scan_acc)
    val isMultiUserFeatureEnabled by produceState(initialValue = false, producer = {
        mainScreenVieModel.isMultiUserFeatureEnabled().collect { value = it }
    })

    TopAppBar(
        title = {
            if (isMultiUserFeatureEnabled) {
                TopAppBarMultiUser(
                    mainScreenVieModel,
                    onClickEditProfiles = {
                        navController.navigate(
                            MainNavigationScreens.Settings.path(
                                SettingsScrollTo.Profiles
                            )
                        )
                    },
                    onClickEdit = { navController.navigate(MainNavigationScreens.EditProfile.path(it)) }
                )
            } else {
                Text(stringResource(R.string.main_nav_title_prescriptions))
            }
        },
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface,
        actions = @Composable {
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

private fun isMlKitInitialized() =
    try {
        MlKitContext.getInstance()
        true
    } catch (_: Exception) {
        false
    }

@Composable
private fun MlKitPermissionDialog(
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
