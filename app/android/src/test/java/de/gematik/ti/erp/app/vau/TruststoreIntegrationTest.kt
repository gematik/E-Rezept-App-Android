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

package de.gematik.ti.erp.app.vau

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.utils.addSystemProxy
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.repository.VauRemoteDataSource
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststore
import de.gematik.ti.erp.app.vau.usecase.TruststoreConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import kotlin.time.Duration

class TruststoreIntegrationTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var trustStoreLocalDataSource: TrustStoreLocalDataSource

    @Suppress("JSON_FORMAT_REDUNDANT")
    private val jsonConverterFactory = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }.asConverterFactory("application/json".toMediaType())

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `create truststore from remote source`() = runTest {
        Assume.assumeTrue(BuildKonfig.TEST_RUN_WITH_TRUSTSTORE_INTEGRATION)

        coEvery { trustStoreLocalDataSource.loadUntrusted() } returns flowOf(null)
        coEvery { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) } coAnswers { }
        coEvery { trustStoreLocalDataSource.deleteAll() } coAnswers { }

        val okhttp = OkHttpClient.Builder()
            .addSystemProxy()
            .addInterceptor(
                Interceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .addHeader("User-Agent", BuildKonfig.USER_AGENT)
                            .header("X-Api-Key", BuildKonfig.ERP_API_KEY)
                            .addHeader("Accept", "application/json")
                            .build()
                    )
                }
            )
            .addInterceptor(
                HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.HEADERS
                }
            )
            .build()

        val vauService = Retrofit.Builder()
            .client(okhttp)
            .baseUrl(BuildKonfig.BASE_SERVICE_URI)
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create(VauService::class.java)

        val config = TruststoreConfig { return@TruststoreConfig BuildKonfig.APP_TRUST_ANCHOR_BASE64 }

        val useCase = TruststoreUseCase(
            config,
            VauRepository(trustStoreLocalDataSource, VauRemoteDataSource(vauService), coroutineRule.dispatchers.io, config),
            { Clock.System.now() },
            { untrustedOCSPList: UntrustedOCSPList,
                    untrustedCertList: UntrustedCertList,
                    trustAnchor: X509CertificateHolder,
                    ocspResponseMaxAge: Duration,
                    timestamp: Instant
                ->
                TrustedTruststore.create(
                    untrustedOCSPList = untrustedOCSPList,
                    untrustedCertList = untrustedCertList,
                    trustAnchor = trustAnchor,
                    ocspResponseMaxAge = ocspResponseMaxAge,
                    timestamp = timestamp
                )
            }
        )

        val pubKey = useCase.withValidVauPublicKey {
            it
        }

        println("Truststore established - received public key: ${pubKey.w.affineX} ${pubKey.w.affineY}")
    }
}
