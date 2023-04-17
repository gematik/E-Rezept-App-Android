/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.invoice.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.model.extractTaskIdsFromChargeItemBundle
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import java.net.HttpURLConnection

private const val InvoiceMaxPageSize = 25

class InvoiceRepository(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val localDataSource: InvoiceLocalDataSource,
    private val dispatchers: DispatchProvider
) : ResourcePaging<Int>(dispatchers, InvoiceMaxPageSize, maxPages = 1) {

    suspend fun downloadInvoices(profileId: ProfileIdentifier) = downloadPaged(profileId) { prev: Int?, next: Int ->
        (prev ?: 0) + next
    }.map {
        it ?: 0
    }

    fun invoices(profileId: ProfileIdentifier) =
        localDataSource.loadInvoices(profileId).flowOn(dispatchers.IO)

    fun invoiceById(taskId: String) =
        localDataSource.loadInvoiceById(taskId).flowOn(dispatchers.IO)

    suspend fun saveInvoice(profileId: ProfileIdentifier, bundle: JsonElement) {
        localDataSource.saveInvoice(profileId, bundle)
    }

    override val tag: String = "InvoiceRepository"

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Int>> =
        remoteDataSource.getChargeItems(
            profileId = profileId,
            lastUpdated = timestamp,
            count = count
        ).mapCatching { fhirBundle ->
            withContext(dispatchers.IO) {
                val (total, taskIds) = extractTaskIdsFromChargeItemBundle(fhirBundle)

                supervisorScope {
                    val results = taskIds.map { taskId ->
                        async {
                            downloadInvoiceWithBundle(taskId = taskId, profileId = profileId)
                        }
                    }.awaitAll()

                    // return number of bundles saved to db
                    ResourceResult(total, results.size)
                }
            }
        }
    private suspend fun downloadInvoiceWithBundle(
        taskId: String,
        profileId: ProfileIdentifier
    ) = withContext(dispatchers.IO) {
        remoteDataSource.getChargeItemBundleById(profileId, taskId).mapCatching { bundle ->
            requireNotNull(localDataSource.saveInvoice(profileId, bundle))
        }
    }

    suspend fun deleteInvoiceById(
        taskId: String,
        profileId: ProfileIdentifier
    ) = withContext(dispatchers.IO) {
        val result = remoteDataSource.deleteChargeItemById(profileId, taskId)
            .onSuccess {
                localDataSource.deleteInvoiceById(taskId)
            }.onFailure {
                if (it is ApiCallException) {
                    when (it.response.code()) {
                        HttpURLConnection.HTTP_NOT_FOUND,
                        HttpURLConnection.HTTP_GONE ->
                            localDataSource.deleteInvoiceById(taskId)
                    }
                }
            }
        result
    }

    fun loadInvoiceAttachments(taskId: String) =
        localDataSource.loadInvoiceAttachments(taskId)

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestInvoiceModifiedTimestamp(profileId).first()
}
