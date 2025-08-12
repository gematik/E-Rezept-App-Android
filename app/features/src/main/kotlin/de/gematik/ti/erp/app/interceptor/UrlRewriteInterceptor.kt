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
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites any absolute @Url requests so that:
 *  • scheme, host & port come from your configured baseUrl
 *  • path and query come from the server’s nextLink
 *
 * This runs before your BearerHeaderInterceptor, so when you ask
 * for a token‐scope by host, it still matches your base host.
 */
@Requirement(
    "A_20602#2",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = """
        When the server hands back an absolute next-page URL we must still apply
        our standard Bearer-token interceptor. This [UrlRewriteInterceptor]
        rewrites any unknown host back onto our configured baseUrl, so that
        the BearerHeaderInterceptor sees the expected host, loads the correct
        token scope, and injects the Authorization header.
    """
)
class UrlRewriteInterceptor(
    private val baseUrl: HttpUrl
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val profileId = original.tag(ProfileIdentifier::class.java)
        val originalUrl = original.url

        // Only rewrite if the host or scheme differs from our base
        if (originalUrl.host != baseUrl.host || originalUrl.scheme != baseUrl.scheme) {
            val rewrittenUrl = originalUrl.newBuilder()
                .scheme(baseUrl.scheme)
                .host(baseUrl.host)
                .port(baseUrl.port)
                .build()

            Napier.d(tag = "UrlRewriteInterceptor") { "rewriting $originalUrl → $rewrittenUrl" }

            // Re‐attach the ProfileIdentifier tag so downstream interceptors still see it
            val newReq = original.newBuilder()
                .url(rewrittenUrl)
                .tag(ProfileIdentifier::class.java, profileId)
                .build()

            return chain.proceed(newReq)
        }

        // No change needed; pass through
        return chain.proceed(original)
    }
}
