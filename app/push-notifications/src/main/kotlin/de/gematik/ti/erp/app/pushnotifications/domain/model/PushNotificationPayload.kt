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

package de.gematik.ti.erp.app.pushnotifications.domain.model

/**
 * Decrypted and parsed push notification payload.
 * ```json
 * { "ChannelId": "erp.task.activate", "Identifier": "160.000.000.000.001", "IdentifierType": "TaskId" }
 * ```
 * [rawPayload] preserves the original JSON string for debugging.
 */
data class PushNotificationPayload(
    val channelId: String? = null,
    val identifier: String? = null,
    val identifierType: String? = null,
    val rawPayload: String
)
