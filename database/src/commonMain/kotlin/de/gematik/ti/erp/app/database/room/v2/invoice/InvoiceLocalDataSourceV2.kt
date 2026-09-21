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

package de.gematik.ti.erp.app.database.room.v2.invoice

import de.gematik.ti.erp.app.database.api.invoice.InvoiceLocalDataSource
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.invoice.mapper.toErpModel
import de.gematik.ti.erp.app.invoice.model.InvoiceStatusErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import org.bouncycastle.util.encoders.Base64

class InvoiceLocalDataSourceV2(private val dao: InvoiceDao) : InvoiceLocalDataSource {
    override fun latestInvoiceModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> =
        dao.latestInvoiceModifiedTimestamp(profileId).map { it?.let { Instant.fromEpochMilliseconds(it) } }

    override suspend fun saveInvoice(
        profileId: ProfileIdentifier,
        chargeItemCollection: FhirPkvChargeItemsErpModelCollection
    ) {
        val entities = chargeItemCollection.chargeItems.map { item ->
            val erpModel = item.toErpModel(profileId)

            InvoiceRoomEntity(
                taskId = erpModel.taskId,
                profileId = profileId,
                accessCode = erpModel.accessCode,
                timestampEpochMillis = erpModel.timestamp.toEpochMilliseconds(),
                consumed = erpModel.consumed,
                whenHandedOverIso = erpModel.whenHandedOver?.formattedString(),
                dataJson = SafeJson.value.encodeToString(PKVInvoiceErpModel.serializer(), erpModel),
                invoiceBinaryB64 = erpModel.invoiceBinary?.let { Base64.toBase64String(it) },
                kbvBinaryB64 = erpModel.kbvBinary?.let { Base64.toBase64String(it) },
                erpPrBinaryB64 = erpModel.erpPrBinary?.let { Base64.toBase64String(it) }
            )
        }
        dao.upsertAll(entities)
    }

    override suspend fun saveInvoice(invoice: PKVInvoiceErpModel) {
        val entity = InvoiceRoomEntity(
            taskId = invoice.taskId,
            profileId = invoice.profileId,
            accessCode = invoice.accessCode,
            timestampEpochMillis = invoice.timestamp.toEpochMilliseconds(),
            consumed = invoice.consumed,
            whenHandedOverIso = invoice.whenHandedOver?.formattedString(),
            dataJson = SafeJson.value.encodeToString(PKVInvoiceErpModel.serializer(), invoice),
            invoiceBinaryB64 = invoice.invoiceBinary?.let { Base64.toBase64String(it) },
            kbvBinaryB64 = invoice.kbvBinary?.let { Base64.toBase64String(it) },
            erpPrBinaryB64 = invoice.erpPrBinary?.let { Base64.toBase64String(it) }
        )
        dao.upsertAll(listOf(entity))
    }

    override fun loadInvoices(profileId: ProfileIdentifier): Flow<List<PKVInvoiceErpModel>> =
        dao.observeByProfile(profileId).map { list -> list.map { it.toErpModel() } }

    override fun loadInvoiceByTaskId(taskId: String): Flow<PKVInvoiceErpModel?> =
        dao.observeByTaskId(taskId).map { it?.toErpModel() }

    override fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceStatusErpModel>> =
        dao.observeByProfile(profileId).map { list -> list.map { InvoiceStatusErpModel(it.taskId, it.consumed) } }

    override fun getAllUnreadInvoices(): Flow<List<InvoiceStatusErpModel>> =
        dao.observeUnread().map { list -> list.map { InvoiceStatusErpModel(it.taskId, it.consumed) } }

    override suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean) {
        dao.updateConsumedStatus(taskId, consumed)
    }

    override fun loadInvoiceAttachments(taskId: String): List<Triple<String, String, ByteArray>>? {
        // Implementation for Room
        return null
    }

    override suspend fun deleteInvoiceById(taskId: String) {
        dao.deleteByTaskId(taskId)
    }

    override fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean> {
        return dao.countUnread(taskIds).map { it > 0 }
    }

    private fun InvoiceRoomEntity.toErpModel(): PKVInvoiceErpModel {
        val model = SafeJson.value.decodeFromString(PKVInvoiceErpModel.serializer(), dataJson)
        return model.copy(
            invoiceBinary = invoiceBinaryB64?.let { Base64.decode(it) },
            kbvBinary = kbvBinaryB64?.let { Base64.decode(it) },
            erpPrBinary = erpPrBinaryB64?.let { Base64.decode(it) }
        )
    }
}
