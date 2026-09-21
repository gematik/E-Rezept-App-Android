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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.idp.api.models.JWSKey
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.repository.JWSDiscoveryDocument
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class JWSConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? =
        when (type) {
            JsonWebSignature::class.javaObjectType -> JsonWebSignatureConverter()
            JWSDiscoveryDocument::class.javaObjectType -> JWSDiscoveryDocumentConverter()
            JWSKey::class.javaObjectType -> JWSKeyConverter()
            JWSPublicKey::class.javaObjectType -> JWSPublicKeyConverter()
            else -> null
        }
}

class JWSDiscoveryDocumentConverter : Converter<ResponseBody, JWSDiscoveryDocument> {
    override fun convert(value: ResponseBody): JWSDiscoveryDocument {
        return value.use { response ->
            JWSDiscoveryDocument(
                JsonWebStructure.fromCompactSerialization(
                    response.string()
                ) as JsonWebSignature
            )
        }
    }
}

class JsonWebSignatureConverter : Converter<ResponseBody, JsonWebSignature> {
    override fun convert(value: ResponseBody): JsonWebSignature {
        return value.use { response ->
            JsonWebStructure.fromCompactSerialization(
                response.string()
            ) as JsonWebSignature
        }
    }
}

class JWSKeyConverter : Converter<ResponseBody, JWSKey> {
    override fun convert(value: ResponseBody): JWSKey {
        return value.use { response ->
            JWSKey(
                JsonWebKey.Factory.newJwk(response.string())
            )
        }
    }
}

class JWSPublicKeyConverter : Converter<ResponseBody, JWSPublicKey> {

    override fun convert(value: ResponseBody): JWSPublicKey {
        val body = value.use { it.string() }

        /**
         * Some IDP responses might contain null values in JWK fields which jose4j's parser
         * doesn't handle gracefully. We sanitize the JSON by removing nulls, provided it's
         * not a JWS compact serialization (which contains dots and isn't a plain JSON object).
         */
        val processedBody = if (body.isCompactSerialization()) body else body.sanitizeJwkJson()

        val jwk = parseJwkWithFallback(processedBody)
        return JWSPublicKey(jwk)
    }

    private fun String.isCompactSerialization(): Boolean = count { it == '.' } == 2

    private fun String.sanitizeJwkJson(): String {
        return try {
            val element = Json.parseToJsonElement(this)
            if (element is JsonObject) {
                JsonObject(element.filterValues { it !is JsonNull }).toString()
            } else this
        } catch (_: Exception) {
            this
        }
    }

    private fun parseJwkWithFallback(body: String): PublicJsonWebKey {
        return try {
            parseJwk(body, forceOpposite = false)
        } catch (e: Exception) {
            // Fallback: try the opposite format once
            parseJwk(body, forceOpposite = true)
        }
    }

    private fun parseJwk(body: String, forceOpposite: Boolean): PublicJsonWebKey {
        val shouldParseAsJws = body.isCompactSerialization() xor forceOpposite

        return if (shouldParseAsJws) {
            // Parse compact JWS: extract payload first
            val jws = JsonWebStructure.fromCompactSerialization(body) as JsonWebSignature
            PublicJsonWebKey.Factory.newPublicJwk(jws.payload)
        } else {
            // Parse raw JWK JSON
            PublicJsonWebKey.Factory.newPublicJwk(body)
        }
    }
}
