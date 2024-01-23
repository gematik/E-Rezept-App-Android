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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyOrderController
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.deliveryUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isDeliveryWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isOnlineServiceWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isPickupWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.onlineUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.pickupUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.shortToast

private const val MAX_OPTIONS = 3

@Composable
internal fun OrderSelection(
    pharmacy: Pharmacy,
    pharmacyOrderController: PharmacyOrderController,
    onOrderClicked: (Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val directRedeemEnabled by pharmacyOrderController.isDirectRedeemEnabledState

    val hasNoPickupContact = pharmacy.contacts.pickUpUrl.isEmpty()
    val hasNoDeliveryContact = pharmacy.contacts.pickUpUrl.isEmpty()
    val hasNoOnlineServiceContact = pharmacy.contacts.pickUpUrl.isEmpty()
    val hasNoContacts = listOf(hasNoPickupContact, hasNoDeliveryContact, hasNoOnlineServiceContact)

    val hasNoContactUrls = hasNoContacts.all { it }

    // service availability checks
    val pickUpServiceAvailable = directRedeemEnabled && pharmacy.pickupUrlNotEmpty()
    val deliveryServiceAvailable = directRedeemEnabled && pharmacy.deliveryUrlNotEmpty()
    val onlineServiceAvailable = directRedeemEnabled && pharmacy.onlineUrlNotEmpty()

    // visibility checks
    val pickUpServiceVisible = pickUpServiceAvailable ||
        pharmacy.pickupUrlNotEmpty() ||
        pharmacy.isPickupWithoutContactUrls(hasNoContactUrls)

    val deliveryServiceVisible = deliveryServiceAvailable ||
        pharmacy.deliveryUrlNotEmpty() ||
        pharmacy.isDeliveryWithoutContactUrls(hasNoContactUrls)

    val onlineServiceVisible = onlineServiceAvailable ||
        pharmacy.onlineUrlNotEmpty() ||
        pharmacy.isOnlineServiceWithoutContactUrls(hasNoContactUrls)

    // enabled checks
    val pickupServiceEnabled = pharmacy.pickupUrlNotEmpty() ||
        pickUpServiceAvailable ||
        !directRedeemEnabled && pharmacy.isPickupService

    val deliveryServiceEnabled = pharmacy.deliveryUrlNotEmpty() ||
        deliveryServiceAvailable ||
        !directRedeemEnabled && pharmacy.isDeliveryService

    val onlineServiceEnabled = pharmacy.onlineUrlNotEmpty() ||
        onlineServiceAvailable ||
        !directRedeemEnabled && pharmacy.isOnlineService

    val numberOfServices = remember(pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible) {
        listOf(pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible).count { it }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        val modifier = Modifier.weight(weight = 0.5f).fillMaxHeight()

        if (pickUpServiceVisible) {
            OrderButton(
                modifier = modifier.testTag(TestTag.PharmacySearch.OrderOptions.PickUpOptionButton),
                isServiceEnabled = pickupServiceEnabled,
                isLarge = numberOfServices != MAX_OPTIONS,
                text = stringResource(R.string.pharmacy_order_opt_collect),
                image = painterResource(R.drawable.pharmacy_small),
                onClick = {
                    onOrderClicked(
                        pharmacy,
                        PharmacyScreenData.OrderOption.PickupService
                    )
                }
            )
        }

        if (deliveryServiceVisible) {
            OrderButton(
                modifier = modifier.testTag(TestTag.PharmacySearch.OrderOptions.CourierDeliveryOptionButton),
                isServiceEnabled = deliveryServiceEnabled,
                isLarge = numberOfServices != MAX_OPTIONS,
                text = stringResource(R.string.pharmacy_order_opt_delivery),
                image = painterResource(R.drawable.delivery_car_small),
                onClick = {
                    onOrderClicked(
                        pharmacy,
                        PharmacyScreenData.OrderOption.CourierDelivery
                    )
                }
            )
        }
        if (onlineServiceVisible) {
            OrderButton(
                modifier = modifier.testTag(TestTag.PharmacySearch.OrderOptions.MailDeliveryOptionButton),
                isServiceEnabled = onlineServiceEnabled,
                isLarge = numberOfServices != MAX_OPTIONS,
                text = stringResource(R.string.pharmacy_order_opt_mail),
                image = painterResource(R.drawable.truck_small),
                onClick = { onOrderClicked(pharmacy, PharmacyScreenData.OrderOption.MailDelivery) }
            )
        }

        if (numberOfServices == 1) {
            Spacer(Modifier.weight(weight = 0.5f))
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun OrderButton(
    modifier: Modifier,
    isServiceEnabled: Boolean,
    isLarge: Boolean = true,
    text: String,
    image: Painter,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val serviceDisabledText = stringResource(R.string.connect_for_pharmacy_service)
    var showToast by remember { mutableStateOf(false) }

    // set the toast to be false on every recomposition
    LaunchedEffect(showToast) {
        showToast = false
    }

    Column(
        modifier = modifier
            .background(AppTheme.colors.neutral100, shape)
            .clip(shape)
            .clickable(
                role = Role.Button,
                onClick = {
                    when {
                        isServiceEnabled -> onClick()
                        else -> showToast = true
                    }
                }
            )
            .padding(PaddingDefaults.Medium)
            .alpha(
                when {
                    isServiceEnabled -> 1f
                    else -> 0.3f
                }
            )
    ) {
        val imgModifier = when {
            isLarge -> Modifier.align(Alignment.End)
            else -> Modifier.align(Alignment.CenterHorizontally)
        }
        Image(image, null, modifier = imgModifier)
        SpacerTiny()

        val txtModifier = when {
            isLarge -> Modifier.align(Alignment.Start)
            else -> Modifier.align(Alignment.CenterHorizontally)
        }
        Text(text, modifier = txtModifier, style = AppTheme.typography.subtitle2)
    }

    AnimatedVisibility(showToast) {
        shortToast(serviceDisabledText)
    }
}
