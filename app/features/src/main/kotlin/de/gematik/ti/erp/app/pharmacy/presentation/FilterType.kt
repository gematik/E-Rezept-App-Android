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

package de.gematik.ti.erp.app.pharmacy.presentation

import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.serialization.Serializable

@Serializable
enum class FilterType {
    NEARBY,
    OPEN_NOW,
    DELIVERY_SERVICE,
    ONLINE_SERVICE
    ;

    companion object {
        fun FilterType.getUpdatedFilter(filter: PharmacyUseCaseData.Filter) =
            when (this) {
                NEARBY -> filter.copy(nearBy = !filter.nearBy)
                OPEN_NOW -> filter.copy(openNow = !filter.openNow)
                DELIVERY_SERVICE -> filter.copy(deliveryService = !filter.deliveryService)
                ONLINE_SERVICE -> filter.copy(onlineService = !filter.onlineService)
            }
    }
}
