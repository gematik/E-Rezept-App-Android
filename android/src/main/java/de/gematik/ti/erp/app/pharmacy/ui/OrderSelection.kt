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
import androidx.compose.runtime.rememberCoroutineScope
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
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.shortToast
import kotlinx.coroutines.launch

private const val MAX_OPTIONS = 3

@Composable
internal fun OrderSelection(
    pharmacy: PharmacyUseCaseData.Pharmacy,
    orderState: PharmacyOrderState,
    onOrderClicked: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val scope = rememberCoroutineScope()
    var directRedeemEnabled by remember {
        mutableStateOf(false)
    }
    val directRedeemUrlsNotPresent = (
        pharmacy.contacts.pickUpUrl.isEmpty() &&
            pharmacy.contacts.deliveryUrl.isEmpty() &&
            pharmacy.contacts.onlineServiceUrl.isEmpty()
        )

    LaunchedEffect(Unit) {
        scope.launch {
            directRedeemEnabled = orderState.profile.lastAuthenticated == null
        }
    }
    val directPickUpServiceAvailable =
        directRedeemEnabled && pharmacy.contacts.pickUpUrl.isNotEmpty()
    val pickUpServiceVisible =
        pharmacy.contacts.pickUpUrl.isNotEmpty() || directRedeemUrlsNotPresent
    val pickupServiceEnabled = directPickUpServiceAvailable ||
        !directRedeemEnabled && pharmacy.pickupServiceAvailable()

    val directDeliveryServiceAvailable =
        directRedeemEnabled && pharmacy.contacts.deliveryUrl.isNotEmpty()
    val deliveryServiceVisible = directDeliveryServiceAvailable ||
        pharmacy.contacts.deliveryUrl.isNotEmpty() ||
        (directRedeemUrlsNotPresent && pharmacy.deliveryServiceAvailable())
    val deliveryServiceEnabled = directDeliveryServiceAvailable ||
        !directRedeemEnabled && pharmacy.deliveryServiceAvailable()

    val directOnlineServiceAvailable =
        directRedeemEnabled && pharmacy.contacts.onlineServiceUrl.isNotEmpty()
    val mailDeliveryVisible = directOnlineServiceAvailable ||
        pharmacy.contacts.onlineServiceUrl.isNotEmpty() ||
        (directRedeemUrlsNotPresent && pharmacy.onlineServiceAvailable())
    val onlineServiceEnabled = directOnlineServiceAvailable ||
        !directRedeemEnabled && pharmacy.onlineServiceAvailable()

    val nrOfServices = remember(pickUpServiceVisible, deliveryServiceVisible, mailDeliveryVisible) {
        listOf(pickUpServiceVisible, deliveryServiceVisible, mailDeliveryVisible).count { it }
    }
    val isSingle = nrOfServices == 1
    val isLarge = nrOfServices != MAX_OPTIONS

    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        val orderModifier = Modifier
            .weight(weight = 0.5f)
            .fillMaxHeight()
        if (pickUpServiceVisible) {
            OrderButton(
                modifier = orderModifier.testTag(TestTag.PharmacySearch.OrderOptions.PickUpOptionButton),
                isServiceEnabled = pickupServiceEnabled,
                onClick = {
                    onOrderClicked(
                        pharmacy,
                        PharmacyScreenData.OrderOption.PickupService
                    )
                },
                isLarge = isLarge,
                text = stringResource(R.string.pharmacy_order_opt_collect),
                image = painterResource(R.drawable.pharmacy_small)
            )
        }

        if (deliveryServiceVisible) {
            OrderButton(
                modifier = orderModifier.testTag(TestTag.PharmacySearch.OrderOptions.CourierDeliveryOptionButton),
                isServiceEnabled = deliveryServiceEnabled,
                onClick = {
                    onOrderClicked(
                        pharmacy,
                        PharmacyScreenData.OrderOption.CourierDelivery
                    )
                },
                isLarge = isLarge,
                text = stringResource(R.string.pharmacy_order_opt_delivery),
                image = painterResource(R.drawable.delivery_car_small)
            )
        }
        if (mailDeliveryVisible) {
            OrderButton(
                modifier = orderModifier
                    .testTag(TestTag.PharmacySearch.OrderOptions.MailDeliveryOptionButton),
                isServiceEnabled = onlineServiceEnabled,
                onClick = { onOrderClicked(pharmacy, PharmacyScreenData.OrderOption.MailDelivery) },
                isLarge = isLarge,
                text = stringResource(R.string.pharmacy_order_opt_mail),
                image = painterResource(R.drawable.truck_small)
            )
        }

        if (isSingle) {
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
