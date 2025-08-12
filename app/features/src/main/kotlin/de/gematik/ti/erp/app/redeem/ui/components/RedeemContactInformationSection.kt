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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.ShippingContact
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.redeem.model.RedeemContactValidationState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview

@Composable
internal fun RedeemContactInformationSection(
    contact: ShippingContact,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    state: RedeemContactValidationState? = RedeemContactValidationState.NoError
) {
    val isError = state?.isValid() == false || contact.isEmpty()
    Row {
        ContactDetails(
            contact = contact,
            selectedOrderOption = selectedOrderOption,
            isError = isError
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Box(
            modifier = Modifier.align(Alignment.CenterVertically),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                tint = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral600,
                contentDescription = null
            )
        }
    }
}

@Composable
internal fun RedeemContactMissingSection(isError: Boolean) {
    Row {
        Text(
            text = stringResource(R.string.pharmacy_order_no_contacts),
            style = AppTheme.typography.subtitle1,
            color = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral600
        )
        Spacer(modifier = Modifier.weight(0.5f))
        Box(
            modifier = Modifier.align(Alignment.CenterVertically),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                tint = if (isError) AppTheme.colors.red700 else AppTheme.colors.neutral600,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ContactDetails(
    modifier: Modifier = Modifier,
    contact: ShippingContact,
    selectedOrderOption: PharmacyScreenData.OrderOption?,
    isError: Boolean = false
) {
    val textColor = if (isError) AppTheme.colors.red600 else AppTheme.colors.neutral900
    val title = when (selectedOrderOption) {
        null -> ""
        PharmacyScreenData.OrderOption.Pickup -> stringResource(R.string.pharmacy_order_contact_ordertype_pickup_subtitle)
        else -> stringResource(R.string.pharmacy_order_contact_ordertype_others_subtitle)
    }

    Column(modifier) {
        // Title
        if (title != null) {
            Text(
                text = title,
                style = AppTheme.typography.caption1,
                color = if (isError) AppTheme.colors.red600 else AppTheme.colors.neutral600
            )
        }
        // Name
        if (contact.name.isNotBlank()) {
            Text(
                text = contact.name,
                style = AppTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }

        // Address lines
        contact.address().forEach {
            Text(
                text = it,
                style = AppTheme.typography.body2l,
                color = textColor
            )
        }

        // Mail chip
        if (contact.mail.isNotBlank()) {
            SpacerSmall()
            ContactInformationChip(
                icon = Icons.Outlined.Mail,
                text = contact.mail,
                color = AppTheme.colors.primary100,
                contentColor = AppTheme.colors.primary900
            )
        }
        // Phone chip
        if (contact.telephoneNumber.isNotBlank()) {
            SpacerSmall()
            ContactInformationChip(
                icon = Icons.Outlined.Phone,
                text = contact.telephoneNumber,
                color = AppTheme.colors.primary100,
                contentColor = AppTheme.colors.primary900
            )
        }

        // Delivery info
        if (contact.deliveryInformation.isNotBlank()) {
            SpacerSmall()
            Text(
                text = contact.deliveryInformation,
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ShippingInformationSectionWithErrorPreview() {
    PreviewTheme {
        RedeemContactInformationSection(
            selectedOrderOption = PharmacyScreenData.OrderOption.Pickup,
            state = RedeemContactValidationState.MissingPhone,
            contact = ShippingContact(
                name = "Ubelix Ewiglangername",
                line1 = "Kantstraße 149",
                line2 = "",
                postalCode = "12099",
                city = "Berlin",
                telephoneNumber = "01653 387123199",
                mail = "mailadresse@provider.de",
                deliveryInformation = "Bitte im Vordherhaus abgeben."
            )
        )
    }
}

@LightDarkPreview
@Composable
private fun ShippingInformationSectionPreview() {
    PreviewTheme {
        RedeemContactInformationSection(
            selectedOrderOption = PharmacyScreenData.OrderOption.Delivery,
            state = RedeemContactValidationState.NoError,
            contact = ShippingContact(
                name = "Ubelix Ewiglangername",
                line1 = "Kantstraße 149",
                line2 = "",
                postalCode = "12099",
                city = "Berlin",
                telephoneNumber = "01653 387123199",
                mail = "mailadresse@provider.de",
                deliveryInformation = "Bitte im Vordherhaus abgeben."
            )
        )
    }
}

@LightDarkPreview
@Composable
private fun RedeemContactMissingSectionPreview() {
    PreviewTheme {
        Column {
            RedeemContactMissingSection(isError = true)
            RedeemContactMissingSection(isError = false)
        }
    }
}
