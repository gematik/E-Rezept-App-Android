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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PrescriptionPharmacyUseCase {
    private val okHttp by lazy { OkHttpClient.Builder().build() }

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }
    fun rejectTask(taskId: String, accessCode: String, secret: String) {
        val response = okHttp.newCall(
            @Suppress("ktlint:max-line-length", "MaxLineLength")
            Request.Builder()
                .url(
                    "${TestConfig.FD.DefaultServer}/pharm/${TestConfig.FD.DefaultPharmacy}/reject/?taskId=$taskId&ac=$accessCode&secret=$secret"
                )
                .post("".toRequestBody())
                .build()
        ).execute()

        require(response.isSuccessful) { "Response was: $response" }
    }

    fun acceptTask(taskId: String, accessCode: String): Task {
        val response = okHttp.newCall(
            @Suppress("ktlint:max-line-length", "MaxLineLength")
            Request.Builder()
                .url(
                    "${TestConfig.FD.DefaultServer}/pharm/${TestConfig.FD.DefaultPharmacy}/accept/?taskId=$taskId&ac=$accessCode"
                )
                .post("".toRequestBody())
                .build()
        ).execute()

        require(response.isSuccessful) { "Response was: $response" }

        val body = requireNotNull(response.body).string()
        Log.d("acceptTask", body)

        return json.decodeFromString(body)
    }

    fun abortTask(taskId: String, accessCode: String, secret: String) {
        val response = okHttp.newCall(
            @Suppress("ktlint:max-line-length", "MaxLineLength")
            Request.Builder()
                .url(
                    "${TestConfig.FD.DefaultServer}/pharm/${TestConfig.FD.DefaultPharmacy}/abort/?taskId=$taskId&ac=$accessCode&secret=$secret"
                )
                .delete()
                .build()
        ).execute()

        require(response.isSuccessful) { "Response was: $response" }
    }

    fun replyWithCommunication(taskId: String, kvNr: String, message: CommunicationPayloadInbox) {
        val supplyOption = when (message.supplyOptionsType) {
            SupplyOptionsType.Shipment -> "shipment"
            SupplyOptionsType.OnPremise -> "onPremise"
            SupplyOptionsType.Delivery -> "delivery"
        }

        val response = okHttp.newCall(
            @Suppress("ktlint:max-line-length", "MaxLineLength")
            Request.Builder()
                .url(
                    "${TestConfig.FD.DefaultServer}/pharm/${TestConfig.FD.DefaultPharmacy}/reply/?taskId=$taskId&kvnr=$kvNr&supplyOption=$supplyOption"
                )
                .post(json.encodeToString(message).toRequestBody())
                .build()
        ).execute()

        require(response.isSuccessful) { "Response was: $response" }
    }

    fun dispenseMedication(taskId: String, accessCode: String, secret: String) {
        val response = okHttp.newCall(
            @Suppress("ktlint:max-line-length", "MaxLineLength")
            Request.Builder()
                .url(
                    "${TestConfig.FD.DefaultServer}/pharm/${TestConfig.FD.DefaultPharmacy}/close/?taskId=$taskId&ac=$accessCode&secret=$secret"
                )
                .delete()
                .build()
        ).execute()

        require(response.isSuccessful) { "Response was: $response" }
    }
}
