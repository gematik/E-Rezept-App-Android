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

package de.gematik.ti.erp.app.digas.ui.screen

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.bottombar.AnimatedBottomBar
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.digas.presentation.DigasGraphController
import de.gematik.ti.erp.app.digas.presentation.RedeemDigaController
import de.gematik.ti.erp.app.digas.presentation.rememberRedeemDigasController
import de.gematik.ti.erp.app.digas.ui.component.ActionSection
import de.gematik.ti.erp.app.digas.ui.component.DigaAsyncImage
import de.gematik.ti.erp.app.digas.ui.component.DigaDropdownMenu
import de.gematik.ti.erp.app.digas.ui.component.DigaPrimaryButtonLoading
import de.gematik.ti.erp.app.digas.ui.components.DigaDeleteBlockedDialog
import de.gematik.ti.erp.app.digas.ui.components.DigaDeleteDialog
import de.gematik.ti.erp.app.digas.ui.components.DigaRedeemDialog
import de.gematik.ti.erp.app.digas.ui.model.DigaBfarmUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaSegmentedControllerTap
import de.gematik.ti.erp.app.digas.ui.model.DigasActions
import de.gematik.ti.erp.app.digas.ui.model.ErrorScreenData
import de.gematik.ti.erp.app.digas.ui.model.ErrorScreenDataWithoutRetry
import de.gematik.ti.erp.app.digas.ui.preview.DigaDetailPreviewParameterProvider
import de.gematik.ti.erp.app.digas.ui.preview.DigaOverviewPreviewParameterProvider
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewData
import de.gematik.ti.erp.app.digas.ui.screens.DigaContent
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.profiles.ui.extension.extract
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.topbar.AnimatedTitleContent
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.Banner
import de.gematik.ti.erp.app.utils.compose.BannerClickableIcon
import de.gematik.ti.erp.app.utils.compose.BannerIcon
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.NamedPreviewBox
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.workmanager.listenToWorkManagerState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class DigasMainScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val sharedViewModel: DigasGraphController,
    private val errorScreenData: ErrorScreenData
) : Screen() {
    @Composable
    override fun Content() {
        navBackStackEntry.onReturnAction(DigasRoutes.DigasMainScreen) {
            // reload profile when user comes back to this screen to check for valid sso token
            sharedViewModel.refresh()
        }

        val scope = uiScope
        val listState = rememberLazyListState()
        val context = LocalContext.current
        val dialog = LocalDialog.current
        val haptic = LocalHapticFeedback.current
        val snackbarScaffold = LocalSnackbarScaffold.current
        val intentHandler = LocalIntentHandler.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val codeLabel = stringResource(R.string.code_received)
        val onlyIosAppError = stringResource(R.string.diga_app_not_current_platform)
        val snackbarClose = stringResource(R.string.snackbar_close)

        val taskId = navBackStackEntry.arguments?.getString(DigasRoutes.DIGAS_NAV_TASK_ID)
        val isReady = navBackStackEntry.arguments?.getBoolean(DigasRoutes.DIGAS_IS_READY)
        val controller = rememberRedeemDigasController()

        LaunchedEffect(Unit) { sharedViewModel.updateTaskId(taskId, isReady) }

        // events
        val iosDeepLinkDetectedEvent: ComposableEvent<Unit> = ComposableEvent()
        val deleteDigaEvent: ComposableEvent<Unit> = ComposableEvent()

        // ui-data
        val diga by sharedViewModel.diga.collectAsStateWithLifecycle()
        val digaBfarm by sharedViewModel.digaBfarm.collectAsStateWithLifecycle()

        // redemption rules
        val isRedemptionAllowed by sharedViewModel.isRedemptionAllowed.collectAsStateWithLifecycle()

        // redeem state
        val isRedeeming by controller.isRedeeming.collectAsStateWithLifecycle()
        val isProfileRefreshing by sharedViewModel.isProfileRefreshing.collectAsStateWithLifecycle()
        val isLoadingTask by sharedViewModel.isLoadingTask.collectAsStateWithLifecycle()
        val showLoadingIndicator by remember(isRedeeming, isProfileRefreshing) {
            mutableStateOf(isRedeeming || isProfileRefreshing)
        }

        // ui infos
        val insuranceName by sharedViewModel.insuranceName.collectAsStateWithLifecycle()
        val isConnectionValid by sharedViewModel.isInternetConnected.collectAsStateWithLifecycle()
        val isDownloading by sharedViewModel.isDownloading.collectAsStateWithLifecycle()

        val lastRefreshedTime by sharedViewModel.lastRefreshedOn.collectAsStateWithLifecycle()

        var selectedTab by rememberSaveable { mutableStateOf(DigaSegmentedControllerTap.OVERVIEW) }

        val uriHandler = LocalUriHandler.current
        val bfarmUrl = stringResource(R.string.diga_bfarm_url)

        val onBack: () -> Unit = {
            sharedViewModel.reset()
            navController.navigateUp()
        }

        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                sharedViewModel.refresh()
            }
        }

        ChooseAuthenticationNavigationEventsListener(controller, navController)
        with(controller) {
            onBiometricAuthenticationSuccessEvent.listen {
                sharedViewModel.refresh()
            }
        }

        // switching from redeem to graph controller
        controller.onAutoDownloadEvent.listen { (workManager, workRequest) ->
            workManager.listenToWorkManagerState(
                workRequest = workRequest,
                lifecycleOwner = lifecycleOwner,
                onObserveWorkState = sharedViewModel::observeIsDownloading,
                onWorkInfoSucceeded = sharedViewModel::refresh
            )
        }

        AuthenticationFailureDialog(
            event = controller.showAuthenticationErrorDialog,
            dialogScaffold = dialog
        )

        DigaDeleteDialog(
            dialogScaffold = dialog,
            event = deleteDigaEvent
        ) {
            sharedViewModel.onDeleteDiga()
        }

        DigaDeleteBlockedDialog(
            dialogScaffold = dialog,
            event = sharedViewModel.showDeleteBlockedDialogEvent
        )

        DigaRedeemDialog(
            dialogScaffold = dialog,
            event = controller.redeemEvent,
            onTryAgainRequest = {
                scope.launch {
                    redeem(
                        taskId = taskId,
                        context = context,
                        isRedemptionAllowed = isRedemptionAllowed,
                        controller = controller
                    )
                }
            }
        )

        sharedViewModel.deleteCompletedEvent.listen { onBack() }

        iosDeepLinkDetectedEvent.listen {
            snackbarScaffold.showWithDismissButton(
                message = onlyIosAppError,
                actionLabel = snackbarClose,
                scope = scope
            )
        }

        sharedViewModel.needLoggedInTokenForDeletionEvent.listen { profile ->
            controller.chooseAuthenticationMethod(profile)
        }

        BackHandler { onBack() }

        DigaMainScreenScaffold(
            uiState = diga,
            uiStateBfarm = digaBfarm,
            isLoadingTask = isLoadingTask,
            insuranceName = insuranceName,
            errorScreenData = errorScreenData,
            listState = listState,
            showLoadingIndicator = showLoadingIndicator,
            isConnectionValid = isConnectionValid,
            lastRefreshedTime = lastRefreshedTime,
            selectedTab = selectedTab,
            isDownloading = isDownloading,
            logo = {
                UiStateMachine(state = digaBfarm, onLoading = {
                    Column(
                        modifier = Modifier.shimmer()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.digas_logo_placeholder),
                            contentDescription = stringResource(R.string.diga_Logo_placeholder)
                        )
                    }
                }, onEmpty = {
                        Image(
                            painter = painterResource(R.drawable.digas_logo_placeholder),
                            contentDescription = stringResource(R.string.diga_Logo_placeholder)
                        )
                    }, onContent = { state ->
                        if (state.iconUrl.isNullOrEmpty()) {
                            Image(
                                painter = painterResource(R.drawable.digas_logo_placeholder),
                                contentDescription = stringResource(R.string.diga_Logo_placeholder)
                            )
                        } else {
                            DigaAsyncImage(
                                modifier = Modifier
                                    .size(SizeDefaults.sevenfold)
                                    .clip(CircleShape),
                                url = state.iconUrl ?: "",
                                contentDescription = stringResource(R.string.diga_Logo),
                                contentScale = ContentScale.Fit
                            )
                        }
                    })
            },
            onTabChange = {
                selectedTab = DigaSegmentedControllerTap.entries.toTypedArray()[it]
            },
            actions = DigasActions(
                onClickOnReady = { _ ->
                    scope.launch {
                        redeem(
                            taskId = taskId,
                            context = context,
                            isRedemptionAllowed = isRedemptionAllowed,
                            controller = controller
                        )
                    }
                },
                onClickOnCompletedSuccessfully = {
                    sharedViewModel.onDownloadDiga()
                    diga.data?.deepLink?.let {
                        intentHandler.tryStartingExternalApp(deepLink = it, onIosDeeplink = {
                            iosDeepLinkDetectedEvent.trigger()
                        })
                    }
                },
                onClickOnOpenAppWithRedeemCode = {
                    sharedViewModel.onOpenAppWithRedeemCodeDiga()
                    diga.data?.deepLink?.let {
                        intentHandler.tryStartingExternalApp(deepLink = it, onIosDeeplink = {
                            iosDeepLinkDetectedEvent.trigger()
                        })
                    }
                },
                onClickOnDigaOpen = {
                    diga.data?.deepLink?.let {
                        intentHandler.tryStartingExternalApp(deepLink = it, onIosDeeplink = {
                            iosDeepLinkDetectedEvent.trigger()
                        })
                    }
                },
                onClickOnReadyForSelfArchive = {
                    sharedViewModel.onArchiveDiga()
                    navController.navigateUp()
                },
                onClickOnArchiveRevert = sharedViewModel::onArchiveRevert,
                onClickRefresh = { controller.downloadResources(context) },
                onClickDelete = { deleteDigaEvent.trigger(Unit) },
                onClickCopy = {
                    diga.data?.code?.let {
                        ClipBoardCopy.copyToClipboardWithHaptic(
                            text = it,
                            label = codeLabel,
                            context = context,
                            hapticFeedback = haptic
                        )
                    }
                },
                onShowHowLongValidBottomSheet = {
                    taskId?.let {
                        navController.navigate(DigasRoutes.DigasValidityBottomSheetScreen.path(it))
                    }
                },
                onShowSupportBottomSheet = {
                    digaBfarm.data?.helpUrl?.let {
                        navController.navigate(
                            DigasRoutes.DigaSupportBottomSheetScreen.path(
                                link = it,
                                pdfLink = digaBfarm.data?.handbookUrl
                            )
                        )
                    }
                },
                onShowContributionInfoBottomSheet = {
                    navController.navigate(
                        DigasRoutes.DigaContributionInfoBottomSheetScreen.path()
                    )
                },
                onNavigateToDescriptionScreen = {
                    navController.navigate(
                        DigasRoutes.DigasDescriptionScreen.path(
                            title = diga.data?.name,
                            description = digaBfarm.data?.description
                        )
                    )
                },
                onNavigateToPatient = {
                    taskId?.let {
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailPatientScreen.path(it)
                        )
                    }
                },
                onNavigateToPractitioner = {
                    taskId?.let {
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailPrescriberScreen.path(it)
                        )
                    }
                },
                onNavigateToOrganization = {
                    taskId?.let {
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.path(it)
                        )
                    }
                },
                onNavigateToTechnicalInformation = {
                    taskId?.let {
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.path(it)
                        )
                    }
                },
                onNavigateToInsuranceSearch = {
                    navController.navigate(DigasRoutes.InsuranceSearchListScreen.path())
                },
                onRegisterFeedBack = {
                    sharedViewModel.registerFeedbackPrompt(context)
                },
                onNavigatetoBfarm = {
                    uriHandler.openUriWhenValid(bfarmUrl)
                }
            ),
            onBack = { onBack() }
        )
    }

    private suspend fun redeem(
        taskId: String?,
        context: Context,
        isRedemptionAllowed: Boolean,
        controller: RedeemDigaController
    ) {
        sharedViewModel.activeProfile.extract()?.let { profile ->
            redeemOnCondition(
                taskId = taskId,
                isRedemptionAllowed = isRedemptionAllowed,
                onNotRedeemable = {
                    controller.chooseAuthenticationMethod(profile)
                },
                redeem = { notNullTaskId ->
                    controller.redeem(
                        context = context,
                        profileId = profile.id,
                        telematikId = sharedViewModel.telematikId,
                        taskId = notNullTaskId
                    )
                }
            )
        }
    }

    private fun redeemOnCondition(
        taskId: String?,
        isRedemptionAllowed: Boolean,
        onNotRedeemable: () -> Unit,
        redeem: (String) -> Unit
    ) {
        when {
            !isRedemptionAllowed -> onNotRedeemable()
            !taskId.isNullOrEmpty() -> redeem(taskId)
            else -> {
                // something is wrong and process does not continue
                Napier.e { "DigaMainScreen task-id is missing, something is wrong" }
            }
        }
    }
}

@Composable
fun DigaMainScreenScaffold(
    listState: LazyListState = rememberLazyListState(),
    insuranceName: String?,
    isLoadingTask: Boolean,
    uiState: UiState<DigaMainScreenUiModel>,
    uiStateBfarm: UiState<DigaBfarmUiModel>,
    errorScreenData: ErrorScreenData,
    isDownloading: Boolean,
    logo: @Composable ColumnScope.() -> Unit,
    selectedTab: DigaSegmentedControllerTap,
    lastRefreshedTime: Instant,
    actions: DigasActions,
    isConnectionValid: Boolean,
    showLoadingIndicator: Boolean,
    onTabChange: (Int) -> Unit,
    onBack: () -> Unit = {}
) {
    Box {
        AnimatedElevationScaffold(
            modifier = Modifier.testTag(TestTag.Digas.DigaMainScreen),
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            listState = listState,
            navigationMode = NavigationBarMode.Back,
            onBack = onBack,
            topBarTitle = {
                AnimatedTitleContent(
                    listState = listState,
                    title = uiState.data?.name ?: ""
                )
            },
            actions = {
                DigaDropdownMenu(
                    isDeletable = true,
                    isRedeemableAgain = uiState.data?.canBeRedeemedAgain ?: false,
                    isReadyForRequest = uiState.data?.status == DigaStatus.Ready,
                    isArchivable = uiState.data?.isArchivable() ?: false,
                    onClickRedeemAgain = { actions.onClickOnReady(true) },
                    isArchiveRevertable = uiState.data?.isArchiveRevertable ?: false,
                    onClickRefresh = { actions.onClickOnReady(false) },
                    onClickArchive = { actions.onClickOnReadyForSelfArchive(uiState.data?.status) },
                    onClickRevertArchive = { actions.onClickOnArchiveRevert(uiState.data?.status) },
                    onClickDelete = actions.onClickDelete
                )
            },
            bottomBar = {
                AnimatedBottomBar(
                    listState = listState
                ) {
                    UiStateMachine(
                        state = uiState,
                        onLoading = {
                            Column(
                                modifier = Modifier.background(AppTheme.colors.neutral000)
                            ) {
                                DigaPrimaryButtonLoading()
                            }
                        }
                    ) { state ->
                        Column(
                            modifier = Modifier.background(AppTheme.colors.neutral000)
                        ) {
                            AnimatedVisibility(!isConnectionValid) {
                                Banner(
                                    modifier = Modifier
                                        .padding(horizontal = PaddingDefaults.Medium)
                                        .padding(top = PaddingDefaults.Medium),
                                    startIcon = BannerClickableIcon(BannerIcon.Warning) {},
                                    contentColor = AppTheme.colors.neutral900,
                                    containerColor = AppTheme.colors.neutral100,
                                    borderColor = AppTheme.colors.neutral600,
                                    text = stringResource(R.string.diga_error_no_internet)
                                )
                            }
                            ActionSection(
                                step = state.status,
                                isLoadingTask = isLoadingTask,
                                insuranceName = insuranceName,
                                isArchived = state.isArchived,
                                onClickOnReady = {
                                    actions.onClickOnReady(false)
                                },
                                onClickOnCompletedSuccessfully = actions.onClickOnCompletedSuccessfully,
                                onClickOnOpenAppWithRedeemCode = actions.onClickOnOpenAppWithRedeemCode,
                                onClickOnReadyForSelfArchive = { actions.onClickOnReadyForSelfArchive(state.status) },
                                onClickOnDigaOpen = actions.onClickOnDigaOpen,
                                onClickOnRevertArchive = { actions.onClickOnArchiveRevert(state.status) },
                                onClickOnNavigateToInsuranceSearch = { actions.onNavigateToInsuranceSearch() }
                            )
                        }
                    }
                }
            }
        ) { contentPadding ->
            DigaContent(
                contentPadding = contentPadding,
                listState = listState,
                uiState = uiState,
                uiStateBfarm = uiStateBfarm,
                logo = logo,
                lastRefreshedTime = lastRefreshedTime,
                errorTitle = stringResource(errorScreenData.title),
                errorBody = stringResource(errorScreenData.body),
                isDownloading = isDownloading,
                selectedTab = selectedTab,
                onTabChange = onTabChange,
                actions = actions
            )
        }

        if (showLoadingIndicator) {
            LoadingIndicator()
        }
    }
}

@Suppress("MultipleEmitters")
@LightDarkPreview
@Composable
internal fun DigaMainScreenScaffoldOverviewPreview(
    @PreviewParameter(DigaOverviewPreviewParameterProvider::class) previewData: DigaPreviewData
) {
    PreviewAppTheme {
        NamedPreviewBox(
            name = previewData.name
        ) {
            DigaMainScreenScaffold(
                uiState = previewData.uiData,
                uiStateBfarm = previewData.uiBfarmData,
                insuranceName = null,
                logo = { Image(painter = painterResource(R.drawable.digas_logo_placeholder), contentDescription = null) },
                errorScreenData = ErrorScreenDataWithoutRetry(),
                selectedTab = previewData.selectedTap,
                lastRefreshedTime = Instant.parse("2024-08-01T10:00:00Z"),
                actions = DigasActions(),
                isConnectionValid = true,
                showLoadingIndicator = false,
                isLoadingTask = false,
                isDownloading = false,
                onTabChange = {}
            )
        }
    }
}

@LightDarkLongPreview
@Composable
internal fun DigaMainScreenScaffoldDetailPreview(
    @PreviewParameter(DigaDetailPreviewParameterProvider::class) previewData: DigaPreviewData
) {
    PreviewAppTheme {
        NamedPreviewBox(
            name = previewData.name
        ) {
            DigaMainScreenScaffold(
                uiState = previewData.uiData,
                uiStateBfarm = previewData.uiBfarmData,
                insuranceName = null,
                logo = { Image(painter = painterResource(R.drawable.digas_logo_placeholder), contentDescription = null) },
                errorScreenData = ErrorScreenDataWithoutRetry(),
                selectedTab = previewData.selectedTap,
                actions = DigasActions(),
                lastRefreshedTime = Instant.parse("2024-08-01T10:00:00Z"),
                isConnectionValid = true,
                showLoadingIndicator = false,
                isLoadingTask = false,
                isDownloading = false,
                onTabChange = { }
            )
        }
    }
}
