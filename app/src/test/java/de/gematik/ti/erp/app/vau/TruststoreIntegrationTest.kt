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

package de.gematik.ti.erp.app.vau

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.vau.api.VauService
import de.gematik.ti.erp.app.vau.api.model.OCSPAdapter
import de.gematik.ti.erp.app.vau.api.model.X509Adapter
import de.gematik.ti.erp.app.vau.repository.VauLocalDataSource
import de.gematik.ti.erp.app.vau.repository.VauRemoteDataSource
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststoreProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreTimeSourceProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class TruststoreIntegrationTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var localDataSource: VauLocalDataSource

    private val moshi = Moshi.Builder().add(OCSPAdapter()).add(X509Adapter()).build()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `create truststore from remote source`() {
        assumeTrue(BuildConfig.TEST_RUN_WITH_TRUSTSTORE_INTEGRATION)

        coEvery { localDataSource.loadUntrusted() } coAnswers { null }
        coEvery { localDataSource.saveLists(any(), any()) } coAnswers { }
        coEvery { localDataSource.deleteAll() } coAnswers { }

        val okhttp = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .addHeader("User-Agent", "test")
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
            .baseUrl(BuildConfig.BASE_SERVICE_URI)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    moshi
                )
            )
            .build()
            .create(VauService::class.java)

        val useCase = TruststoreUseCase(
            TruststoreConfig(),
            VauRepository(localDataSource, VauRemoteDataSource(vauService), coroutineRule.testDispatchProvider),
            TruststoreTimeSourceProvider(),
            TrustedTruststoreProvider()
        )

        val pubKey = runBlocking {
            useCase.withValidVauPublicKey {
                it
            }
        }

        println("Truststore established - received public key: ${pubKey.w.affineX} ${pubKey.w.affineY}")
    }
}
