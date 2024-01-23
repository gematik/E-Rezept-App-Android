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

import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy

object PharmacyOrderExtensions {
    internal fun Pharmacy.isPickupWithoutContactUrls(hasNoContactUrls: Boolean) =
        hasNoContactUrls && isPickupService
    internal fun Pharmacy.isDeliveryWithoutContactUrls(hasNoContactUrls: Boolean) =
        hasNoContactUrls && isDeliveryService
    internal fun Pharmacy.isOnlineServiceWithoutContactUrls(hasNoContactUrls: Boolean) =
        hasNoContactUrls && isOnlineService
    internal fun Pharmacy.pickupUrlEmpty() = contacts.pickUpUrl.isEmpty()
    internal fun Pharmacy.pickupUrlNotEmpty() = contacts.pickUpUrl.isNotEmpty()
    internal fun Pharmacy.deliveryUrlEmpty() = contacts.deliveryUrl.isEmpty()
    internal fun Pharmacy.deliveryUrlNotEmpty() = contacts.deliveryUrl.isNotEmpty()
    internal fun Pharmacy.onlineUrlEmpty() = contacts.onlineServiceUrl.isEmpty()
    internal fun Pharmacy.onlineUrlNotEmpty() = contacts.onlineServiceUrl.isNotEmpty()
}
