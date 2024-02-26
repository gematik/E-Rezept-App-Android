/*
 * Copyright (c) 2024 gematik GmbH
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

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.max
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyOrderController
import de.gematik.ti.erp.app.pharmacy.ui.model.addressSupplementInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.cityInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.deliveryInformationInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.mailInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.nameInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.phoneNumberInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.postalCodeInputField
import de.gematik.ti.erp.app.pharmacy.ui.model.streetAndNumberInputField
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyCity
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyLine1
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyMail
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyName
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyPhoneNumber
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isEmptyPostalCode
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidCity
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidDeliveryInformation
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidLine1
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidLine2
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidMail
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidName
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidPhoneNumber
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isInvalidPostalCode
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase.Companion.isValid
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ValidationResult(
    val isEmpty: Boolean,
    val isInvalid: Boolean
)

@Requirement(
    "O.Purp_2#6",
    "O.Data_6#6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Contact information is collected when needed for redeeming."
)
@Suppress("LongMethod")
@Composable
fun EditShippingContactScreen(
    pharmacyOrderController: PharmacyOrderController,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

    val orderState by pharmacyOrderController.orderState
    val selectedOrderOption = remember { pharmacyOrderController.selectedOrderOption }

    var contact by remember(orderState.contact) { mutableStateOf(orderState.contact) }
    val shippingContactState = remember(orderState, contact) {
        if (selectedOrderOption != null) {
            pharmacyOrderController.shippingContactState(contact, selectedOrderOption)
        } else null
    }

    val directRedeemEnabled by pharmacyOrderController.isDirectRedeemEnabledState

    var showBackAlert by remember { mutableStateOf(false) }

    if (showBackAlert) { BackAlert(onCancel = { showBackAlert = false }, onBack = onBack) }

    shippingContactState?.let { state ->

        AnimatedElevationScaffold(
            navigationMode = NavigationBarMode.Back,
            bottomBar = {
                ContactBottomBar(
                    enabled = state.isValid(),
                    onClick = {
                        pharmacyOrderController.onSaveContact(contact)
                        onBack()
                    }
                )
            },
            topBarTitle = stringResource(R.string.edit_shipping_contact_top_bar_title),
            listState = listState,
            onBack = {
                if (state.isValid()) {
                    pharmacyOrderController.onSaveContact(contact)
                    onBack()
                } else {
                    showBackAlert = true
                }
            }
        ) { contentPadding ->
            val imePadding = WindowInsets.ime.asPaddingValues()

            val focusManager = LocalFocusManager.current

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
                contentPadding = PaddingValues(
                    top = PaddingDefaults.Medium + contentPadding.calculateTopPadding(),
                    bottom = PaddingDefaults.Medium + max(
                        imePadding.calculateBottomPadding(),
                        contentPadding.calculateBottomPadding()
                    ),
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium
                )
            ) {
                item { ContactHeader() }
                phoneNumberInputField(
                    listState = listState,
                    value = contact.telephoneNumber,
                    validationResult = ValidationResult(
                        isEmpty = state.isEmptyPhoneNumber(),
                        isInvalid = state.isInvalidPhoneNumber()
                    ),
                    onValueChange = { phone ->
                        contact = contact.copy(
                            telephoneNumber = phone.trim()
                        )
                    },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
                )
                // we sent the mail currently only on direct redeem
                if (directRedeemEnabled) {
                    mailInputField(
                        listState = listState,
                        validationResult = ValidationResult(
                            isEmpty = state.isEmptyMail(),
                            isInvalid = state.isInvalidMail()
                        ),
                        value = contact.mail,
                        onValueChange = { mail -> contact = (contact.copy(mail = mail.trim())) },
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }

                item { AddressHeader() }

                nameInputField(
                    listState = listState,
                    value = contact.name,
                    onValueChange = { name -> contact = (contact.copy(name = name)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    validationResult = ValidationResult(
                        isEmpty = state.isEmptyName(),
                        isInvalid = state.isInvalidName()
                    )
                )

                streetAndNumberInputField(
                    listState = listState,
                    value = contact.line1,
                    onValueChange = { line1 -> contact = (contact.copy(line1 = line1)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    validationResult = ValidationResult(
                        isEmpty = state.isEmptyLine1(),
                        isInvalid = state.isInvalidLine1()
                    )
                )

                addressSupplementInputField(
                    listState = listState,
                    value = contact.line2,
                    validationResult = ValidationResult(
                        isEmpty = false, // optional,
                        isInvalid = state.isInvalidLine2()
                    ),
                    onValueChange = { line2 -> contact = (contact.copy(line2 = line2)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
                )

                postalCodeInputField(
                    listState = listState,
                    value = contact.postalCode,
                    onValueChange = { postalCode ->
                        contact = (contact.copy(postalCode = postalCode))
                    },
                    validationResult = ValidationResult(
                        isEmpty = state.isEmptyPostalCode(),
                        isInvalid = state.isInvalidPostalCode()
                    ),
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
                )

                cityInputField(
                    listState = listState,
                    value = contact.city,
                    onValueChange = { city ->
                        contact = (contact.copy(city = city))
                    },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    validationResult = ValidationResult(
                        isEmpty = state.isEmptyCity(),
                        isInvalid = state.isInvalidCity()
                    )
                )

                deliveryInformationInputField(
                    listState = listState,
                    value = contact.deliveryInformation,
                    validationResult = ValidationResult(
                        isEmpty = false, // optional,
                        isInvalid = state.isInvalidDeliveryInformation()
                    ),
                    onValueChange = { deliveryInformation ->
                        contact = (contact.copy(deliveryInformation = deliveryInformation))
                    },
                    onSubmit = { focusManager.clearFocus() }
                )
            }
        } ?: run {
            Napier.e { "ShippingContact is null" }
        }
    }
}

@Composable
fun ContactBottomBar(enabled: Boolean, onClick: () -> Unit) {
    BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onClick,
            enabled = enabled
        ) {
            Text(stringResource(R.string.edit_shipping_contact_save))
        }
        SpacerSmall()
    }
}

@Composable
fun AddressHeader() {
    SpacerLarge()
    Text(
        stringResource(R.string.edit_shipping_contact_title_address),
        style = AppTheme.typography.h6
    )
}

@Composable
fun BackAlert(onCancel: () -> Unit, onBack: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.edit_contact_back_alert_header),
        info = stringResource(R.string.edit_contact_back_alert_information),
        onCancel = onCancel,
        onClickAction = onBack,
        cancelText = stringResource(R.string.edit_contact_back_alert_change),
        actionText = stringResource(R.string.edit_contact_back_alert_action)
    )
}

@Composable
fun ContactHeader() {
    Text(
        stringResource(R.string.edit_shipping_contact_title_contact),
        style = AppTheme.typography.h6
    )
}

private const val LayoutDelay = 330L

// TODO: Move to a different place, used in many places
@OptIn(ExperimentalLayoutApi::class)
fun Modifier.scrollOnFocus(to: Int, listState: LazyListState, offset: Int = 0) = composed {
    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    var hasFocus by remember { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(hasFocus, keyboardVisible) {
        if (hasFocus && keyboardVisible) {
            mutex.mutate {
                delay(LayoutDelay)
                listState.animateScrollToItem(to, offset)
            }
        }
    }

    onFocusChanged {
        if (it.hasFocus) {
            hasFocus = true
            coroutineScope.launch {
                mutex.mutate(MutatePriority.UserInput) {
                    delay(LayoutDelay)
                    listState.animateScrollToItem(to, offset)
                }
            }
        } else {
            hasFocus = false
        }
    }
}
