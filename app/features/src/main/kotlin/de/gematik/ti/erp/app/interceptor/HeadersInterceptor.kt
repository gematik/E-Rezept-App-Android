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

@file: Suppress("TopLevelPropertyNaming")

package de.gematik.ti.erp.app.interceptor

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val X_API_KEY_IDENTIFIER = "X-Api-Key"
private const val USER_AGENT_IDENTIFIER = "User-Agent"

class PharmacySearchApiKeyInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .header(X_API_KEY_IDENTIFIER, endpointHelper.getPharmacyApiKey())
            .build()
        return chain.proceed(request)
    }
}

class ERezeptBackendTokenApiKeyInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
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
        val original = chain.request()
        val profileId = original.tag(ProfileIdentifier::class.java)

        val request = original.newBuilder()
            .tag(ProfileIdentifier::class.java, profileId)
            .header(USER_AGENT_IDENTIFIER, "${BuildKonfig.USER_AGENT}/${endpointHelper.getClientId()}")
            .build()

        return chain.proceed(request)
    }
}

class ErpApiKeyHeaderInterceptor(private val endpointHelper: EndpointHelper) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val profileId = original.tag(ProfileIdentifier::class.java)

        val request = original.newBuilder()
            .tag(ProfileIdentifier::class.java, profileId)
            .header(X_API_KEY_IDENTIFIER, endpointHelper.getErpApiKey())
            .build()

        return chain.proceed(request)
    }
}
