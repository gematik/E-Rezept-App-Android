package de.gematik.ti.erp.app.network.interceptor

import de.gematik.ti.erp.app.Constants
import okhttp3.Interceptor
import okhttp3.Response

class UserAgentHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", Constants.userAgent)
            .build()

        return chain.proceed(request)
    }
}

class ApiKeyHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("X-Api-Key", Constants.ERP.apiKey)
            .build()

        return chain.proceed(request)
    }
}
