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

package de.gematik.ti.erp.app.interceptor

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection

@Requirement(
    "A_20529-01#01",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
)
@Requirement(
    "A_20602#1",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "The interceptor pattern is used to add the bearer token to the request."
)
class BearerHeaderInterceptor(
    private val idpUseCase: IdpUseCase
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val profileId = original.tag(ProfileIdentifier::class.java) ?: error("no profile id given")

        // First attempt with existing token
        val firstRequest = requestWithAuth(original, loadAccessToken(false, profileId), profileId)
        val response = chain.proceed(firstRequest)

        return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED &&
            response.header(invalidAccessTokenHeader) == invalidAccessTokenValue
        ) {
            // on 401 + invalid token, refresh and retry once
            Napier.d("Received 401 → refreshing access token")
            val retryRequest = requestWithAuth(original, loadAccessToken(true, profileId), profileId)
            chain.proceed(retryRequest)
        } else {
            response
        }
    }

    private fun loadAccessToken(refresh: Boolean, profileId: ProfileIdentifier): String =
        runBlocking {
            idpUseCase.loadAccessToken(refresh = refresh, profileId = profileId)
        }

    /**
     * Builds a new request that:
     *  • re-attaches the ProfileIdentifier tag so downstream interceptors still see it
     *  • adds only the Authorization header (Accept/Content-Type are handled elsewhere)
     */
    private fun requestWithAuth(
        original: Request,
        token: String,
        profileId: ProfileIdentifier
    ): Request =
        original.newBuilder()
            .tag(ProfileIdentifier::class.java, profileId)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/fhir+json")
            .header("Content-Type", "application/fhir+json; charset=UTF-8")
            .build()

    companion object {
        private const val invalidAccessTokenHeader = "Www-Authenticate"
        private const val invalidAccessTokenValue = "Bearer realm='prescriptionserver.telematik', error='invalACCESS_TOKEN'"
    }
}
