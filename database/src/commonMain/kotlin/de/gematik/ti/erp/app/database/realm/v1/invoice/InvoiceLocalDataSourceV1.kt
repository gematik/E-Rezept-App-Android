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

package de.gematik.ti.erp.app.database.realm.v1.invoice

import de.gematik.ti.erp.app.database.api.invoice.InvoiceLocalDataSource
import de.gematik.ti.erp.app.database.api.model.PrescriptionDataNotFoundException
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.tryWrite
import de.gematik.ti.erp.app.database.realm.v1.invoice.mapper.InvoiceDatabaseMappers.toInvoiceDatabaseModel
import de.gematik.ti.erp.app.database.realm.v1.invoice.mapper.toErpModel
import de.gematik.ti.erp.app.database.realm.v1.profile.ProfileEntityV1
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.invoice.model.InvoiceStatusErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant

class InvoiceLocalDataSourceV1(private val realm: Realm) : InvoiceLocalDataSource {

    override fun latestInvoiceModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> =
        realm.query<PKVInvoiceEntityV1>("parent.id = $0", profileId)
            .max<RealmInstant>("timestamp")
            .asFlow()
            .map {
                it?.toInstant()
            }

    private val mutex = Mutex()

    private fun MutableRealm.findProfile(profileId: ProfileIdentifier): ProfileEntityV1 {
        return queryFirst<ProfileEntityV1>("id = $0", profileId)
            ?: throw PrescriptionDataNotFoundException("ProfileEntity with id $profileId not found in database")
    }

    private fun MutableRealm.findExistingInvoice(taskId: String): PKVInvoiceEntityV1? =
        queryFirst<PKVInvoiceEntityV1>("taskId = $0", taskId)

    override suspend fun saveInvoice(
        profileId: ProfileIdentifier,
        chargeItemCollection: FhirPkvChargeItemsErpModelCollection
    ) {
        mutex.withLock {
            realm.safeWrite {
                val profile = findProfile(profileId)

                chargeItemCollection.chargeItems.forEach { chargeItem ->
                    val entity = chargeItem.taskId
                        ?.let { findExistingInvoice(it) }
                        ?: copyToRealm(PKVInvoiceEntityV1()).also { newEntity ->
                            // Only add if we just created it
                            profile.invoices.add(newEntity)
                        }

                    // Always update fields, regardless of new or existing
                    chargeItem.toInvoiceDatabaseModel(profile, entity)
                }
            }
        }
    }

    override suspend fun saveInvoice(invoice: PKVInvoiceErpModel) {
        // No-op for V1 migration
    }

    override fun loadInvoices(profileId: ProfileIdentifier): Flow<List<PKVInvoiceErpModel>> =
        realm.query<PKVInvoiceEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { invoices ->
                invoices.list.map { it.toErpModel() }
            }

    override fun loadInvoiceByTaskId(taskId: String): Flow<PKVInvoiceErpModel?> =
        realm.query<PKVInvoiceEntityV1>("taskId = $0", taskId)
            .sort("timestamp", Sort.DESCENDING)
            .asFlow()
            .map { result ->
                result.list.firstOrNull()?.toErpModel()
            }

    override fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceStatusErpModel>> =
        realm.query<PKVInvoiceEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { invoices ->
                invoices.list.map { invoice ->
                    InvoiceStatusErpModel(taskId = invoice.taskId, consumed = invoice.consumed)
                }
            }

    override fun getAllUnreadInvoices(): Flow<List<InvoiceStatusErpModel>> =
        realm.query<PKVInvoiceEntityV1>("consumed = false")
            .asFlow()
            .map { invoices ->
                invoices.list.map { invoice ->
                    InvoiceStatusErpModel(taskId = invoice.taskId, consumed = invoice.consumed)
                }
            }

    override suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean) {
        realm.tryWrite {
            this.query<PKVInvoiceEntityV1>("taskId == $0", taskId)
                .first()
                .find()?.apply {
                    this.consumed = consumed
                } ?: Napier.w("Task ID $taskId not found, unable to update consumed status")
        }
    }

    override fun loadInvoiceAttachments(taskId: String) =
        realm.queryFirst<PKVInvoiceEntityV1>("taskId = $0", taskId)?.let {
            listOf(
                Triple("${taskId}_verordnung.p7s", "application/pkcs7-mime", it.kbvBinary),
                Triple("${taskId}_abgabedaten.p7s", "application/pkcs7-mime", it.invoiceBinary),
                Triple("${taskId}_quittung.p7s", "application/pkcs7-mime", it.erpPrBinary)
            )
        }

    override suspend fun deleteInvoiceById(taskId: String): Unit =
        realm.tryWrite {
            queryFirst<PKVInvoiceEntityV1>("taskId = $0", taskId)?.let { delete(it) }
        }

    override fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean> {
        return if (taskIds.isEmpty()) {
            flowOf(false)
        } else {
            val orQuery = taskIds.indices.joinToString(" || ") { "taskId = $$it" }

            realm.query<PKVInvoiceEntityV1>(
                orQuery,
                *taskIds.toTypedArray()
            )
                .query("consumed = false")
                .count()
                .asFlow()
                .map { it > 0 }
        }
    }
}
