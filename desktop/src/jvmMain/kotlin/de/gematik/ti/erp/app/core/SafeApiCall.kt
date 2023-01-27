/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.core

import retrofit2.Response
import java.io.IOException

class ApiCallException(message: String, val response: Response<*>) : IOException(message)

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
    } catch (e: Exception) {
        // An exception was thrown when calling the API so we're converting this to an [IOException]
        Result.failure(IOException(errorMessage, e))
    }

suspend fun <T : Any> safeApiCallNullable(
    errorMessage: String,
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
        // An exception was thrown when calling the API so we're converting this to an [IOException]
        Result.failure(IOException(errorMessage, e))
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
        // An exception was thrown when calling the API so we're converting this to an IOException
        Result.failure(IOException(errorMessage, e))
    }
