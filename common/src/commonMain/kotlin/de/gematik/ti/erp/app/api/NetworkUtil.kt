/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.api

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiCallException(message: String, val response: Response<*>) : IOException(message)

data class NoInternetException(override val message: String? = null, val exception: Exception? = null) : IOException(message)

/**
 * Wraps a remote call in a try catch and returns [Result.Error] with an [IOException] in case [call] couldn't be executed.
 * In case of a successful response, the [Result.Success] contains the body of it.
 */
suspend fun <T : Any> safeApiCall(
    errorMessage: String,
    call: suspend () -> Response<T>
): Result<T> =
    try {
        val response = call()
        if (response.isSuccessful) {
            requireNotNull(response.body()).let { Result.success(it) }
        } else {
            Result.failure(
                ApiCallException("Error executing safe api call ${response.code()} ${response.message()}", response)
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Napier.e("Api Call Error", e)
        e.mapToResultFailure(errorMessage)
    }

private fun <T : Any> Exception.mapToResultFailure(errorMessage: String): Result<T> =
    when {
        isNetworkException(this) -> Result.failure(NoInternetException(errorMessage, this))

        // An exception was thrown when calling the API so we're converting this to an [IOException]
        else -> Result.failure(IOException(errorMessage, this))
    }

private fun isNetworkException(e: Exception): Boolean {
    return e is UnknownHostException || e is ConnectException || e is SocketTimeoutException
}

/**
 * This safeApi call should only be used if it's necessary to do all error handling on its own apart from an io exception.
 */
suspend fun <T : Any> safeApiCallRaw(
    errorMessage: String,
    call: suspend () -> Result<T>
): Result<T> =
    try {
        call()
    } catch (e: Exception) {
        Napier.e("Api Call Error", e)
        // An exception was thrown when calling the API so we're converting this to an IOException
        e.mapToResultFailure(errorMessage)
    }

suspend fun <T : Any> safeApiCallNullable(
    call: suspend () -> Response<T>
): Result<T?> =
    try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) } ?: Result.success(null)
        } else {
            Result.failure(
                ApiCallException("Error executing safe api call ${response.code()} ${response.message()}", response)
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
