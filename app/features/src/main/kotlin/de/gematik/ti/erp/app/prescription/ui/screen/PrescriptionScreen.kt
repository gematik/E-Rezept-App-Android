/*
 * Copyright 2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

@file:Suppress("UsingMaterialAndMaterial3Libraries")

package de.gematik.ti.erp.app.prescription.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarResult
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app.ApplicationInnerPadding
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.base.model.DownloadResourcesState.Companion.isFinished
import de.gematik.ti.erp.app.base.model.DownloadResourcesState.NotStarted
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.model.MultiProfileAppBarFlowWrapper
import de.gematik.ti.erp.app.mainscreen.ui.MultiProfileTopAppBar
import de.gematik.ti.erp.app.mainscreen.ui.RedeemFloatingActionButton
import de.gematik.ti.erp.app.mlkit.navigation.MlKitRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.ConsentValidator
import de.gematik.ti.erp.app.pkv.presentation.rememberConsentController
import de.gematik.ti.erp.app.pkv.ui.screens.HandleConsentState
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.prescription.presentation.rememberPrescriptionsController
import de.gematik.ti.erp.app.prescription.ui.components.UserNotAuthenticatedDialog
import de.gematik.ti.erp.app.prescription.ui.components.archiveSection
import de.gematik.ti.erp.app.prescription.ui.components.emptyContentSection
import de.gematik.ti.erp.app.prescription.ui.components.prescriptionContentSection
import de.gematik.ti.erp.app.prescription.ui.components.profileConnectorSection
import de.gematik.ti.erp.app.prescription.ui.model.ConsentClickAction
import de.gematik.ti.erp.app.prescription.ui.model.MultiProfileTopAppBarClickAction
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionsScreenContentClickAction
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionScreenPreviewData
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionScreenPreviewParameterProvider
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.pulltorefresh.PullToRefresh
import de.gematik.ti.erp.app.pulltorefresh.extensions.trigger
import de.gematik.ti.erp.app.pulltorefresh.extensions.triggerEnd
import de.gematik.ti.erp.app.pulltorefresh.extensions.triggerStart
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val ZERO_DAYS_LEFT = 0
const val ONE_DAY_LEFT = 1
const val TWO_DAYS_LEFT = 2

class PrescriptionsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("CyclomaticComplexMethod")
    @Composable
    override fun Content() {
        val fabPadding = (LocalActivity.current as? BaseActivity)?.applicationInnerPadding
        val controller = rememberPrescriptionsController()
        val consentController = rememberConsentController()

        val pullToRefreshState = rememberPullToRefreshState()
        val snackbar = LocalSnackbarScaffold.current
        val dialog = LocalDialog.current
        val intentHandler = LocalIntentHandler.current
        val scope = rememberCoroutineScope()

        val actionString = stringResource(R.string.consent_action_to_invoices)
        val consentRevokedInfo = stringResource(R.string.consent_revoked_info)
        val consentGrantedInfo = stringResource(R.string.consent_granted_info)

        val activePrescriptions by controller.activePrescriptions.collectAsStateWithLifecycle()
        val isArchiveEmpty by controller.isArchiveEmpty.collectAsStateWithLifecycle()
        val hasRedeemableTasks by controller.hasRedeemableTasks.collectAsStateWithLifecycle()

        val profileData by controller.activeProfile.collectAsStateWithLifecycle()
        val resourcesDownloadedState by controller.resourcesDownloadedState.collectAsState(NotStarted)

        val mlKitAccepted by controller.isMLKitAccepted.collectAsStateWithLifecycle()
        val consentState by consentController.consentState.collectAsStateWithLifecycle()
        var topBarElevated by remember { mutableStateOf(true) }

        DisposableEffect(Unit) {
            onDispose { controller.trackPrescriptionCounts() }
        }

        with(controller) {
            showCardWallEvent.listen { profileId ->
                navController.navigate(CardWallRoutes.CardWallIntroScreen.path(profileId))
            }
            showCardWallWithFilledCanEvent.listen { cardWallData ->
                navController.navigate(
                    CardWallRoutes.CardWallPinScreen.path(
                        profileIdentifier = cardWallData.profileId,
                        can = cardWallData.can
                    )
                )
            }
            refreshEvent.listen { state ->
                when {
                    state -> pullToRefreshState.triggerStart()
                    else -> pullToRefreshState.triggerEnd()
                }
            }
            showGidEvent.listen { gidData ->
                navController.navigate(
                    CardWallRoutes.CardWallIntroScreen.pathWithGid(
                        profileIdentifier = gidData.profileId,
                        gidEventData = gidData
                    )
                )
            }
        }

        navBackStackEntry.onReturnAction(PrescriptionRoutes.PrescriptionsScreen) {
            controller.refreshDownload()
        }

        LaunchedEffect(true) {
            if (controller.shouldShowWelcomeDrawer.first()) {
                navController.navigate(PrescriptionRoutes.WelcomeDrawerBottomSheetScreen.path())
            }
            if (controller.shouldShowGrantConsentDrawer.first()) {
                navController.navigate(PrescriptionRoutes.GrantConsentBottomSheetScreen.path())
            }
        }

        LaunchedEffect(resourcesDownloadedState) {
            if (resourcesDownloadedState.isFinished()) controller.disablePrescriptionRefresh()
        }

        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                controller.refreshDownload()
            }
        }

        UserNotAuthenticatedDialog(
            event = controller.onUserNotAuthenticatedErrorEvent,
            dialogScaffold = dialog,
            onShowCardWall = {
                profileData.data?.let { activeProfile ->
                    navController.navigate(CardWallRoutes.CardWallIntroScreen.path(activeProfile.id))
                }
            }
        )

        // TODO: handle Consent not Granted on PrescriptionScreen, InvoiceListScreen and PrescriptionDetailsScreen
        HandleConsentState(
            consentState = consentState,
            dialog = dialog,
            onShowCardWall = {
                profileData.data?.let { activeProfile ->
                    navController.navigate(CardWallRoutes.CardWallIntroScreen.path(activeProfile.id))
                }
            },
            onRetry = { consentContext ->
                profileData.data?.let { activeProfile ->
                    when (consentContext) {
                        ConsentContext.GetConsent -> consentController.getChargeConsent(activeProfile.id)
                        ConsentContext.GrantConsent -> consentController.grantChargeConsent(activeProfile.id)
                        ConsentContext.RevokeConsent -> {} // revoke is not available on mainScreen
                    }
                }
            },
            onConsentGranted = {
                scope.launch {
                    val result =
                        snackbar.showSnackbar(
                            message = consentGrantedInfo,
                            actionLabel = actionString
                        )
                    when (result) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed ->
                            profileData.data?.id?.let {
                                navController.navigate(PkvRoutes.InvoiceListScreen.path(it))
                            }
                    }
                }
            },
            onConsentRevoked = {
                scope.launch {
                    val result =
                        snackbar.showSnackbar(
                            message = consentRevokedInfo,
                            actionLabel = actionString
                        )
                    when (result) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed ->
                            profileData.data?.id?.let {
                                navController.navigate(PkvRoutes.InvoiceListScreen.path(it))
                            }
                    }
                }
            }
        )

        PrescriptionsScreenScaffold(
            pullToRefreshState = pullToRefreshState,
            listState = listState,
            isTopBarElevated = topBarElevated,
            fabPadding = fabPadding,
            multiProfileData = controller.multiProfileData,
            profileData = profileData,
            activePrescriptions = activePrescriptions,
            isArchiveEmpty = isArchiveEmpty,
            hasRedeemableTasks = hasRedeemableTasks,
            consentState = consentState,
            topAppBarClickAction = MultiProfileTopAppBarClickAction(
                onClickAddProfile = { navController.navigate(ProfileRoutes.ProfileAddNameBottomSheetScreen.path()) },
                onClickChangeProfileName = { profile -> navController.navigate(ProfileRoutes.ProfileEditNameBottomSheetScreen.path(profile.id)) },
                onClickAddScannedPrescription = {
                    when {
                        mlKitAccepted -> navController.navigate(PrescriptionRoutes.PrescriptionScanScreen.path())
                        else -> navController.navigate(MlKitRoutes.MlKitScreen.path())
                    }
                },
                onSwitchActiveProfile = { profile ->
                    controller.disablePrescriptionRefresh()
                    controller.switchActiveProfile(profile.id)
                },
                onElevateTopAppBar = { topBarElevated = it }
            ),
            prescriptionClickAction = PrescriptionsScreenContentClickAction(
                onClickLogin = { profile -> controller.chooseAuthenticationMethod(profile.id) },
                onClickAvatar = { profile -> navController.navigate(ProfileRoutes.ProfileEditPictureBottomSheetScreen.path(profile.id)) },
                onClickArchive = { navController.navigate(PrescriptionRoutes.PrescriptionsArchiveScreen.path()) },
                onClickPrescription = { taskId -> navController.navigate(PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId)) },
                onChooseAuthenticationMethod = { profileId -> controller.chooseAuthenticationMethod(profileId) },
                onClickRedeem = { navController.navigate(RedeemRoutes.RedeemMethodSelection.path()) },
                onClickRefresh = controller::refreshDownload
            ),
            consentClickAction = ConsentClickAction(
                onGetChargeConsent = { profileId -> consentController.getChargeConsent(profileId) }
            )
        )

        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Scaffold(
                isFloatingActionButtonDocked = true,
                floatingActionButtonPosition = FabPosition.End,
                topBar = {
                    MultiProfileTopAppBar(
                        multiProfileData = controller.multiProfileData,
                        elevated = topBarElevated,
                        onClickAddProfile = { navController.navigate(ProfileRoutes.ProfileAddNameBottomSheetScreen.path()) },
                        onClickChangeProfileName = { profile -> navController.navigate(ProfileRoutes.ProfileEditNameBottomSheetScreen.path(profile.id)) },
                        onClickAddPrescription = {
                            when {
                                mlKitAccepted -> navController.navigate(PrescriptionRoutes.PrescriptionScanScreen.path())
                                else -> navController.navigate(MlKitRoutes.MlKitScreen.path())
                            }
                        },
                        switchActiveProfile = { profile ->
                            controller.disablePrescriptionRefresh()
                            controller.switchActiveProfile(profile.id)
                        }
                    )
                },
                floatingActionButton = {
                    if (hasRedeemableTasks) {
                        RedeemFloatingActionButton(
                            modifier = fabPadding?.applicationScaffoldPadding?.let { Modifier.padding(it) } ?: Modifier,
                            onClick = {
                                navController.navigate(RedeemRoutes.RedeemMethodSelection.path())
                            }
                        )
                    }
                }
            ) {
                Column {
                    ProfileSection(
                        profileData = profileData,
                        consentState = consentState,
                        pullToRefreshState = pullToRefreshState,
                        onRefresh = {
                            controller.refreshDownload()
                        },
                        onGetConsent = { profileId ->
                            consentController.getChargeConsent(profileId)
                        },
                        onChooseAuthenticationMethod = { profileId ->
                            controller.chooseAuthenticationMethod(profileId)
                        }
                    )
                    PrescriptionsSection(
                        modifier = Modifier.padding(it),
                        listState = listState,
                        activeProfile = profileData,
                        activePrescriptions = activePrescriptions,
                        isArchiveEmpty = isArchiveEmpty,
                        onClickRefresh = controller::refreshDownload,
                        onClickLogin = {
                            profileData.data?.let { activeProfile ->
                                controller.chooseAuthenticationMethod(activeProfile.id)
                            }
                        },
                        onClickAvatar = {
                            profileData.data?.let { activeProfile ->
                                navController.navigate(
                                    ProfileRoutes.ProfileEditPictureBottomSheetScreen.path(activeProfile.id)
                                )
                            }
                        },
                        onElevateTopBar = { topBarElevated = it },
                        onClickArchive = {
                            navController.navigate(PrescriptionRoutes.PrescriptionsArchiveScreen.path())
                        },
                        onClickPrescription = { taskId ->
                            navController.navigate(
                                PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId)
                            )
                        }
                    )
                }
            }
            PullToRefresh(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = SizeDefaults.sixfoldAndQuarter),
                pullToRefreshState = pullToRefreshState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionsScreenScaffold(
    pullToRefreshState: PullToRefreshState,
    listState: LazyListState,
    multiProfileData: MultiProfileAppBarFlowWrapper,
    profileData: UiState<ProfilesUseCaseData.Profile>,
    activePrescriptions: UiState<List<Prescription>>,
    fabPadding: ApplicationInnerPadding?,
    isTopBarElevated: Boolean,
    hasRedeemableTasks: Boolean,
    consentState: ConsentState,
    isArchiveEmpty: Boolean,
    prescriptionClickAction: PrescriptionsScreenContentClickAction,
    topAppBarClickAction: MultiProfileTopAppBarClickAction,
    consentClickAction: ConsentClickAction
) {
    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Scaffold(
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.End,
            topBar = {
                MultiProfileTopAppBar(
                    multiProfileData = multiProfileData,
                    elevated = isTopBarElevated,
                    onClickAddProfile = topAppBarClickAction.onClickAddProfile,
                    onClickChangeProfileName = topAppBarClickAction.onClickChangeProfileName,
                    onClickAddPrescription = topAppBarClickAction.onClickAddScannedPrescription,
                    switchActiveProfile = topAppBarClickAction.onSwitchActiveProfile
                )
            },
            floatingActionButton = {
                if (hasRedeemableTasks) {
                    RedeemFloatingActionButton(
                        modifier = fabPadding?.applicationScaffoldPadding?.let { Modifier.padding(it) } ?: Modifier,
                        onClick = prescriptionClickAction.onClickRedeem
                    )
                }
            }
        ) {
            Column {
                ProfileSection(
                    profileData = profileData,
                    consentState = consentState,
                    pullToRefreshState = pullToRefreshState,
                    onRefresh = prescriptionClickAction.onClickRefresh,
                    onGetConsent = consentClickAction.onGetChargeConsent,
                    onChooseAuthenticationMethod = prescriptionClickAction.onChooseAuthenticationMethod
                )
                PrescriptionsSection(
                    modifier = Modifier.padding(it),
                    listState = listState,
                    activeProfile = profileData,
                    activePrescriptions = activePrescriptions,
                    isArchiveEmpty = isArchiveEmpty,
                    onClickRefresh = prescriptionClickAction.onClickRefresh,
                    onClickLogin = {
                        profileData.data?.let { activeProfile ->
                            prescriptionClickAction.onClickLogin(activeProfile)
                        }
                    },
                    onClickAvatar = {
                        profileData.data?.let { activeProfile ->
                            prescriptionClickAction.onClickAvatar(activeProfile)
                        }
                    },
                    onClickArchive = prescriptionClickAction.onClickArchive,
                    onClickPrescription = prescriptionClickAction.onClickPrescription,
                    onElevateTopBar = topAppBarClickAction.onElevateTopAppBar
                )
            }
        }
        PullToRefresh(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = SizeDefaults.sixfoldAndQuarter),
            pullToRefreshState = pullToRefreshState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSection(
    profileData: UiState<ProfilesUseCaseData.Profile>,
    pullToRefreshState: PullToRefreshState,
    consentState: ConsentState,
    onGetConsent: (id: ProfileIdentifier) -> Unit,
    onChooseAuthenticationMethod: (id: ProfileIdentifier) -> Unit,
    onRefresh: () -> Unit
) {
    UiStateMachine(
        state = profileData,
        onError = {
            ErrorScreenComponent(
                onClickRetry = onRefresh
            )
        },
        onEmpty = {
            Center {
                CircularProgressIndicator()
            }
        },
        onLoading = {
            Center {
                CircularProgressIndicator()
            }
        },
        onContent = { activeProfile ->
            val ssoTokenValid = activeProfile.isSSOTokenValid()
            LaunchedEffect(activeProfile) {
                if (activeProfile.isPkv()) {
                    ConsentValidator.validateAndExecute(
                        isSsoTokenValid = ssoTokenValid,
                        consentState = consentState,
                        getChargeConsent = {
                            onRefresh()
                            onGetConsent(activeProfile.id)
                        },
                        onConsentGranted = onRefresh
                    )
                } else {
                    onRefresh()
                }
            }

            @Requirement(
                "A_24857#1",
                sourceSpecification = "gemSpec_eRp_FdV",
                rationale = "Refreshing the prescription list happens only if the user is authenticated. " +
                    "If the user is not authenticated, the user is prompted to authenticate."
            )
            with(pullToRefreshState) {
                trigger(
                    block = {
                        if (activeProfile.isPkv()) {
                            ConsentValidator.validateAndExecute(
                                isSsoTokenValid = ssoTokenValid,
                                consentState = consentState,
                                getChargeConsent = {
                                    onRefresh()
                                    onGetConsent(activeProfile.id)
                                },
                                onConsentGranted = onRefresh
                            )
                        } else {
                            onRefresh()
                        }
                    },
                    onNavigation = {
                        if (!ssoTokenValid) {
                            endRefresh()
                            onChooseAuthenticationMethod(activeProfile.id)
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun PrescriptionsSection(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    activeProfile: UiState<ProfilesUseCaseData.Profile>,
    activePrescriptions: UiState<List<Prescription>>,
    isArchiveEmpty: Boolean,
    onElevateTopBar: (Boolean) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickLogin: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickAvatar: () -> Unit,
    onClickArchive: () -> Unit
) {
    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }.collect {
            onElevateTopBar(it)
        }
    }

    UiStateMachine(
        state = activePrescriptions,
        onLoading = {
            Center {
                CircularProgressIndicator()
            }
        },
        onError = {
            ErrorScreenComponent(
                onClickRetry = onClickLogin
            )
        },
        onEmpty = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTag.Prescriptions.Content),
                state = listState,
                contentPadding = PaddingValues(bottom = SizeDefaults.eightfoldAndThreeQuarter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                emptyContentSection(
                    activeProfile = activeProfile,
                    onClickConnect = onClickLogin,
                    onClickAvatar = onClickAvatar
                )
                archiveSection(
                    isArchiveEmpty = isArchiveEmpty,
                    onClickArchive = onClickArchive
                )
            }
        },
        onContent = { prescriptions ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .testTag(TestTag.Prescriptions.Content),
                state = listState,
                contentPadding = PaddingValues(bottom = SizeDefaults.eightfoldAndThreeQuarter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                profileConnectorSection(
                    activeProfile = activeProfile,
                    onClickAvatar = onClickAvatar,
                    onClickLogin = onClickLogin,
                    onClickRefresh = onClickRefresh
                )
                prescriptionContentSection(
                    activePrescriptions = prescriptions,
                    onClickPrescription = onClickPrescription
                )
                archiveSection(
                    isArchiveEmpty = isArchiveEmpty,
                    onClickArchive = onClickArchive
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreview
@Composable
fun PrescriptionsScreenScaffoldPreview(
    @PreviewParameter(PrescriptionScreenPreviewParameterProvider::class) data: PrescriptionScreenPreviewData
) {
    PreviewAppTheme {
        PrescriptionsScreenScaffold(
            pullToRefreshState = rememberPullToRefreshState(),
            listState = rememberLazyListState(),
            activePrescriptions = data.activePrescription,
            isArchiveEmpty = data.isArchivedEmpty,
            hasRedeemableTasks = data.hasRedeemableTasks,
            multiProfileData = data.multiProfileAppBarFlowWrapper,
            profileData = data.profileData,
            consentState = data.consentState,
            isTopBarElevated = data.isTopBarElevated,
            fabPadding = data.fabPadding,
            prescriptionClickAction = data.prescriptionsClickAction,
            topAppBarClickAction = data.topAppBarClickAction,
            consentClickAction = data.consentClickAction
        )
    }
}
