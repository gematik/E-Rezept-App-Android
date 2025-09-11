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

package de.gematik.ti.erp.app.database.datastore.featuretoggle

import androidx.datastore.core.Serializer
import de.gematik.ti.erp.app.database.datastore.DataStoreCryptography
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

class FeatureEntitySerializer(
    private val cryptography: DataStoreCryptography
) : Serializer<FeatureEntitySchema> {
    override val defaultValue: FeatureEntitySchema
        get() = FeatureEntitySchema(
            classes = emptySet()
        )

    override suspend fun readFrom(input: InputStream): FeatureEntitySchema {
        try {
            val encryptedBytes = withContext(Dispatchers.IO) {
                input.use {
                    it.readBytes()
                }
            }
            val encryptedBytesBase64Decoded = Base64.getDecoder().decode(encryptedBytes)
            val decodedBytes = cryptography.decrypt(encryptedBytesBase64Decoded)
            val decodedString = decodedBytes.decodeToString()
            return SafeJson.value.decodeFromString(decodedString)
        } catch (e: Exception) {
            Napier.e { "Failed to read FeatureEntity: $e" }
            return defaultValue
        }
    }

    override suspend fun writeTo(t: FeatureEntitySchema, output: OutputStream) {
        try {
            val json = SafeJson.value.encodeToString(t)
            val bytes = json.toByteArray()
            val encryptedBytes = cryptography.encrypt(bytes)
            val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
            withContext(Dispatchers.IO) {
                output.use {
                    it.write(encryptedBytesBase64)
                }
            }
        } catch (e: Exception) {
            Napier.e { "Failed to write FeatureEntity: $e" }
        }
    }
}
