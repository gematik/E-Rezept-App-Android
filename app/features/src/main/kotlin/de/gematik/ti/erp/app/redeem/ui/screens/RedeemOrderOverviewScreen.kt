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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.bottombar.AnimatedBottomBar
import de.gematik.ti.erp.app.button.SelectionSummaryButton
import de.gematik.ti.erp.app.button.SelectionSummaryButtonData
import de.gematik.ti.erp.app.button.selectionSummaryButtonText
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments.Companion.from
import de.gematik.ti.erp.app.pharmacy.model.orderID
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.ui.components.TopBarColor
import de.gematik.ti.erp.app.pharmacy.ui.components.VideoContent
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.redeem.mapper.getText
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.ContactValidationState
import de.gematik.ti.erp.app.redeem.model.ContactValidationState.Companion.redeemValidationState
import de.gematik.ti.erp.app.redeem.model.RedeemContactValidationState
import de.gematik.ti.erp.app.redeem.model.RedeemEventModel.ProcessStateEvent
import de.gematik.ti.erp.app.redeem.model.RedeemEventModel.RedeemClickEvent
import de.gematik.ti.erp.app.redeem.navigation.RedeemRouteBackStackEntryArguments
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemSharedViewModel
import de.gematik.ti.erp.app.redeem.presentation.RedeemOrderOverviewScreenController
import de.gematik.ti.erp.app.redeem.presentation.RedeemPrescriptionsController
import de.gematik.ti.erp.app.redeem.presentation.rememberOrderOverviewScreenController
import de.gematik.ti.erp.app.redeem.presentation.rememberRedeemPrescriptionsController
import de.gematik.ti.erp.app.redeem.ui.components.OrderSelectedServicesRow
import de.gematik.ti.erp.app.redeem.ui.components.RedeemButton
import de.gematik.ti.erp.app.redeem.ui.components.RedeemContactInformationSection
import de.gematik.ti.erp.app.redeem.ui.components.RedeemContactMissingSection
import de.gematik.ti.erp.app.redeem.ui.components.RedeemStateHandler.handleRedeemedState
import de.gematik.ti.erp.app.redeem.ui.components.SelfPayerPrescriptionWarning
import de.gematik.ti.erp.app.redeem.ui.components.selectVideoSource
import de.gematik.ti.erp.app.redeem.ui.preview.PrescriptionSelectionSectionParameter
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenParameter
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.Banner
import de.gematik.ti.erp.app.utils.compose.BannerClickableIcon
import de.gematik.ti.erp.app.utils.compose.BannerIcon
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.NavigateBackButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.coroutines.flow.collectLatest

private const val OrderSuccessVideoAspectRatio = 1.69f

class RedeemOrderOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val sharedViewModel: OnlineRedeemSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val dialog = LocalDialog.current

        navBackStackEntry.onReturnAction(RedeemRoutes.RedeemOrderOverviewScreen) {
            // reload profile when user comes back to this screen to check for valid sso token
            sharedViewModel.refreshActiveProfile()
        }

        val hasOrderOptionInitializedFromNav = rememberSaveable { mutableStateOf(false) }
        val navigationArguments = RedeemRouteBackStackEntryArguments(navBackStackEntry)
        val taskId = remember { navBackStackEntry.arguments?.getString(RedeemRoutes.REDEEM_NAV_TASK_ID) }
        val pharmacy = navigationArguments.getPharmacy()

        // TODO: too many controllers? Can we reduce them
        val appController = rememberAppController()
        val orderOverviewController = rememberOrderOverviewScreenController()
        val redeemController = rememberRedeemPrescriptionsController()

        val isLoadingIndicatorShown by orderOverviewController.isLoadingIndicatorShown.collectAsStateWithLifecycle()
        val orderHasError by orderOverviewController.orderHasError.collectAsStateWithLifecycle()

        val activeProfile by sharedViewModel.activeProfile.collectAsStateWithLifecycle()
        val isRedemptionAllowed by sharedViewModel.isRedemptionAllowed.collectAsStateWithLifecycle()
        val orderOption by sharedViewModel.selectedOrderOption.collectAsStateWithLifecycle()
        val selectedOrderState by sharedViewModel.selectedOrderState
        val hasAttemptedRedeem by sharedViewModel.hasAttemptedRedeem.collectAsStateWithLifecycle()

        val redeemPrescriptionState by redeemController.redeemedState.collectAsStateWithLifecycle()
        val isProfileRefreshing by orderOverviewController.isProfileRefreshing.collectAsStateWithLifecycle()

        val contactValidationState = remember(selectedOrderState, orderOption, hasAttemptedRedeem) {
            val option = orderOption // to allow smart-casting
            when {
                !hasAttemptedRedeem -> RedeemContactValidationState.NoError
                option == null -> ContactValidationState.NoOrderOption(null).redeemValidationState()
                else ->
                    sharedViewModel
                        .validateContactInformation(
                            contact = selectedOrderState.contact,
                            selectedOrderOption = option
                        )
                        .redeemValidationState()
            }
        }

        val isRedeemEnabled = remember(
            pharmacy,
            contactValidationState,
            orderOption,
            selectedOrderState,
            hasAttemptedRedeem
        ) {
            when {
                !hasAttemptedRedeem -> true
                else ->
                    pharmacy != null &&
                            orderOption != null &&
                            contactValidationState.isValid() &&
                            selectedOrderState.prescriptionsInOrder.isNotEmpty()
            }
        }
        val intentHandler = LocalIntentHandler.current
        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                sharedViewModel.refreshActiveProfile()
            }
        }
        AuthenticationFailureDialog(
            event = orderOverviewController.showAuthenticationErrorDialog,
            dialogScaffold = dialog
        )
        val isPrescriptionError = remember(
            selectedOrderState.prescriptionsInOrder,
            hasAttemptedRedeem
        ) {
            when {
                !hasAttemptedRedeem -> false
                else -> selectedOrderState.prescriptionsInOrder.isEmpty()
            }
        }

        val isPharmacyError = remember(
            pharmacy,
            hasAttemptedRedeem
        ) {
            when {
                !hasAttemptedRedeem -> false
                else -> pharmacy == null || pharmacy.name.isEmpty() || pharmacy.address?.isEmpty() == true
            }
        }

        val isContactError = remember(
            orderOption,
            contactValidationState,
            selectedOrderState.contact,
            hasAttemptedRedeem
        ) {
            when {
                !hasAttemptedRedeem || orderOption == null -> false
                orderOption == PharmacyScreenData.OrderOption.Pickup -> !contactValidationState.isValid()
                else -> !contactValidationState.isValid() || selectedOrderState.contact.isEmpty()
            }
        }

        RedeemOrderDialogs(
            dialog = dialog,
            orderOverviewController = orderOverviewController,
            appController = appController,
            sharedViewModel = sharedViewModel,
            pharmacy = pharmacy,
            orderHasError = orderHasError,
            onDismiss = {
                navController.navigateAndClearStack(PrescriptionRoutes.PrescriptionListScreen.route)
            }
        ) {
            redeemOrder(
                redeemController = redeemController,
                profile = it,
                selectedOrderState = selectedOrderState,
                selectedOrderOption = orderOption,
                selectedPharmacy = pharmacy,
                isRedemptionAllowed = isRedemptionAllowed,
                onNotRedeemable = {
                    orderOverviewController.disableLoadingIndicator()
                    orderOverviewController.chooseAuthenticationMethod(it)
                }
            )
        }

        LaunchedEffect(Unit) {
            // this stops the navigation from over-riding the local selection on backstack pop
            if (!hasOrderOptionInitializedFromNav.value) {
                val orderOption = navigationArguments.getOrderOption()

                if (sharedViewModel.selectedOrderOption.value == null || orderOption != sharedViewModel.selectedOrderOption.value) {
                    sharedViewModel.updateSelectedOrderOption(orderOption)
                }

                hasOrderOptionInitializedFromNav.value = true
            }

            sharedViewModel.initializePrescriptionSelectionState(taskId)
        }

        ChooseAuthenticationNavigationEventsListener(orderOverviewController, navController)
        with(orderOverviewController) {
            observerProfileRefresh(isProfileRefreshing, this)
            observeRedeemState(redeemPrescriptionState, this)
            onBiometricAuthenticationSuccessEvent.listen {
                sharedViewModel.refreshActiveProfile()
            }
        }

        Box {
            UiStateMachine(
                state = activeProfile,
                onLoading = {
                    Center {
                        CircularProgressIndicator()
                    }
                }
            ) { profile ->
                val redeemClickEvent = RedeemClickEvent(
                    onSelectPrescriptions = {
                        orderOverviewController.disableLoadingIndicator()
                        navController.navigate(RedeemRoutes.RedeemPrescriptionSelection.path(isModal = true))
                    },
                    onChangePharmacy = {
                        orderOverviewController.disableLoadingIndicator()
                        navController.navigate(
                            PharmacyRoutes.PharmacyStartScreenModal.path(taskId = "")
                        )
                    },
                    onClickContacts = {
                        orderOverviewController.disableLoadingIndicator()
                        orderOption?.let {
                            navController.navigate(RedeemRoutes.RedeemEditShippingContactScreen.path(it))
                        }
                    },
                    onChangeService = { selectedOrderOption ->
                        sharedViewModel.updateSelectedOrderOption(selectedOrderOption)
                    },
                    onBack = {
                        orderOverviewController.disableLoadingIndicator()
                        sharedViewModel.onResetPrescriptionSelection()
                        navController.popBackStack()
                    }
                )

                val processStateEvent = ProcessStateEvent(
                    processStartedEvent = redeemController.onProcessStartEvent,
                    processEndEvent = redeemController.onProcessEndEvent,
                    onNotRedeemable = { orderOverviewController.chooseAuthenticationMethod(profile) },
                    onProcessStarted = orderOverviewController::enableLoadingIndicator,
                    onProcessEnded = orderOverviewController::disableLoadingIndicator
                )

                RedeemOrderOverviewScreenContent(
                    profile = profile,
                    selectedOrderState = selectedOrderState,
                    selectedOrderOption = orderOption,
                    contactValidationState = contactValidationState,
                    selectedPharmacy = pharmacy,
                    redeemClickEvent = redeemClickEvent,
                    processStateEvent = processStateEvent,
                    isRedeemEnabled = isRedeemEnabled,
                    isPrescriptionError = isPrescriptionError,
                    isPharmacyError = isPharmacyError,
                    isContactError = isContactError,
                    videoContent = {
                        val videoHeightPx = remember { mutableFloatStateOf(0f) }
                        val shape = RoundedCornerShape(bottomStart = SizeDefaults.fourfold, bottomEnd = SizeDefaults.fourfold)
                        Crossfade(targetState = orderOption, label = "VideoCrossfade") { targetOrderOption ->
                            val currentSource by remember(targetOrderOption) {
                                mutableIntStateOf(targetOrderOption.selectVideoSource())
                            }

                            val description = targetOrderOption?.getText()

                            VideoContent(
                                modifier = Modifier
                                    .onPlaced { videoHeightPx.floatValue = it.size.height.toFloat() }
                                    .semantics { contentDescription = description ?: "" }
                                    .clip(shape)
                                    .background(TopBarColor)
                                    .statusBarsPadding()
                                    .fillMaxWidth(),
                                source = currentSource,
                                aspectRatioOverwrite = OrderSuccessVideoAspectRatio
                            )
                        }
                    },
                    onClickRedeem = {
                        redeemOrder(
                            redeemController = redeemController,
                            profile = profile,
                            selectedOrderState = selectedOrderState,
                            isRedemptionAllowed = isRedemptionAllowed,
                            selectedOrderOption = orderOption,
                            selectedPharmacy = pharmacy,
                            onNotRedeemable = {
                                orderOverviewController.disableLoadingIndicator()
                                orderOverviewController.chooseAuthenticationMethod(profile)
                            }
                        )
                    }
                )
            }

            if (isLoadingIndicatorShown) {
                LoadingIndicator()
            }
        }
    }

    /**
     * Attempts to redeem the selected prescriptions after validating the input state.
     *
     * This function first validates whether the redemption attempt is eligible based on the selected
     * prescriptions, pharmacy, contact information, and order option. If the validation fails or
     * redemption is not allowed, the fallback callback [onNotRedeemable] is invoked.
     *
     * If validation passes and redemption is allowed, the redemption process is triggered using
     * [redeemController], provided that both [selectedOrderOption] and [selectedPharmacy] are not null.
     *
     * @param redeemController The controller responsible for processing the redemption request.
     * @param profile The active user profile for whom the redemption is being performed.
     * @param selectedOrderState The current order state including prescriptions and contact information.
     * @param selectedOrderOption The selected order option (e.g., delivery or pickup); must be non-null to proceed.
     * @param selectedPharmacy The selected pharmacy for redemption; must be non-null to proceed.
     * @param isRedemptionAllowed Flag indicating whether the redemption can proceed at this point.
     * @param onNotRedeemable Callback to be invoked if redemption cannot proceed due to failed validation
     *                        or disallowed conditions.
     */
    private fun redeemOrder(
        redeemController: RedeemPrescriptionsController,
        profile: Profile,
        selectedOrderState: PharmacyUseCaseData.OrderState,
        selectedOrderOption: PharmacyScreenData.OrderOption?,
        selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
        isRedemptionAllowed: Boolean,
        onNotRedeemable: () -> Unit
    ) {
        val hasSuccessfullyValidated = sharedViewModel.attemptRedeemValidation(
            prescriptions = selectedOrderState.prescriptionsInOrder,
            pharmacy = selectedPharmacy,
            selectedOrderOption = selectedOrderOption,
            contact = selectedOrderState.contact
        )

        if (!hasSuccessfullyValidated) return

        if (!isRedemptionAllowed) {
            onNotRedeemable()
            return
        }

        letNotNull(selectedOrderOption, selectedPharmacy, { orderOption, pharmacy ->
            redeemController.processPrescriptionRedemptions(
                arguments = orderID().from(
                    profile = profile,
                    order = selectedOrderState,
                    redeemOption = orderOption,
                    pharmacy = pharmacy
                )
            )
        }, elseBlock = onNotRedeemable)
    }

    @Suppress("ComposableNaming")
    @Composable
    private fun observeRedeemState(
        redeemPrescriptionState: BaseRedeemState,
        orderOverviewController: RedeemOrderOverviewScreenController
    ) {
        with(orderOverviewController) {
            LaunchedEffect(redeemPrescriptionState) {
                redeemPrescriptionState.handleRedeemedState(
                    onShowPrescriptionRedeemAlertDialog = { showPrescriptionRedeemAlertDialogEvent.trigger(it) },
                    onOrderHasError = { toggleOrderHasError(it) },
                    // hide the loading indicator when the  dialog is to be shown
                    onIncompleteOrder = {
                        disableLoadingIndicator()
                        showErrorOnRedeemAlertDialogEvent.trigger(it)
                    },
                    onInvalidOrder = {
                        disableLoadingIndicator()
                        showErrorOnRedeemAlertDialogEvent.trigger(it)
                    }
                )
            }
        }
    }

    @Suppress("ComposableNaming")
    @Composable
    private fun observerProfileRefresh(
        isProfileRefreshing: Boolean,
        orderOverviewController: RedeemOrderOverviewScreenController
    ) {
        LaunchedEffect(isProfileRefreshing) {
            orderOverviewController.toggleLoadingIndicator(isProfileRefreshing)
        }
    }
}

@Composable
fun RedeemOrderOverviewScreenContent(
    profile: Profile,
    selectedOrderState: PharmacyUseCaseData.OrderState,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    contactValidationState: RedeemContactValidationState,
    selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
    isRedeemEnabled: Boolean,
    isPrescriptionError: Boolean,
    isPharmacyError: Boolean,
    isContactError: Boolean,
    processStateEvent: ProcessStateEvent,
    redeemClickEvent: RedeemClickEvent,
    videoContent: @Composable BoxScope.() -> Unit,
    onClickRedeem: () -> Unit
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderSummary.Screen),
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.systemBarsPadding()) },
        bottomBar = {
            AnimatedBottomBar(listState) {
                AnimatedVisibility(
                    visible = !contactValidationState.isValid() || isPrescriptionError || isPharmacyError,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top) + slideInVertically(),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically()
                ) {
                    // Error banner
                    Banner(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        startIcon = BannerClickableIcon(BannerIcon.Warning) {},
                        contentColor = AppTheme.colors.red900,
                        containerColor = AppTheme.colors.red100,
                        borderColor = AppTheme.colors.red600,
                        text = when {
                            isPrescriptionError -> stringResource(R.string.pharmacy_order_no_prescriptions_error)
                            isPharmacyError -> stringResource(R.string.pharmacy_order_no_pharmacy_error)
                            else -> contactValidationState.error?.let { stringResource(it) } ?: ""
                        }
                    )
                }
                RedeemButton(
                    modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
                    isEnabled = isRedeemEnabled,
                    processStateEvent = processStateEvent,
                    onClickRedeem = onClickRedeem
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)
        ) {
            item {
                Box {
                    videoContent()
                    NavigateBackButton(
                        onClick = redeemClickEvent.onBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
            redeemOrderOverviewTitle()
            prescriptionSelectionSection(
                prescriptions = selectedOrderState.prescriptionsInOrder,
                isPrescriptionError = isPrescriptionError,
                onClick = redeemClickEvent.onSelectPrescriptions
            )
            pharmacySelectionSection(
                selectedPharmacy = selectedPharmacy,
                selectedOrderOption = selectedOrderOption,
                isPharmacyError = isPharmacyError,
                onClick = redeemClickEvent.onChangePharmacy,
                onServiceSelection = redeemClickEvent.onChangeService
            )
            contactSelectionAnimatedItem(
                show = selectedOrderOption != null
            ) {
                ContactSelectionSection(
                    profile = profile,
                    selectedOrderOption = selectedOrderOption,
                    contact = selectedOrderState.contact,
                    contactValidationState = contactValidationState,
                    isContactError = isContactError,
                    onClick = redeemClickEvent.onClickContacts
                )
            }

            if (selectedOrderState.selfPayerPrescriptionNames.isNotEmpty()) {
                item {
                    SelfPayerPrescriptionWarning(selectedOrderState.selfPayerPrescriptionNames)
                }
            }
            item {
                SpacerLarge()
            }
        }
    }
}

private fun LazyListScope.redeemOrderOverviewTitle() {
    item {
        Text(
            stringResource(R.string.pharmacy_order_title),
            textAlign = TextAlign.Start,
            style = AppTheme.typography.h5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium)
        )
    }
}

private fun LazyListScope.prescriptionSelectionSection(
    prescriptions: List<PharmacyUseCaseData.PrescriptionInOrder>,
    isPrescriptionError: Boolean,
    onClick: () -> Unit
) {
    item {
        SelectionSummaryButton(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .testTag(TestTag.PharmacySearch.OrderSummary.PrescriptionSelectionButton),
            data = SelectionSummaryButtonData(
                buttonTitleText = stringResource(R.string.pharmacy_order_with_prescriptions_button_subtitle),
                errorTitleText = stringResource(R.string.pharmacy_order_no_prescriptions),
                errorHintText = stringResource(R.string.pharmacy_order_no_prescriptions_error),
                buttonTexts = prescriptions.mapNotNull { it.title }.map { selectionSummaryButtonText(it) }
            ),
            errorContentDescription = stringResource(R.string.a11y_error_prefix),
            isError = isPrescriptionError,
            onClick = onClick
        )
    }
}

private fun LazyListScope.pharmacySelectionSection(
    selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    isPharmacyError: Boolean,
    onServiceSelection: (PharmacyScreenData.OrderOption) -> Unit,
    onClick: () -> Unit
) {
    item {
        val pharmacyName = selectedPharmacy?.name?.let { selectionSummaryButtonText(text = it, maxLines = 3) }
        val address = selectedPharmacy?.address?.let {
            selectionSummaryButtonText(
                text = it,
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                maxLines = 3
            )
        }
        SelectionSummaryButton(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium)
                .testTag(TestTag.PharmacySearch.OrderSummary.PharmacySelectionButton),
            data = SelectionSummaryButtonData(
                buttonTitleText = stringResource(R.string.pharmacy_order_pharmacy),
                errorTitleText = stringResource(R.string.pharmacy_order_no_pharmacy),
                errorHintText = stringResource(R.string.pharmacy_order_no_pharmacy_error),
                buttonTexts = listOfNotNull(pharmacyName, address)
            ),
            errorContentDescription = stringResource(R.string.a11y_error_prefix),
            isError = isPharmacyError,
            onClick = onClick,
            bottomContent = {
                if (selectedPharmacy != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = PaddingDefaults.Medium))
                    OrderSelectedServicesRow(
                        selectedOption = selectedOrderOption,
                        pharmacy = selectedPharmacy
                    ) {
                        onServiceSelection(it)
                    }
                    SpacerTiny()
                }
            }
        )
    }
}

private fun LazyListScope.contactSelectionAnimatedItem(
    show: Boolean,
    content: @Composable () -> Unit
) {
    item(key = "contactSelectionAnimatedItem") {
        AnimatedVisibility(
            visible = show,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top) + slideInVertically(),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically()
        ) {
            content()
        }
    }
}

@Composable
private fun ContactSelectionSection(
    profile: Profile,
    contactValidationState: RedeemContactValidationState,
    contact: PharmacyUseCaseData.ShippingContact,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    isContactError: Boolean,
    onClick: () -> Unit
) {
    SelectionSummaryButton(
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium)
            .testTag(TestTag.PharmacySearch.OrderSummary.ContactSelectionButton),
        data = SelectionSummaryButtonData(
            buttonTitleText = stringResource(R.string.pharmacy_order_contact),
            errorHintText = contactValidationState.error?.let { stringResource(it) } ?: "",
            buttonTexts = listOfNotNull(selectionSummaryButtonText(profile.name)),
            errorTitleText = "" // does not apply for contact section since buttonText always exist
        ),
        errorContentDescription = stringResource(R.string.a11y_error_prefix),
        isError = isContactError,
        onClick = onClick,
        overrideIcon = true,
        leadingContent = {
            Avatar(
                modifier = Modifier.size(SizeDefaults.fivefold),
                iconModifier = Modifier.size(SizeDefaults.double),
                emptyIcon = Icons.Rounded.PersonOutline,
                profile = profile
            )
        },
        bottomContent = {
            HorizontalDivider(modifier = Modifier.padding(vertical = PaddingDefaults.Medium))
            if (contact.isEmpty()) {
                RedeemContactMissingSection(isContactError)
            } else {
                RedeemContactInformationSection(
                    contact = contact,
                    selectedOrderOption = selectedOrderOption,
                    state = contactValidationState
                )
            }
            SpacerTiny()
        }
    )
}

@LightDarkPreview
@Composable
private fun PrescriptionSelectionSectionPreview(
    @PreviewParameter(PrescriptionSelectionSectionParameter::class) state: List<PharmacyUseCaseData.PrescriptionInOrder>
) {
    PreviewTheme {
        LazyColumn {
            prescriptionSelectionSection(state, false, {})
        }
    }
}

@LightDarkPreview
@Composable
private fun PharmacySelectionSectionPreview() {
    PreviewTheme {
        LazyColumn {
            pharmacySelectionSection(
                selectedPharmacy = RedeemOverviewScreenPreviewParameter.pharmacyPreviewData,
                selectedOrderOption = PharmacyScreenData.OrderOption.Pickup,
                isPharmacyError = false,
                onServiceSelection = {},
                onClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ContactSelectionSectionPreview() {
    PreviewTheme {
        LazyColumn {
            contactSelectionAnimatedItem(true) {
                ContactSelectionSection(
                    profile = RedeemOverviewScreenPreviewParameter.profilePreviewData,
                    contactValidationState = RedeemContactValidationState.NoError,
                    contact = RedeemOverviewScreenPreviewParameter.contactPreviewData,
                    selectedOrderOption = PharmacyScreenData.OrderOption.Pickup,
                    isContactError = false
                ) {}
            }
        }
    }
}

@LightDarkLongPreview
@Composable
internal fun RedeemOrderOverviewScreenContentPreview(
    @PreviewParameter(RedeemOverviewScreenParameter::class) state: RedeemOverviewScreenPreviewData
) {
    PreviewTheme {
        RedeemOrderOverviewScreenContent(
            profile = state.profile,
            selectedOrderState = state.orderState(),
            selectedOrderOption = state.orderOption,
            contactValidationState = state.contactValidationState,
            selectedPharmacy = state.pharmacy,
            isRedeemEnabled = state.isRedeemEnabled,
            isPrescriptionError = state.isPrescriptionError,
            isPharmacyError = state.isPharmacyError,
            isContactError = state.isContactError,
            processStateEvent = ProcessStateEvent(
                ComposableEvent(),
                ComposableEvent(),
                {},
                {},
                {}
            ),
            redeemClickEvent = RedeemClickEvent({}, {}, {}, {}, {}),
            videoContent = {
                Icon(
                    modifier = Modifier
                        .background(TopBarColor)
                        .statusBarsPadding()
                        .height(SizeDefaults.fifteenfold)
                        .fillMaxWidth(),
                    painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                    contentDescription = "Video content comes here"
                )
            },
            {}
        )
    }
}
