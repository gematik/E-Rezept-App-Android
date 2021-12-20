/*
 * Copyright (c) 2021 gematik GmbH
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

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import ca.uhn.fhir.parser.IParser
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.FhirConverterFactory
import de.gematik.ti.erp.app.api.PharmacySearchService
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.models.JWSAdapter
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.interceptor.BearerHeadersInterceptor
import de.gematik.ti.erp.app.interceptor.PharmacySearchInterceptor
import de.gematik.ti.erp.app.interceptor.UserAgentHeaderInterceptor
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.api.model.OCSPAdapter
import de.gematik.ti.erp.app.vau.api.model.X509Adapter
import de.gematik.ti.erp.app.vau.api.model.X509ArrayAdapter
import de.gematik.ti.erp.app.vau.interceptor.VauChannelInterceptor
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

private const val HTTP_CONNECTION_TIMEOUT = 10000L
private const val HTTP_READ_TIMEOUT = 10000L
private const val HTTP_WRITE_TIMEOUT = 10000L
private const val NETWORK_SECURE_PREFS_FILE_NAME = "networkingSecurePrefs"
private const val NETWORK_PREFS_FILE_NAME = "networkingPrefs"
private const val MASTER_KEY_ALIAS = "netWorkMasterKey"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DevelopReleaseHeaderInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserAgentInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BearerInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkSecureSharedPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkSharedPreferences

@Module
@InstallIn(SingletonComponent::class)
class NetworkingModule {

    @Singleton
    @Provides
    fun idpService(
        baseClient: OkHttpClient,
        moshi: Moshi,
        @UserAgentInterceptor userAgentInterceptor: Interceptor,
        @DevelopReleaseHeaderInterceptor headersInterceptor: Interceptor,
        endpointHelper: EndpointHelper
    ): IdpService {
        val client = baseClient.newBuilder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().also {
                    if (BuildKonfig.INTERNAL) it.setLevel(HttpLoggingInterceptor.Level.BODY)
                }
            )
            .followRedirects(false)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(endpointHelper.idpServiceUri)
            .addConverterFactory(JWSConverterFactory())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    moshi.newBuilder().add(JWSAdapter()).add(X509ArrayAdapter()).build()
                )
            )
            .build()
            .create(IdpService::class.java)
    }

    @Named("userAgent")
    @Provides
    fun providesUserAgent(): String {
        return BuildKonfig.USER_AGENT
    }

    @UserAgentInterceptor
    @Provides
    fun providesUserAgentInterceptor(@Named("userAgent") userAgent: String): Interceptor =
        UserAgentHeaderInterceptor(userAgent)

    @BearerInterceptor
    @Provides
    fun providesBearerInterceptor(
        idpUseCase: IdpUseCase
    ): Interceptor =
        BearerHeadersInterceptor(idpUseCase)

    @Singleton
    @Provides
    fun eRpService(
        baseClient: OkHttpClient,
        fhirParser: IParser,
        @BearerInterceptor bearerInterceptor: Interceptor,
        @UserAgentInterceptor userAgentInterceptor: Interceptor,
        @DevelopReleaseHeaderInterceptor devHeadersInterceptor: Interceptor,
        endpointHelper: EndpointHelper,
        vauChannelInterceptor: VauChannelInterceptor
    ): ErpService {
        val clientBuilder = baseClient.newBuilder()
        clientBuilder.cache(null)

        clientBuilder.addInterceptor(bearerInterceptor)

        clientBuilder.addInterceptor(
            HttpLoggingInterceptor(PrefixedLogger("inner request")).also {
                if (BuildKonfig.INTERNAL) it.setLevel(HttpLoggingInterceptor.Level.BODY)
            }
        )

        clientBuilder.addInterceptor(vauChannelInterceptor)

        // user agent & dev headers at outer request
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(devHeadersInterceptor)

        clientBuilder.addInterceptor(
            HttpLoggingInterceptor(PrefixedLogger("outer request")).also {
                if (BuildKonfig.INTERNAL) it.setLevel(HttpLoggingInterceptor.Level.BODY)
            }
        )

        return Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.eRezeptServiceUri)
            .addConverterFactory(FhirConverterFactory.create(fhirParser))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ErpService::class.java)
    }

    @Singleton
    @Provides
    fun pharmacyService(
        baseClient: OkHttpClient,
        fhirParser: IParser,
        endpointHelper: EndpointHelper
    ): PharmacySearchService {
        val clientBuilder = baseClient.newBuilder().addInterceptor(PharmacySearchInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

        return Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.pharmacySearchBaseUri)
            .addConverterFactory(FhirConverterFactory.create(fhirParser))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PharmacySearchService::class.java)
    }

    // The VAU service is only used to get CertList & OCSPList and NOT to post to the VAU endpoint

    @Singleton
    @Provides
    fun vauService(
        baseClient: OkHttpClient,
        moshi: Moshi,
        endpointHelper: EndpointHelper,
        @UserAgentInterceptor userAgentInterceptor: Interceptor,
        @DevelopReleaseHeaderInterceptor devHeadersInterceptor: Interceptor
    ): VauService {
        val clientBuilder = baseClient.newBuilder()

        clientBuilder.addInterceptor(devHeadersInterceptor)
        clientBuilder.addInterceptor(userAgentInterceptor)
        clientBuilder.addInterceptor(
            HttpLoggingInterceptor().also {
                if (BuildKonfig.INTERNAL) it.setLevel(HttpLoggingInterceptor.Level.BODY)
            }
        )

        return Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.eRezeptServiceUri)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    moshi.newBuilder().add(OCSPAdapter()).add(X509Adapter()).build()
                )
            )
            .build()
            .create(VauService::class.java)
    }

    @Singleton
    @Provides
    fun providesBaseOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
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

    @Singleton
    @Provides
    fun providesFhirParser(): IParser {
        return LazyFhirParser()
    }

    @Singleton
    @Provides
    @NetworkSecureSharedPreferences
    fun providesSecPrefs(@ApplicationContext context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            NETWORK_SECURE_PREFS_FILE_NAME,
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @NetworkSharedPreferences
    @Singleton
    @Provides
    fun providesNetworkSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(NETWORK_PREFS_FILE_NAME, Context.MODE_PRIVATE)
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
}

private class PrefixedLogger(val prefix: String) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        HttpLoggingInterceptor.Logger.DEFAULT.log("[$prefix] $message")
    }
}
