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

package de.gematik.ti.erp.app.orders.usecase

import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetOrdersUsingProfileIdUseCase(
    private val repository: CommunicationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(profileIdentifier: ProfileIdentifier): Flow<List<OrderUseCaseData.Order>> =
        combine(
            repository.loadFirstDispReqCommunications(profileIdentifier),
            repository.loadPharmacies()
        ) { communications, pharmacies ->
            communications.map { communication ->
                communication.dispenseRequestCommunicationToOrder(
                    communicationRepository = repository,
                    withMedicationNames = false,
                    pharmacyName = pharmacies.find { it.telematikId == communication.recipient }?.name
                )
            }
        }.flowOn(dispatcher)
}
