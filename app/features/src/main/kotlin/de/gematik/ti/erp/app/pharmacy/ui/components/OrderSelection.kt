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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.deliveryUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isDeliveryWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isOnlineServiceWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.onlineUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.pickupUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.shortToast

private const val DISABLED_ALPHA = 0.3f
private const val ENABLED_ALPHA = 1f

@Requirement(
    "A_24579#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Implementation of the order selection for the pharmacy screen."
)
@Composable
internal fun OrderSelection(
    pharmacy: Pharmacy,
    isDirectRedeemEnabled: Boolean,
    onOrderClicked: (Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val directRedeemUrlsNotPresent = pharmacy.directRedeemUrlsNotPresent

    val (pickUpContactAvailable, deliveryContactAvailable, onlineContactAvailable) =
        pharmacy.checkRedemptionAndContactAvailabilityForPharmacy(isDirectRedeemEnabled)

    val (pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible) =
        pharmacy.checkServiceVisibility(
            directRedeemUrlsNotPresent = directRedeemUrlsNotPresent,
            deliveryServiceAvailable = deliveryContactAvailable,
            onlineServiceAvailable = onlineContactAvailable
        )

    val (pickupServiceEnabled, deliveryServiceEnabled, onlineServiceEnabled) =
        pharmacy.checkServiceAvailability(
            directRedeemEnabled = isDirectRedeemEnabled,
            pickUpContactAvailable = pickUpContactAvailable,
            deliveryContactAvailable = deliveryContactAvailable,
            onlineContactAvailable = onlineContactAvailable
        )

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        if (pickUpServiceVisible) {
            OrderButton(
                modifier = Modifier
                    .testTag(TestTag.PharmacySearch.OrderOptions.PickUpOptionButton),
                isServiceEnabled = pickupServiceEnabled,
                text = stringResource(R.string.pharmacy_order_opt_collect_two_lines),
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
                modifier = Modifier
                    .testTag(TestTag.PharmacySearch.OrderOptions.CourierDeliveryOptionButton),
                isServiceEnabled = deliveryServiceEnabled,
                text = stringResource(R.string.pharmacy_order_opt_delivery_two_lines),
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
                modifier = Modifier
                    .testTag(TestTag.PharmacySearch.OrderOptions.MailDeliveryOptionButton),
                isServiceEnabled = onlineServiceEnabled,
                text = stringResource(R.string.pharmacy_order_opt_mail_two_lines),
                image = painterResource(R.drawable.truck_small),
                onClick = { onOrderClicked(pharmacy, PharmacyScreenData.OrderOption.MailDelivery) }
            )
        }
    }
}

@Composable
private fun RowScope.OrderButton(
    modifier: Modifier,
    isServiceEnabled: Boolean,
    text: String,
    image: Painter,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(PaddingDefaults.Medium)
    val serviceDisabledText = stringResource(R.string.connect_for_pharmacy_service)
    var showToast by remember { mutableStateOf(false) }

    // set the toast to be false on every recomposition
    LaunchedEffect(showToast) {
        showToast = false
    }

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
                    isServiceEnabled -> ENABLED_ALPHA
                    else -> DISABLED_ALPHA
                }
            )
    ) {
        Image(image, null, modifier = Modifier.align(Alignment.CenterHorizontally))
        SpacerTiny()
        Text(
            text,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.subtitle2
        )
    }

    AnimatedVisibility(showToast) {
        shortToast(serviceDisabledText)
    }
}

private fun Pharmacy.checkRedemptionAndContactAvailabilityForPharmacy(
    directRedeemEnabled: Boolean
): Triple<Boolean, Boolean, Boolean> {
    val pickUpServiceAvailable = directRedeemEnabled && pickupUrlNotEmpty()

    val deliveryServiceAvailable = directRedeemEnabled && deliveryUrlNotEmpty()

    val onlineServiceAvailable = directRedeemEnabled && onlineUrlNotEmpty()

    return Triple(pickUpServiceAvailable, deliveryServiceAvailable, onlineServiceAvailable)
}

private fun Pharmacy.checkServiceVisibility(
    directRedeemUrlsNotPresent: Boolean,
    deliveryServiceAvailable: Boolean,
    onlineServiceAvailable: Boolean
): Triple<Boolean, Boolean, Boolean> {
    val pickUpServiceVisible = pickupUrlNotEmpty() || directRedeemUrlsNotPresent

    val deliveryServiceVisible = deliveryServiceAvailable ||
        deliveryUrlNotEmpty() ||
        isDeliveryWithoutContactUrls(directRedeemUrlsNotPresent)

    val onlineServiceVisible = onlineServiceAvailable ||
        onlineUrlNotEmpty() ||
        isOnlineServiceWithoutContactUrls(directRedeemUrlsNotPresent)

    return Triple(pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible)
}

private fun Pharmacy.checkServiceAvailability(
    directRedeemEnabled: Boolean,
    pickUpContactAvailable: Boolean,
    deliveryContactAvailable: Boolean,
    onlineContactAvailable: Boolean
): Triple<Boolean, Boolean, Boolean> {
    val pickupServiceEnabled = pickupUrlNotEmpty() ||
        pickUpContactAvailable ||
        !directRedeemEnabled && isPickupService

    val deliveryServiceEnabled = deliveryUrlNotEmpty() ||
        deliveryContactAvailable ||
        !directRedeemEnabled && isDeliveryService

    val onlineServiceEnabled = onlineUrlNotEmpty() ||
        onlineContactAvailable ||
        !directRedeemEnabled && isOnlineService

    return Triple(pickupServiceEnabled, deliveryServiceEnabled, onlineServiceEnabled)
}
