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

package de.gematik.ti.erp.app.appupdate.usecase

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.HttpURLConnection

@Requirement(
    "O.Arch_10#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Business logic to make the check for an update using APOVZD and FD."
)
class CheckVersionUseCase(
    private val okHttp: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun isUpdateRequired(): Boolean = withContext(dispatcher) {
        runCatching {
            val response = okHttp.newCall(
                Request.Builder()
                    .header("X-Api-Key", BuildKonfig.ERP_API_KEY)
                    .url("${BuildKonfig.BASE_SERVICE_URI}VAUCertificate")
                    .get()
                    .build()
            ).execute()

            response.use {
                Napier.i(tag = "CheckVersionUseCase") {
                    "isUpdateRequired: ${it.code}, message: ${it.message}"
                }
                it.code == HttpURLConnection.HTTP_UNAUTHORIZED
            }
        }.getOrElse { e ->
            when (e) {
                is IOException -> Napier.e(tag = "CheckVersionUseCase", throwable = e) {
                    "Network error checking API key expiration"
                }

                else -> Napier.e(tag = "CheckVersionUseCase", throwable = e) {
                    "Unexpected error checking API key expiration"
                }
            }
            false
        }
    }
}
