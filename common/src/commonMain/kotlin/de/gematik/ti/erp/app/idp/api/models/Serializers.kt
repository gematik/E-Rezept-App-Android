/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.idp.api.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure

object JWSSerializer : KSerializer<JWSChallenge> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JWSSerializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: JWSChallenge) = error("not implemented")
    override fun deserialize(decoder: Decoder): JWSChallenge {
        val jws = decoder.decodeString()
        return JWSChallenge(JsonWebStructure.fromCompactSerialization(jws) as JsonWebSignature, jws)
    }
}
