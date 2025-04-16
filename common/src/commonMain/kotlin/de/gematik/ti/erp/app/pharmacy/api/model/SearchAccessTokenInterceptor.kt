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

package de.gematik.ti.erp.app.pharmacy.api.model

import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacySearchAccessTokenProvider
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class SearchAccessTokenInterceptor(private val tokenProvider: PharmacySearchAccessTokenProvider) : Interceptor {

    companion object {
        private const val AUTH_HEADER_NAME = "Authorization"
        private const val BEARER = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val searchAccessTokenResult = fetchSearchAccessToken()
        return if (searchAccessTokenResult is PharmacySearchAccessTokenProvider.SearchAccessTokenResult.Success) {
            val newRequest = chain.request().newBuilder()
                .addHeader(AUTH_HEADER_NAME, "$BEARER ${searchAccessTokenResult.token}")
                .build()
            chain.proceed(newRequest)
        } else {
            Napier.e("Failed to get search access token $searchAccessTokenResult")
            chain.proceed(chain.request())
        }
    }

    // Ensures the token is fetched synchronously
    private fun fetchSearchAccessToken(): PharmacySearchAccessTokenProvider.SearchAccessTokenResult {
        return runBlocking { tokenProvider.getValidToken() }
    }
}
