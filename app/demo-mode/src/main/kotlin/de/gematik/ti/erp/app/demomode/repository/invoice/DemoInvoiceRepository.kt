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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
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

package de.gematik.ti.erp.app.demomode.repository.invoice

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.invoice.model.InvoiceStatusErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class DemoInvoiceRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : InvoiceRepository {

    override fun getLatestTimeStamp(profileId: ProfileIdentifier): Flow<String?> =
        flowOf(null)

    override suspend fun downloadChargeItemBundle(
        profileId: ProfileIdentifier,
        lastUpdated: String?
    ): Result<JsonElement> = Result.success(JsonObject(emptyMap()))

    override suspend fun downloadChargeItemByTaskId(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement> = Result.success(JsonObject(emptyMap()))

    override suspend fun downloadInvoices(profileId: ProfileIdentifier): Result<Int> =
        Result.success(dataSource.invoices.value.size)

    override fun invoices(profileId: ProfileIdentifier): Flow<List<PKVInvoiceErpModel>> =
        dataSource.invoices.map { invoices ->
            invoices.filter { it.profileId == profileId }
        }.flowOn(dispatcher)

    override fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceStatusErpModel>> =
        dataSource.invoices.map { invoices ->
            invoices.filter { it.profileId == profileId }.map {
                InvoiceStatusErpModel(taskId = it.taskId, consumed = it.consumed)
            }
        }.flowOn(dispatcher)

    override fun getAllUnreadInvoices(): Flow<List<InvoiceStatusErpModel>> =
        dataSource.invoices.map { invoices ->
            invoices.filter { !it.consumed }.map {
                InvoiceStatusErpModel(taskId = it.taskId, consumed = it.consumed)
            }
        }.flowOn(dispatcher)

    override suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean) {
        withContext(dispatcher) {
            dataSource.invoices.update { list ->
                list.map {
                    if (it.taskId == taskId) it.copy(consumed = consumed) else it
                }.toMutableList()
            }
        }
    }

    override fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean> =
        dataSource.invoices.map { invoices ->
            invoices.any { it.taskId in taskIds && !it.consumed }
        }.flowOn(dispatcher)

    override fun invoiceByTaskId(taskId: String): Flow<PKVInvoiceErpModel?> =
        dataSource.invoices.map { invoices ->
            invoices.find { it.taskId == taskId }
        }.flowOn(dispatcher)

    override suspend fun saveInvoice(
        profileId: ProfileIdentifier,
        bundle: FhirPkvChargeItemsErpModelCollection
    ) {
        // no-op for demo mode
    }

    override suspend fun deleteRemoteInvoiceById(
        taskId: String,
        profileId: ProfileIdentifier
    ): Result<Unit> = Result.success(Unit)

    override fun loadInvoiceAttachments(taskId: String): List<Triple<String, String, ByteArray>>? =
        null

    override suspend fun deleteLocalInvoiceById(taskId: String) {
        withContext(dispatcher) {
            dataSource.invoices.update { list ->
                list.filterNot { it.taskId == taskId }.toMutableList()
            }
        }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        null
}
