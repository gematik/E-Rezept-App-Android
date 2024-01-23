/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

private const val MaxLogEntries = 5000
private const val LogEntriesToDelete = 100

class VisibleDebugTree : Antilog() {
    private val _rotatingLog = MutableStateFlow<List<String>>(emptyList())
    val rotatingLog: StateFlow<List<String>>
        get() = _rotatingLog

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        _rotatingLog.update {
            if (it.size > MaxLogEntries + LogEntriesToDelete) {
                it.drop(LogEntriesToDelete)
            } else {
                it
            } + buildString {
                val lvl = when (priority) {
                    LogLevel.VERBOSE -> "V"
                    LogLevel.DEBUG -> "D"
                    LogLevel.INFO -> "I"
                    LogLevel.WARNING -> "W"
                    LogLevel.ERROR -> "E"
                    LogLevel.ASSERT -> "A"
                }
                append("${ tag ?: "unknown" } $lvl: ${message ?: ""}")
                throwable?.run {
                    append("\n")
                    append(throwable.stackTraceToString())
                }
            }
        }
    }
}
