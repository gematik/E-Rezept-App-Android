/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.demomode.repository.orders

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/**
 * Simulates a communication download.
 * It gets the existing [dataSource.communications] and [dataSource.syncedTasks] and if the
 * [syncedTasks] are redeemable, then by a coin-toss decides if a new communication needs to be
 * added to the [dataSource] or not. It then provides this updated list back
 */
class DemoDownloadCommunicationResource(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Suppress("CyclomaticComplexMethod")
    operator fun invoke(
        profileId: ProfileIdentifier
    ): Flow<MutableList<DemoModeProfileLinkedCommunication>> =
        combine(
            dataSource.communications,
            dataSource.syncedTasks
        ) { communications, syncedTasks ->
            syncedTasks.map { syncedTask ->
                // simulates the communication being fetched from the backend
                val isCommunicationFetched = Random.nextBoolean()
                // coin-toss between simulates between request and reply
                val isCommunicationRequest = Random.nextBoolean()
                if (syncedTask.redeemState().isRedeemable()) {
                    val isExisting = communications
                        .indexOfFirst { it.taskId == syncedTask.taskId }
                        .takeIf { index -> index != INDEX_OUT_OF_BOUNDS }
                    isExisting?.let { index ->
                        val communication = communications[index]
                            .copy(
                                profileId = profileId,
                                recipient = "recipient",
                                payload = DemoModeDataSource.communicationPayload,
                                consumed = false,
                                sender = profileId,
                                sentOn = Clock.System.now().minus(45.minutes),
                                orderId = UUID.randomUUID().toString(),
                                profile = when (isCommunicationRequest) {
                                    true -> CommunicationProfile.ErxCommunicationDispReq
                                    false -> CommunicationProfile.ErxCommunicationReply
                                }
                            )
                        communication to isCommunicationFetched
                    } ?: run {
                        val communication = DemoModeProfileLinkedCommunication(
                            profileId = profileId,
                            taskId = syncedTask.taskId,
                            communicationId = UUID.randomUUID().toString(),
                            orderId = UUID.randomUUID().toString(),
                            consumed = false,
                            recipient = "recipient",
                            payload = DemoModeDataSource.communicationPayload,
                            sender = "sender",
                            sentOn = Clock.System.now().minus(3.days),
                            profile = when (isCommunicationRequest) {
                                true -> CommunicationProfile.ErxCommunicationDispReq
                                false -> CommunicationProfile.ErxCommunicationReply
                            }
                        )
                        communication to isCommunicationFetched
                    }
                } else {
                    null to false
                }
            }
        }.mapNotNull { communicationPairs ->
            val communications = dataSource.communications.value
            communicationPairs.forEach { communicationPair ->
                val (communication, successfulNetworkCall) = communicationPair

                val index = communications.indexOfFirst { item ->
                    item.taskId == communicationPair.first?.taskId
                }.takeIf { it != INDEX_OUT_OF_BOUNDS }
                index?.let { nonNullIndex ->
                    when (successfulNetworkCall && communication != null) {
                        true -> communications[nonNullIndex] = communication
                        else -> communications.removeAt(nonNullIndex)
                    }
                } ?: run {
                    if (successfulNetworkCall && communication != null) {
                        communications.add(communication)
                    }
                }
            }
            communications
        }.map { communications ->
            dataSource.profileCommunicationLog.value =
                updateCommunicationLog(dataSource.profileCommunicationLog, profileId)

            dataSource.communications.value = communications
            communications
        }.flowOn(dispatcher)
}

/**
 * This addition is to be done only once for the profile, this flag is added to not do it twice
 */
private fun updateCommunicationLog(
    dataSource: MutableStateFlow<MutableMap<String, Boolean>>,
    profileId: ProfileIdentifier
) =
    dataSource.updateAndGet {
        it.putIfAbsent(profileId, true)
        it
    }
