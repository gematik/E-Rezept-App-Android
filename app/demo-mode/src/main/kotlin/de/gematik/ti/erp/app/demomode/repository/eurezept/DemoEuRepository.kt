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

package de.gematik.ti.erp.app.demomode.repository.eurezept

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.extensions.demo
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.EuTaskEvent
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.fhir.FhirCountryErpModel
import de.gematik.ti.erp.app.fhir.FhirCountryErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirErpModel
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants.FhirEuRedeemAccessCodeRequestMeta
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirTaskEuPatchInputModelConstants
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class DemoEuRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EuRepository {

    override suspend fun fetchAvailableCountries(): Result<FhirErpModel?> {
        val countries = dataSource.euCountries.first()
        val fhirCountries = countries.map { country ->
            FhirCountryErpModel(
                name = country.name,
                code = country.code
            )
        }
        val fhirModel = FhirCountryErpModelCollection(countries = fhirCountries)
        delay(250)
        return Result.success(fhirModel)
    }

    override fun observeEuOrder(orderId: String): Flow<EuOrder?> = dataSource.euOrders.mapNotNull {
        val order = it.firstOrNull { order -> order.orderId == orderId }
        Napier.demo { "observed EuOrder ${order?.euAccessCode?.accessCode}" }
        order
    }

    override fun observeAllEuOrders(): Flow<List<EuOrder>> = dataSource.euOrders

    override suspend fun toggleIsEuRedeemableByPatientAuthorization(
        taskId: String,
        profileId: String,
        metadata: FhirTaskEuPatchInputModelConstants.FhirTaskEuPatchMeta,
        isEuRedeemableByPatientAuthorization: Boolean
    ): Result<Unit> =
        withContext(dispatcher) {
            // update the order with the required events
            val currentOrders = dataSource.euOrders.value.toMutableList()
            val eventType = if (isEuRedeemableByPatientAuthorization) EuEventType.TASK_ADDED else EuEventType.TASK_REMOVED

            val updated = currentOrders.map { order ->
                if (order.profileId == profileId && taskId in order.relatedTaskIds) {
                    val newEvent = EuTaskEvent(
                        id = UUID.randomUUID().toString(),
                        type = eventType,
                        taskId = taskId,
                        createdAt = Clock.System.now(),
                        isUnread = true
                    )
                    order.copy(events = (order.events + newEvent).toMutableList())
                } else {
                    order
                }
            }
            dataSource.euOrders.value = updated.toMutableList()

            // update the synced task
            val data = dataSource.syncedTasks.updateAndGet { syncedList ->
                val index = syncedList.indexOfFirst { it.taskId == taskId }
                val updatedList = syncedList
                if (index != INDEX_OUT_OF_BOUNDS) {
                    val updatedItem = syncedList[index].copy(
                        isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
                        lastModified = Clock.System.now()
                    )
                    updatedList[index] = updatedItem
                }
                updatedList
            }
            dataSource.syncedTasks.value = emptyList<SyncedTaskData.SyncedTask>().toMutableList()
            delay(1) // this delay is required for the ui to react to this change, otherwise we dont get it reactive
            dataSource.syncedTasks.value = data
            Result.success(Unit)
        }

    override suspend fun createEuRedeemAccessCode(
        profileId: ProfileIdentifier,
        metadata: FhirEuRedeemAccessCodeRequestMeta,
        countryCode: String,
        relatedTaskIds: List<String>
    ): Result<EuAccessCode> {
        val now = Clock.System.now()

        // Create the new access code
        val code = EuAccessCode(
            countryCode = countryCode,
            accessCode = dataSource.generateCode(),
            createdAt = now,
            validUntil = now.plus(10.minutes),
            profileIdentifier = profileId
        )

        // Existing order lookup (same profile, same country, ANY overlapping task)
        val existingOrder = dataSource.euOrders.value.firstOrNull { order ->
            order.profileId == profileId && order.countryCode == countryCode
        }

        val orders = dataSource.euOrders.value.toMutableList()

        if (existingOrder != null) {
            // --- UPDATE EXISTING ORDER ---

            val mergedTasks = (existingOrder.relatedTaskIds + relatedTaskIds)
                .toSet()
                .toList()

            val updatedEvents = existingOrder.events.toMutableList().apply {
                // Add regeneration events for all new tasks
                relatedTaskIds.forEach { taskId ->
                    add(
                        EuTaskEvent(
                            id = UUID.randomUUID().toString(),
                            taskId = taskId,
                            createdAt = now,
                            isUnread = true,
                            type = EuEventType.ACCESS_CODE_RECREATED
                        )
                    )
                }
            }

            val updatedOrder = existingOrder.copy(
                euAccessCode = code,
                createdAt = now,
                relatedTaskIds = mergedTasks,
                events = updatedEvents
            )

            // replace the old order
            val index = orders.indexOfFirst { it.orderId == existingOrder.orderId }
            if (index != -1) {
                orders[index] = updatedOrder
            }

            // save lists back
            dataSource.euOrders.value = orders
            dataSource.euAccessCodes.value = (dataSource.euAccessCodes.value + code).toMutableList()

            Napier.demo { "Updated EuOrder $updatedOrder" }
        } else {
            // --- CREATE NEW ORDER ---

            val newOrder = EuOrder(
                countryCode = countryCode,
                orderId = UUID.randomUUID().toString(),
                createdAt = now,
                profileId = profileId,
                relatedTaskIds = relatedTaskIds,
                euAccessCode = code,
                events = relatedTaskIds.map { taskId ->
                    EuTaskEvent(
                        id = UUID.randomUUID().toString(),
                        taskId = taskId,
                        createdAt = now,
                        isUnread = true,
                        type = EuEventType.ACCESS_CODE_CREATED
                    )
                }.toMutableList()
            )

            dataSource.euOrders.value = (orders + newOrder).toMutableList()
            dataSource.euAccessCodes.value = (dataSource.euAccessCodes.value + code).toMutableList()

            Napier.demo { "Created new EuOrder $newOrder" }
        }

        return Result.success(code)
    }

    override suspend fun getLatestValidEuAccessCodeByProfileIdAndCountry(profileId: ProfileIdentifier, countryCode: String): Flow<EuAccessCode?> =
        dataSource.euAccessCodes.mapNotNull {
            it.firstOrNull { it.profileIdentifier == profileId && it.countryCode == countryCode && it.validUntil > Clock.System.now() }
        }

    override suspend fun deleteEuRedeemAccessCode(
        profileId: ProfileIdentifier,
        inProgress: () -> Unit,
        failed: (Throwable) -> Unit,
        completed: () -> Unit
    ) {
        inProgress()
        val codes = dataSource.euAccessCodes.value
        val codesToRemove = codes.filter { it.profileIdentifier == profileId }
        val removedAccessCodes = codesToRemove.map { it.accessCode }

        codes.removeAll(codesToRemove)
        dataSource.euAccessCodes.value = codes

        val updatedOrders = dataSource.euOrders.value.map { order ->
            when {
                order.profileId == profileId && removedAccessCodes.contains(order.euAccessCode?.accessCode) -> order.copy(euAccessCode = null)
                else -> order
            }
        }

        dataSource.euOrders.value = updatedOrders.toMutableList()

        delay(500)
        completed()
    }

    override suspend fun markEventsAsRead(eventIds: List<String>) {
        val currentOrders = dataSource.euOrders.value.toMutableList()

        val updatedOrders = currentOrders.map { order ->
            val updatedEvents = order.events.map { event ->
                event.copy(isUnread = false)
            }
            // Replace the events in the order
            order.copy(events = updatedEvents.toMutableList())
        }

        dataSource.euOrders.value = updatedOrders.toMutableList()
    }

    override fun getEuAccessCode(accessCode: String): Flow<EuAccessCode?> = dataSource.euAccessCodes.mapNotNull {
        it.firstOrNull { it.accessCode == accessCode }
    }
}
