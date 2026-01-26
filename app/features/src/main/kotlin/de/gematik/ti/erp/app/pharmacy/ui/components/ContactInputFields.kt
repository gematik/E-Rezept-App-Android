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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.redeem.ui.screens.ValidationResult
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus

fun LazyListScope.phoneNumberInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_1") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(1, listState)
                .fillParentMaxWidth()
                .semantics() {
                    contentType = ContentType.PhoneNumber
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = {
                Text(
                    stringResource(R.string.edit_shipping_contact_phone)
                )
            },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(stringResource(R.string.edit_shipping_contact_empty_phone), color = AppTheme.colors.red700)
                    validationResult.isInvalid -> Text(stringResource(R.string.edit_shipping_contact_invalid_phone), color = AppTheme.colors.red700)
                    else -> null
                }
            },
            keyBoardType = KeyboardType.Phone
        )
    }
}

fun LazyListScope.mailInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_2") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(2, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = {
                stringResource(R.string.edit_shipping_contact_mail)
            },
            errorText = {
                when {
                    validationResult.isEmpty -> Text(stringResource(R.string.edit_shipping_contact_empty_mail), color = AppTheme.colors.red700)
                    validationResult.isInvalid -> Text(stringResource(R.string.edit_shipping_contact_invalid_mail), color = AppTheme.colors.red700)
                    else -> null
                }
            },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            keyBoardType = KeyboardType.Email
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.nameInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_3") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(3, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.PersonFullName
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_name)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_name),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_name),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.streetAndNumberInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_4") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(4, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.AddressStreet
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_title_line1)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_line1),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_line1),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.addressSupplementInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_5") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(5, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.AddressAuxiliaryDetails
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_line2)) },
            isError = validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> null
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_line2),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.postalCodeInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_6") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(6, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.PostalAddress
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_postal_code)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_postal_code),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_postal_code),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            },
            keyBoardType = KeyboardType.Number
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.cityInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_7") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(7, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.AddressRegion
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_city)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_city),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_city),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.deliveryInformationInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_8") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(8, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            singleLine = false,
            label = { Text(stringResource(R.string.edit_shipping_contact_delivery_information)) },
            isError = validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> null
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_delivery_information),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            }
        )
    }
}
