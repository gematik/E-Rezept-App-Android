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

package de.gematik.ti.erp.app.pharmacy.ui.model

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.ui.ValidationResult
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.utils.compose.InputField

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
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = {
                Text(
                    if (!validationResult.isEmpty) {
                        stringResource(R.string.edit_shipping_contact_phone_optional)
                    } else {
                        stringResource(R.string.edit_shipping_contact_phone)
                    }
                )
            },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                if (validationResult.isEmpty) {
                    Text(stringResource(R.string.edit_shipping_contact_empty_phone))
                } else {
                    Text(stringResource(R.string.edit_shipping_contact_invalid_phone))
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
                if (validationResult.isEmpty) {
                    Text(stringResource(R.string.edit_shipping_contact_empty_mail))
                } else {
                    Text(stringResource(R.string.edit_shipping_contact_invalid_mail))
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
                .scrollOnFocus(4, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_name)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                if (validationResult.isEmpty) {
                    Text(stringResource(R.string.edit_shipping_contact_empty_name))
                } else {
                    Text(stringResource(R.string.edit_shipping_contact_invalid_name))
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
                .scrollOnFocus(5, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_title_line1)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                if (validationResult.isEmpty) {
                    Text(stringResource(R.string.edit_shipping_contact_empty_line1))
                } else {
                    Text(stringResource(R.string.edit_shipping_contact_invalid_line1))
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
                .scrollOnFocus(6, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_line2)) },
            isError = validationResult.isInvalid,
            errorText = {
                Text(stringResource(R.string.edit_shipping_contact_invalid_line2))
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
                .scrollOnFocus(7, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_postal_code)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                if (validationResult.isEmpty) {
                    Text(stringResource(R.string.edit_shipping_contact_empty_postal_code))
                } else {
                    Text(stringResource(R.string.edit_shipping_contact_invalid_postal_code))
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
                .scrollOnFocus(8, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_city)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                if (validationResult.isEmpty) {
                    Text(stringResource(R.string.edit_shipping_contact_empty_city))
                } else {
                    Text(stringResource(R.string.edit_shipping_contact_invalid_city))
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
                .scrollOnFocus(9, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_delivery_information)) },
            isError = validationResult.isInvalid,
            errorText = {
                Text(stringResource(R.string.edit_shipping_contact_invalid_delivery_information))
            }
        )
    }
}
