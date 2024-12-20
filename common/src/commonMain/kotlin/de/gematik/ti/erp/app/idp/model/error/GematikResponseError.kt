/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.idp.model.error

import de.gematik.ti.erp.app.idp.extension.extractNullableQueryParameter
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.net.URI

/**
 * Error obtained when the GiD (mostly) and sometimes fast-track process does not go correctly
 */
data class GematikResponseError(
    val error: String,
    val gematikCode: String,
    val gematikTimestamp: String,
    val gematikUuid: String,
    val gematikErrorText: String
) : ResponseError(error) {
    companion object {

        fun emptyResponseError(
            error: String? = null,
            code: String? = null
        ) = GematikResponseError(
            error = error ?: "Unable to decode error",
            gematikCode = code ?: "Unable to decode code",
            gematikTimestamp = "0",
            gematikUuid = "Unable to decode uuid",
            gematikErrorText = "Unable to decode error text"
        )

        fun URI.parsedUriToError() = GematikResponseError(
            error = extractNullableQueryParameter("error") ?: "Unable to decode error",
            gematikCode = extractNullableQueryParameter("gematik_code") ?: "Unable to decode code",
            gematikTimestamp = extractNullableQueryParameter("gematik_timestamp") ?: "0",
            gematikUuid = extractNullableQueryParameter("gematik_uuid") ?: "Unable to decode uuid",
            gematikErrorText = extractNullableQueryParameter("gematik_error_text") ?: "Unable to decode error text"
        )

        fun GematikResponseError.makeErrorPretty(): String {
            val errorText = gematikErrorText.replace("+", " ")
            val error = error.replace("_", " ")
            return "\u25AA Code: $gematikCode\n" +
                "\u25AA Error: ($error) $errorText\n" +
                "\u25AA Timestamp: $gematikTimestamp\n" +
                "\u25AA Uuid: $gematikUuid\n"
        }

        fun GematikResponseError.prettyErrorText() = gematikErrorText.replace("+", " ")
        fun GematikResponseError.prettyErrorCode() = error.replace("_", " ")

        @Suppress("ReturnCount")
        fun String.parseToError(): GematikResponseError {
            val defaultError = GematikResponseError(
                error = "Unable to decode error",
                gematikCode = "Unable to decode code",
                gematikTimestamp = "0",
                gematikUuid = "Unable to decode uuid",
                gematikErrorText = "Unable to decode error text"
            )
            try {
                val jsonObject = Json.parseToJsonElement(this).jsonObject
                return GematikResponseError(
                    error = jsonObject["error"].toString(),
                    gematikCode = jsonObject["gematik_code"].toString(),
                    gematikTimestamp = jsonObject["gematik_timestamp"].toString(),
                    gematikUuid = jsonObject["gematik_uuid"].toString(),
                    gematikErrorText = jsonObject["gematik_error_text"].toString()
                )
            } catch (e: SerializationException) {
                Napier.e { "exception swallowed $e" }
                return defaultError
            } catch (e: IllegalArgumentException) {
                Napier.e { "exception swallowed $e" }
                return defaultError
            }
        }
    }
}
