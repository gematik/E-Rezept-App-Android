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

import kotlinx.serialization.Serializable

/**
 * A derived monthly key pair for AES-256-GCM push notification encryption.
 *
 * @property encryptionKey  hex-encoded 32-byte AES key — last 32 bytes of HKDF output.
 * @property secret         hex-encoded 32-byte secret — first 32 bytes of HKDF output, used as IKM for the next month.
 * @property month          "YYYY-MM" this generation is valid for.
 * @property keyIdentifier  UUID identifying the HKDF chain; echoed back in every FCM push as `key_identifier`.
 */
@Serializable
data class PushNotificationKeyGeneration(
    val encryptionKey: String,
    val secret: String,
    val month: String,
    val keyIdentifier: String
)
