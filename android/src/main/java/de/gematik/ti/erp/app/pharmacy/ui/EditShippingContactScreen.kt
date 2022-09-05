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

import android.util.Patterns
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.max
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("LongMethod")
@Composable
fun EditShippingContactScreen(
    navController: NavController,
    viewModel: PharmacySearchViewModel
) {
    val listState = rememberLazyListState()

    val state by viewModel.orderScreenState().collectAsState(PharmacyScreenData.defaultOrderState)

    var contact by remember(state.contact) { mutableStateOf(state.contact) }

    var showBackAlert by remember { mutableStateOf(false) }

    val telephoneOptional by derivedStateOf {
        state.orderOption == PharmacyScreenData.OrderOption.ReserveInPharmacy
    }
    val telephoneError by derivedStateOf { !isPhoneValid(contact.telephoneNumber, telephoneOptional) }
    val nameError by derivedStateOf { contact.name.isBlank() }
    val line1Error by derivedStateOf { contact.line1.isBlank() }
    val codeAndCityError by derivedStateOf { contact.postalCodeAndCity.isBlank() }
    val mailError by derivedStateOf { !isMailValid(contact.mail) }

    if (showBackAlert) {
        CommonAlertDialog(
            header = stringResource(R.string.edit_contact_back_alert_header),
            info = stringResource(R.string.edit_contact_back_alert_information),
            onCancel = { showBackAlert = false },
            onClickAction = { navController.popBackStack() },
            cancelText = stringResource(R.string.edit_contact_back_alert_change),
            actionText = stringResource(R.string.edit_contact_back_alert_action)
        )
    }

    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.onSaveContact(contact)
                        navController.popBackStack()
                    },
                    enabled = !telephoneError && !mailError && !nameError && !line1Error && !codeAndCityError
                ) {
                    Text(stringResource(R.string.edit_shipping_contact_save))
                }
                SpacerSmall()
            }
        },
        topBarTitle = stringResource(R.string.edit_shipping_contact_top_bar_title),
        listState = listState,
        onBack = {
            if (!telephoneError && !mailError && !nameError && !line1Error && !codeAndCityError) {
                viewModel.onSaveContact(contact)
                navController.popBackStack()
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
            item {
                Text(
                    stringResource(R.string.edit_shipping_contact_title_contact),
                    style = AppTheme.typography.h6
                )
            }
            item(key = "InputField_1") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(1, listState)
                        .fillParentMaxWidth(),
                    value = contact.telephoneNumber,
                    onValueChange = { phone -> contact = contact.copy(telephoneNumber = phone.trim()) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    label = {
                        Text(
                            if (telephoneOptional) {
                                stringResource(R.string.edit_shipping_contact_phone_optional)
                            } else {
                                stringResource(R.string.edit_shipping_contact_phone)
                            }
                        )
                    },
                    isError = telephoneError,
                    errorText = { Text(stringResource(R.string.edit_shipping_contact_error_phone)) },
                    keyBoardType = KeyboardType.Phone
                )
            }
            item(key = "InputField_2") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(2, listState)
                        .fillParentMaxWidth(),
                    value = contact.mail,
                    onValueChange = { mail -> contact = (contact.copy(mail = mail)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    label = { Text(stringResource(R.string.edit_shipping_contact_mail)) },
                    isError = mailError,
                    keyBoardType = KeyboardType.Email
                )
            }
            item {
                SpacerLarge()
                Text(
                    stringResource(R.string.edit_shipping_contact_title_address),
                    style = AppTheme.typography.h6
                )
            }
            item(key = "InputField_3") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(4, listState)
                        .fillParentMaxWidth(),
                    value = contact.name,
                    onValueChange = { name -> contact = (contact.copy(name = name)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    label = { Text(stringResource(R.string.edit_shipping_contact_name)) },
                    isError = nameError,
                    errorText = { Text(stringResource(R.string.edit_shipping_contact_error_name)) }
                )
            }
            item(key = "InputField_4") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(5, listState)
                        .fillParentMaxWidth(),
                    value = contact.line1,
                    onValueChange = { line1 -> contact = (contact.copy(line1 = line1)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    label = { Text(stringResource(R.string.edit_shipping_contact_title_line1)) },
                    isError = line1Error,
                    errorText = { Text(stringResource(R.string.edit_shipping_contact_error_line1)) }
                )
            }
            item(key = "InputField_5") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(6, listState)
                        .fillParentMaxWidth(),
                    value = contact.line2,
                    onValueChange = { line2 -> contact = (contact.copy(line2 = line2)) },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    label = { Text(stringResource(R.string.edit_shipping_contact_line2)) },
                    isError = false
                )
            }
            item(key = "InputField_6") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(7, listState)
                        .fillParentMaxWidth(),
                    value = contact.postalCodeAndCity,
                    onValueChange = { postalCodeAndCity ->
                        contact = (contact.copy(postalCodeAndCity = postalCodeAndCity))
                    },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                    label = { Text(stringResource(R.string.edit_shipping_contact_postal_code_and_city)) },
                    isError = codeAndCityError,
                    errorText = { Text(stringResource(R.string.edit_shipping_contact_error_postal_code_and_city)) }
                )
            }
            item(key = "InputField_7") {
                InputField(
                    modifier = Modifier
                        .scrollOnFocus(8, listState)
                        .fillParentMaxWidth(),
                    value = contact.deliveryInformation,
                    onValueChange = { deliveryInformation ->
                        contact = (contact.copy(deliveryInformation = deliveryInformation))
                    },
                    onSubmit = { focusManager.clearFocus() },
                    label = { Text(stringResource(R.string.edit_shipping_contact_delivery_information)) },
                    isError = false
                )
            }
        }
    }
}

fun isMailValid(mail: String?): Boolean {
    return mail.isNullOrEmpty() || Patterns.EMAIL_ADDRESS.matcher(mail).matches()
}

fun isPhoneValid(telephoneNumber: String?, optional: Boolean): Boolean {
    return telephoneNumber != null &&
        if (optional) true else telephoneNumber.length >= 4
}

private const val LayoutDelay = 330L

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
fun Modifier.scrollOnFocus() = composed {
    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var hasFocus by remember { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(hasFocus, keyboardVisible) {
        if (hasFocus && keyboardVisible) {
            mutex.mutate {
                delay(LayoutDelay)
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged {
            if (it.hasFocus) {
                hasFocus = true
                coroutineScope.launch {
                    mutex.mutate(MutatePriority.UserInput) {
                        delay(LayoutDelay)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            } else {
                hasFocus = false
            }
        }
}
