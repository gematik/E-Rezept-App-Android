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

package de.gematik.ti.erp.app.logger.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.nanoseconds

@Serializable
data class LogEntry(
    val timestamp: String,
    val request: RequestLog,
    val response: ResponseLog,
    val timings: TimingsLog
) {
    companion object {
        fun LogEntry.toJson(): String {
            return SafeJson.value.encodeToString(this)
        }
    }
}

@Serializable
data class RequestLog(
    val method: String,
    val url: String,
    val headers: List<HeaderLog>
) {
    companion object {
        fun RequestLog.toJson(): String {
            return SafeJson.value.encodeToString(this)
        }
    }
}

@Serializable
data class ResponseLog(
    val status: Int,
    val statusText: String,
    val headers: List<HeaderLog>,
    val content: ContentLog
) {
    companion object {
        fun ResponseLog.toJson(): String {
            return SafeJson.value.encodeToString(this)
        }
    }
}

@Serializable
data class HeaderLog(
    val name: String,
    val value: String
)

@Serializable
data class ContentLog(
    val mimeType: String,
    val text: String
)

@Serializable
data class TimingsLog(
    val send: Long,
    val wait: Long,
    val receive: Long
) {
    fun totalTime() = send + wait + receive
    fun send() = send.nanoseconds
    fun wait() = wait.nanoseconds
    fun receive() = receive.nanoseconds
}
