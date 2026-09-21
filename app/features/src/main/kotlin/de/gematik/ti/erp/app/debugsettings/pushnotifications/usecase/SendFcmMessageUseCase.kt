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

package de.gematik.ti.erp.app.debugsettings.pushnotifications.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

class SendFcmMessageUseCase(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val fcmService = Retrofit.Builder()
        .baseUrl(FCM_BASE_URL)
        .build()
        .create(DebugFcmService::class.java)

    suspend operator fun invoke(projectId: String, oauthToken: String, json: String) =
        withContext(dispatcher) {
            val response = fcmService.sendMessage(
                projectId = projectId,
                authorization = "Bearer $oauthToken",
                body = json.toRequestBody(JSON_MEDIA_TYPE)
            )
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code()}: ${response.errorBody()?.string()?.take(200)}")
            }
        }

    companion object {
        const val FCM_BASE_URL = "https://fcm.googleapis.com/"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}

private interface DebugFcmService {
    @POST("v1/projects/{projectId}/messages:send")
    suspend fun sendMessage(
        @Path("projectId") projectId: String,
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Response<Unit>
}
