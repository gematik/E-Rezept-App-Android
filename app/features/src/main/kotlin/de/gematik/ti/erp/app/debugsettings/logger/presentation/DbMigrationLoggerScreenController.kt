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

package de.gematik.ti.erp.app.debugsettings.logger.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.logger.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.logger.model.DbMigrationLogEntry.Companion.toJson
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.io.File

class DbMigrationLoggerScreenController(
    private val logHolder: DbMigrationLogHolder
) : Controller() {
    private val _dbMigrationLogEntries = MutableStateFlow<UiState<List<DbMigrationLogEntry>>>(UiState.Loading())
    val dbMigrationLogEntries: StateFlow<UiState<List<DbMigrationLogEntry>>> = _dbMigrationLogEntries

    private val _searchValue = MutableStateFlow("")
    val searchValue: StateFlow<String> = _searchValue

    val expandedListItems: MutableSet<String> = mutableSetOf()

    init {
        controllerScope.launch {
            try {
                combine(
                    logHolder.dbMigrationLogEntries,
                    searchValue
                ) { logEntries, search ->
                    logEntries.filter { it.toJson().contains(search) }
                }.onEmpty {
                    _dbMigrationLogEntries.update { UiState.Empty() }
                }
                    .collect {
                            logEntries ->
                        _dbMigrationLogEntries.update { UiState.Data(logEntries) }
                    }
            } catch (e: Exception) {
                _dbMigrationLogEntries.update { UiState.Error(e) }
            }
        }
    }

    fun changeSearch(searchInput: String) {
        _searchValue.update { searchInput }
    }

    fun resetSearch() {
        _searchValue.update { "" }
    }

    fun toggleListItems(uuid: String) {
        if (expandedListItems.contains(uuid)) {
            expandedListItems - uuid
        } else {
            expandedListItems + uuid
        }
    }

    fun saveLogs(dbMigrationLogEntry: DbMigrationLogEntry, filePath: String) {
        try {
            File(filePath).bufferedWriter().use { it.write(dbMigrationLogEntry.toString()) }
        } catch (e: Throwable) {
            Napier.e { "error savings logs ${e.stackTraceToString()}" }
            throw FileSavingException("Error saving logs: ${e.message}")
        }
    }

    fun resetLogs() {
        logHolder.resetLogs()
    }
}

@Composable
fun rememberDbMigrationLoggerScreenController(): DbMigrationLoggerScreenController {
    val logHolder by rememberInstance<DbMigrationLogHolder>()
    return remember {
        DbMigrationLoggerScreenController(logHolder)
    }
}
