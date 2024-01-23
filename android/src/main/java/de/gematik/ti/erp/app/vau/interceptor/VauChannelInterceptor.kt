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

package de.gematik.ti.erp.app.vau.interceptor

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.vau.VauChannelSpec
import de.gematik.ti.erp.app.vau.VauCryptoConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import io.github.aakira.napier.Napier
import java.io.IOException
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.security.Provider
import java.security.SecureRandom

class DefaultCryptoConfig : VauCryptoConfig {
    override val provider: Provider by lazy { BCProvider }
    override val random: SecureRandom
        get() = secureRandomInstance()
}

private const val VAU_USER_ALIAS_PREF_KEY = "VAU_USER_ALIAS"

/**
 * Wrapper for exceptions originating from the [VauChannelInterceptor].
 */
class VauException(e: Exception) : IOException(e)

@Requirement(
    "A_20161-01#4",
    "A_20174#1  ",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Handle VAU response."
)
class VauChannelInterceptor(
    endpointHelper: EndpointHelper,
    private val truststore: TruststoreUseCase,
    private val cryptoConfig: VauCryptoConfig,
    private val dispatchers: DispatchProvider,
    private val networkSecPrefs: SharedPreferences
) : Interceptor {
    private var previousUserAlias = networkSecPrefs.getString(VAU_USER_ALIAS_PREF_KEY, null) ?: "0"
        set(v) {
            field = v
            networkSecPrefs.edit {
                putString(VAU_USER_ALIAS_PREF_KEY, v)
            }
        }
    private val baseUrl = endpointHelper.eRezeptServiceUri.toHttpUrl()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (BuildKonfig.INTERNAL && !BuildKonfig.VAU_ENABLE_INTERCEPTOR) {
            Napier.d("VAU interceptor disabled - pass requests")
            return chain.proceed(chain.request())
        }

        try {
            val encryptedRequest = runBlocking(dispatchers.IO) {
                truststore.withValidVauPublicKey { publicKey ->
                    VauChannelSpec.V1.encryptHttpRequest(
                        chain.request(),
                        previousUserAlias,
                        publicKey,
                        baseUrl,
                        cryptoConfig
                    )
                }
            }

            // outer response
            val encryptedResponse = chain.proceed(encryptedRequest.first)

            return if (!encryptedResponse.isSuccessful) {
                // e.g. 401 -> user pseudonym unknown -> reset to zero
                if (encryptedResponse.code == HTTP_UNAUTHORIZED || encryptedResponse.code == HTTP_FORBIDDEN) {
                    previousUserAlias = "0"
                }

                encryptedResponse
            } else {
                val (decryptedResponse, userpseudonym) = VauChannelSpec.V1.decryptHttpResponse(
                    encryptedResponse,
                    encryptedRequest.first,
                    encryptedRequest.second,
                    cryptoConfig
                )

                userpseudonym?.let {
                    previousUserAlias = it
                }

                decryptedResponse
            }
        } catch (e: Exception) {
            previousUserAlias = "0"

            // wrap all exceptions
            throw VauException(e)
        }
    }
}
