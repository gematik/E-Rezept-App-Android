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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.derivedStateOf
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.PrimaryButton
import de.gematik.ti.erp.app.cardwall.ui.SecondaryButton
import de.gematik.ti.erp.app.mainscreen.ui.ssoStatusColor
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.ui.profileColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.dateTimeShortText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("LongMethod")
@Composable
fun PharmacyOrderScreen(
    navController: NavController,
    viewModel: PharmacySearchViewModel,
    onSuccessfullyOrdered: (PharmacyScreenData.OrderOption) -> Unit
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()

    val state by produceState(PharmacyScreenData.defaultOrderState) {
        viewModel.orderScreenState().collect {
            value = it
        }
    }

    val shippingContactCompleted by derivedStateOf {
        state.prescriptions.isNotEmpty() &&
            if (state.orderOption == PharmacyScreenData.OrderOption.ReserveInPharmacy) {
                !state.contact.addressIsMissing()
            } else {
                !state.contact.phoneOrAddressMissing()
            }
    }

    val reserveTitle = stringResource(R.string.pharmacy_order_top_bar_title_order)
    val orderTitle = stringResource(R.string.pharmacy_order_top_bar_title_order)
    val reserveButtonText = stringResource(R.string.pharmacy_order_button_text_reserve)
    val orderButtonText = stringResource(R.string.pharmacy_order_button_text_order)

    val topBarTitle = remember(state) {
        when (state.orderOption) {
            PharmacyScreenData.OrderOption.ReserveInPharmacy -> reserveTitle
            else -> orderTitle
        }
    }
    val buttonText = remember(state) {
        when (state.orderOption) {
            PharmacyScreenData.OrderOption.ReserveInPharmacy -> reserveButtonText
            else -> orderButtonText
        }
    }
    val uploadErrorText = stringResource(R.string.redeem_online_error_uploading)

    var uploadInProgress by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        navigationMode = NavigationBarMode.Back,
        bottomBar = {
            Surface(
                color = MaterialTheme.colors.surface,
                elevation = 12.dp
            ) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.pharmacy_order_bottom_information),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.caption1l,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SpacerSmall()
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = shippingContactCompleted && state.anySelected() && !uploadInProgress,
                        onClick = {
                            uploadInProgress = true
                            coroutineScope.launch {
                                try {
                                    delay(1000)
                                    viewModel.triggerOrderInPharmacy(state).fold(
                                        onSuccess = {
                                            withContext(Dispatchers.Main) {
                                                onSuccessfullyOrdered(state.orderOption)
                                            }
                                        },
                                        onFailure = { scaffoldState.snackbarHostState.showSnackbar(uploadErrorText) }
                                    )
                                } finally {
                                    uploadInProgress = false
                                }
                            }
                        }
                    ) {
                        if (uploadInProgress) {
                            val color by ButtonDefaults.buttonColors().contentColor(false)
                            CircularProgressIndicator(Modifier.size(24.dp), color = color, strokeWidth = 2.dp)
                            SpacerSmall()
                        }
                        Text(buttonText)
                    }
                }
            }
        },
        topBarTitle = topBarTitle,
        listState = listState,
        onBack = { navController.popBackStack() }
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(
                top = PaddingDefaults.Medium + it.calculateTopPadding(),
                bottom = PaddingDefaults.Medium + it.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        ) {
            item {
                DescriptionHeader(state.selectedPharmacy.name)
                SpacerLarge()
            }
            item {
                Text(
                    stringResource(R.string.pharmacy_order_contact_and_delivery_address),
                    style = AppTheme.typography.h6
                )
            }
            item {
                if (state.prescriptions.isNotEmpty()) {
                    Contact(state.activeProfile, state.contact, shippingContactCompleted, onClickEdit = {
                        navController.navigate(PharmacyNavigationScreens.EditShippingContact.path())
                    })
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        CircularProgressIndicator(
                            Modifier
                                .size(120.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
                SpacerLarge()
            }
            item {
                Text(stringResource(R.string.pharmacy_order_title_prescriptions), style = AppTheme.typography.h6)
            }
            state.prescriptions.forEach { (prescription, selected) ->
                item {
                    Prescription(
                        prescription = prescription,
                        selected = selected,
                        onSelect = { select ->
                            if (select) {
                                viewModel.onSelectOrder(prescription)
                            } else {
                                viewModel.onDeselectOrder(prescription)
                            }
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = uploadInProgress,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .alpha(0.33f)
                    .background(AppTheme.colors.neutral000)
                    .semantics(mergeDescendants = false) {}
                    .pointerInput(Unit) { }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Contact(
    activeProfile: ProfilesUseCaseData.Profile,
    contact: PharmacyUseCaseData.ShippingContact,
    shippingContactCompleted: Boolean,
    onClickEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp,
        onClick = onClickEdit
    ) {
        if (contact.addressIsMissing()) {
            Column(Modifier.padding(PaddingDefaults.Medium)) {
                Row {
                    val colors = profileColor(profileColorNames = activeProfile.color)
                    val ssoStatusColor = ssoStatusColor(activeProfile, activeProfile.ssoTokenScope)

                    Avatar(Modifier.size(40.dp), activeProfile, ssoStatusColor)
                    SpacerMedium()
                    Text(
                        stringResource(R.string.pharmacy_order_contact_required),
                        style = AppTheme.typography.body1
                    )
                }
                SpacerMedium()
                SecondaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingDefaults.Medium),
                    onClick = { onClickEdit() }
                ) {
                    Text(stringResource(R.string.pharmacy_order_edit_contact))
                }
            }
        } else {
            Row(Modifier.padding(PaddingDefaults.Medium)) {
                val colors = profileColor(profileColorNames = activeProfile.color)
                val ssoStatusColor = ssoStatusColor(activeProfile, activeProfile.ssoTokenScope)

                Avatar(Modifier.size(40.dp), activeProfile, ssoStatusColor)
                SpacerMedium()
                Column(Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            if (contact.name.isNotBlank()) {
                                Text(contact.name, style = AppTheme.typography.subtitle1)
                            }
                            contact.address().forEach {
                                Text(it, style = AppTheme.typography.body1)
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
                Icon(Icons.Rounded.Edit, null, tint = AppTheme.colors.neutral400)
            }
        }
    }
}

@Composable
private fun SmallChip(
    icon: ImageVector,
    text: String
) {
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
}

@Preview
@Composable
private fun SmallChipPreview() {
    AppTheme {
        SmallChip(Icons.Outlined.Phone, "0049123456789")
    }
}

@Preview
@Composable
private fun ContactPreview() {
    AppTheme {
        Contact(
            activeProfile = ProfilesUseCaseData.Profile(
                id = "0",
                name = "Irina Muster",
                insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(),
                active = false,
                color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                lastAuthenticated = null,
                ssoTokenScope = null,
                avatarFigure = ProfilesData.AvatarFigure.Initials
            ),
            contact = PharmacyUseCaseData.ShippingContact(
                name = "Beate Muster",
                line1 = "Friedrichstraße 123",
                line2 = "Test",
                postalCodeAndCity = "10998 Berlin",
                telephoneNumber = "00123456789",
                mail = "mailadresse@provider.de",
                deliveryInformation = "Bitte im Vorderhaus bei Familie Schmidt abgeben."
            ),
            onClickEdit = {},
            shippingContactCompleted = false
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Prescription(
    prescription: PharmacyUseCaseData.PrescriptionOrder,
    selected: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp,
        onClick = { onSelect(!selected) }
    ) {
        Row(Modifier.padding(PaddingDefaults.Medium)) {
            Column(
                Modifier
                    .weight(1f)
                    .then(
                        if (prescription.substitutionsAllowed) Modifier
                        else Modifier.align(Alignment.CenterVertically)
                    )
            ) {
                val prescriptionTitle = if (prescription.scannedOn != null) {
                    stringResource(R.string.order_scanned_prescription_header)
                } else {
                    prescription.title
                }
                Text(prescriptionTitle, style = AppTheme.typography.subtitle1)
                if (prescription.scannedOn != null) {
                    dateTimeShortText(prescription.scannedOn)
                    Text(
                        text = stringResource(
                            R.string.order_scanned_on_info,
                            dateTimeShortText(prescription.scannedOn)
                        ),
                        style = AppTheme.typography.body2l
                    )
                }
                if (prescription.substitutionsAllowed) {
                    SpacerSmall()
                    Text(
                        text = stringResource(id = R.string.pres_detail_aut_idem_info),
                        style = AppTheme.typography.body2l
                    )
                }
            }
            SpacerMedium()
            if (selected) {
                Icon(Icons.Filled.CheckCircle, null, tint = AppTheme.colors.primary600)
            } else {
                Icon(Icons.Filled.RadioButtonUnchecked, null, tint = AppTheme.colors.neutral400)
            }
        }
    }
}

@Preview
@Composable
private fun SelectedPrescriptionPreview() {
    AppTheme {
        Prescription(
            PharmacyUseCaseData.PrescriptionOrder(
                taskId = "",
                title = "Ivermectin",
                substitutionsAllowed = false,
                accessCode = ""
            ),
            selected = true,
            onSelect = {}
        )
    }
}

@Preview
@Composable
private fun UnselectedPrescriptionPreview() {
    AppTheme {
        Prescription(
            PharmacyUseCaseData.PrescriptionOrder(
                taskId = "",
                title = "Ivermectin",
                substitutionsAllowed = true,
                accessCode = ""
            ),
            selected = false,
            onSelect = {}
        )
    }
}

@Composable
private fun DescriptionHeader(pharmacyName: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth(),
        text = annotatedStringResource(
            id = R.string.pharm_reserve_subheader,
            buildAnnotatedString {
                withStyle(AppTheme.typography.subtitle2.toSpanStyle()) {
                    append(pharmacyName)
                }
            }
        ),
        textAlign = TextAlign.Center,
        style = AppTheme.typography.body2l
    )
}
