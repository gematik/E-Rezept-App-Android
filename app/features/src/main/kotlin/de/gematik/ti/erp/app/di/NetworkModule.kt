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

package de.gematik.ti.erp.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.PharmacyRedeemService
import de.gematik.ti.erp.app.api.PharmacySearchService
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.interceptor.ApiKeyHeaderInterceptor
import de.gematik.ti.erp.app.interceptor.BearerHeaderInterceptor
import de.gematik.ti.erp.app.interceptor.PharmacyRedeemInterceptor
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
import okhttp3.Interceptor
import okhttp3.Response
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

class AuditEventFilteredHttpLoggingInterceptor(
    private val loggingInterceptor: HttpLoggingInterceptor
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        loggingInterceptor.intercept(chain)
}

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

@Requirement(
    "A_19187",
    "A_19739",
    "A_19938-01#1",
    "A_20033",
    "A_20206-01",
    "A_20283-01#3",
    "A_20529-01",
    "A_20606",
    "A_20608",
    "A_20607",
    "A_20609",
    "A_20617-01#1",
    "A_20618",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Any connection to the IDP or the ERP service uses this configuration."
)
@Requirement(
    "GS-A_5035",
    "GS-A_4387",
    "GS-A_4385",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Any connection to the IDP or the ERP service uses this configuration."
)
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
    @Requirement(
        "O.Ntwk_1#2",
        "O.Ntwk_2#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Bind the connection specification."
    )
    @Requirement(
        "O.Ntwk_3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "We use OkHttp for network communication"
    )
    @Requirement(
        "GS-A_5322",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "We initialize our okhttp client as singleton. Thus, we support TLS resumption, it is handled by " +
            "okhttp. See https://square.github.io/okhttp/4.x/okhttp/okhttp3/-connection/ for more details."
    )
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

    bindMultiton<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag) { (tagSuffix, withBody) ->
        AuditEventFilteredHttpLoggingInterceptor(
            HttpLoggingInterceptor(NapierLogger(tagSuffix)).also {
                if (BuildKonfig.INTERNAL) {
                    if (withBody) {
                        it.setLevel(HttpLoggingInterceptor.Level.BODY)
                    } else {
                        it.setLevel(HttpLoggingInterceptor.Level.HEADERS)
                    }
                }
            }
        )
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
            instance<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag, "[inner request]" to true)
        val outerLoggingInterceptor =
            instance<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag, "[outer request]" to false)

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

        if (BuildKonfig.INTERNAL) {
            clientBuilder.addInterceptor(PharmacyRedeemInterceptor())
        }

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

@Requirement(
    "A_20206-01",
    "A_17322",
    "A_18464",
    "A_18467",
    "A_21332",
    "A_21275-01",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
)
@Requirement(
    "GS-A_4357-2#1",
    "GS-A_4361-2#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Cipher Suites regarding sha256WithRsaEncryption are listed below, see RSA specific cipher suites."
)
@Requirement(
    "O.Ntwk_1#1",
    "O.Ntwk_2#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
)
@Requirement(
    "GS-A_5035#1",
    "GS-A_4385#1",
    "GS-A_4387#1",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
)
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
