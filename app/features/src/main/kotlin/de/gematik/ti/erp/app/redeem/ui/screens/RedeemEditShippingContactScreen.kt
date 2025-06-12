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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.max
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.onReturnAction
import de.gematik.ti.erp.app.pharmacy.ui.components.addressSupplementInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.cityInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.deliveryInformationInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.mailInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.nameInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.phoneNumberInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.postalCodeInputField
import de.gematik.ti.erp.app.pharmacy.ui.components.streetAndNumberInputField
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
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.navigation.RedeemRouteBackStackEntryArguments
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemEditShippingPreviewParameter
import de.gematik.ti.erp.app.redeem.ui.preview.ShippingContactPreviewData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.letNotNull

data class ValidationResult(
    val isEmpty: Boolean,
    val isInvalid: Boolean
)

@Requirement(
    "O.Purp_2#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Contact information is collected when needed for redeeming."
)
class RedeemEditShippingContactScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnlineRedeemGraphController
) : Screen() {

    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val showDialogEvent = ComposableEvent<Unit>()

        val activeProfile by graphController.activeProfile.collectAsStateWithLifecycle()
        var isDirectRedeemEnabled = remember(activeProfile) { false }

        CheckAddressChangeDialog(
            event = showDialogEvent,
            dialogScaffold = LocalDialog.current,
            onBack = { navController.popBackStack() }
        )

        navBackStackEntry.onReturnAction(
            RedeemRoutes.RedeemEditShippingContactScreen
        ) {
            isDirectRedeemEnabled = activeProfile.data?.isDirectRedeemEnabled ?: false
        }

        RedeemRouteBackStackEntryArguments(navBackStackEntry)
            .getOrderOption()?.let { selectedOrderOption ->

                val orderState by graphController.selectedOrderState

                var contact by remember(orderState.contact) { mutableStateOf(orderState.contact) }

                val shippingContactState = remember(orderState, contact) {
                    graphController.validateAndGetShippingContactState(contact, selectedOrderOption)
                }

                letNotNull(
                    shippingContactState,
                    contact
                ) { state, notNullContact ->
                    RedeemEditShippingContactScreenContent(
                        state = state,
                        notNullContact = notNullContact,
                        isDirectRedeemEnabled = isDirectRedeemEnabled,
                        listState = listState,
                        onContactChange = { contact = it },
                        onSave = {
                            graphController.saveShippingContact(contact)
                            navController.popBackStack()
                        },
                        onShowDialog = { showDialogEvent.trigger() }
                    )
                }
            }
    }
}

@Composable
fun RedeemEditShippingContactScreenContent(
    state: ShippingContactState,
    notNullContact: PharmacyUseCaseData.ShippingContact,
    isDirectRedeemEnabled: Boolean,
    listState: LazyListState,
    onContactChange: (PharmacyUseCaseData.ShippingContact) -> Unit,
    onSave: () -> Unit,
    onShowDialog: () -> Unit
) {
    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        bottomBar = {
            @Requirement(
                "O.Data_6#7",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Contact information is collected when needed for redeeming."
            )
            SaveContactInformationBottomBar(
                enabled = state.isValid(),
                onClick = onSave
            )
        },
        topBarTitle = stringResource(R.string.edit_shipping_contact_top_bar_title),
        listState = listState,
        onBack = {
            if (state.isValid()) {
                onSave()
            } else {
                onShowDialog()
            }
        }
    ) { padding ->

        val imePadding = WindowInsets.ime.asPaddingValues()
        val focusManager = LocalFocusManager.current

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            contentPadding = PaddingValues(
                top = PaddingDefaults.Medium + padding.calculateTopPadding(),
                bottom = PaddingDefaults.Medium + max(
                    imePadding.calculateBottomPadding(),
                    padding.calculateBottomPadding()
                ),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        ) {
            item { ContactNumberHeader() }
            phoneNumberInputField(
                listState = listState,
                value = notNullContact.telephoneNumber,
                validationResult = ValidationResult(
                    isEmpty = state.isEmptyPhoneNumber(),
                    isInvalid = state.isInvalidPhoneNumber()
                ),
                onValueChange = { phone ->
                    onContactChange(notNullContact.copy(telephoneNumber = phone.trim()))
                },
                onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
            )
            // we sent the mail currently only on direct redeem
            if (isDirectRedeemEnabled) {
                mailInputField(
                    listState = listState,
                    validationResult = ValidationResult(
                        isEmpty = state.isEmptyMail(),
                        isInvalid = state.isInvalidMail()
                    ),
                    value = notNullContact.mail,
                    onValueChange = { mail ->
                        onContactChange(notNullContact.copy(mail = mail.trim()))
                    },
                    onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
                )
            }

            item { DeliveryAddressHeader() }

            nameInputField(
                listState = listState,
                value = notNullContact.name,
                onValueChange = { name ->
                    onContactChange(notNullContact.copy(name = name))
                },
                onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                validationResult = ValidationResult(
                    isEmpty = state.isEmptyName(),
                    isInvalid = state.isInvalidName()
                )
            )

            streetAndNumberInputField(
                listState = listState,
                value = notNullContact.line1,
                onValueChange = { line1 ->
                    onContactChange(notNullContact.copy(line1 = line1))
                },
                onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                validationResult = ValidationResult(
                    isEmpty = state.isEmptyLine1(),
                    isInvalid = state.isInvalidLine1()
                )
            )

            addressSupplementInputField(
                listState = listState,
                value = notNullContact.line2,
                validationResult = ValidationResult(
                    isEmpty = false, // optional,
                    isInvalid = state.isInvalidLine2()
                ),
                onValueChange = { line2 ->
                    onContactChange(notNullContact.copy(line2 = line2))
                },
                onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
            )

            postalCodeInputField(
                listState = listState,
                value = notNullContact.postalCode,
                onValueChange = { postalCode ->
                    onContactChange(notNullContact.copy(postalCode = postalCode))
                },
                validationResult = ValidationResult(
                    isEmpty = state.isEmptyPostalCode(),
                    isInvalid = state.isInvalidPostalCode()
                ),
                onSubmit = { focusManager.moveFocus(FocusDirection.Down) }
            )

            cityInputField(
                listState = listState,
                value = notNullContact.city,
                onValueChange = { city ->
                    onContactChange(notNullContact.copy(city = city))
                },
                onSubmit = { focusManager.moveFocus(FocusDirection.Down) },
                validationResult = ValidationResult(
                    isEmpty = state.isEmptyCity(),
                    isInvalid = state.isInvalidCity()
                )
            )

            deliveryInformationInputField(
                listState = listState,
                value = notNullContact.deliveryInformation,
                validationResult = ValidationResult(
                    isEmpty = false, // optional,
                    isInvalid = state.isInvalidDeliveryInformation()
                ),
                onValueChange = { deliveryInformation ->
                    onContactChange(notNullContact.copy(deliveryInformation = deliveryInformation))
                },
                onSubmit = { focusManager.clearFocus() }
            )
        }
    }
}

@Composable
fun SaveContactInformationBottomBar(enabled: Boolean, onClick: () -> Unit) {
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
private fun DeliveryAddressHeader() {
    SpacerLarge()
    Text(
        stringResource(R.string.edit_shipping_contact_title_address),
        style = AppTheme.typography.h6
    )
}

@Composable
fun CheckAddressChangeDialog(
    event: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onBack: () -> Unit
) {
    event.listen {
        dialogScaffold.show {
            ErezeptAlertDialog(
                title = stringResource(R.string.edit_contact_back_alert_header),
                bodyText = stringResource(R.string.edit_contact_back_alert_information),
                dismissText = stringResource(R.string.edit_contact_back_alert_change),
                confirmText = stringResource(R.string.edit_contact_back_alert_action),
                onDismissRequest = {
                    it.dismiss()
                },
                onConfirmRequest = {
                    it.dismiss()
                    onBack()
                }
            )
        }
    }
}

@Composable
fun ContactNumberHeader() {
    Text(
        stringResource(R.string.edit_shipping_contact_title_contact),
        style = AppTheme.typography.h6
    )
}

@LightDarkPreview
@Composable
fun RedeemEditShippingScreenPreview(
    @PreviewParameter(RedeemEditShippingPreviewParameter::class) shippingContactPreviewData: ShippingContactPreviewData
) {
    PreviewAppTheme {
        RedeemEditShippingContactScreenContent(
            state = when {
                shippingContactPreviewData.invalidShippingContactState != null -> {
                    ShippingContactState.InvalidShippingContactState(
                        errorList = shippingContactPreviewData.invalidShippingContactState.errorList
                    )
                }
                shippingContactPreviewData.errorShippingContactState != null -> {
                    ShippingContactState.InvalidShippingContactState(
                        errorList = shippingContactPreviewData.errorShippingContactState.errorList
                    )
                }
                else -> ShippingContactState.ValidShippingContactState.OK
            },
            notNullContact = shippingContactPreviewData.shippingContact,
            isDirectRedeemEnabled = true,
            listState = rememberLazyListState(),
            onContactChange = {},
            onSave = {},
            onShowDialog = {}
        )
    }
}
