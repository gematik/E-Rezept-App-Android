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

package de.gematik.ti.erp.app.apicheck.usecase

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
    "O.Arch_10#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Currently only implemented via APIKey usage against the FD. " +
        "All requests against the backends may respond with an 403 status code."
)
class CheckVersionUseCase(
    private val okHttp: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun isUpdateRequired(): Boolean = withContext(dispatcher) {
        if (BuildKonfig.INTERNAL) {
            return@withContext false
        }

        try {
            val response = okHttp.newCall(
                Request.Builder()
                    .header("X-Api-Key", BuildKonfig.ERP_API_KEY)
                    .url(BuildKonfig.BASE_SERVICE_URI + "CertList")
                    .get()
                    .build()
            ).execute()

            response.code == HttpURLConnection.HTTP_UNAUTHORIZED
        } catch (e: IOException) {
            Napier.e(e) { "Couldn't check if api key is expired" }
            false
        }
    }
}
