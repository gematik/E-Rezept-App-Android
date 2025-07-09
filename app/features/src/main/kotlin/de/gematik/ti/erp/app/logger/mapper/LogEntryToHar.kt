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

package de.gematik.ti.erp.app.logger.mapper

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.logger.model.Cache
import de.gematik.ti.erp.app.logger.model.Content
import de.gematik.ti.erp.app.logger.model.Creator
import de.gematik.ti.erp.app.logger.model.Entry
import de.gematik.ti.erp.app.logger.model.Har
import de.gematik.ti.erp.app.logger.model.Header
import de.gematik.ti.erp.app.logger.model.Log
import de.gematik.ti.erp.app.logger.model.LogEntry
import de.gematik.ti.erp.app.logger.model.Request
import de.gematik.ti.erp.app.logger.model.Response
import de.gematik.ti.erp.app.logger.model.Timings
import kotlinx.serialization.json.Json

fun List<LogEntry>.toHar() = Har(
    log = Log(
        version = "1.2",
        creator = Creator(
            name = "ErpApp Android Logger",
            version = BuildKonfig.VERSION_NAME
        ),
        pages = emptyList(),
        entries = this.toHarEntries()
    )
)

private fun List<LogEntry>.toHarEntries(): List<Entry> =
    this.map { logEntry ->
        Entry(
            startedDateTime = logEntry.timestamp,
            time = logEntry.timings.totalTime(),
            request = Request(
                httpVersion = "HTTP/1.1",
                method = logEntry.request.method,
                url = logEntry.request.url,
                headers = logEntry.request.headers.map { header ->
                    Header(
                        name = header.name,
                        value = header.value
                    )
                },
                queryString = emptyList()
            ),
            response = Response(
                httpVersion = "HTTP/1.1",
                status = logEntry.response.status,
                statusText = logEntry.response.statusText,
                headers = logEntry.response.headers.map { header ->
                    Header(
                        name = header.name,
                        value = header.value
                    )
                },
                content = Content(
                    size = logEntry.response.content.text.length.toLong(),
                    mimeType = logEntry.response.content.mimeType,
                    text = logEntry.response.content.text
                )
            ),
            cache = Cache(),
            timings = Timings(
                send = logEntry.timings.send,
                wait = logEntry.timings.wait,
                receive = logEntry.timings.receive
            )
        )
    }

private val json = Json { prettyPrint = true }

fun Har.toJson() = json.encodeToString(this)
