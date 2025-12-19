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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.messages.mappers.EuOrderToMessagesMapper
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant

/**
 * Use case for retrieving and observing EU prescription order messages within a specified time window.
 *
 * This use case:
 * - Observes changes to an EU order from the repository
 * - Filters events that fall within the specified thread time window (threadStart to threadEnd)
 * - Maps the filtered events to UI models with prescription names
 * - Returns an empty list if the order is not found or if no events exist in the time window
 * - Handles errors gracefully by returning an empty flow
 *
 * @param euRepository Repository for accessing EU prescription order data
 * @param prescriptionRepository Repository for accessing prescription task data to retrieve medication names
 * @param mapper Mapper to convert EU order events into UI models
 * @param dispatcher Coroutine dispatcher for background operations, defaults to IO dispatcher
 */
class GetEuOrderMessagesUseCase(
    private val euRepository: EuRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val mapper: EuOrderToMessagesMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Retrieves EU order messages for a specific order within a defined time window.
     *
     * This function:
     * 1. Observes the EU order by orderId from the repository
     * 2. Filters order events that fall within the threadStart and threadEnd time range
     * 3. For each filtered event, fetches the associated prescription names from the prescription repository
     * 4. Maps the filtered events and prescription names to [EuOrderMessageUiModel] list
     * 5. Returns a continuous flow that emits updates whenever the order or related data changes
     *
     * @param orderId The unique identifier of the EU prescription order to retrieve messages for
     * @param threadStart The start timestamp of the message thread window (inclusive)
     * @param threadEnd The end timestamp of the message thread window (inclusive)
     * @return A flow emitting a list of [EuOrderMessageUiModel], or an empty list if:
     *         - The order is not found
     *         - No events exist within the specified time window
     *         - An error occurs during processing
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        orderId: String,
        threadStart: Instant,
        threadEnd: Instant
    ): Flow<List<EuOrderMessageUiModel>> =
        try {
            euRepository.observeEuOrder(orderId)
                .mapLatest { order ->
                    if (order == null) return@mapLatest emptyList()

                    // Filter events inside the thread window
                    val threadEvents = order.events.filter { event ->
                        event.createdAt in threadStart..threadEnd
                    }

                    if (threadEvents.isEmpty()) return@mapLatest emptyList()

                    Napier.d(tag = "eu-order", message = "Found $threadEvents events in the thread window")

                    // Map only the requested thread’s events
                    val orderMessages = mapper.map(
                        order = order,
                        threadEvents = threadEvents
                    ) { taskIds ->
                        taskIds.map { taskId ->
                            when (val task = prescriptionRepository.getTask(taskId)) {
                                is SyncedTaskData.SyncedTask -> task.medicationName() ?: taskId
                                is ScannedTaskData.ScannedTask -> task.name
                                else -> taskId
                            }
                        }
                    }

                    Napier.d(tag = "eu-order", message = "Found $orderMessages messages in the thread window")
                    orderMessages
                }
                .flowOn(dispatcher)
        } catch (e: Exception) {
            Napier.e("Error on getting order messages", e)
            flowOf(emptyList())
        }
}
