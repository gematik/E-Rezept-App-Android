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

@file: Suppress("TopLevelPropertyNaming")

package de.gematik.ti.erp.app.interceptor

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection

private const val invalidAccessTokenHeader = "Www-Authenticate"
private const val invalidAccessTokenValue = "Bearer realm='prescriptionserver.telematik', error='invalACCESS_TOKEN'"
private const val X_API_KEY_IDENTIFIER = "X-Api-Key"
private const val USER_AGENT_IDENTIFIER = "User-Agent"

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
        val original: Request = chain.request()
        val profileId = original.tag(ProfileIdentifier::class.java)
        val response = chain.proceed(request(original, loadAccessToken(false, profileId)))
        return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED &&
            response.header(invalidAccessTokenHeader) == invalidAccessTokenValue
        ) {
            Napier.d("Received 401 -> refresh access token")
            chain.proceed(request(original, loadAccessToken(true, profileId)))
        } else {
            response
        }
    }

    private fun loadAccessToken(refresh: Boolean, profileId: ProfileIdentifier?) =
        runBlocking {
            idpUseCase.loadAccessToken(refresh = refresh, profileId = profileId ?: error("no profile id given"))
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

class PharmacySearchApiKeyInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .header(X_API_KEY_IDENTIFIER, endpointHelper.getPharmacyApiKey())
            .build()
        return chain.proceed(request)
    }
}

class PharmacySearchAccessTokenApiKeyInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .header(X_API_KEY_IDENTIFIER, endpointHelper.getSearchAccessTokenApiKey())
            .build()
        return chain.proceed(request)
    }
}

class PharmacyRedeemInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .build()
        return chain.proceed(request)
    }
}

class UserAgentHeaderInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(USER_AGENT_IDENTIFIER, "${BuildKonfig.USER_AGENT}/${endpointHelper.getClientId()}")
            .build()

        return chain.proceed(request)
    }
}

class ErpApiKeyHeaderInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(X_API_KEY_IDENTIFIER, endpointHelper.getErpApiKey())
            .build()

        return chain.proceed(request)
    }
}
