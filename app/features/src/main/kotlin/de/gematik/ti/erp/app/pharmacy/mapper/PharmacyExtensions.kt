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

package de.gematik.ti.erp.app.pharmacy.mapper

import de.gematik.ti.erp.app.pharmacy.model.PharmacyOrderServiceState
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceState
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.deliveryUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isDeliveryWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.isOnlineServiceWithoutContactUrls
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.onlineUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderExtensions.pickupUrlNotEmpty
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy

/**
 * Calculates the visibility and availability state for each pharmacy service (pickup, delivery, online).
 *
 * This function aggregates multiple checks regarding contact URLs, supported services, and
 * redemption flow to determine:
 * - whether each service should be visible in the UI
 * - whether it should be interactable (enabled)
 *
 * @receiver [Pharmacy] The pharmacy for which the service states are to be calculated.
 * @return A [PharmacyOrderServiceState] object containing the visibility and enabled state for each service.
 */
internal fun Pharmacy.calculateServiceState(): PharmacyOrderServiceState {
    val directRedeemUrlsNotPresent = this.directRedeemUrlsNotPresent

    val (pickUpContactAvailable, deliveryContactAvailable, onlineContactAvailable) =
        checkRedemptionAndContactAvailabilityForPharmacy()

    val (pickUpServiceVisible, deliveryServiceVisible, onlineServiceVisible) =
        checkServiceVisibility(
            directRedeemUrlsNotPresent = directRedeemUrlsNotPresent,
            deliveryServiceAvailable = deliveryContactAvailable,
            onlineServiceAvailable = onlineContactAvailable
        )

    val (pickupServiceEnabled, deliveryServiceEnabled, onlineServiceEnabled) =
        checkServiceAvailability(
            pickUpContactAvailable = pickUpContactAvailable,
            deliveryContactAvailable = deliveryContactAvailable,
            onlineContactAvailable = onlineContactAvailable
        )

    return PharmacyOrderServiceState(
        pickup = PharmacyServiceState(
            visible = pickUpServiceVisible,
            enabled = pickupServiceEnabled
        ),
        delivery = PharmacyServiceState(
            visible = deliveryServiceVisible,
            enabled = deliveryServiceEnabled
        ),
        online = PharmacyServiceState(
            visible = onlineServiceVisible,
            enabled = onlineServiceEnabled
        )
    )
}

/**
 * Determines if the pharmacy provides contact URLs for each service type.
 *
 * Checks whether the pickup, delivery, and online service contact URLs are available.
 *
 * @receiver [Pharmacy] The pharmacy being checked.
 * @return A [Triple] containing flags for pickup, delivery, and online contact availability (in that order).
 */
private fun Pharmacy.checkRedemptionAndContactAvailabilityForPharmacy(): Triple<Boolean, Boolean, Boolean> {
    val pickUpServiceAvailable = pickupUrlNotEmpty()

    val deliveryServiceAvailable = deliveryUrlNotEmpty()

    val onlineServiceAvailable = onlineUrlNotEmpty()

    return Triple(pickUpServiceAvailable, deliveryServiceAvailable, onlineServiceAvailable)
}

/**
 * Determines whether each pharmacy service should be visible in the UI.
 *
 * A service is considered visible if:
 * - A contact URL is available
 * - Or if fallback conditions are met (e.g., no direct redeem URLs available)
 *
 * @param directRedeemUrlsNotPresent Whether the pharmacy lacks direct redemption URLs.
 * @param deliveryServiceAvailable Whether delivery contact is available.
 * @param onlineServiceAvailable Whether online contact is available.
 * @return A [Triple] indicating UI visibility for pickup, delivery, and online services (in that order).
 */
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

/**
 * Determines whether each pharmacy service should be enabled (interactable).
 *
 * A service is considered enabled if:
 * - It has a contact URL
 * - Or it is supported by the pharmacy or a matching contact method is available
 *
 * @param pickUpContactAvailable Whether a pickup contact is available.
 * @param deliveryContactAvailable Whether a delivery contact is available.
 * @param onlineContactAvailable Whether an online contact is available.
 * @return A [Triple] indicating enablement for pickup, delivery, and online services (in that order).
 */
private fun Pharmacy.checkServiceAvailability(
    pickUpContactAvailable: Boolean,
    deliveryContactAvailable: Boolean,
    onlineContactAvailable: Boolean
): Triple<Boolean, Boolean, Boolean> {
    val pickupServiceEnabled = pickupUrlNotEmpty() || pickUpContactAvailable || isPickupService
    val deliveryServiceEnabled = deliveryUrlNotEmpty() || deliveryContactAvailable || isDeliveryService
    val onlineServiceEnabled = onlineUrlNotEmpty() || onlineContactAvailable || isOnlineService

    return Triple(pickupServiceEnabled, deliveryServiceEnabled, onlineServiceEnabled)
}
