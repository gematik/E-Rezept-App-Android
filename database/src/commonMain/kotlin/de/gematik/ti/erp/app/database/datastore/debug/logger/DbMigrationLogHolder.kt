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

package de.gematik.ti.erp.app.database.datastore.debug.logger

import de.gematik.ti.erp.app.database.api.debug.DbMigrationLogsLocalDataSource
import de.gematik.ti.erp.app.debug.model.DbMigrationLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DbMigrationLogHolder(
    private val dbMigrationLogsLocalDataSource: DbMigrationLogsLocalDataSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val switchOnLogs: Boolean = false
) {
    private val _dbMigrationLogEntries = MutableStateFlow<List<DbMigrationLogEntry>>(emptyList())
    val dbMigrationLogEntries: StateFlow<List<DbMigrationLogEntry>> = _dbMigrationLogEntries

    init {
        scope.launch(Dispatchers.IO) {
            if (switchOnLogs) {
                dbMigrationLogsLocalDataSource.dbMigrationLogs.collect { entries ->
                    _dbMigrationLogEntries.update { entries }
                }
            }
        }
    }

    fun addLog(entry: DbMigrationLogEntry) {
        if (!switchOnLogs) return

        scope.launch(Dispatchers.IO) {
            _dbMigrationLogEntries.update { (it + entry).takeLast(MAX_LOGS) }
            dbMigrationLogsLocalDataSource.saveDbMigrationLogs(_dbMigrationLogEntries.value)
        }
    }

    fun logOperation(operation: String, usesRoom: Boolean) {
        if (!switchOnLogs) {
            scope.launch {
                addLog(DbMigrationLogEntry(operation = operation, usesRoom = usesRoom))
            }
        }
    }

    suspend fun resetLogs() {
        if (switchOnLogs) {
            _dbMigrationLogEntries.update { emptyList() }
            dbMigrationLogsLocalDataSource.saveDbMigrationLogs(emptyList())
        }
    }

    companion object {
        private const val MAX_LOGS = 50
    }
}
