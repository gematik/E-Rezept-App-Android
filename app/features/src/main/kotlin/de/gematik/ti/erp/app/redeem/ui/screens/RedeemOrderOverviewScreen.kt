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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import de.gematik.ti.erp.app.authentication.ui.components.AuthenticationFailureDialog
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.ui.components.TopBarColor
import de.gematik.ti.erp.app.pharmacy.ui.components.VideoContent
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isContactInformationMissing
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isValid
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState.ValidShippingContactState.OK
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.navigation.RedeemRouteBackStackEntryArguments
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.presentation.rememberOrderOverviewScreenController
import de.gematik.ti.erp.app.redeem.ui.components.RedeemButton
import de.gematik.ti.erp.app.redeem.ui.components.SelfPayerPrescriptionWarning
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.NavigateBackButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.coroutines.flow.collectLatest

private const val OrderSuccessVideoAspectRatio = 1.69f

class RedeemOrderOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnlineRedeemGraphController
) : Screen() {
    @Composable
    override fun Content() {
        val intentHandler = LocalIntentHandler.current
        navBackStackEntry.onReturnAction(RedeemRoutes.RedeemOrderOverviewScreen) {
            // reload profile when user comes back to this screen to check for valid sso token
            graphController.refreshActiveProfile()
        }
        val navigationArguments = RedeemRouteBackStackEntryArguments(navBackStackEntry)

        letNotNull(
            navigationArguments.getPharmacy(),
            navigationArguments.getOrderOption()
        ) { pharmacy, orderOption ->

            val appController = rememberAppController()
            val controller = rememberOrderOverviewScreenController()

            val activeProfile by graphController.activeProfile.collectAsStateWithLifecycle()
            val isRedemptionAllowed by graphController.isRedemptionAllowed.collectAsStateWithLifecycle()
            val isProfileRefreshing by controller.isProfileRefreshing.collectAsStateWithLifecycle()

            val taskId = navBackStackEntry.arguments?.getString(RedeemRoutes.REDEEM_NAV_TASK_ID) ?: ""

            LaunchedEffect(Unit) {
                if (taskId.isNotEmpty()) {
                    graphController.deselectPrescriptions(taskId)
                }
            }

            val selectedOrderState by graphController.selectedOrderState()

            val shippingContactState = remember(selectedOrderState, orderOption) {
                graphController.validateAndGetShippingContactState(selectedOrderState.contact, orderOption)
            }

            var isLoadingIndicatorShown by remember { mutableStateOf(false) }

            fun hideLoadingIndicator() {
                isLoadingIndicatorShown = false
            }

            fun showLoadingIndicator() {
                isLoadingIndicatorShown = true
            }

            LaunchedEffect(isProfileRefreshing) {
                when {
                    isProfileRefreshing -> showLoadingIndicator()
                    else -> hideLoadingIndicator()
                }
            }

            LaunchedEffect(Unit) {
                intentHandler.gidSuccessfulIntent.collectLatest {
                    graphController.refreshActiveProfile()
                }
            }

            AuthenticationFailureDialog(
                event = controller.showAuthenticationErrorDialog,
                dialogScaffold = dialog
            )

            Box {
                UiStateMachine(
                    state = activeProfile,
                    onLoading = {
                        Center {
                            CircularProgressIndicator()
                        }
                    }
                ) { profile ->

                    with(controller) {
                        onBiometricAuthenticationSuccessEvent.listen {
                            graphController.refreshActiveProfile()
                        }

                        showCardWallEvent.listen { id ->
                            navController.navigate(CardWallRoutes.CardWallIntroScreen.path(id))
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
                                CardWallRoutes.CardWallIntroScreen.pathWithGid(
                                    profileIdentifier = gidData.profileId,
                                    gidEventData = gidData
                                )
                            )
                        }
                    }

                    RedeemOrderOverviewScreenContent(
                        profile = profile,
                        selectedOrderState = selectedOrderState,
                        selectedOrderOption = orderOption,
                        shippingContactState = shippingContactState,
                        selectedPharmacy = pharmacy,
                        isRedemptionPossible = isRedemptionAllowed,
                        onClickContacts = {
                            hideLoadingIndicator()
                            navController.navigate(
                                RedeemRoutes.RedeemEditShippingContactScreen.path(orderOption)
                            )
                        },
                        onSelectPrescriptions = {
                            hideLoadingIndicator()
                            navController.navigate(RedeemRoutes.RedeemPrescriptionSelection.path(isModal = true))
                        },
                        onBack = {
                            hideLoadingIndicator()
                            graphController.onResetPrescriptionSelection()
                            navController.popBackStack()
                        },
                        onChangePharmacy = {
                            hideLoadingIndicator()
                            navController.navigate(
                                PharmacyRoutes.PharmacyStartScreenModal.path(taskId = "")
                            )
                        },
                        onNotRedeemable = {
                            controller.chooseAuthenticationMethod(profile.id)
                        },
                        onProcessStarted = {
                            showLoadingIndicator()
                        },
                        onProcessEnded = {
                            hideLoadingIndicator()
                        },
                        onFinish = { orderHasError ->
                            hideLoadingIndicator()
                            appController.onOrdered(hasError = orderHasError)
                            graphController.onResetPrescriptionSelection()
                            navController.navigateAndClearStack(route = PrescriptionRoutes.PrescriptionsScreen.route)
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
}

@Composable
fun RedeemOrderOverviewScreenContent(
    profile: ProfilesUseCaseData.Profile,
    selectedOrderState: PharmacyUseCaseData.OrderState,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    shippingContactState: ShippingContactState?,
    selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
    isRedemptionPossible: Boolean,
    onFinish: (Boolean) -> Unit,
    onNotRedeemable: () -> Unit,
    onClickContacts: () -> Unit,
    onSelectPrescriptions: () -> Unit,
    onChangePharmacy: () -> Unit,
    onProcessStarted: () -> Unit,
    onProcessEnded: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val videoHeightPx = remember { mutableFloatStateOf(0f) }
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderSummary.Screen),
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.systemBarsPadding()) },
        topBar = {
            // no top bar
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
                val shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                Box {
                    VideoContent(
                        Modifier
                            .onPlaced {
                                videoHeightPx.floatValue = it.size.height.toFloat()
                            }
                            .clip(shape)
                            .background(TopBarColor)
                            .statusBarsPadding()
                            .fillMaxWidth(),
                        source = when (selectedOrderOption) {
                            PharmacyScreenData.OrderOption.PickupService -> R.raw.animation_local
                            PharmacyScreenData.OrderOption.CourierDelivery -> R.raw.animation_courier
                            PharmacyScreenData.OrderOption.MailDelivery -> R.raw.animation_mail
                            else -> {
                                // show default animation until the order option is not null
                                R.raw.animation_local
                            }
                        },
                        aspectRatioOverwrite = OrderSuccessVideoAspectRatio
                    )
                    NavigateBackButton(onClick = onBack)
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
                            onClick = onClickContacts
                        )
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                    Text(stringResource(R.string.pharmacy_order_prescriptions), style = AppTheme.typography.h6)
                    SpacerMedium()
                    selectedOrderState.prescriptionOrders.takeIf { it.isNotEmpty() }?.let { prescriptions ->
                        PrescriptionSelectionButton(
                            prescriptions = prescriptions,
                            onClick = onSelectPrescriptions
                        )
                    }
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
                            onClick = onChangePharmacy
                        )
                    }
                    SpacerMedium()
                }
            }

            val selectedSelfPayerPrescriptionNames = selectedOrderState.prescriptionOrders
                .filter { it.taskId in selectedOrderState.selfPayerPrescriptionIds }
                .mapNotNull { it.title }

            if (selectedSelfPayerPrescriptionNames.isNotEmpty()) {
                item {
                    SelfPayerPrescriptionWarning(
                        selectedSelfPayerPrescriptionNames
                    )
                }
            }
            item {
                letNotNull(
                    profile,
                    selectedPharmacy,
                    selectedOrderOption
                ) { activeProfile, pharmacy, orderOption ->
                    RedeemButton(
                        profile = activeProfile,
                        selectedPharmacy = pharmacy,
                        selectedOrderOption = orderOption,
                        order = selectedOrderState,
                        shippingContactCompleted = shippingContactState == OK,
                        isRedemptionPossible = isRedemptionPossible,
                        onNotRedeemable = onNotRedeemable,
                        onProcessStarted = onProcessStarted,
                        onProcessEnded = onProcessEnded,
                        onFinish = onFinish
                    )
                }
            }
        }
    }
}

@Composable
fun PrescriptionRedeemAlertDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    AcceptDialog(
        header = title,
        info = description,
        onClickAccept = {
            onDismiss()
        },
        acceptText = stringResource(R.string.pharmacy_search_apovz_call_failed_accept)
    )
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
    prescriptions: List<PharmacyUseCaseData.PrescriptionOrder>,
    onClick: () -> Unit
) {
    val (title, desc) = when (prescriptions.size) {
        1 -> Pair(prescriptions.first().title ?: "", null)
        else -> Pair(
            stringResource(R.string.pharmacy_order_nr_of_prescriptions, prescriptions.size),
            prescriptions.joinToString { it.title ?: "" }
        )
    }

    FlatButton(
        modifier = Modifier.testTag(TestTag.PharmacySearch.OrderSummary.PrescriptionSelectionButton),
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = AppTheme.typography.subtitle1)
                desc?.let {
                    SpacerTiny()
                    Text(desc, style = AppTheme.typography.body2l, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            SpacerMedium()
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
        }
    }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FlatButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) =
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.neutral025,
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp
    ) {
        Box(Modifier.padding(PaddingDefaults.Medium)) {
            content()
        }
    }
