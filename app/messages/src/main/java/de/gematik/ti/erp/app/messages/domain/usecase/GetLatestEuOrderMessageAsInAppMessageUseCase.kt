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

import android.content.Context
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.EuTaskEvent
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.messages.mappers.EuOrderToMessagesMapper
import de.gematik.ti.erp.app.messages.mappers.toInAppMessage
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.utils.plusDuration
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.time.Duration.Companion.days

class GetLatestEuOrderMessageAsInAppMessageUseCase(
    private val context: Context,
    private val euRepository: EuRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val mapper: EuOrderToMessagesMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val MAX_GAP_IN_DAYS_BETWEEN_EVENTS_FOR_SAME_THREAD = 7L
        private const val SECONDS_IN_ONE_DAY = 86_400L
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<InAppMessage>> =
        euRepository.observeAllEuOrders()
            .mapLatest { orders ->
                if (orders.isEmpty()) return@mapLatest emptyList()

                val threads = orders
                    .flatMap { order ->
                        // 1 order → many threads
                        val threads: List<List<EuTaskEvent>> = order.splitIntoThreads()

                        val hasUnread = threads.any { thread ->
                            thread.any { event -> event.isUnread }
                        }
                        // Convert each thread into 1 InAppMessage
                        threads.mapNotNull { threadEvents ->
                            val threadStart = threadEvents.lastOrNull()?.createdAt
                            val threadEnd = threadStart?.plusDuration(MAX_GAP_IN_DAYS_BETWEEN_EVENTS_FOR_SAME_THREAD.days)
                            val uiModels = mapper.map(
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
                            val latest = uiModels.maxByOrNull { it.timestamp ?: Instant.DISTANT_PAST }
                            val title = context.getString(R.string.eu_messages_list_latest_title)
                            latest?.toInAppMessage(title, threadStart, threadEnd, hasUnread)
                        }
                    }
                    .sortedByDescending { it.timeState.timestamp }
                    .distinctBy { it.id }

                Napier.d(tag = "eu-order", message = "Number of threads for the given orders ${threads.size}")
                threads
            }
            .flowOn(dispatcher)

    private fun EuOrder.splitIntoThreads(): List<List<EuTaskEvent>> {
        if (events.isEmpty()) return emptyList()

        val sorted = events.sortedByDescending { it.createdAt }

        return sorted.fold(mutableListOf<MutableList<EuTaskEvent>>()) { groups, event ->
            val lastGroup = groups.lastOrNull()

            if (
                lastGroup == null ||
                daysBetween(lastGroup.last().createdAt, event.createdAt) > MAX_GAP_IN_DAYS_BETWEEN_EVENTS_FOR_SAME_THREAD
            ) {
                // Start new thread
                groups += mutableListOf(event)
            } else {
                // Same thread
                lastGroup += event
            }

            groups
        }
    }

    private fun daysBetween(a: Instant, b: Instant): Long =
        abs(a.epochSeconds - b.epochSeconds) / SECONDS_IN_ONE_DAY
}
