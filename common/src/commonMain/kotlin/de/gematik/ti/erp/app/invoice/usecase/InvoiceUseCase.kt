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

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class InvoiceUseCase(
    private val invoiceRepository: InvoiceRepository,
    private val dispatchers: DispatchProvider
) {
    fun invoicesFlow(profileId: ProfileIdentifier): Flow<List<InvoiceData.PKVInvoice>> =
        invoiceRepository.invoices(profileId).flowOn(dispatchers.io)

    fun invoices(profileId: ProfileIdentifier): Flow<Map<Int, List<InvoiceData.PKVInvoice>>> =
        invoicesFlow(profileId).map { invoices ->
            invoices.sortedWith(
                compareByDescending {
                    it.timestamp
                }
            ).groupBy {
                it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).year
            }
        }

    suspend fun deleteInvoice(profileId: ProfileIdentifier, taskId: String): Result<Unit> {
        return invoiceRepository.deleteInvoiceById(profileId = profileId, taskId = taskId)
    }

    private class Request(
        val resultChannel: Channel<Result<Int>>,
        val forProfileId: ProfileIdentifier
    )

    private val scope = CoroutineScope(dispatchers.io)

    private val requestChannel =
        Channel<Request>(onUndeliveredElement = { it.resultChannel.close(CancellationException()) })

    private val _refreshInProgress = MutableStateFlow(false)
    val refreshInProgress: StateFlow<Boolean>
        get() = _refreshInProgress

    init {
        scope.launch {
            for (request in requestChannel) {
                _refreshInProgress.value = true
                Napier.d { "Start refreshing as per request" }

                val profileId = request.forProfileId

                val result = runCatching {
                    val nrOfNewInvoices = invoiceRepository.downloadInvoices(profileId).getOrThrow()
                    nrOfNewInvoices
                }
                request.resultChannel.trySend(result)

                Napier.d { "Finished refreshing" }
                _refreshInProgress.value = false
            }
        }
    }

    private suspend fun download(profileId: ProfileIdentifier): Result<Int> {
        val resultChannel = Channel<Result<Int>>()
        try {
            requestChannel.send(Request(resultChannel = resultChannel, forProfileId = profileId))

            return resultChannel.receive()
        } catch (cancellation: CancellationException) {
            Napier.d { "Cancelled waiting for result of refresh request" }
            withContext(NonCancellable) {
                resultChannel.close(cancellation)
            }
            throw cancellation
        }
    }

    fun downloadInvoices(profileId: ProfileIdentifier): Flow<Int> =
        flow {
            emit(download(profileId).getOrThrow())
        }

    fun loadAttachments(taskId: String) =
        invoiceRepository.loadInvoiceAttachments(taskId)

    fun invoiceById(taskId: String): Flow<InvoiceData.PKVInvoice?> =
        invoiceRepository.invoiceById(taskId)
}
