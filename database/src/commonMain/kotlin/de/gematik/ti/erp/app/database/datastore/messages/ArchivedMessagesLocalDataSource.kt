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

package de.gematik.ti.erp.app.database.datastore.messages

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val ARCHIVED_MESSAGES_DATA_SOURCE = "ArchivedMessages"

class ArchivedMessagesLocalDataSource(
    private val dataStore: DataStore<ArchivedMessagesEntitySchema>
) {
    val archivedMessageKeys: Flow<Set<String>> = dataStore.data.map { it.archivedMessageKeys }
    val hideCompleted: Flow<Boolean> = dataStore.data.map { it.hideCompleted }

    suspend fun archiveMessageKeys(messageKeys: Set<String>) {
        if (messageKeys.isEmpty()) {
            return
        }
        dataStore.updateData { schema ->
            schema.copy(archivedMessageKeys = schema.archivedMessageKeys + messageKeys)
        }
    }

    suspend fun unarchiveMessageKeys(messageKeys: Set<String>) {
        if (messageKeys.isEmpty()) {
            return
        }
        dataStore.updateData { schema ->
            schema.copy(archivedMessageKeys = schema.archivedMessageKeys - messageKeys)
        }
    }

    suspend fun setHideCompleted(hide: Boolean) {
        dataStore.updateData { schema ->
            schema.copy(hideCompleted = hide)
        }
    }
}
