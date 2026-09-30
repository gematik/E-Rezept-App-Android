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

package de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation

import de.gematik.ti.erp.app.debugsettings.pushnotifications.usecase.SendFcmMessageUseCase
import de.gematik.ti.erp.app.pushnotifications.BuildConfig

internal object DebugPushNotificationCurlCommands {

    fun buildEncryptedCurlCommand(
        projectId: String,
        oauthToken: String,
        fcmToken: String,
        ciphertext: String,
        month: String,
        keyIdentifier: String
    ) = """
curl -X POST '${SendFcmMessageUseCase.FCM_BASE_URL}v1/projects/$projectId/messages:send' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer $oauthToken' \
  -d '{
  "message": {
    "token": "$fcmToken",
    "android": {
      "priority": "HIGH"
    },
    "data": {
      "ciphertext": "$ciphertext",
      "time_message_encrypted": "$month",
      "key_identifier": "$keyIdentifier"
    }
  }
}'
    """.trimIndent()

    fun buildEncryptedPushGatewayCurlCommand(
        applicationId: String,
        fcmToken: String,
        ciphertext: String,
        month: String,
        keyIdentifier: String,
        gatewayUrl: String = BuildConfig.PUSH_GATEWAY_URL_RU
    ): String {
        val normalizedUrl = if (gatewayUrl.endsWith("/")) gatewayUrl else "$gatewayUrl/"
        return """
curl -X POST '${normalizedUrl}notifyEncrypted/batch' \
  -H 'Content-Type: application/json' \
  --cert .../client.crt \
  --key .../client.key \
  -d '{
  "notifications": [
    {
      "id": "android_test_1",
      "notification": {
        "ciphertext": "$ciphertext",
        "time_message_encrypted": "$month",
        "key_identifier": "$keyIdentifier",
        "prio": "high",
        "device": {
          "app_id": "$applicationId",
          "pushkey": "$fcmToken"
        }
      }
    }
  ]
}'
        """.trimIndent()
    }
}
