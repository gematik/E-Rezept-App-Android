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

package de.gematik.ti.erp.app.interceptor

import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.net.HttpURLConnection

class BearerHeadersInterceptor(
    private val idpUseCase: IdpUseCase,
    private val profilesUseCase: ProfilesUseCase,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()

        val response = chain.proceed(request(original, loadAccessToken(false)))
        return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Timber.d("Received 401 -> refresh access token")

            chain.proceed(request(original, loadAccessToken(true)))
        } else {
            response
        }
    }

    private fun loadAccessToken(refresh: Boolean) =
        runBlocking {
            val activeProfileName = profilesUseCase.activeProfileName().first()
            idpUseCase.loadAccessToken(refresh, activeProfileName)
        }

    private fun request(original: Request, token: String) =
        original.newBuilder()
            .header("Accept", "application/fhir+json")
            .header("Content-Type", "application/fhir+json; charset=UTF-8")
            .header(
                "Authorization",
                "Bearer $token"
            )
            .build()
}

class PharmacySearchInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .build()
        return chain.proceed(request)
    }
}

class UserAgentHeaderInterceptor(
    private val userAgent: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .build()

        return chain.proceed(request)
    }
}
