/*
 * Copyright (c) 2024 gematik GmbH
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

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import java.net.HttpURLConnection
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import io.github.aakira.napier.Napier

private const val invalidAccessTokenHeader = "Www-Authenticate"
private const val invalidAccessTokenValue = "Bearer realm='prescriptionserver.telematik', error='invalACCESS_TOKEN'"

@Requirement(
    "A_19187",
    "A_20529-01",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Any connection initiated by the app uses TLS 1.2 or higher."
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
            idpUseCase.loadAccessToken(refresh, profileId ?: error("no profile id given"))
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

class PharmacySearchInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
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

class UserAgentHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", BuildKonfig.USER_AGENT)
            .build()

        return chain.proceed(request)
    }
}

class ApiKeyHeaderInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("X-Api-Key", endpointHelper.getErpApiKey())
            .build()

        return chain.proceed(request)
    }
}
