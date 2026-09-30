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

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.datastore.DataStoreCryptography
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

class PushKeyMaterialEntitySerializer(
    private val cryptography: DataStoreCryptography
) : Serializer<PushKeyMaterialEntitySchema> {
    override val defaultValue = PushKeyMaterialEntitySchema()

    override suspend fun readFrom(input: InputStream): PushKeyMaterialEntitySchema = try {
        val encryptedBytes = withContext(Dispatchers.IO) { input.use { it.readBytes() } }
        val decryptedBytes = cryptography.decrypt(Base64.getDecoder().decode(encryptedBytes))
        SafeJson.value.decodeFromString<PushKeyMaterialEntitySchema>(decryptedBytes.decodeToString())
            .also { schema ->
                if (schema.version != PUSH_KEY_MATERIAL_SCHEMA_VERSION) {
                    throw CorruptionException(
                        "Unsupported push key material schema version ${schema.version}; " +
                            "expected $PUSH_KEY_MATERIAL_SCHEMA_VERSION"
                    )
                }
            }
    } catch (error: CorruptionException) {
        throw error
    } catch (error: Exception) {
        throw CorruptionException("Unable to read encrypted push key material DataStore", error)
    }

    @Requirement(
        "A_27177#2",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Serializes and encrypts the persisted shared secrets and AES/GCM keys before writing them to DataStore.",
        codeLines = 9
    )
    override suspend fun writeTo(t: PushKeyMaterialEntitySchema, output: OutputStream) {
        check(t.version == PUSH_KEY_MATERIAL_SCHEMA_VERSION) {
            "Unsupported push key material schema version ${t.version}"
        }
        val json = SafeJson.value.encodeToString(t)
        val encryptedBytes = cryptography.encrypt(json.encodeToByteArray())
        val encodedBytes = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) { output.use { it.write(encodedBytes) } }
    }
}
