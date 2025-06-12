/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.button.ButtonWithConditionalHint
import de.gematik.ti.erp.app.button.ButtonWithConditionalHintData
import de.gematik.ti.erp.app.button.FlatButton
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.CardWallIntroScreen
import de.gematik.ti.erp.app.core.LocalIntentHandler
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
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isContactInformationMissing
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isValid
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState.ValidShippingContactState.OK
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.profiles.ui.extension.extract
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.RedeemEventModel.ProcessStateEvent
import de.gematik.ti.erp.app.redeem.model.RedeemEventModel.RedeemClickEvent
import de.gematik.ti.erp.app.redeem.navigation.RedeemRouteBackStackEntryArguments
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.presentation.RedeemOrderOverviewScreenController
import de.gematik.ti.erp.app.redeem.presentation.RedeemPrescriptionsController
import de.gematik.ti.erp.app.redeem.presentation.rememberOrderOverviewScreenController
import de.gematik.ti.erp.app.redeem.presentation.rememberRedeemPrescriptionsController
import de.gematik.ti.erp.app.redeem.ui.components.ErrorOnRedeemablePrescriptionDialog
import de.gematik.ti.erp.app.redeem.ui.components.PrescriptionRedeemAlertDialog
import de.gematik.ti.erp.app.redeem.ui.components.RedeemButton
import de.gematik.ti.erp.app.redeem.ui.components.RedeemStateHandler.handleRedeemedState
import de.gematik.ti.erp.app.redeem.ui.components.SelfPayerPrescriptionWarning
import de.gematik.ti.erp.app.redeem.ui.components.selectVideoSource
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.NavigateBackButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val OrderSuccessVideoAspectRatio = 1.69f

class RedeemOrderOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnlineRedeemGraphController
) : Screen() {
    @Composable
    override fun Content() {
        val scope = uiScope
        val dialog = LocalDialog.current

        navBackStackEntry.onReturnAction(RedeemRoutes.RedeemOrderOverviewScreen) {
            // reload profile when user comes back to this screen to check for valid sso token
            graphController.refreshActiveProfile()
        }

        val navigationArguments = RedeemRouteBackStackEntryArguments(navBackStackEntry)
        val taskId = navBackStackEntry.arguments?.getString(RedeemRoutes.REDEEM_NAV_TASK_ID)

        val pharmacy = navigationArguments.getPharmacy()
        val orderOption = navigationArguments.getOrderOption()

        val appController = rememberAppController()
        val orderOverviewController = rememberOrderOverviewScreenController()
        val redeemController = rememberRedeemPrescriptionsController()

        val isLoadingIndicatorShown by orderOverviewController.isLoadingIndicatorShown.collectAsStateWithLifecycle()
        val orderHasError by orderOverviewController.orderHasError.collectAsStateWithLifecycle()

        val activeProfile by graphController.activeProfile.collectAsStateWithLifecycle()
        val isRedemptionAllowed by graphController.isRedemptionAllowed.collectAsStateWithLifecycle()
        val selectedOrderState by graphController.selectedOrderState
        val redeemPrescriptionState by redeemController.redeemedState.collectAsStateWithLifecycle()
        val isProfileRefreshing by orderOverviewController.isProfileRefreshing.collectAsStateWithLifecycle()

        val shippingContactState = letNotNull(pharmacy, orderOption) { _, _ ->
            remember(selectedOrderState, orderOption) {
                graphController.validateAndGetShippingContactState(selectedOrderState.contact, orderOption)
            }
        }

        AuthenticationFailureDialog(
            event = orderOverviewController.showAuthenticationErrorDialog,
            dialogScaffold = dialog
        )

        PrescriptionRedeemAlertDialog(
            event = orderOverviewController.showPrescriptionRedeemAlertDialogEvent,
            dialog = dialog,
            onDismiss = {
                orderOverviewController.disableLoadingIndicator()
                appController.onOrdered(hasError = orderHasError)
                graphController.onResetPrescriptionSelection()
                navController.navigateAndClearStack(route = PrescriptionRoutes.PrescriptionListScreen.route)
            }
        )

        ErrorOnRedeemablePrescriptionDialog(
            event = orderOverviewController.showErrorOnRedeemAlertDialogEvent,
            dialog = dialog,
            onClickForInvalidOrder = {
                graphController.onResetPrescriptionSelection()
                navController.navigateAndClearStack(route = PrescriptionRoutes.PrescriptionListScreen.route)
            },
            onClickForIncompleteOrder = { nonRedeemableTaskIds ->
                // remove the prescriptions that are not redeemable from the order
                graphController.deselectInvalidPrescriptions(nonRedeemableTaskIds)
                // restart the redeem process with the new prescriptions
                scope.launch {
                    graphController.activeProfile.extract()?.let { profile ->
                        redeemOrder(
                            redeemController = redeemController,
                            profile = profile,
                            order = selectedOrderState,
                            isRedemptionAllowed = isRedemptionAllowed,
                            selectedOrderOption = orderOption,
                            selectedPharmacy = pharmacy,
                            onNotRedeemable = {
                                orderOverviewController.disableLoadingIndicator()
                                orderOverviewController.chooseAuthenticationMethod(profile.id)
                            }
                        )
                    }
                }
            },
            onClickToCancel = {
                // nothing happens as of now
            }
        )

        LaunchedEffect(Unit) {
            graphController.setPrescriptionSelectionState(taskId)
        }

        letNotNull(pharmacy, orderOption) { pharmacyNotNull, orderOptionNotNull ->
            Box {
                UiStateMachine(
                    state = activeProfile,
                    onLoading = {
                        Center {
                            CircularProgressIndicator()
                        }
                    }
                ) { profile ->

                    with(orderOverviewController) {
                        observerProfileRefresh(isProfileRefreshing, this)
                        observeRedeemState(redeemPrescriptionState, this)
                        listenForAuthenticationEvents(this)
                    }

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
                            navController.navigate(
                                RedeemRoutes.RedeemEditShippingContactScreen.path(orderOptionNotNull)
                            )
                        },
                        onBack = {
                            orderOverviewController.disableLoadingIndicator()
                            graphController.onResetPrescriptionSelection()
                            navController.popBackStack()
                        }
                    )

                    val processStateEvent = ProcessStateEvent(
                        processStartedEvent = redeemController.onProcessStartEvent,
                        processEndEvent = redeemController.onProcessEndEvent,
                        onNotRedeemable = { orderOverviewController.chooseAuthenticationMethod(profile.id) },
                        onProcessStarted = orderOverviewController::enableLoadingIndicator,
                        onProcessEnded = orderOverviewController::disableLoadingIndicator
                    )

                    RedeemOrderOverviewScreenContent(
                        profile = profile,
                        selectedOrderState = selectedOrderState,
                        selectedOrderOption = orderOptionNotNull,
                        shippingContactState = shippingContactState,
                        selectedPharmacy = pharmacyNotNull,
                        redeemClickEvent = redeemClickEvent,
                        processStateEvent = processStateEvent,
                        onClickRedeem = {
                            redeemOrder(
                                redeemController = redeemController,
                                profile = profile,
                                order = selectedOrderState,
                                isRedemptionAllowed = isRedemptionAllowed,
                                selectedOrderOption = orderOptionNotNull,
                                selectedPharmacy = pharmacyNotNull,
                                onNotRedeemable = {
                                    orderOverviewController.disableLoadingIndicator()
                                    orderOverviewController.chooseAuthenticationMethod(profile.id)
                                }
                            )
                        }
                    )
                }

                if (isLoadingIndicatorShown) {
                    LoadingIndicator()
                }
            }
        } ?: run {
            // in case of missing pharmacy or order option we show an error screen asking the user to go back and start the process
            Center {
                ErrorScreenComponent(
                    onClickRetry = navController::navigateUp
                )
            }
        }
    }

    private fun redeemOrder(
        redeemController: RedeemPrescriptionsController,
        profile: Profile,
        order: PharmacyUseCaseData.OrderState,
        isRedemptionAllowed: Boolean,
        selectedOrderOption: PharmacyScreenData.OrderOption?,
        selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
        onNotRedeemable: () -> Unit
    ) {
        if (!isRedemptionAllowed) {
            onNotRedeemable()
            return
        }
        letNotNull(selectedOrderOption, selectedPharmacy, { orderOption, pharmacy ->
            redeemController.processPrescriptionRedemptions(
                arguments = orderID().from(
                    profile = profile,
                    order = order,
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

    @Suppress("ComposableNaming")
    @Composable
    private fun listenForAuthenticationEvents(orderOverviewController: RedeemOrderOverviewScreenController) {
        val intentHandler = LocalIntentHandler.current
        LaunchedEffect(Unit) {
            intentHandler.gidSuccessfulIntent.collectLatest {
                graphController.refreshActiveProfile()
            }
        }

        with(orderOverviewController) {
            onBiometricAuthenticationSuccessEvent.listen {
                graphController.refreshActiveProfile()
            }
            showCardWallEvent.listen { id ->
                navController.navigate(CardWallIntroScreen.path(id))
            }
            showCardWallWithFilledCanEvent.listen { cardWallData ->
                navController.navigate(
                    CardWallRoutes.CardWallPinScreen.path(
                        profileIdentifier = cardWallData.profileId,
                        can = cardWallData.can
                    )
                )
            }
            showGidEvent.listen { gidData ->
                navController.navigate(
                    CardWallIntroScreen.pathWithGid(gidData)
                )
            }
        }
    }
}

@Composable
fun RedeemOrderOverviewScreenContent(
    profile: Profile,
    selectedOrderState: PharmacyUseCaseData.OrderState,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    shippingContactState: ShippingContactState?,
    selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
    processStateEvent: ProcessStateEvent,
    redeemClickEvent: RedeemClickEvent,
    onClickRedeem: () -> Unit
) {
    val listState = rememberLazyListState()
    val videoHeightPx = remember { mutableFloatStateOf(0f) }
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderSummary.Screen),
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.systemBarsPadding()) }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)
        ) {
            item {
                val shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                Box {
                    VideoContent(
                        Modifier
                            .onPlaced { videoHeightPx.floatValue = it.size.height.toFloat() }
                            .clip(shape)
                            .background(TopBarColor)
                            .statusBarsPadding()
                            .fillMaxWidth(),
                        source = selectedOrderOption.selectVideoSource(),
                        aspectRatioOverwrite = OrderSuccessVideoAspectRatio
                    )
                    NavigateBackButton(onClick = redeemClickEvent.onBack)
                }
            }
            item {
                SpacerMedium()
                Text(
                    stringResource(R.string.pharmacy_order_title),
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Medium)
                )
            }
            item {
                Column(Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                    Text(stringResource(R.string.pharmacy_order_receiver), style = AppTheme.typography.h6)
                    SpacerMedium()
                    shippingContactState?.let { shippingContact ->
                        ContactSelectionButton(
                            contact = selectedOrderState.contact,
                            shippingContactState = shippingContact,
                            onClick = redeemClickEvent.onClickContacts
                        )
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                    SpacerMedium()
                    PrescriptionSelectionButton(
                        prescriptions = selectedOrderState.prescriptionsInOrder,
                        onClick = redeemClickEvent.onSelectPrescriptions
                    )
                }
            }
            item {
                Column(Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                    Text(stringResource(R.string.pharmacy_order_pharmacy), style = AppTheme.typography.h6)
                    SpacerMedium()
                    letNotNull(selectedPharmacy, selectedOrderOption) { pharmacy, orderOption ->
                        PharmacySelectionButton(
                            selectedPharmacy = pharmacy,
                            selectedOrderOption = orderOption,
                            onClick = redeemClickEvent.onChangePharmacy
                        )
                    }
                    SpacerMedium()
                }
            }

            if (selectedOrderState.selfPayerPrescriptionNames.isNotEmpty()) {
                item {
                    SelfPayerPrescriptionWarning(selectedOrderState.selfPayerPrescriptionNames)
                }
            }

            letNotNull(
                profile,
                selectedPharmacy,
                selectedOrderOption
            ) { _, _, _ ->
                item {
                    RedeemButton(
                        isEnabled = shippingContactState != null && shippingContactState == OK && selectedOrderState.prescriptionsInOrder.isNotEmpty(),
                        processStateEvent = processStateEvent,
                        onClickRedeem = onClickRedeem
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactSelectionButton(
    contact: PharmacyUseCaseData.ShippingContact,
    onClick: () -> Unit,
    shippingContactState: ShippingContactState
) {
    FlatButton(
        onClick = onClick
    ) {
        if (contact.isEmpty()) {
            Text(
                stringResource(R.string.pharmacy_order_add_contacts),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.primary700
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            if (contact.name.isNotBlank()) {
                                Text(contact.name, style = AppTheme.typography.subtitle1)
                            }
                            contact.address().forEach {
                                Text(it, style = AppTheme.typography.body1l)
                            }
                        }
                        if (contact.other().isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (contact.telephoneNumber.isNotBlank()) {
                                    SmallChip(Icons.Outlined.Phone, contact.telephoneNumber)
                                }
                                if (contact.mail.isNotBlank()) {
                                    SmallChip(Icons.Outlined.Mail, contact.mail)
                                }
                            }
                        }
                        if (contact.deliveryInformation.isNotBlank()) {
                            Text(contact.deliveryInformation, style = AppTheme.typography.body1l)
                        }
                    }
                    if (!shippingContactState.isValid()) {
                        val text = if (shippingContactState.isContactInformationMissing()) {
                            stringResource(R.string.pharmacy_order_further_contact_information_required)
                        } else {
                            stringResource(R.string.pharmacy_order_contact_information_invalid)
                        }
                        SpacerSmall()
                        Surface(shape = RoundedCornerShape(8.dp), color = AppTheme.colors.red100) {
                            Text(
                                text,
                                color = AppTheme.colors.red900,
                                style = AppTheme.typography.subtitle2,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                SpacerMedium()
                Text(
                    stringResource(R.string.pharmacy_order_change_contacts),
                    style = AppTheme.typography.subtitle2,
                    color = AppTheme.colors.primary700
                )
            }
        }
    }
}

@Composable
private fun PrescriptionSelectionButton(
    prescriptions: List<PharmacyUseCaseData.PrescriptionInOrder>,
    onClick: () -> Unit
) {
    ButtonWithConditionalHint(
        modifier = Modifier
            .padding(vertical = PaddingDefaults.Medium)
            .testTag(TestTag.PharmacySearch.OrderSummary.PrescriptionSelectionButton),
        data = ButtonWithConditionalHintData(
            buttonTitleText = stringResource(R.string.pharmacy_order_with_prescriptions_button_subtitle),
            errorTitleText = stringResource(R.string.pharmacy_order_no_prescriptions),
            hintText = stringResource(R.string.pharmacy_order_no_prescriptions_error),
            buttonTexts = prescriptions.mapNotNull { it.title }
        ),
        isError = prescriptions.isEmpty(),
        onClick = onClick
    )
}

@Composable
private fun SmallChip(
    icon: ImageVector,
    text: String
) =
    Surface(shape = RoundedCornerShape(SizeDefaults.one), color = AppTheme.colors.neutral100) {
        Row(
            Modifier.padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.quarter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AppTheme.colors.neutral500)
            SpacerSmall()
            Text(
                text,
                style = AppTheme.typography.body1
            )
        }
    }

@Composable
private fun PharmacySelectionButton(
    selectedPharmacy: PharmacyUseCaseData.Pharmacy,
    selectedOrderOption: PharmacyScreenData.OrderOption,
    onClick: () -> Unit
) {
    FlatButton(
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    selectedPharmacy.name,
                    style = AppTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SpacerTiny()
                Text(
                    selectedPharmacy.singleLineAddress(),
                    style = AppTheme.typography.body2l,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SpacerShortMedium()
                ServiceOption(
                    option = selectedOrderOption
                )
            }
            SpacerMedium()
            Text(
                stringResource(R.string.pharmacy_order_change_order),
                style = AppTheme.typography.subtitle2,
                color = AppTheme.colors.primary700
            )
        }
    }
}

@Composable
private fun ServiceOption(
    option: PharmacyScreenData.OrderOption
) {
    val text = when (option) {
        PharmacyScreenData.OrderOption.PickupService -> stringResource(R.string.pharmacy_order_collect)
        PharmacyScreenData.OrderOption.CourierDelivery -> stringResource(R.string.pharmacy_order_delivery)
        PharmacyScreenData.OrderOption.MailDelivery -> stringResource(R.string.pharmacy_order_mail)
    }
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .background(AppTheme.colors.green200, shape)
            .padding(
                horizontal = PaddingDefaults.ShortMedium,
                vertical = PaddingDefaults.ShortMedium / 2
            )
    ) {
        Text(text, style = AppTheme.typography.subtitle2, color = AppTheme.colors.green900)
    }
}
