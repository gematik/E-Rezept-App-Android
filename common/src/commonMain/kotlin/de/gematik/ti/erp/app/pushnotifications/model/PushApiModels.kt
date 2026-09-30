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
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.pushnotifications.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PusherRegistrationRequest(
    @SerialName("pushkey") val pushKey: String,
    @SerialName("kind") val kind: String,
    @SerialName("app_id") val appId: String,
    @SerialName("app_display_name") val appDisplayName: String,
    @SerialName("device_display_name") val deviceDisplayName: String,
    @SerialName("lang") val lang: String,
    @SerialName("data") val data: PusherData,
    @SerialName("encryption") val encryption: PusherEncryption,
    @SerialName("append") val append: Boolean
)

@Serializable
data class PusherDeregistrationRequest(
    @SerialName("pushkey") val pushKey: String,
    @SerialName("kind") val kind: String? = null,
    @SerialName("app_id") val appId: String
)

@Serializable
data class PusherData(
    @SerialName("url") val url: String
)

@Serializable
data class PusherEncryption(
    @SerialName("method") val method: String,
    @SerialName("time_iss_created") val timeIssCreated: String,
    @SerialName("iss") val iss: String,
    @SerialName("key_identifier") val keyIdentifier: String
)

@Serializable
data class PushChannelRequest(
    @SerialName("channels") val channels: List<PushChannel>
)

@Serializable
data class PushChannelsResponse(
    @SerialName("channels") val channels: List<PushChannel>
)

@Serializable
data class PushersResponse(
    @SerialName("pushers") val pushers: List<Pusher>
)

@Serializable
data class Pusher(
    @SerialName("pushkey") val pushKey: String,
    @SerialName("kind") val kind: String? = null,
    @SerialName("app_id") val appId: String,
    @SerialName("device_display_name") val deviceDisplayName: String? = null
)

@Serializable
data class PushChannel(
    @SerialName("id") val id: String,
    @SerialName("status") val status: String
)

object PushChannelStatus {
    const val ENABLED = "enabled"
    const val DISABLED = "disabled"
    const val NOT_SET = "not_set"
}

@Serializable
class EmptyJsonObjectResponse
