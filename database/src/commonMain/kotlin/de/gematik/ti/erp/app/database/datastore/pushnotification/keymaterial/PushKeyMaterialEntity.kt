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
 */

package de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial

import kotlinx.serialization.Serializable

const val PUSH_KEY_MATERIAL_SCHEMA_VERSION = 1
const val PUSH_KEY_MATERIAL_DATA_STORE_FILE = "PushKeyMaterial"

/**
 * Persisted representation of one monthly push-notification key generation.
 * Its sensitive key material is encrypted by [PushKeyMaterialEntitySerializer] before it is written to DataStore.
 */
@Serializable
data class PushKeyGenerationEntity(
    val encryptionKey: String,
    val secret: String,
    val month: String,
    val keyIdentifier: String
)

/** Groups the retained monthly key generations belonging to one key identifier. */
@Serializable
data class PushKeyMaterialEntity(
    val keyIdentifier: String,
    val generations: List<PushKeyGenerationEntity>
)

/** Versioned DataStore root that stores each push-notification key chain by its key identifier. */
@Serializable
data class PushKeyMaterialEntitySchema(
    val version: Int = PUSH_KEY_MATERIAL_SCHEMA_VERSION,
    val keyMaterial: Map<String, PushKeyMaterialEntity> = emptyMap()
)
