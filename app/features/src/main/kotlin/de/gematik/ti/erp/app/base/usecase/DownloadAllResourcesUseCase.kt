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

package de.gematik.ti.erp.app.base.usecase

import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.model.DownloadResourcesState
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.repository.DownloadResourcesStateRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * UseCase to download all resources for a given profile,
 * including tasks, communications, invoices and new prescriptions count
 */

class DownloadAllResourcesUseCase(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val taskRepository: TaskRepository,
    private val communicationRepository: CommunicationRepository,
    private val invoicesRepository: InvoiceRepository,
    private val profileRepository: ProfileRepository,
    private val stateRepository: DownloadResourcesStateRepository,
    private val settingsRepository: SettingsRepository,
    private val networkStatusTracker: NetworkStatusTracker
) {
    // 1) A SupervisorJob + IO dispatcher that lives as long as this UseCase lives.
    //    Because it's not tied to any ViewModel, it will keep working across
    //    configuration changes/screens.
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // 2) An unbounded queue so we never drop requests
    private data class Request(
        val profileId: ProfileIdentifier,
        val reply: CompletableDeferred<Int>
    )

    private val requests = Channel<Request>(
        capacity = Channel.CONFLATED,
        onUndeliveredElement = { droppedRequest ->
            // rollback any “started” state for that profile
            stateRepository.closeSnapshotState()
            // tell the caller it failed
            droppedRequest.reply.completeExceptionally(
                CancellationException("Download request dropped")
            )
        }
    )

    init {
        // 3) Start one consumer in that long-lived scope
        scope.launch {
            for (req in requests) {
                handleDownload(req)
            }
        }
    }

    /**
     * Public API:
     * Enqueue a download request and return either
     *   • Result.success(newPrescriptionsCount)
     *   • Result.failure(exception)
     */
    suspend operator fun invoke(profileId: ProfileIdentifier): Result<Int> = runCatching {
        // 1) Prepare a one-shot deferred for the reply
        val deferred = CompletableDeferred<Int>()

        // 2) Enqueue; if the channel is already closed this will throw,
        //    and runCatching will catch it for you.
        requests.send(Request(profileId, deferred))

        // 3) Wait for either a success or an exception from handleDownload
        deferred.await()
    }

    private suspend fun handleDownload(request: Request) {
        val (profileId, reply) = request

        stateRepository.updateSnapshotState(DownloadResourcesState.NotStarted)

        if (networkStatusTracker.networkStatus.firstOrNull() == true) {
            stateRepository.updateSnapshotState(DownloadResourcesState.InProgress)
            stateRepository.updateDetailState(DownloadResourcesState.InProgress)
        }

        runCatching {
            // 1) Tasks
            val newCount = safeDownload("tasks") {
                taskRepository.downloadTasks(profileId)
            }
            stateRepository.updateDetailState(DownloadResourcesState.TasksDownloaded)

            // 2) Communications
            safeDownload("communications") {
                communicationRepository.downloadCommunications(profileId)
            }
            stateRepository.updateDetailState(DownloadResourcesState.CommunicationsDownloaded)

            // 3) Invoices (if PKV)
            if (profileRepository.checkIsProfilePKV(profileId)) {
                safeDownload("invoices") {
                    invoicesRepository.downloadInvoices(profileId)
                }
                stateRepository.updateDetailState(DownloadResourcesState.InvoicesDownloaded)
            }

            newCount
        }
            .onSuccess { count ->
                settingsRepository.updateRefreshTime() //  Change this to profile based if needed
                reply.complete(count)
            }
            .onFailure { error ->
                Napier.e(error) { "Download failed for $profileId" }
                reply.completeExceptionally(error)
            }
            .also {
                stateRepository.updateSnapshotState(DownloadResourcesState.Finished)
                stateRepository.updateDetailState(DownloadResourcesState.Finished)
            }
    }

    /** Log failures and rethrow, inside NonCancellable so downloads finish even if cancelled */
    private suspend fun <T> safeDownload(
        name: String,
        block: suspend () -> Result<T>
    ): T = withContext(NonCancellable) {
        block()
            .onSuccess { Napier.i { "Successfully downloaded $name" } }
            .onFailure { Napier.e(it) { "Error downloading $name" } }
            .getOrThrow()
    }
}
