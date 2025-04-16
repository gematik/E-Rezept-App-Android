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

package de.gematik.ti.erp.app.logger

import de.gematik.ti.erp.app.logger.model.ContentLog
import de.gematik.ti.erp.app.logger.model.HeaderLog
import de.gematik.ti.erp.app.logger.model.LogEntry
import de.gematik.ti.erp.app.logger.model.RequestLog
import de.gematik.ti.erp.app.logger.model.ResponseLog
import de.gematik.ti.erp.app.logger.model.TimingsLog
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class HttpAppLogger(
    private val sessionLog: SessionLogHolder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        val response: Response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            Napier.e { "error on HttpAppLogger ${e.stackTraceToString()}" }
            throw e
        }

        val endTime = System.nanoTime()

        // Peek the response body without consuming it since some response are only one time read
        val responseBody = response.peekBody(Long.MAX_VALUE)

        val logEntry = LogEntry(
            timestamp = Clock.System.now().toString(),
            request = RequestLog(
                method = request.method,
                url = request.url.toString(),
                headers = request.headers.map { header ->
                    HeaderLog(
                        name = header.first,
                        value = header.second
                    )
                }
            ),
            response = ResponseLog(
                status = response.code,
                statusText = response.message,
                headers = response.headers.map { header ->
                    HeaderLog(
                        name = header.first,
                        value = header.second
                    )
                },
                content = ContentLog(
                    mimeType = response.body?.contentType()?.type ?: "",
                    text = response.body?.string().orEmpty()
                )
            ),
            timings = TimingsLog(
                send = startTime,
                wait = endTime - startTime,
                receive = System.nanoTime() - endTime
            )
        )

        // add this to the logs
        sessionLog.http.value.add(logEntry)

        // return the recreated response with the peeked body to ensure it is still available for further consumption
        return response.newBuilder()
            .body(responseBody.bytes().toResponseBody(responseBody.contentType()))
            .build()
    }
}
