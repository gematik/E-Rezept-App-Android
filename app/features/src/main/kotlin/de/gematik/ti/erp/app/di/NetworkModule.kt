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

package de.gematik.ti.erp.app.di

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
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
import de.gematik.ti.erp.app.logger.HttpAppLogger
import de.gematik.ti.erp.app.logger.HttpTerminalLogger
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindMultiton
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import retrofit2.Retrofit
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

// A_20617-01,
@Requirement(
    "A_20033#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Any connection to the IDP or the ERP service uses this configuration."
)
@Requirement(
    "A_19938-01#1",
    "A_19938-01#2",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Any connection to the IDP or the ERP service uses this configuration."
)
@Requirement(
    "A_20623#4",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Any connection to the IDP or the ERP service uses this configuration"
)
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
        "A_20283-01#4",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Any connection to the IDP or the ERP service uses this configuration."
    )
    @Requirement(
        "O.Ntwk_2#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Bind the connection specification."
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

    // Two loggers are used to log the HTTP requests and responses. One is used for the terminal and the other for the app.
    bindProvider { HttpLoggingInterceptor(HttpTerminalLogger()).also { it.setLevel(HttpLoggingInterceptor.Level.BODY) } }
    bindProvider { HttpAppLogger(instance()) }

    bindMultiton<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag) { (tagSuffix, withBody) ->
        AuditEventFilteredHttpLoggingInterceptor(
            HttpLoggingInterceptor(HttpTerminalLogger(tagSuffix)).also {
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

    @Requirement(
        "O.Ntwk_3#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "We use OkHttp with different interceptors to make it more secure."
    )
    // IDP Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val httpTerminalLogger = instance<HttpLoggingInterceptor>()
        val httpAppLogger = instance<HttpAppLogger>()

        val client = clientBuilder
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addCertificateTransparencyInterceptor()
            .addInterceptor(httpTerminalLogger)
            .addInterceptor(httpAppLogger)
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

    @Requirement(
        "O.Ntwk_3#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "We use OkHttp with different interceptors to make it more secure."
    )
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
        val httpAppLogger = instance<HttpAppLogger>()

        clientBuilder.cache(null)

        clientBuilder.addInterceptor(bearerInterceptor)

        clientBuilder.addInterceptor(innerLoggingInterceptor)

        clientBuilder.addInterceptor(vauChannelInterceptor)

        // user agent & dev headers at outer request
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(apiKeyInterceptor)

        clientBuilder.addCertificateTransparencyInterceptor()

        clientBuilder.addInterceptor(outerLoggingInterceptor)

        clientBuilder.addInterceptor(httpAppLogger)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.eRezeptServiceUri)
            .addConverterFactory(instance(JsonFhirConverterFactoryTag))
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(ErpService::class.java)
    }

    @Requirement(
        "O.Ntwk_3#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "We use OkHttp with different interceptors to make it more secure."
    )
    // The VAU service is only used to get CertList & OCSPList and NOT to post to the VAU endpoint
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()
        val apiKeyInterceptor = instance<ApiKeyHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()
        val httpAppLogger = instance<HttpAppLogger>()
        val httpTerminalLogger = instance<HttpLoggingInterceptor>()

        clientBuilder.addInterceptor(apiKeyInterceptor)
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addCertificateTransparencyInterceptor()
        clientBuilder.addInterceptor(httpTerminalLogger)
        clientBuilder.addInterceptor(httpAppLogger)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.eRezeptServiceUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(VauService::class.java)
    }

    @Requirement(
        "O.Ntwk_3#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "We use OkHttp with different interceptors to make it more secure."
    )
    // Pharmacy Redeem Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val httpAppLogger = instance<HttpAppLogger>()
        val httpTerminalLogger = instance<HttpLoggingInterceptor>()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()

        clientBuilder
            .addInterceptor(userAgentInterceptor)
            .addCertificateTransparencyInterceptor()
            .addInterceptor(httpTerminalLogger)
            .addInterceptor(httpAppLogger)

        if (BuildKonfig.INTERNAL) {
            clientBuilder.addInterceptor(PharmacyRedeemInterceptor())
        }

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl("https://localhost") // unused but required
            .build()
            .create(PharmacyRedeemService::class.java)
    }

    @Requirement(
        "O.Ntwk_3#5",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "We use OkHttp with different interceptors to make it more secure."
    )
    // Pharmacy Search Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>().newBuilder()
        val endpointHelper = instance<EndpointHelper>()
        val httpAppLogger = instance<HttpAppLogger>()
        val httpTerminalLogger = instance<HttpLoggingInterceptor>()
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>()

        clientBuilder
            .addInterceptor(PharmacySearchInterceptor(instance()))
            .addInterceptor(userAgentInterceptor)
            .addCertificateTransparencyInterceptor()
            .addInterceptor(httpTerminalLogger)
            .addInterceptor(httpAppLogger)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.pharmacySearchBaseUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(PharmacySearchService::class.java)
    }
}

@Requirement(
    "A_21332-02#1",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
)
@Requirement(
    "O.Ntwk_2#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
)
@Requirement(
    "O.Auth_12#2",
    "O.Resi_6#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The TLS specification is done here and using RESTRICTED_TLS. We inherently support tlsExtensions."
)
@Requirement(
    "A_20606#1",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Add TLS versioning through connectionSpecs.",
    codeLines = 6
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
        CipherSuite.TLS_AES_256_GCM_SHA384
    )
    .build()
    .let {
        listOf(it)
    }

private fun OkHttpClient.Builder.addCertificateTransparencyInterceptor() =
    addNetworkInterceptor(
        certificateTransparencyInterceptor {
            failOnError = true
        }
    )
