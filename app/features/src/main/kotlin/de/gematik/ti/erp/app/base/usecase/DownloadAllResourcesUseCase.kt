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

package de.gematik.ti.erp.app.base.usecase

import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.model.DownloadResourcesState
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.repository.DownloadResourcesStateRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * UseCase to download all resources for a given profile,
 * including tasks, communications, invoices and new prescriptions count
 */

class DownloadAllResourcesUseCase(
    private val taskRepository: TaskRepository,
    private val communicationRepository: CommunicationRepository,
    private val invoicesRepository: InvoiceRepository,
    private val profileRepository: ProfileRepository,
    private val stateRepository: DownloadResourcesStateRepository,
    private val settingsRepository: SettingsRepository,
    private val networkStatusTracker: NetworkStatusTracker,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(profileId: ProfileIdentifier): Result<Int> =
        withContext(dispatcher) {
            val resultChannel = Channel<Result<Int>>()
            try {
                Napier.i { "Requesting download for $profileId" }
                requestChannel.send(
                    Request(
                        resultChannel = resultChannel,
                        profileId = profileId
                    )
                )
                return@withContext resultChannel.receive()
            } catch (cancelException: CancellationException) {
                withContext(NonCancellable) {
                    resultChannel.close(cancelException)
                }
                return@withContext Result.failure(cancelException)
            }
        }

    private class Request(
        val resultChannel: Channel<Result<Int>>,
        val profileId: ProfileIdentifier
    )

    private val scope = CoroutineScope(dispatcher + SupervisorJob()) // to allow it to run even if the viewmodel is cleared

    private val requestChannel =
        Channel<Request>(
            capacity = CONFLATED,
            onUndeliveredElement = {
                stateRepository.closeSnapshotState()
                it.resultChannel.close(CancellationException())
            }
        )

    init {
        scope.launch {
            for (request in requestChannel) {
                Napier.i { "Downloading queue has ${request.profileId}" }
                handleDownloadRequest(request)
            }
        }
    }

    private suspend fun handleDownloadRequest(request: Request) =
        runCatching {
            val profileId = request.profileId
            stateRepository.updateSnapshotState(DownloadResourcesState.NotStarted)
            if (networkStatusTracker.networkStatus.first()) { // tell in progress only if nw is connected
                with(stateRepository) {
                    updateSnapshotState(DownloadResourcesState.InProgress)
                    updateDetailState(DownloadResourcesState.InProgress)
                }
            }

            val newPrescriptionsCount = downloadTasks(profileId)
            stateRepository.updateDetailState(DownloadResourcesState.TasksDownloaded)

            downloadCommunications(profileId) {
                stateRepository.updateDetailState(DownloadResourcesState.CommunicationsDownloaded)
            }

            if (profileRepository.checkIsProfilePKV(profileId)) {
                Napier.i { "Downloaded invoices for $profileId" }
                downloadInvoices(profileId)
                stateRepository.updateDetailState(DownloadResourcesState.InvoicesDownloaded)
            }
            Napier.i { "Refresh finished for $profileId with $newPrescriptionsCount new prescriptions" }
            request.resultChannel.send(Result.success(newPrescriptionsCount))
        }.onFailure { error ->
            when (error) {
                is CancellationException -> {
                    Napier.e(error) { "CancellationException on downloading resources" }
                    request.resultChannel.close(error)
                }

                else -> {
                    Napier.e(error) { "Error downloading resources" }
                    request.resultChannel.send(Result.failure(error))
                }
            }
        }.onSuccess {
            settingsRepository.updateRefreshTime()
        }
            .finally {
                with(stateRepository) {
                    updateSnapshotState(DownloadResourcesState.Finished)
                    updateDetailState(DownloadResourcesState.Finished)
                }
            }

    private suspend fun downloadTasks(profileId: ProfileIdentifier): Int =
        withContext(NonCancellable) {
            try {
                taskRepository.downloadTasks(profileId).getOrThrow { throw it }
            } catch (e: Exception) {
                Napier.e(e) { "Error downloading tasks" }
                throw e
            }
        }

    private suspend fun downloadCommunications(
        profileId: ProfileIdentifier,
        onComplete: () -> Unit
    ) {
        withContext(NonCancellable) {
            try {
                communicationRepository.downloadCommunications(profileId).getOrThrow { throw it }
            } catch (e: Exception) {
                Napier.e(e) { "Error downloading communications" }
                throw e
            } finally {
                onComplete()
            }
        }
    }

    private suspend fun downloadInvoices(profileId: ProfileIdentifier): Int =
        withContext(NonCancellable) {
            try {
                invoicesRepository.downloadInvoices(profileId).getOrThrow { throw it }
            } catch (e: Exception) {
                Napier.e(e) { "Error downloading invoices" }
                throw e
            }
        }

    private fun <T> Result<T>.getOrThrow(block: (Throwable) -> Nothing): T {
        return getOrElse {
            Napier.e(it) { "Error downloading resources" }
            block(it)
        }
    }

    private inline fun <T> Result<T>.finally(block: () -> Unit): Result<T> {
        block()
        return this
    }
}
