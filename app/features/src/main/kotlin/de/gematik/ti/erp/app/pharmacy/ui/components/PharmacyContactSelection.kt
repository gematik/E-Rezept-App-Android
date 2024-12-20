/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.ui.preview.mockPharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.BigFontDarkPreview
import de.gematik.ti.erp.app.utils.compose.DarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.gotoCoordinates

@Composable
fun PharmacyContactSelection(
    pharmacy: Pharmacy,
    onPhoneClicked: (String) -> Unit,
    onMailClicked: (String) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        PharmacyContactButton(
            modifier = Modifier
                .testTag(TestTag.PharmacySearch.OrderOptions.PickUpOptionButton),
            text = stringResource(R.string.pharmacy_contact_map_two_lines),
            icon = Icons.Outlined.PinDrop,

            onClick = {
                pharmacy.coordinates?.let { context.gotoCoordinates(it) }
            }
        )
        PharmacyContactButton(
            modifier = Modifier
                .testTag(TestTag.PharmacySearch.OrderOptions.CourierDeliveryOptionButton),
            text = stringResource(R.string.pharmacy_contact_phone_two_lines),
            icon = Icons.Outlined.Phone,
            onClick = {
                onPhoneClicked(pharmacy.contacts.phone)
            }
        )
        PharmacyContactButton(
            modifier = Modifier
                .testTag(TestTag.PharmacySearch.OrderOptions.MailDeliveryOptionButton),
            text = stringResource(R.string.pharmacy_contact_email_two_lines),
            icon = Icons.Outlined.MailOutline,
            onClick = {
                onMailClicked(pharmacy.contacts.mail)
            }
        )
    }
}

@Composable
private fun RowScope.PharmacyContactButton(
    modifier: Modifier,
    text: String,
    icon: ImageVector? = null,
    image: Painter? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(PaddingDefaults.Medium)
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxSize(1f)
            .shadow(elevation = SizeDefaults.half, shape = shape)
            .background(AppTheme.colors.neutral025, shape)
            .border(
                width = SizeDefaults.eighth,
                shape = shape,
                color = AppTheme.colors.neutral300
            )
            .clip(shape)
            .clickable(
                role = Role.Button,
                onClick = { onClick() }
            )
            .padding(PaddingDefaults.ShortMedium)

    ) {
        if (icon != null) {
            Icon(
                icon,
                null,
                tint = AppTheme.colors.primary600,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(SizeDefaults.fourfold)
            )
        }
        if (image != null) {
            Image(
                image,
                null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(SizeDefaults.fourfold)
            )
        }
        SpacerTiny()
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.subtitle2
            )
        }
    }
}

@BigFontDarkPreview
@Composable
fun PreviewContactSelectionBigFont() {
    PreviewAppTheme {
        PharmacyContactSelection(
            pharmacy = mockPharmacy,
            onPhoneClicked = {},
            onMailClicked = {}
        )
    }
}

@DarkPreview
@Composable
fun PreviewContactSelection() {
    PreviewAppTheme {
        PharmacyContactSelection(
            pharmacy = mockPharmacy,
            onPhoneClicked = {},
            onMailClicked = {}
        )
    }
}
