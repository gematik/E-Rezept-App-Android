/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

@file:Suppress("UsingMaterialAndMaterial3Libraries")

package de.gematik.ti.erp.app.prescription.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.base.model.DownloadResourcesState.Companion.isFinished
import de.gematik.ti.erp.app.base.model.DownloadResourcesState.NotStarted
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.CardWallIntroScreen
import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.mainscreen.model.MultiProfileAppBarWrapper
import de.gematik.ti.erp.app.mainscreen.ui.MultiProfileTopAppBar
import de.gematik.ti.erp.app.mainscreen.ui.RedeemFloatingActionButton
import de.gematik.ti.erp.app.mlkit.navigation.MlKitRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.padding.ApplicationInnerPadding
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberConsentController
import de.gematik.ti.erp.app.pkv.ui.screens.HandleConsentState
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.prescription.presentation.rememberPrescriptionListController
import de.gematik.ti.erp.app.prescription.ui.components.PrescriptionsSection
import de.gematik.ti.erp.app.prescription.ui.components.ProfileLoadingSection
import de.gematik.ti.erp.app.prescription.ui.components.UserNotAuthenticatedDialog
import de.gematik.ti.erp.app.prescription.ui.model.ConsentClickAction
import de.gematik.ti.erp.app.prescription.ui.model.MultiProfileTopAppBarClickAction
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionsScreenContentClickAction
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionScreenPreviewData
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionScreenPreviewParameterProvider
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.pulltorefresh.PullToRefresh
import de.gematik.ti.erp.app.pulltorefresh.extensions.triggerEnd
import de.gematik.ti.erp.app.pulltorefresh.extensions.triggerStart
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
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

class PrescriptionListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("CyclomaticComplexMethod")
    @Composable
    override fun Content() {
        val fabPadding = (LocalActivity.current as? BaseActivity)?.applicationInnerPadding
        val controller = rememberPrescriptionListController()
        val consentController = rememberConsentController()

        val pullToRefreshState = pullToRefreshState
        val snackbar = LocalSnackbarScaffold.current
        val dialog = LocalDialog.current
        val scope = uiScope

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
        val onBack by rememberUpdatedState {
            navController.popBackStack()
        }

        DisposableEffect(Unit) {
            onDispose { controller.trackPrescriptionCounts() }
        }

        val intentHandler = LocalIntentHandler.current
        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                controller.refreshDownload()
            }
        }

        ChooseAuthenticationNavigationEventsListener(controller, navController)
        with(controller) {
            refreshEvent.listen { state ->
                when {
                    state -> pullToRefreshState.triggerStart()
                    else -> pullToRefreshState.triggerEnd()
                }
            }
        }

        navBackStackEntry.onReturnAction(PrescriptionRoutes.PrescriptionListScreen) {
            controller.refreshDownload()
        }

        LaunchedEffect(profileData) {
            if (controller.shouldShowWelcomeDrawer.first()) {
                profileData.data?.let { navController.navigate(CardWallRoutes.CardWallSelectInsuranceTypeBottomSheetScreen.path(it.id)) }
            }
            if (controller.shouldShowGrantConsentDrawer.first()) {
                navController.navigate(PrescriptionRoutes.GrantConsentBottomSheetScreen.path())
            }
        }

        LaunchedEffect(resourcesDownloadedState) {
            if (resourcesDownloadedState.isFinished()) controller.disablePrescriptionRefresh()
        }

        UserNotAuthenticatedDialog(
            event = controller.onUserNotAuthenticatedErrorEvent,
            dialogScaffold = dialog,
            onShowCardWall = {
                profileData.data?.let { activeProfile ->
                    navController.navigate(CardWallIntroScreen.path(activeProfile.id))
                }
            }
        )

        // TODO: handle Consent not Granted on PrescriptionScreen, InvoiceListScreen and PrescriptionDetailsScreen
        HandleConsentState(
            consentState = consentState,
            dialog = dialog,
            onShowCardWall = {
                profileData.data?.let { activeProfile ->
                    navController.navigate(CardWallIntroScreen.path(activeProfile.id))
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

        BackHandler { onBack() }
        PrescriptionListScreenScaffold(
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
            consentClickAction = ConsentClickAction(
                onGetChargeConsent = { profileId -> consentController.getChargeConsent(profileId) }
            ),
            prescriptionClickAction = PrescriptionsScreenContentClickAction(
                onClickLogin = { profile -> controller.chooseAuthenticationMethod(profile) },
                onClickAvatar = { profile -> navController.navigate(ProfileRoutes.ProfileEditPictureBottomSheetScreen.path(profile.id)) },
                onClickArchive = { navController.navigate(PrescriptionRoutes.PrescriptionsArchiveScreen.path()) },
                onChooseAuthenticationMethod = controller::chooseAuthenticationMethod,
                onClickRefresh = controller::refreshDownload,
                onClickPrescription = { taskId, isDiga, isReady ->
                    if (isDiga) {
                        navController.navigate(DigasRoutes.DigasMainScreen.path(taskId, isReady))
                    } else {
                        navController.navigate(PrescriptionDetailRoutes.PrescriptionDetailScreen.path(taskId))
                    }
                },
                onClickRedeem = {
                    if (hasRedeemableTasks) {
                        navController.navigate(RedeemRoutes.HowToRedeemScreen.path())
                    }
                }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrescriptionListScreenScaffold(
    pullToRefreshState: PullToRefreshState,
    listState: LazyListState,
    multiProfileData: MultiProfileAppBarWrapper,
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
                ProfileLoadingSection(
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
                    profileLifecycleState = multiProfileData.profileLifecycleState,
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
@LightDarkPreview
@Composable
internal fun PrescriptionsScreenScaffoldPreview(
    @PreviewParameter(PrescriptionScreenPreviewParameterProvider::class) data: PrescriptionScreenPreviewData
) {
    PreviewAppTheme {
        PrescriptionListScreenScaffold(
            pullToRefreshState = rememberPullToRefreshState(),
            listState = rememberLazyListState(),
            activePrescriptions = data.activePrescription,
            isArchiveEmpty = data.isArchivedEmpty,
            hasRedeemableTasks = data.hasRedeemableTasks,
            multiProfileData = data.multiProfileAppBarWrapper,
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
