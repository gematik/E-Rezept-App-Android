/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RefreshState {
    NotStarted, InProgress, Finished
}

class RefreshPrescriptionUseCase(
    private val repository: TaskRepository,
    private val communicationRepository: CommunicationRepository,
    private val invoiceRepository: InvoiceRepository,
    private val profilesRepository: ProfileRepository,
    dispatchers: DispatchProvider
) {
    private class Request(
        val resultChannel: Channel<Result<Int>>,
        val forProfileId: ProfileIdentifier
    )

    private val scope = CoroutineScope(dispatchers.io)

    private val requestChannel =
        Channel<Request>(onUndeliveredElement = { it.resultChannel.close(CancellationException()) })

    private val _refreshInProgress = MutableStateFlow(RefreshState.NotStarted)
    val refreshInProgress: StateFlow<RefreshState>
        get() = _refreshInProgress

    init {
        scope.launch {
            for (request in requestChannel) {
                _refreshInProgress.value = RefreshState.InProgress
                Napier.d { "Start refreshing as per request" }

                val profileId = request.forProfileId

                val result = runCatching {
                    val nrOfNewPrescriptions = repository.downloadTasks(profileId).getOrThrow()
                    communicationRepository.downloadCommunications(profileId).getOrThrow()
                    if (profilesRepository.checkIsProfilePKV(profileId)) {
                        invoiceRepository.downloadInvoices(profileId)
                    }
                    // downloadResourcesStateRepository.updateState(nrOfNewPrescriptions)
                    nrOfNewPrescriptions
                }

                // may be closed already
                request.resultChannel.trySend(result)

                Napier.d { "Finished refreshing" }
                _refreshInProgress.value = RefreshState.Finished
            }
        }
    }

    private suspend fun download(profileId: ProfileIdentifier): Result<Int> {
        val resultChannel = Channel<Result<Int>>()
        try {
            requestChannel.send(
                Request(
                    resultChannel = resultChannel,
                    forProfileId = profileId
                )
            )
            return resultChannel.receive()
        } catch (cancellation: CancellationException) {
            Napier.d { "Cancelled waiting for result of refresh request" }
            withContext(NonCancellable) {
                resultChannel.close(cancellation)
            }
            throw cancellation
        }
    }

    fun downloadFlow(profileId: ProfileIdentifier): Flow<Int> =
        flow {
            emit(download(profileId).getOrThrow())
        }
}
