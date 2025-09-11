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

package de.gematik.ti.erp.app.invoice.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.fhir.pkv.parser.ChargeItemEPrescriptionParsers
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

private const val InvoiceMaxPageSize = 25

class DefaultInvoiceRepository(
    private val remoteDataSource: InvoiceRemoteDataSource,
    private val localDataSource: InvoiceLocalDataSource,
    private val parsers: ChargeItemEPrescriptionParsers,
    private val dispatchers: DispatchProvider
) : InvoiceRepository, ResourcePaging<Int>(dispatchers, InvoiceMaxPageSize, maxPages = 1) {

    override fun getLatestTimeStamp(profileId: ProfileIdentifier): Flow<String?> =
        localDataSource.latestInvoiceModifiedTimestamp(profileId).map { it.toTimestampString() }

    override suspend fun downloadChargeItemBundle(
        profileId: ProfileIdentifier,
        lastUpdated: String?
    ): Result<JsonElement> =
        remoteDataSource.getChargeItems(
            profileId = profileId,
            lastUpdated = lastUpdated
        )

    override suspend fun downloadChargeItemByTaskId(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement> = remoteDataSource.getChargeItemBundleById(profileId, taskId)

    override suspend fun downloadInvoices(profileId: ProfileIdentifier): Result<Int> =
        downloadPaged(profileId) { prev: Int?, next: Int ->
            (prev ?: 0) + next
        }.map {
            it ?: 0
        }

    override fun invoices(profileId: ProfileIdentifier): Flow<List<InvoiceData.PKVInvoiceRecord>> =
        localDataSource.loadInvoices(profileId)

    override fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceData.InvoiceStatus>> =
        localDataSource.getInvoiceTaskIdAndConsumedStatus(profileId)

    override fun getAllUnreadInvoices(): Flow<List<InvoiceData.InvoiceStatus>> =
        localDataSource.getAllUnreadInvoices()

    override suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean) {
        localDataSource.updateInvoiceCommunicationStatus(taskId, consumed)
    }

    override fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean> =
        localDataSource.hasUnreadInvoiceMessages(taskIds)

    override fun invoiceByTaskId(taskId: String): Flow<InvoiceData.PKVInvoiceRecord?> =
        localDataSource.loadInvoiceByTaskId(taskId)

    override suspend fun saveInvoice(profileId: ProfileIdentifier, bundle: FhirPkvChargeItemsErpModelCollection) {
        localDataSource.saveInvoice(profileId, bundle)
    }

    override suspend fun deleteRemoteInvoiceById(
        taskId: String,
        profileId: ProfileIdentifier
    ): Result<Unit> = withContext(dispatchers.io) {
        remoteDataSource.deleteChargeItemById(profileId, taskId)
    }

    override fun loadInvoiceAttachments(taskId: String): List<Triple<String, String, ByteArray>>? =
        localDataSource.loadInvoiceAttachments(taskId)

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestInvoiceModifiedTimestamp(profileId).first()

    override suspend fun deleteLocalInvoiceById(taskId: String): Unit =
        localDataSource.deleteInvoiceById(taskId)

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
            withContext(dispatchers.io) {
                val chargeItemEntryParserResultErpModel = parsers.entryParser.extract(fhirBundle)
                val total = chargeItemEntryParserResultErpModel?.bundleTotal
                val taskIds = chargeItemEntryParserResultErpModel?.chargeItemEntries?.map { it.taskId }

                supervisorScope {
                    val results = taskIds?.map { taskId ->
                        async {
                            downloadInvoiceWithBundle(taskId = taskId, profileId = profileId)
                        }
                    }?.awaitAll()

                    // return number of bundles saved to db
                    ResourceResult(total ?: 0, results?.size ?: 0)
                }
            }
        }

    private suspend fun downloadInvoiceWithBundle(
        taskId: String,
        profileId: ProfileIdentifier
    ) = withContext(dispatchers.io) {
        remoteDataSource.getChargeItemBundleById(profileId, taskId).mapCatching { bundle ->
            val bundleCollection = parsers.bundleParser.extract(bundle)
            localDataSource.saveInvoice(profileId, bundleCollection)
        }
    }
}
