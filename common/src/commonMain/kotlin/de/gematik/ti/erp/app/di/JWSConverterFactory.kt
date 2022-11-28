/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.idp.api.models.JWSKey
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.repository.JWSDiscoveryDocument
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
        return JWSDiscoveryDocument(
            JsonWebStructure.fromCompactSerialization(
                value.string()
            ) as JsonWebSignature
        )
    }
}

class JsonWebSignatureConverter : Converter<ResponseBody, JsonWebSignature> {
    override fun convert(value: ResponseBody): JsonWebSignature {
        return JsonWebStructure.fromCompactSerialization(
            value.string()
        )as JsonWebSignature
    }
}

class JWSKeyConverter : Converter<ResponseBody, JWSKey> {
    override fun convert(value: ResponseBody): JWSKey {
        return JWSKey(
            JsonWebKey.Factory.newJwk(value.string())
        )
    }
}

class JWSPublicKeyConverter : Converter<ResponseBody, JWSPublicKey> {
    override fun convert(value: ResponseBody): JWSPublicKey {
        return JWSPublicKey(
            PublicJsonWebKey.Factory.newPublicJwk(value.string())
        )
    }
}
