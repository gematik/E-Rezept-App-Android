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

package de.gematik.ti.erp.app.consent.model

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HTTP_BAD_REQUEST
import de.gematik.ti.erp.app.api.HTTP_CONFLICT
import de.gematik.ti.erp.app.api.HTTP_FORBIDDEN
import de.gematik.ti.erp.app.api.HTTP_INTERNAL_ERROR
import de.gematik.ti.erp.app.api.HTTP_METHOD_NOT_ALLOWED
import de.gematik.ti.erp.app.api.HTTP_NOT_FOUND
import de.gematik.ti.erp.app.api.HTTP_REQUEST_TIMEOUT
import de.gematik.ti.erp.app.api.HTTP_TOO_MANY_REQUESTS
import de.gematik.ti.erp.app.api.HTTP_UNAUTHORIZED
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException
import kotlin.test.assertEquals

class ConsentStateTest {

    @Test
    fun `map consent error state Unknown`() {
        val error = Throwable(message = "", cause = Throwable())
        val context = ConsentContext.GetConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.Unknown, state)
    }

    @Test
    fun `map consent error state UnknownHostException`() {
        val error = Throwable(message = "", cause = Throwable("", UnknownHostException()))
        val context = ConsentContext.GetConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.NoInternet(context), state)
    }

    @Test
    fun `map consent error state HTTP_CONFLICT`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_CONFLICT, "".toResponseBody(null)))
        val context = ConsentContext.GetConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.AlreadyGranted, state)
    }

    @Test
    fun `map consent error state HTTP_REQUEST_TIMEOUT`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_REQUEST_TIMEOUT, "".toResponseBody(null)))
        val context = ConsentContext.GetConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.ServerTimeout(context), state)
    }

    @Test
    fun `map consent error state HTTP_INTERNAL_ERROR`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_INTERNAL_ERROR, "".toResponseBody(null)))
        val context = ConsentContext.GetConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.InternalError(context), state)
    }

    @Test
    fun `map consent error state HTTP_TOO_MANY_REQUESTS`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_TOO_MANY_REQUESTS, "".toResponseBody(null)))
        val context = ConsentContext.GetConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.TooManyRequests(context), state)
    }

    @Test
    fun `map consent error state HTTP_NOT_FOUND`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_NOT_FOUND, "".toResponseBody(null)))
        val context = ConsentContext.RevokeConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.ChargeConsentAlreadyRevoked, state)
    }

    @Test
    fun `map consent error state HTTP_BAD_REQUEST`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_BAD_REQUEST, "".toResponseBody(null)))
        val context = ConsentContext.RevokeConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.BadRequest, state)
    }

    @Test
    fun `map consent error state HTTP_METHOD_NOT_ALLOWED`() {
        val error = ApiCallException(
            "",
            Response.error<HttpException>(HTTP_METHOD_NOT_ALLOWED, "".toResponseBody(null))
        )
        val context = ConsentContext.RevokeConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.BadRequest, state)
    }

    @Test
    fun `map consent error state HTTP_FORBIDDEN`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_FORBIDDEN, "".toResponseBody(null)))
        val context = ConsentContext.RevokeConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.Forbidden, state)
    }

    @Test
    fun `map consent error state HTTP_UNAUTHORIZED`() {
        val error = ApiCallException("", Response.error<HttpException>(HTTP_UNAUTHORIZED, "".toResponseBody(null)))
        val context = ConsentContext.RevokeConsent
        val state = mapConsentErrorStates(error, context)
        assertEquals(ConsentState.ConsentErrorState.Unauthorized, state)
    }
}
