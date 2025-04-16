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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.local

import de.gematik.ti.erp.app.pharmacy.repository.PharmacySearchAccessTokenRepository
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import io.github.aakira.napier.Napier
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

// todo: check if we need to add some requirement here
class PharmacySearchAccessTokenProvider(
    private val repository: PharmacySearchAccessTokenRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private val TOKEN_EXPIRY_DURATION = 12.hours
        private val TAG = PharmacySearchAccessTokenProvider::class.simpleName
    }

    sealed class SearchAccessTokenResult {
        data class Success(val token: String) : SearchAccessTokenResult()
        data class Error(val error: Throwable) : SearchAccessTokenResult()
        data class SocketTimeoutError(val error: Throwable) : SearchAccessTokenResult()
        data object Empty : SearchAccessTokenResult()
    }

    suspend fun getValidToken(): SearchAccessTokenResult {
        return withContext(dispatcher) {
            val currentTime = RealmInstant.now()
            val token = repository.searchAccessToken.first()

            Napier.i(tag = TAG) { "Current token ${token?.accessToken}" }

            if (token != null &&
                token.accessToken.isNotNullOrEmpty() &&
                !isTokenExpired(token.lastUpdate, currentTime)
            ) {
                Napier.i(tag = TAG) { "Returning cached token ${token.accessToken}" }
                return@withContext SearchAccessTokenResult.Success(token.accessToken)
            }

            Napier.i(tag = TAG) { "Search access-token expired or not found, fetching a new one" }
            // Token expired or not found → Fetch new token
            val tokenResult = fetchNewToken()
            if (tokenResult is SearchAccessTokenResult.Success) {
                repository.saveToken(tokenResult.token, currentTime)
            }
            tokenResult
        }
    }

    private suspend fun fetchNewToken(): SearchAccessTokenResult {
        return withContext(dispatcher) {
            try {
                val response = repository.fetchNewToken()
                if (!response.isSuccessful || response.body() == null) {
                    Napier.e(tag = TAG, message = "Error fetching new search access-token: $response")
                    return@withContext SearchAccessTokenResult.Empty
                }
                response.body()?.let { accessToken ->
                    Napier.i(tag = TAG) { "Fetched new search access-token: $accessToken" }
                    SearchAccessTokenResult.Success(accessToken.accessToken)
                } ?: run {
                    Napier.e(tag = TAG, message = "Error fetching new Search access token, no token in body: $response")
                    SearchAccessTokenResult.Empty
                }
            } catch (e: SocketTimeoutException) {
                Napier.e(tag = TAG, message = "Timeout while fetching Search access token.", throwable = e)
                SearchAccessTokenResult.SocketTimeoutError(e)
            } catch (e: Throwable) {
                Napier.e(tag = TAG, message = "Error fetching new search access token", throwable = e)
                SearchAccessTokenResult.Error(e)
            }
        }
    }

    private fun isTokenExpired(lastUpdate: RealmInstant, currentTime: RealmInstant): Boolean {
        val isExpired = lastUpdate.epochSeconds + TOKEN_EXPIRY_DURATION.toJavaDuration().seconds <= currentTime.epochSeconds
        Napier.i(tag = TAG) { "search access token expiry state $isExpired" }
        return isExpired
    }
}
