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

package de.gematik.ti.erp.app.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.PharmacyRedeemService
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.interceptor.BearerHeaderInterceptor
import de.gematik.ti.erp.app.interceptor.ErpApiKeyHeaderInterceptor
import de.gematik.ti.erp.app.interceptor.PharmacyRedeemInterceptor
import de.gematik.ti.erp.app.interceptor.UserAgentHeaderInterceptor
import de.gematik.ti.erp.app.logger.HttpAppLogger
import de.gematik.ti.erp.app.logger.HttpTerminalLogger
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindMultiton
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import retrofit2.Retrofit

class AuditEventFilteredHttpLoggingInterceptor(
    private val loggingInterceptor: HttpLoggingInterceptor
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        loggingInterceptor.intercept(chain)
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

    bindSingleton { UserAgentHeaderInterceptor(instance()) }
    bindSingleton { ErpApiKeyHeaderInterceptor(instance()) }
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
        val clientBuilder = instance<OkHttpClient>(REQUIRED_HTTP_CLIENT).newBuilder()
        val endpointHelper = instance<EndpointHelper>()
        val apiKeyInterceptor = instance<ErpApiKeyHeaderInterceptor>()

        val client = clientBuilder
            .addInterceptor(apiKeyInterceptor)
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
        val apiKeyInterceptor = instance<ErpApiKeyHeaderInterceptor>()
        val bearerInterceptor = instance<BearerHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()
        val innerLoggingInterceptor =
            instance<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag, "[inner request]" to true)
        val outerLoggingInterceptor =
            instance<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag, "[outer request]" to false)

        clientBuilder.apply {
            cache(null)
            addInterceptor(bearerInterceptor)
            addInterceptor(innerLoggingInterceptor)
            addInterceptor(vauChannelInterceptor)

            // NOTE: user agent & dev headers at outer request
            addInterceptor(userAgentInterceptor)
            addInterceptor(apiKeyInterceptor)
            addCertificateTransparencyInterceptor()
            addInterceptor(outerLoggingInterceptor)
            // adding debug logger
            if (BuildConfigExtension.isInternalDebug) {
                val context = instance<Context>()
                clientBuilder.addInterceptor(endpointHelper.getHttpLoggingInterceptor(context))
            }
        }

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
        val clientBuilder = instance<OkHttpClient>(REQUIRED_HTTP_CLIENT).newBuilder()
        val apiKeyInterceptor = instance<ErpApiKeyHeaderInterceptor>()
        val endpointHelper = instance<EndpointHelper>()

        clientBuilder.addInterceptor(apiKeyInterceptor)

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
        val clientBuilder = instance<OkHttpClient>(REQUIRED_HTTP_CLIENT).newBuilder()

        if (BuildKonfig.INTERNAL) {
            clientBuilder.addInterceptor(PharmacyRedeemInterceptor())
        }

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl("https://localhost") // unused but required
            .build()
            .create(PharmacyRedeemService::class.java)
    }
}
