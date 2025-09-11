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

import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

interface InvoiceRepository {

    fun getLatestTimeStamp(profileId: ProfileIdentifier): Flow<String?>

    suspend fun downloadChargeItemBundle(profileId: ProfileIdentifier, lastUpdated: String?): Result<JsonElement>

    suspend fun downloadChargeItemByTaskId(profileId: ProfileIdentifier, taskId: String): Result<JsonElement>

    suspend fun downloadInvoices(profileId: ProfileIdentifier): Result<Int>

    fun invoices(profileId: ProfileIdentifier): Flow<List<InvoiceData.PKVInvoiceRecord>>

    fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceData.InvoiceStatus>>

    fun getAllUnreadInvoices(): Flow<List<InvoiceData.InvoiceStatus>>

    suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean)

    fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean>

    fun invoiceByTaskId(taskId: String): Flow<InvoiceData.PKVInvoiceRecord?>

    suspend fun saveInvoice(profileId: ProfileIdentifier, bundle: FhirPkvChargeItemsErpModelCollection)

    suspend fun deleteRemoteInvoiceById(taskId: String, profileId: ProfileIdentifier): Result<Unit>

    fun loadInvoiceAttachments(taskId: String): List<Triple<String, String, ByteArray>>?

    suspend fun deleteLocalInvoiceById(taskId: String)

    suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant?
}
