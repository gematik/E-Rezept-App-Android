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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToFlag
import de.gematik.ti.erp.app.eurezept.mapper.countryCodeToName
import de.gematik.ti.erp.app.redeem.ui.screens.ValidationResult
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus

fun LazyListScope.phoneNumberInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    selectedCallingCode: CountryCallingCode = CountryCallingCode("DE", countryCodeToName("DE"), 49, "🇩🇪"),
    onCallingCodeClick: () -> Unit = {},
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_1") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(1, listState)
                .fillParentMaxWidth()
                .semantics {
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
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_phone),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_phone),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            },
            keyBoardType = KeyboardType.Phone,
            leadingIcon = {
                Row(
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onCallingCodeClick)
                        .padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedCallingCode.flagEmoji,
                        style = AppTheme.typography.body1
                    )
                    Text(
                        text = selectedCallingCode.displayCallingCode,
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.neutral900,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = AppTheme.colors.neutral600
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .height(24.dp)
                            .width(1.dp)
                            .background(AppTheme.colors.neutral400)
                    )
                }
            }
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
                .fillParentMaxWidth()
                .semantics {
                    contentType = ContentType.EmailAddress
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = {
                Text(
                    stringResource(R.string.edit_shipping_contact_mail)
                )
            },
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_mail),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_mail),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    else -> null
                }
            },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            keyBoardType = KeyboardType.Email
        )
    }
}

@Suppress("MagicNumber")
fun LazyListScope.firstNameInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_FirstName") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(3, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.PersonFirstName
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_first_name)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_first_name),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_first_name),
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
fun LazyListScope.lastNameInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    item(key = "InputField_LastName") {
        InputField(
            modifier = Modifier
                .scrollOnFocus(4, listState)
                .fillParentMaxWidth().semantics() {
                    contentType = ContentType.PersonLastName
                },
            value = value,
            onValueChange = onValueChange,
            onSubmit = onSubmit,
            label = { Text(stringResource(R.string.edit_shipping_contact_last_name)) },
            isError = validationResult.isEmpty || validationResult.isInvalid,
            errorText = {
                when {
                    validationResult.isEmpty -> Text(
                        stringResource(R.string.edit_shipping_contact_empty_last_name),
                        color = AppTheme.colors.red700,
                        style = AppTheme.typography.caption1
                    )
                    validationResult.isInvalid -> Text(
                        stringResource(R.string.edit_shipping_contact_invalid_last_name),
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
fun LazyListScope.countryInputField(
    listState: LazyListState,
    value: String,
    validationResult: ValidationResult,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    item(key = "InputField_Country") {
        val displayValue = remember(value) {
            if (value.isBlank()) {
                ""
            } else {
                val flag = countryCodeToFlag(value)
                val name = countryCodeToName(value)
                if (flag.isNotBlank() && name != value) {
                    "$flag $name (${value.uppercase()})"
                } else {
                    value.uppercase()
                }
            }
        }
        val isError = validationResult.isEmpty || validationResult.isInvalid

        Box(modifier = Modifier.fillParentMaxWidth()) {
            ErezeptOutlineText(
                modifier = Modifier
                    .scrollOnFocus(9, listState)
                    .fillParentMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics {
                        contentType = ContentType.AddressCountry
                    },
                value = if (onClick != null) displayValue else value,
                onValueChange = { onValueChange(it.uppercase().take(2)) },
                readOnly = onClick != null,
                singleLine = true,
                label = { Text(stringResource(R.string.edit_shipping_contact_country)) },
                isError = isError,
                trailingIcon = if (onClick != null) null else {
                    {
                        if (value.isNotBlank()) {
                            IconButton(onClick = { onValueChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.a11y_deleted_text),
                                    tint = AppTheme.colors.neutral600
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppTheme.colors.neutral600
                            )
                        }
                    }
                },
                supportingText = {
                    if (isError) {
                        when {
                            validationResult.isEmpty -> Text(
                                stringResource(R.string.edit_shipping_contact_empty_country),
                                color = AppTheme.colors.red700,
                                style = AppTheme.typography.caption1
                            )
                            validationResult.isInvalid -> Text(
                                stringResource(R.string.edit_shipping_contact_invalid_country),
                                color = AppTheme.colors.red700,
                                style = AppTheme.typography.caption1
                            )
                        }
                    }
                }
            )
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (value.isNotBlank()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.a11y_deleted_text),
                                tint = AppTheme.colors.neutral600
                            )
                        }
                    } else {
                        Icon(
                            modifier = Modifier.padding(end = 12.dp),
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = AppTheme.colors.neutral600
                        )
                    }
                }
            }
        }
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
