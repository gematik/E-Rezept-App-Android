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

package de.gematik.ti.erp.app.pharmacy.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import kotlinx.coroutines.launch
import java.util.UUID

private const val OrderSuccessVideoAspectRatio = 1.69f
private val TopBarColor = Color(0xffd6e9fb)

@Requirement(
    "A_19183#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Displays a summary of a prescription assignment to a pharmacy."
)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OrderOverview(
    orderState: PharmacyOrderState,
    onClickContacts: () -> Unit,
    onSelectPrescriptions: () -> Unit,
    onBack: () -> Unit,
    onFinish: (Boolean) -> Unit
) {
    val order by orderState.order
    val selectedPharmacy = remember { orderState.selectedPharmacy!! }
    val selectedOrderOption = remember { orderState.selectedOrderOption!! }

    val listState = rememberLazyListState()
    val videoHeightPx = remember { mutableFloatStateOf(0f) }

    val contact = order.contact
    val shippingContactCompleted = remember(contact) {
        selectedOrderOption == PharmacyScreenData.OrderOption.PickupService ||
            !contact.phoneOrAddressMissing()
    }

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it, modifier = Modifier.systemBarsPadding())
        }
    ) {
        Column(
            Modifier
                .testTag(TestTag.PharmacySearch.OrderSummary.Screen)
                .fillMaxSize()
        ) {
            Box(
                Modifier.weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)
                ) {
                    item {
                        val shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        VideoContent(
                            Modifier
                                .onPlaced {
                                    videoHeightPx.value = it.size.height.toFloat()
                                }
                                .clip(shape)
                                .background(TopBarColor)
                                .statusBarsPadding()
                                .fillMaxWidth(),
                            source = when (selectedOrderOption) {
                                PharmacyScreenData.OrderOption.PickupService -> R.raw.animation_local
                                PharmacyScreenData.OrderOption.CourierDelivery -> R.raw.animation_courier
                                PharmacyScreenData.OrderOption.MailDelivery -> R.raw.animation_mail
                            },
                            aspectRatioOverwrite = OrderSuccessVideoAspectRatio
                        )
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
                            ContactSelectionButton(
                                contact = order.contact,
                                shippingContactCompleted = shippingContactCompleted,
                                onClick = onClickContacts
                            )
                        }
                    }
                    item {
                        Column(Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                            Text(stringResource(R.string.pharmacy_order_prescriptions), style = AppTheme.typography.h6)
                            SpacerMedium()
                            order.prescriptions.takeIf { it.isNotEmpty() }?.let {
                                PrescriptionSelectionButton(
                                    prescriptions = it,
                                    onClick = onSelectPrescriptions
                                )
                            }
                        }
                    }
                    item {
                        Column(Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                            Text(stringResource(R.string.pharmacy_order_pharmacy), style = AppTheme.typography.h6)
                            SpacerMedium()
                            PharmacySelectionButton(
                                selectedPharmacy = selectedPharmacy,
                                selectedOrderOption = selectedOrderOption,
                                onClick = onBack
                            )
                            SpacerMedium()
                        }
                    }
                }

                VanishingTopBar(
                    listState = listState,
                    videoHeightPx = videoHeightPx,
                    onBack = onBack
                )
            }
            RedeemButton(
                orderState = orderState,
                scaffoldState = scaffoldState,
                shippingContactCompleted = shippingContactCompleted,
                onFinish = onFinish
            )
        }
    }
}

@Composable
private fun RedeemButton(
    orderState: PharmacyOrderState,
    scaffoldState: ScaffoldState,
    shippingContactCompleted: Boolean,
    onFinish: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val redeemController = rememberRedeemPrescriptionsController()

    val order by orderState.order
    val selectedPharmacy = remember { orderState.selectedPharmacy!! }
    val selectedOrderOption = remember { orderState.selectedOrderOption!! }

    var uploadInProgress by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }

    if (showDialog) {
        PrescriptionRedeemAlertDialog(
            title = dialogTitle,
            description = dialogDescription,
            showDialog = showDialog,
            onDismiss = {
                showDialog = false
                onFinish(true)
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(Modifier.navigationBarsPadding()) {
            SpacerMedium()
            PrimaryButtonLarge(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton),
                enabled = shippingContactCompleted && !uploadInProgress,
                onClick = {
                    uploadInProgress = true
                    scope.launch {
                        try {
                            val redeemState = if (orderState.profile.lastAuthenticated == null) {
                                redeemController.orderPrescriptionsDirectly(
                                    orderId = UUID.randomUUID(),
                                    prescriptions = order.prescriptions,
                                    redeemOption = selectedOrderOption,
                                    pharmacy = selectedPharmacy,
                                    contact = order.contact
                                )
                            } else {
                                redeemController
                                    .orderPrescriptions(
                                        profileId = orderState.profile.id,
                                        orderId = UUID.randomUUID(),
                                        prescriptions = order.prescriptions,
                                        redeemOption = selectedOrderOption,
                                        pharmacy = selectedPharmacy,
                                        contact = order.contact
                                    )
                            }
                            when (redeemState) {
                                is PrescriptionServiceErrorState -> {
                                    redeemErrorMessage(context, redeemState)?.let {
                                        scaffoldState.snackbarHostState.showSnackbar(it)
                                    }
                                }

                                is RedeemPrescriptionsController.State.Ordered -> {
                                    val responseCodeMessagesMap = responseCodeMessagesMap(context)

                                    val results = redeemState.results.values
                                    when {
                                        results.size == 1 -> {
                                            // case 1: When one prescription is transferred.
                                            val responseMessagesPairedList: List<Pair<String, String>> =
                                                results.mapNotNull {
                                                    responseCodeMessagesMap[it as PrescriptionServiceState]
                                                }
                                            dialogTitle = responseMessagesPairedList.joinToString { it.first ?: "" }
                                            dialogDescription =
                                                responseMessagesPairedList.joinToString { it.second ?: "" }
                                        }

                                        results.contains<Any?>(RedeemPrescriptionsController.State.Success.Ok) -> {
                                            // case 2.1: When multiple prescriptions are transferred Successfully.
                                            dialogTitle = context.getString(R.string.server_return_code_200_title)
                                            dialogDescription = context.getString(R.string.server_return_code_200)
                                        }

                                        else -> {
                                            // case 2.2: When any multiple prescription are transferred Unsuccessfully. Show a generic error message.
                                            dialogTitle = context.getString(R.string.server_return_code_title_failure)
                                            dialogDescription = context.getString(R.string.several_return_code)
                                        }
                                    }
                                    showDialog = true
                                }
                            }
                        } finally {
                            uploadInProgress = false
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.pharmacy_order_send))
            }
        }
    }
}

fun responseCodeMessagesMap(context: Context): Map<Any, Pair<String, String>> {
    // Mapping server code responses to the Title & Description used in Alert Dialog.
    return mapOf(
        RedeemPrescriptionsController.State.Success.Ok to Pair(
            context.getString(R.string.server_return_code_200_title),
            context.getString(R.string.server_return_code_200)
        ), // 200, 201
        RedeemPrescriptionsController.State.Error.IncorrectDataStructure to Pair(
            context.getString(R.string.server_return_code_400_title),
            context.getString(R.string.server_return_code_400)
        ), // 400
        RedeemPrescriptionsController.State.Error.JsonViolated to Pair(
            context.getString(R.string.server_return_code_title_failure),
            context.getString(R.string.server_return_code_401)
        ), // 401
        RedeemPrescriptionsController.State.Error.UnableToRedeem to Pair(
            context.getString(R.string.server_return_code_title_failure),
            context.getString(R.string.server_return_code_404)
        ), // 404
        RedeemPrescriptionsController.State.Error.Timeout to Pair(
            context.getString(R.string.server_return_code_408_title),
            context.getString(R.string.server_return_code_408)
        ), // 408
        RedeemPrescriptionsController.State.Error.Conflict to Pair(
            context.getString(R.string.server_return_code_409_title),
            context.getString(R.string.server_return_code_409)
        ), // 409
        RedeemPrescriptionsController.State.Error.Gone to Pair(
            context.getString(R.string.server_return_code_410_title),
            context.getString(R.string.server_return_code_410)
        ), // 410
        RedeemPrescriptionsController.State.Error.Unknown to Pair(
            context.getString(R.string.server_return_no_code_title),
            context.getString(R.string.server_return_no_code)
        ) // No error
    )
}

@Composable
fun PrescriptionRedeemAlertDialog(
    title: String,
    description: String,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AcceptDialog(
            header = title,
            info = description,
            onClickAccept = {
                onDismiss()
            },
            acceptText = stringResource(R.string.pharmacy_search_apovz_call_failed_accept)
        )
    }
}

@Composable
private fun VanishingTopBar(
    listState: LazyListState,
    videoHeightPx: State<Float>,
    onBack: () -> Unit
) {
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }

    val showTopBar by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> true
                listState.firstVisibleItemScrollOffset - videoHeightPx.value > -topBarHeightPx -> true
                else -> false
            }
        }
    }
    val showTopBarText by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> true
                else -> false
            }
        }
    }
    val topBarAlpha by animateFloatAsState(if (showTopBar) 1f else 0f, tween())
    val topBarElevation by animateDpAsState(if (showTopBar) 4.dp else 0.dp, tween())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onPlaced {
                topBarHeightPx = it.size.height.toFloat()
            },
        color = TopBarColor.copy(alpha = topBarAlpha),
        elevation = topBarElevation
    ) {
        Row(Modifier.statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.padding(PaddingDefaults.Tiny),
                onClick = onBack
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(AppTheme.colors.neutral000, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = AppTheme.colors.primary600,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            SpacerMedium()
            AnimatedVisibility(
                showTopBarText,
                enter = fadeIn(tween()),
                exit = fadeOut(tween())
            ) {
                Text(
                    text = stringResource(R.string.pharmacy_order_title),
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ContactSelectionButton(
    contact: PharmacyUseCaseData.ShippingContact,
    shippingContactCompleted: Boolean,
    onClick: () -> Unit
) {
    FlatButton(
        onClick = onClick
    ) {
        if (contact.addressIsMissing()) {
            Text(
                stringResource(R.string.pharmacy_order_add_contacts),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.primary600
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
                    if (!shippingContactCompleted) {
                        SpacerSmall()
                        Surface(shape = RoundedCornerShape(8.dp), color = AppTheme.colors.red100) {
                            Text(
                                stringResource(R.string.pharmacy_order_further_contact_information_required),
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
                    color = AppTheme.colors.primary600
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
    val scannedRxTxt = stringResource(R.string.pres_details_scanned_prescription)
    val title = if (prescriptions.size > 1) {
        stringResource(R.string.pharmacy_order_nr_of_prescriptions, prescriptions.size)
    } else {
        prescriptions.first().title ?: scannedRxTxt
    }

    val desc = remember(prescriptions) {
        if (prescriptions.size > 1) {
            prescriptions.joinToString { it.title ?: scannedRxTxt }
        } else {
            null
        }
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
            Icon(Icons.Rounded.KeyboardArrowRight, null)
        }
    }
}

@Composable
private fun SmallChip(
    icon: ImageVector,
    text: String
) =
    Surface(shape = RoundedCornerShape(8.dp), color = AppTheme.colors.neutral100) {
        Row(
            Modifier.padding(horizontal = PaddingDefaults.Small, vertical = 2.dp),
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
                color = AppTheme.colors.primary600
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
            .padding(horizontal = PaddingDefaults.ShortMedium, vertical = PaddingDefaults.ShortMedium / 2)
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
