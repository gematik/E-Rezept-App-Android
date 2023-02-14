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

package de.gematik.ti.erp.app.pharmacy.ui.model

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.utils.compose.InputField

fun LazyListScope.phoneNumberInputField(
    listState: LazyListState,
    value: String,
    telephoneOptional: Boolean,
    isError: Boolean,
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
                    if (telephoneOptional) {
                        stringResource(R.string.edit_shipping_contact_phone_optional)
                    } else {
                        stringResource(R.string.edit_shipping_contact_phone)
                    }
                )
            },
            isError = isError,
            errorText = { Text(stringResource(R.string.edit_shipping_contact_error_phone)) },
            keyBoardType = KeyboardType.Phone
        )
    }
}

fun LazyListScope.mailInputField(
    listState: LazyListState,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    isError: Boolean
) {
    item(key = "InputField_2") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(2, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_mail)) },
            isError = isError,
            keyBoardType = KeyboardType.Email
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.nameInputField(
    listState: LazyListState,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    isError: Boolean
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
            isError = isError,
            errorText = { Text(stringResource(R.string.edit_shipping_contact_error_name)) }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.streetAndNumberInputField(
    listState: LazyListState,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    isError: Boolean
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
            isError = isError,
            errorText = { Text(stringResource(R.string.edit_shipping_contact_error_line1)) }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.addressSupplementInputField(
    listState: LazyListState,
    value: String,
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
            isError = false
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.postalCodeAndCityInputField(
    listState: LazyListState,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    isError: Boolean
) {
    item(key = "InputField_6") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(7, listState)
                .fillParentMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_postal_code_and_city)) },
            isError = isError,
            errorText = { Text(stringResource(R.string.edit_shipping_contact_error_postal_code_and_city)) }
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.deliveryInformationInputField(
    listState: LazyListState,
    value: String,
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
            label = { Text(stringResource(R.string.edit_shipping_contact_delivery_information)) },
            isError = false
        )
    }
}
