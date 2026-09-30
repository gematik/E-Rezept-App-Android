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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package de.gematik.ti.erp.app.interceptor

import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BearerHeaderInterceptorTest {

    private val profileId: ProfileIdentifier = "test-profile"
    private val idpUseCase = mockk<IdpUseCase>()
    private val interceptor = BearerHeaderInterceptor(idpUseCase)

    @Before
    fun setUp() {
        coEvery { idpUseCase.loadAccessToken(any(), any(), any()) } returns "test-access-token"
    }

    private fun captureHeaders(incomingRequest: Request): Map<String, String> {
        var capturedRequest: Request? = null

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                capturedRequest = chain.request()
                Response.Builder()
                    .code(200)
                    .message("OK")
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .body("{}".toResponseBody())
                    .build()
            }
            .build()

        client.newCall(incomingRequest).execute().close()

        val req = requireNotNull(capturedRequest) { "interceptor chain was not invoked" }
        return req.headers.toMap()
    }

    @Test
    fun `FHIR endpoint receives fhir+json Accept header`() {
        val request = fhirRequest("/Task")

        val headers = captureHeaders(request)

        assertEquals("application/fhir+json", headers["Accept"])
    }

    @Test
    fun `FHIR endpoint receives fhir+json Content-Type header`() {
        val request = fhirRequest("/Task")

        val headers = captureHeaders(request)

        assertEquals("application/fhir+json; charset=UTF-8", headers["Content-Type"])
    }

    @Test
    fun `FHIR endpoint receives Bearer Authorization header`() {
        val request = fhirRequest("/Task")

        val headers = captureHeaders(request)

        assertEquals("Bearer test-access-token", headers["Authorization"])
    }

    @Test
    fun `push JSON endpoint preserves application-json Accept header`() {
        val request = pushRequest("/pushers/v1")

        val headers = captureHeaders(request)

        assertEquals("application/json", headers["Accept"])
    }

    @Test
    fun `push JSON endpoint receives application-json Content-Type header`() {
        val request = pushRequest("/pushers/v1")

        val headers = captureHeaders(request)

        assertEquals("application/json; charset=UTF-8", headers["Content-Type"])
    }

    @Test
    fun `push JSON endpoint receives Bearer Authorization header`() {
        val request = pushRequest("/pushers/v1")

        val headers = captureHeaders(request)

        assertEquals("Bearer test-access-token", headers["Authorization"])
    }

    @Test
    fun `push JSON endpoint does NOT receive fhir media type`() {
        val request = pushRequest("/pushers/v1/set")

        val headers = captureHeaders(request)

        assert("fhir" !in (headers["Accept"] ?: "")) {
            "Accept header must not contain 'fhir' for push endpoints, got: ${headers["Accept"]}"
        }
        assert("fhir" !in (headers["Content-Type"] ?: "")) {
            "Content-Type must not contain 'fhir' for push endpoints, got: ${headers["Content-Type"]}"
        }
    }

    @Test
    fun `FHIR endpoint does NOT receive plain application-json Accept`() {
        val request = fhirRequest("/AuditEvent")

        val headers = captureHeaders(request)

        assert(headers["Accept"] == "application/fhir+json") {
            "Accept must be application/fhir+json for FHIR endpoints, got: ${headers["Accept"]}"
        }
    }

    @Test
    fun `endpoint with another explicit media type is preserved`() {
        val request = Request.Builder()
            .url("https://erp.example.com/other")
            .tag(ProfileIdentifier::class.java, profileId)
            .header("Accept", "application/xml")
            .build()

        val headers = captureHeaders(request)

        assertEquals("application/xml", headers["Accept"])
        assertEquals("application/xml; charset=UTF-8", headers["Content-Type"])
    }

    private fun fhirRequest(path: String): Request =
        Request.Builder()
            .url("https://erp.example.com$path")
            .tag(ProfileIdentifier::class.java, profileId)
            .build()

    private fun pushRequest(path: String): Request =
        Request.Builder()
            .url("https://erp.example.com$path")
            .tag(ProfileIdentifier::class.java, profileId)
            .header("Accept", "application/json")
            .build()
}
