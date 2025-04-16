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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.logger.model

import kotlinx.serialization.Serializable

// Har models for the HAR format (HTTP Archive)

@Serializable
data class Har(
    val log: Log
)

@Serializable
data class Log(
    val version: String,
    val creator: Creator,
    val pages: List<Page>,
    val entries: List<Entry>
)

@Serializable
data class Creator(
    val name: String,
    val version: String
)

@Serializable
data class Page(
    val startedDateTime: String,
    val id: String,
    val title: String,
    val pageTimings: PageTimings
)

@Serializable
data class PageTimings(
    val onContentLoad: Long? = null,
    val onLoad: Long? = null
)

@Serializable
data class Entry(
    val startedDateTime: String,
    val time: Long,
    val request: Request,
    val response: Response,
    val cache: Cache,
    val timings: Timings
)

@Serializable
data class Request(
    val method: String,
    val url: String,
    val httpVersion: String,
    val headers: List<Header>,
    val queryString: List<QueryString>,
    val headersSize: Long = -1,
    val bodySize: Long = -1
)

@Serializable
data class Response(
    val status: Int,
    val statusText: String,
    val httpVersion: String,
    val headers: List<Header>,
    val content: Content,
    val redirectURL: String = "",
    val headersSize: Long = -1,
    val bodySize: Long = -1
)

@Serializable
data class Header(
    val name: String,
    val value: String
)

@Serializable
data class QueryString(
    val name: String,
    val value: String
)

@Serializable
data class Content(
    val size: Long,
    val mimeType: String,
    val text: String? = null,
    val encoding: String? = null
)

@Serializable
data class Cache(
    val beforeRequest: CacheDetail? = null,
    val afterRequest: CacheDetail? = null
)

@Serializable
data class CacheDetail(
    val expires: String? = null,
    val lastAccess: String? = null,
    val eTag: String? = null,
    val hitCount: Int? = null
)

@Serializable
data class Timings(
    val send: Long,
    val wait: Long,
    val receive: Long,
    val dns: Long? = null,
    val connect: Long? = null,
    val ssl: Long? = null
)
