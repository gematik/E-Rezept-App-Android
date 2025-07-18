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

package de.gematik.ti.erp.app.fhir.serializer

import de.gematik.ti.erp.app.utils.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object SafeFhirInstantSerializer : KSerializer<FhirTemporal.Instant?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FhirInstant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FhirTemporal.Instant?) {
        encoder.encodeString(value?.value.toString()) // Convert to ISO string
    }

    override fun deserialize(decoder: Decoder): FhirTemporal.Instant? {
        return runCatching {
            FhirTemporal.Instant(Instant.parse(decoder.decodeString()))
        }.onFailure { Napier.e("Failed to parse Instant: $it") }.getOrNull() // Returns `null` instead of throwing an error
    }
}
