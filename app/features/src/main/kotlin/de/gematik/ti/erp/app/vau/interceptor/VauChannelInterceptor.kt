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

package de.gematik.ti.erp.app.vau.interceptor

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.logger.SessionLogHolder
import de.gematik.ti.erp.app.logger.model.ContentLog
import de.gematik.ti.erp.app.logger.model.HeaderLog
import de.gematik.ti.erp.app.logger.model.LogEntry
import de.gematik.ti.erp.app.logger.model.RequestLog
import de.gematik.ti.erp.app.logger.model.ResponseLog
import de.gematik.ti.erp.app.logger.model.TimingsLog
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.vau.VauChannelSpec
import de.gematik.ti.erp.app.vau.VauCryptoConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
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
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Handle VAU response."
)
class VauChannelInterceptor(
    endpointHelper: EndpointHelper,
    private val truststore: TruststoreUseCase,
    private val cryptoConfig: VauCryptoConfig,
    private val networkSecPrefs: SharedPreferences,
    private val sessionLog: SessionLogHolder
) : Interceptor {
    @Requirement(
        "A_20175#2",
        "A_20174#6",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Set/Retrieve a UserPseudonym from secure storage"
    )
    @Requirement(
        "A_20161-01#13",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "7. VAU-Endpoint respects userpseudonym if present"
    )
    private var previousUserAlias = networkSecPrefs.getString(VAU_USER_ALIAS_PREF_KEY, null) ?: "0"
        set(v) {
            field = v
            networkSecPrefs.edit {
                putString(VAU_USER_ALIAS_PREF_KEY, v)
            }
        }
    private val baseUrl = endpointHelper.eRezeptServiceUri.toHttpUrl()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (BuildKonfig.INTERNAL && !BuildKonfig.VAU_ENABLE_INTERCEPTOR) {
            Napier.d("VAU interceptor disabled - pass requests")
            return chain.proceed(chain.request())
        }

        // 1) extract profileId tag from original request
        val profileId = original.tag(ProfileIdentifier::class.java) ?: error("no profile id given")

        try {
            // 2) encrypt the HTTP request with VAU channel spec
            val (encryptedReq, context) = runBlocking(Dispatchers.IO) {
                truststore.withValidVauPublicKey { publicKey ->
                    VauChannelSpec.V1.encryptHttpRequest(
                        innerRequest = chain.request(),
                        userpseudonym = previousUserAlias,
                        publicKey = publicKey,
                        baseUrl = baseUrl,
                        cryptoConfig = cryptoConfig
                    )
                }
            }

            // 3) re-attach the ProfileIdentifier tag to the encrypted request
            val taggedEncryptedReq = encryptedReq.newBuilder()
                .tag(ProfileIdentifier::class.java, profileId)
                .build()

            // 4) send the encrypted request
            val encryptedResponse = chain.proceed(taggedEncryptedReq)

            @Requirement(
                "A_20174#7",
                sourceSpecification = "gemSpec_Krypt",
                rationale = "handle encrypted response and user pseudonym"
            )
            // 5) handle 401/403 by resetting alias
            if (!encryptedResponse.isSuccessful &&
                (
                    encryptedResponse.code == HTTP_UNAUTHORIZED ||
                        encryptedResponse.code == HTTP_FORBIDDEN
                    )
            ) {
                previousUserAlias = "0"
                return encryptedResponse
            }

            // 6) decrypt and update alias
            val (decryptedResponse, userpseudonym) = VauChannelSpec.V1.decryptHttpResponse(
                encryptedResponse,
                taggedEncryptedReq,
                context,
                cryptoConfig
            )

            // 7) log for debug builds
            if (BuildConfigExtension.isInternalDebug) {
                val logEntry = decryptedResponse.buildLogEntryFromVau(taggedEncryptedReq)
                sessionLog.addLog(logEntry)
            }

            // 8) store new alias if provided
            userpseudonym?.let { previousUserAlias = it }

            return decryptedResponse
        } catch (e: Exception) {
            previousUserAlias = "0"

            // wrap all exceptions
            throw VauException(e)
        }
    }

    private fun Response.buildLogEntryFromVau(
        request: Request
    ): LogEntry {
        val responseBody = peekBody(Long.MAX_VALUE)

        return LogEntry(
            timestamp = Clock.System.now().toString(),
            request = RequestLog(
                method = request.method,
                url = request.url.toString(),
                headers = request.headers.map { HeaderLog(it.first, it.second) }
            ),
            response = ResponseLog(
                status = code,
                statusText = message,
                headers = headers.map { HeaderLog(it.first, it.second) },
                content = ContentLog(
                    mimeType = body?.contentType()?.type.orEmpty(),
                    text = responseBody.string()
                )
            ),
            timings = TimingsLog(
                send = 0L, // Optional: you can add real timings if needed
                wait = 0L,
                receive = 0L
            )
        )
    }
}
