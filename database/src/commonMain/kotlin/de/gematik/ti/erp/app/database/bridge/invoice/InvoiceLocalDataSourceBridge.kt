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
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.database.bridge.invoice

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.invoice.InvoiceLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.debug.model.DbMigrationFunctionalState
import de.gematik.ti.erp.app.debug.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.fhir.FhirPkvChargeItemsErpModelCollection
import de.gematik.ti.erp.app.invoice.model.InvoiceStatusErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class InvoiceLocalDataSourceBridge(
    private val v1: InvoiceLocalDataSource,
    private val v2: InvoiceLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : InvoiceLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun latestInvoiceModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> v2.latestInvoiceModifiedTimestamp(profileId)
            else -> v1.latestInvoiceModifiedTimestamp(profileId)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveInvoice(
        profileId: ProfileIdentifier,
        chargeItemCollection: FhirPkvChargeItemsErpModelCollection
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> v2.saveInvoice(profileId, chargeItemCollection)
            else -> v1.saveInvoice(profileId, chargeItemCollection)
        }.also {
            logger.addLog(
                DbMigrationLogEntry(
                    operation = operationName,
                    usesRoom = useRoom,
                    functionalState = DbMigrationFunctionalState.OperationNoCheck
                )
            )
        }
    }

    override suspend fun saveInvoice(invoice: PKVInvoiceErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> v2.saveInvoice(invoice)
            else -> v1.saveInvoice(invoice)
        }.also {
            logger.addLog(
                DbMigrationLogEntry(
                    operation = operationName,
                    usesRoom = useRoom,
                    functionalState = DbMigrationFunctionalState.OperationNoCheck
                )
            )
        }
    }

    override fun loadInvoices(profileId: ProfileIdentifier): Flow<List<PKVInvoiceErpModel>> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> v2.loadInvoices(profileId)
            else -> v1.loadInvoices(profileId)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override fun loadInvoiceByTaskId(taskId: String): Flow<PKVInvoiceErpModel?> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> v2.loadInvoiceByTaskId(taskId)
            else -> v1.loadInvoiceByTaskId(taskId)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override fun getInvoiceTaskIdAndConsumedStatus(profileId: ProfileIdentifier): Flow<List<InvoiceStatusErpModel>> {
        return when {
            useRoom -> v2.getInvoiceTaskIdAndConsumedStatus(profileId)
            else -> v1.getInvoiceTaskIdAndConsumedStatus(profileId)
        }
    }

    override fun getAllUnreadInvoices(): Flow<List<InvoiceStatusErpModel>> {
        return when {
            useRoom -> v2.getAllUnreadInvoices()
            else -> v1.getAllUnreadInvoices()
        }
    }

    override suspend fun updateInvoiceCommunicationStatus(taskId: String, consumed: Boolean) {
        when {
            useRoom -> v2.updateInvoiceCommunicationStatus(taskId, consumed)
            else -> v1.updateInvoiceCommunicationStatus(taskId, consumed)
        }
    }

    override fun loadInvoiceAttachments(taskId: String): List<Triple<String, String, ByteArray>>? {
        return when {
            useRoom -> v2.loadInvoiceAttachments(taskId)
            else -> v1.loadInvoiceAttachments(taskId)
        }
    }

    override suspend fun deleteInvoiceById(taskId: String) {
        when {
            useRoom -> v2.deleteInvoiceById(taskId)
            else -> v1.deleteInvoiceById(taskId)
        }
    }

    override fun hasUnreadInvoiceMessages(taskIds: List<String>): Flow<Boolean> {
        return when {
            useRoom -> v2.hasUnreadInvoiceMessages(taskIds)
            else -> v1.hasUnreadInvoiceMessages(taskIds)
        }
    }
}
