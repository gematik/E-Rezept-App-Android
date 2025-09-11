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
    return PharmacyOrderServiceState(
        pickup = PharmacyServiceState(
            visible = isPickupService,
            enabled = isPickupService
        ),
        delivery = PharmacyServiceState(
            visible = isDeliveryService,
            enabled = isDeliveryService
        ),
        online = PharmacyServiceState(
            visible = isOnlineService,
            enabled = isOnlineService
        )
    )
}
