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
import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.interceptor.UserAgentHeaderInterceptor
import de.gematik.ti.erp.app.logger.HttpAppLogger
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import java.util.concurrent.TimeUnit

private const val HTTP_CONNECTION_TIMEOUT = 10000L
private const val HTTP_READ_TIMEOUT = 10000L
private const val HTTP_WRITE_TIMEOUT = 10000L
internal const val REQUIRED_HTTP_CLIENT = "OkHttpClientWithInterceptors"

private val blockingDns = BlockingDns()

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
val clientBuilderModule = DI.Module("ClientBuilderModule") {
    bindSingleton(REQUIRED_HTTP_CLIENT) {
        val clientBuilder = OkHttpClient.Builder()
            .dns(blockingDns)
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
            .addInterceptor(instance<UserAgentHeaderInterceptor>())
            .addCertificateTransparencyInterceptor()
            .addInterceptor(instance<HttpLoggingInterceptor>())
            .addInterceptor(instance<HttpAppLogger>())

        if (BuildConfigExtension.isInternalDebug) {
            val context = instance<Context>()
            val endpointHelper = instance<EndpointHelper>()
            clientBuilder.addInterceptor(endpointHelper.getHttpLoggingInterceptor(context))
        }
        clientBuilder.build()
    }

    // fallback that if someone does not call the REQUIRED_HTTP_CLIENT, it still returns the default client
    bind<OkHttpClient>() with singleton {
        instance<OkHttpClient>(tag = REQUIRED_HTTP_CLIENT)
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

// Certificate Transparency is a security measure that helps protect against mis-issued certificates.
fun OkHttpClient.Builder.addCertificateTransparencyInterceptor() =
    addNetworkInterceptor(
        certificateTransparencyInterceptor {
            failOnError = true
        }
    )
