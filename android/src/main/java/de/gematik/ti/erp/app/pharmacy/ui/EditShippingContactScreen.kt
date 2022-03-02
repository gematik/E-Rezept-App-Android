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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.max
import androidx.navigation.NavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.launch

@Composable
fun EditShippingContactScreen(
    navController: NavController,
    taskIds: List<String>,
    viewModel: PharmacySearchViewModel
) {
    val listState = rememberLazyListState()

    val state by viewModel.orderScreenState(taskIds).collectAsState(PharmacyScreenData.defaultOrderState)

    var contact by rememberSaveable(state.contact) { mutableStateOf(state.contact) }
    val originalContact by rememberSaveable(contact != null) { mutableStateOf(contact) }

    var showBackAlert by remember { mutableStateOf(false) }

    val telephoneError = remember(contact?.telephoneNumber) { !isPhoneValid(contact?.telephoneNumber) }
    val nameError = remember(contact?.name) { contact?.name?.isBlank() ?: true }
    val line1Error = remember(contact?.line1) { contact?.line1?.isBlank() ?: true }
    val codeAndCityError = remember(contact?.postalCodeAndCity) { contact?.postalCodeAndCity?.isBlank() ?: true }
    val mailError = remember(contact?.mail) { !isMailValid(contact?.mail) }

    if (showBackAlert) {
        CommonAlertDialog(
            header = stringResource(R.string.edit_contact_back_alert_header),
            info = stringResource(R.string.edit_contact_back_alert_info),
            onCancel = { showBackAlert = false },
            onClickAction = { navController.popBackStack() },
            cancelText = stringResource(R.string.edit_contact_back_alert_cancel),
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
                        viewModel.onSaveContact(contact!!)
                        navController.popBackStack()
                    },
                    enabled = !telephoneError && !nameError && !line1Error && !codeAndCityError
                ) {
                    Text(stringResource(R.string.edit_shipping_contact_save))
                }
                SpacerSmall()
            }
        },
        topBarTitle = stringResource(R.string.edit_shipping_contact_top_bar_title),
        listState = listState,
        onBack = {
            if (contact != originalContact) {
                showBackAlert = true
            } else {
                navController.popBackStack()
            }
        }
    ) { contentPadding ->
        val imePadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.ime,
            applyBottom = true,
        )

        val focusManager = LocalFocusManager.current

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(
                top = PaddingDefaults.Medium + contentPadding.calculateTopPadding(),
                bottom = PaddingDefaults.Medium + max(imePadding.calculateBottomPadding(), contentPadding.calculateBottomPadding()),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
            )
        ) {
            contact?.let { c ->
                item {
                    Text(
                        stringResource(R.string.edit_shipping_contact_title_contact),
                        style = MaterialTheme.typography.h6
                    )
                }
                item(key = "InputField_1") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(1, listState).fillParentMaxWidth(),
                        value = c.telephoneNumber,
                        onValueChange = { phone -> contact = c.copy(telephoneNumber = phone) },
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                        label = { Text(stringResource(R.string.edit_shipping_contact_phone)) },
                        isError = telephoneError,
                        errorText = { Text(stringResource(R.string.edit_shipping_contact_error_phone)) },
                        keyBoardType = KeyboardType.Phone
                    )
                }
                item(key = "InputField_2") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(2, listState).fillParentMaxWidth(),
                        value = c.mail,
                        onValueChange = { mail -> contact = (c.copy(mail = mail)) },
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
                        style = MaterialTheme.typography.h6
                    )
                }
                item(key = "InputField_3") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(4, listState).fillParentMaxWidth(),
                        value = c.name,
                        onValueChange = { name -> contact = (c.copy(name = name)) },
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                        label = { Text(stringResource(R.string.edit_shipping_contact_name)) },
                        isError = nameError,
                        errorText = { Text(stringResource(R.string.edit_shipping_contact_error_name)) }
                    )
                }
                item(key = "InputField_4") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(5, listState).fillParentMaxWidth(),
                        value = c.line1,
                        onValueChange = { line1 -> contact = (c.copy(line1 = line1)) },
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                        label = { Text(stringResource(R.string.edit_shipping_contact_title_line1)) },
                        isError = line1Error,
                        errorText = { Text(stringResource(R.string.edit_shipping_contact_error_line1)) }
                    )
                }
                item(key = "InputField_5") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(6, listState).fillParentMaxWidth(),
                        value = c.line2,
                        onValueChange = { line2 -> contact = (c.copy(line2 = line2)) },
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                        label = { Text(stringResource(R.string.edit_shipping_contact_line2)) },
                        isError = false
                    )
                }
                item(key = "InputField_6") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(7, listState).fillParentMaxWidth(),
                        value = c.postalCodeAndCity,
                        onValueChange = { postalCodeAndCity ->
                            contact = (c.copy(postalCodeAndCity = postalCodeAndCity))
                        },
                        onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                        label = { Text(stringResource(R.string.edit_shipping_contact_postal_code_and_city)) },
                        isError = codeAndCityError,
                        errorText = { Text(stringResource(R.string.edit_shipping_contact_error_postal_code_and_city)) }
                    )
                }
                item(key = "InputField_7") {
                    InputField(
                        modifier = Modifier.scrollOnFocus(8, listState).fillParentMaxWidth(),
                        value = c.deliveryInformation,
                        onValueChange = { deliveryInformation ->
                            contact = (c.copy(deliveryInformation = deliveryInformation))
                        },
                        onSubmit = { focusManager.clearFocus() },
                        label = { Text(stringResource(R.string.edit_shipping_contact_delivery_information)) },
                        isError = false,
                    )
                }
            }
        }
    }
}

fun isMailValid(mail: String?): Boolean {
    return if (mail.isNullOrBlank()) {
        true
    } else {
        mail.contains('@') && mail.contains('.')
    }
}

fun isPhoneValid(telephoneNumber: String?): Boolean {
    return telephoneNumber != null && telephoneNumber.length >= 3
}

private fun Modifier.scrollOnFocus(to: Int, listState: LazyListState) = composed {
    val coroutineScope = rememberCoroutineScope()

    onFocusEvent {
        if (it.hasFocus) {
            coroutineScope.launch {
                listState.animateScrollToItem(to)
            }
        }
    }
}
