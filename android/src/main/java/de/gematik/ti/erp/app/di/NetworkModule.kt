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

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.PharmacyRedeemService
import de.gematik.ti.erp.app.api.PharmacySearchService
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.interceptor.ApiKeyHeaderInterceptor
import de.gematik.ti.erp.app.interceptor.BearerHeaderInterceptor
import de.gematik.ti.erp.app.interceptor.PharmacySearchInterceptor
import de.gematik.ti.erp.app.interceptor.UserAgentHeaderInterceptor
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindMultiton
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

private const val HTTP_CONNECTION_TIMEOUT = 10000L
private const val HTTP_READ_TIMEOUT = 10000L
private const val HTTP_WRITE_TIMEOUT = 10000L

class NapierLogger(tagSuffix: String? = null) : HttpLoggingInterceptor.Logger {
    private val tag = if (tagSuffix != null) {
        "OkHttp $tagSuffix"
    } else {
        "OkHttp"
    }

    override fun log(message: String) {
        Napier.d(message, tag = tag)
    }
}

const val PrefixedLoggerTag = "PrefixedLogger"
const val JsonConverterFactoryTag = "JsonConverterFactory"
const val JsonFhirConverterFactoryTag = "JsonFhirConverterFactoryTag"

@OptIn(ExperimentalSerializationApi::class)
val networkModule = DI.Module("Network Module") {
    bindInstance {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
    bindSingleton(JsonConverterFactoryTag) { instance<Json>().asConverterFactory("application/json".toMediaType()) }
    bindSingleton(JsonFhirConverterFactoryTag) {
        instance<Json>().asConverterFactory("application/json+fhir".toMediaType())
    }
    bindSingleton {
        OkHttpClient.Builder()
            .connectTimeout(
                timeout = HTTP_CONNECTION_TIMEOUT,
                unit = TimeUnit.MILLISECONDS
            )
            .readTimeout(
                timeout = HTTP_READ_TIMEOUT,
                unit = TimeUnit.MILLISECONDS
            )
            .writeTimeout(
                timeout = HTTP_WRITE_TIMEOUT,
                unit = TimeUnit.MILLISECONDS
            )
            .connectionSpecs(getConnectionSpec())
            .build()
    }
    bindSingleton { UserAgentHeaderInterceptor() }
    bindSingleton { ApiKeyHeaderInterceptor(instance()) }
    bindSingleton { BearerHeaderInterceptor(instance()) }

    bindProvider {
        HttpLoggingInterceptor(NapierLogger()).also {
            it.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    bindMultiton<Pair<String, Boolean>, HttpLoggingInterceptor>(PrefixedLoggerTag) { (tagSuffix, withBody) ->
        HttpLoggingInterceptor(NapierLogger(tagSuffix)).also {
            if (BuildKonfig.INTERNAL) {
                if (withBody) {
                    it.setLevel(HttpLoggingInterceptor.Level.BODY)
                } else {
                    it.setLevel(HttpLoggingInterceptor.Level.HEADERS)
                }
            }
        }
    }

    // IDP Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val loggingInterceptor = instance<HttpLoggingInterceptor>()

        val client = clientBuilder
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .followRedirects(false)
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(endpointHelper.idpServiceUri)
            .addConverterFactory(JWSConverterFactory())
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(IdpService::class.java)
    }

    // ERP Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val vauChannelInterceptor = instance<VauChannelInterceptor>()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val bearerInterceptor = instance<BearerHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()
        val innerLoggingInterceptor =
            instance<Pair<String, Boolean>, HttpLoggingInterceptor>(PrefixedLoggerTag, "[inner request]" to true)
        val outerLoggingInterceptor =
            instance<Pair<String, Boolean>, HttpLoggingInterceptor>(PrefixedLoggerTag, "[outer request]" to false)

        clientBuilder.cache(null)

        clientBuilder.addInterceptor(bearerInterceptor)

        clientBuilder.addInterceptor(innerLoggingInterceptor)

        clientBuilder.addInterceptor(vauChannelInterceptor)

        // user agent & dev headers at outer request
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(apiKeyInterceptor)

        clientBuilder.addInterceptor(outerLoggingInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.eRezeptServiceUri)
            .addConverterFactory(instance(JsonFhirConverterFactoryTag))
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(ErpService::class.java)
    }

    // The VAU service is only used to get CertList & OCSPList and NOT to post to the VAU endpoint
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()
        val loggingInterceptor = instance<HttpLoggingInterceptor>()

        clientBuilder.addInterceptor(apiKeyInterceptor)
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(loggingInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.eRezeptServiceUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(VauService::class.java)
    }

    // Pharmacy Redeem Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val loggingInterceptor = instance<HttpLoggingInterceptor>()

        clientBuilder
            .addInterceptor(loggingInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl("https://localhost") // unused but required
            .build()
            .create(PharmacyRedeemService::class.java)
    }

    // Pharmacy Search Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val endpointHelper = instance<EndpointHelper>()
        val loggingInterceptor = instance<HttpLoggingInterceptor>()

        clientBuilder
            .addInterceptor(PharmacySearchInterceptor(instance()))
            .addInterceptor(loggingInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.pharmacySearchBaseUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(PharmacySearchService::class.java)
    }
}

private fun getConnectionSpec(): List<ConnectionSpec> = ConnectionSpec
    .Builder(ConnectionSpec.RESTRICTED_TLS)
    .tlsVersions(
        TlsVersion.TLS_1_2,
        TlsVersion.TLS_1_3
    )
    .cipherSuites(
        // TLS 1.2
        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
        // TLS 1.3
        CipherSuite.TLS_AES_128_GCM_SHA256,
        CipherSuite.TLS_AES_256_GCM_SHA384,
        CipherSuite.TLS_CHACHA20_POLY1305_SHA256
    )
    .build()
    .let {
        listOf(it)
    }
