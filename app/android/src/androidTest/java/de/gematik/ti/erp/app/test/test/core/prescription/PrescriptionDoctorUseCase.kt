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

package de.gematik.ti.erp.app.test.test.core.prescription

import android.util.Log
import de.gematik.ti.erp.app.test.test.TestConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private fun defaultPayload(kvnr: String) = """
{
  "patient": {
    "kvnr": "$kvnr"
  },
  "medication": {
    "category": "00"
  }
}
"""

class PrescriptionDoctorUseCase {
    private val okHttp by lazy { OkHttpClient.Builder().build() }

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    fun prescribeToPatient(kvNr: String): Task {
        val payload = defaultPayload(kvNr)

        val response = okHttp.newCall(
            Request.Builder()
                .url("${TestConfig.FD.DefaultServer}/doc/${TestConfig.FD.DefaultDoctor}/prescribe")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()

        require(response.isSuccessful) { "Response was: $response" }

        val body = requireNotNull(response.body).string()
        Log.d("prescribeToPatient", body)

        return json.decodeFromString(body)
    }

    fun prescriptionDetails(taskId: String): Prescription {
        val response = okHttp.newCall(
            Request.Builder()
                .url("${TestConfig.FD.DefaultServer}/prescription/$taskId")
                .get()
                .build()
        ).execute()

        require(response.isSuccessful)

        val body = requireNotNull(response.body).string()
        Log.d("prescriptionDetails", body)

        return json.decodeFromString(body)
    }
}

fun <T> retry(n: Int, block: () -> T): T? {
    var retriesLeft = n
    while (retriesLeft > 0) {
        try {
            return block()
        } catch (e: Exception) {
            Log.e("retry", "Reason: ", e)
            retriesLeft--
        }
    }
    return null
}
