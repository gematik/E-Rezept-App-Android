package de.gematik.ti.erp.app.network.interceptor

import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection

class BearerHeaderInterceptor(
    private val idpUseCase: IdpUseCase
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()

        val response = chain.proceed(request(original, loadAccessToken(false)))
        return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Napier.d("Received 401 -> refresh access token")

            chain.proceed(request(original, loadAccessToken(true)))
        } else {
            response
        }
    }

    private fun loadAccessToken(refresh: Boolean) =
        runBlocking { idpUseCase.loadAccessToken(refresh) }

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
