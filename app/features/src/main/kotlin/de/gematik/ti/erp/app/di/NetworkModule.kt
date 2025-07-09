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
import de.gematik.ti.erp.app.interceptor.UrlRewriteInterceptor
import de.gematik.ti.erp.app.interceptor.UserAgentHeaderInterceptor
import de.gematik.ti.erp.app.logger.HttpAppLogger
import de.gematik.ti.erp.app.logger.HttpTerminalLogger
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
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

    bindSingleton {
        val endpointHelper = instance<EndpointHelper>()
        val baseHttpUrl = endpointHelper.eRezeptServiceUri.toHttpUrl()
        UrlRewriteInterceptor(baseHttpUrl)
    }

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
        rationale = """
            We use OkHttp with a layered interceptor chain to enforce token auth, 
            certificate transparency, VAU channel encryption, consistent User-Agent & API key headers, 
            URL rewriting for pagination, and selective logging for both inner and outer requests.
        """,
        codeLines = 50
    )
    // ERP Service HTTP client binding
    bindSingleton {

        // Core OkHttpClient builder
        val clientBuilder = instance<OkHttpClient>().newBuilder().apply {
            // Disable HTTP response caching in this client
            cache(null)
        }

        // Interceptor instances from DI
        val endpointHelper = instance<EndpointHelper>()
        val urlRewriteInterceptor = instance<UrlRewriteInterceptor>() // normalize dynamic URLs to base host
        val bearerInterceptor = instance<BearerHeaderInterceptor>() // adds Bearer token header, handles refresh
        val innerLoggingInterceptor = instance<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag, "[inner request]" to true)
        val vauChannelInterceptor = instance<VauChannelInterceptor>() // secures FD communication via VAU channel
        val userAgentInterceptor = instance<UserAgentHeaderInterceptor>() // adds app/version & device info header
        val apiKeyInterceptor = instance<ErpApiKeyHeaderInterceptor>() // adds mandatory API key header
        val outerLoggingInterceptor = instance<Pair<String, Boolean>, Interceptor>(PrefixedLoggerTag, "[outer request]" to false)

        // Assemble the interceptor chain in order:
        clientBuilder.apply {
            // 1) Rewrite any server-provided absolute URLs back to our base host
            addInterceptor(urlRewriteInterceptor)
            // 2) Inject the current Bearer token, handle 401 refresh
            addInterceptor(bearerInterceptor)
            // 3) Log internal requests for debugging (prefix [inner request])
            addInterceptor(innerLoggingInterceptor)
            // 4) VAU channel encryption for Fachdienst communications
            addInterceptor(vauChannelInterceptor)
            // 5) Add User-Agent and development headers for outer calls
            addInterceptor(userAgentInterceptor)
            // 6) Add ERP-specific API key header
            addInterceptor(apiKeyInterceptor)
            // 7) Enforce certificate transparency on all TLS connections
            addCertificateTransparencyInterceptor()
            // 8) Log external/outer requests (prefix [outer request])
            addInterceptor(outerLoggingInterceptor)
            // Optional: enable full HTTP logging in internal debug builds
            if (BuildConfigExtension.isInternalDebug) {
                val context = instance<Context>()
                clientBuilder.addInterceptor(endpointHelper.getHttpLoggingInterceptor(context))
            }
        }

        // Build Retrofit with this client and register the ERP service API interface
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
