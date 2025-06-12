/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.debugsettings.logger.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.logger.SessionLogHolder
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.rememberInstance
import java.io.File

class LoggerScreenController(
    private val logHolder: SessionLogHolder
) : Controller() {
    val httpLogs by lazy {
        logHolder.http
            .stateIn(
                scope = controllerScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    }

    fun saveHarToFile(harContent: String, filePath: String) {
        try {
            File(filePath).bufferedWriter().use { it.write(harContent) }
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
fun rememberLoggerScreenController(): LoggerScreenController {
    val logHolder by rememberInstance<SessionLogHolder>()
    return remember {
        LoggerScreenController(logHolder)
    }
}

data class FileSavingException(override val message: String) : Exception(message)
