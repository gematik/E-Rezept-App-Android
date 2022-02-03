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

package de.gematik.ti.erp.app.network.di

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.ti.erp.app.Constants
import de.gematik.ti.erp.app.JWSConverterFactory
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.applicationScope
import de.gematik.ti.erp.app.common.pinning.buildCertificatePinner
import de.gematik.ti.erp.app.core.FhirConverterFactory
import de.gematik.ti.erp.app.core.NapierLogger
import de.gematik.ti.erp.app.core.PrefixedLogger
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.network.interceptor.ApiKeyHeaderInterceptor
import de.gematik.ti.erp.app.network.interceptor.BearerHeaderInterceptor
import de.gematik.ti.erp.app.network.interceptor.UserAgentHeaderInterceptor
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
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindInstance
import org.kodein.di.bindMultiton
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.bindings.ScopeCloseable
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import retrofit2.Converter
import retrofit2.Retrofit

@JvmInline
value class ScopedOkHttpClient(val client: OkHttpClient) : ScopeCloseable {
    override fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}

@OptIn(ExperimentalSerializationApi::class)
val networkModule = DI.Module("Network Module") {
    bindInstance { Json { ignoreUnknownKeys = true } }
    bindSingleton("jsonConverterFactory") { instance<Json>().asConverterFactory("application/json".toMediaType()) }
    bind {
        scoped(applicationScope).singleton {
            ScopedOkHttpClient(
                OkHttpClient.Builder()
                    .certificatePinner(buildCertificatePinner())
                    .connectionSpecs(
                        listOf(
                            ConnectionSpec
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
                        )
                    )
                    .build()
            )
        }
    }
    bindSingleton<IParser> { FhirContext.forR4().newJsonParser() }
    bindSingleton { UserAgentHeaderInterceptor() }
    bindSingleton { ApiKeyHeaderInterceptor() }
    bindSingleton { BearerHeaderInterceptor(instance()) }

    bindProvider {
        HttpLoggingInterceptor(NapierLogger()).also {
            it.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    bindMultiton<String, HttpLoggingInterceptor>("prefixed") { prefix ->
        HttpLoggingInterceptor(PrefixedLogger(prefix)).also {
            it.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    bindSingleton {
        val clientBuilder = instance<ScopedOkHttpClient>().client.newBuilder()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val loggingInterceptor = instance<HttpLoggingInterceptor>()

        clientBuilder
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .followRedirects(false)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(Constants.IDP.serviceUri)
            .addConverterFactory(JWSConverterFactory())
            .addConverterFactory(instance<Converter.Factory>("jsonConverterFactory"))
            .build()
            .create(IdpService::class.java)
    }

    bindSingleton {
        val clientBuilder = instance<ScopedOkHttpClient>().client.newBuilder()
        val fhirParser = instance<IParser>()
        val vauChannelInterceptor = instance<VauChannelInterceptor>()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val bearerInterceptor = instance<BearerHeaderInterceptor>()
        val innerLoggingInterceptor = instance<String, HttpLoggingInterceptor>("prefixed", "inner request")
        val outerLoggingInterceptor = instance<String, HttpLoggingInterceptor>("prefixed", "inner request")

        clientBuilder.addInterceptor(bearerInterceptor)

        clientBuilder.addInterceptor(innerLoggingInterceptor)

        clientBuilder.addInterceptor(vauChannelInterceptor)

        // user agent & dev headers at outer request
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(apiKeyInterceptor)

        clientBuilder.addInterceptor(outerLoggingInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(Constants.ERP.serviceUri)
            .addConverterFactory(FhirConverterFactory.create(fhirParser))
            .addConverterFactory(instance<Converter.Factory>("jsonConverterFactory"))
            .build()
            .create(ErpService::class.java)
    }

    // The VAU service is only used to get CertList & OCSPList and NOT to post to the VAU endpoint
    bindSingleton {
        val clientBuilder = instance<ScopedOkHttpClient>().client.newBuilder()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val loggingInterceptor = instance<HttpLoggingInterceptor>()

        clientBuilder.addInterceptor(apiKeyInterceptor)
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(loggingInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(Constants.ERP.serviceUri)
            .addConverterFactory(instance<Converter.Factory>("jsonConverterFactory"))
            .build()
            .create(VauService::class.java)
    }
}
