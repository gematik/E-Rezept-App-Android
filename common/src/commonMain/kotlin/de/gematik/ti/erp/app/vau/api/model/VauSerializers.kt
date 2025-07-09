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

package de.gematik.ti.erp.app.vau.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.OCSPResp
import org.bouncycastle.util.encoders.Base64

object OCSPSerializer : KSerializer<OCSPResp> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OCSPSerializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: OCSPResp) =
        encoder.encodeString(Base64.toBase64String(value.encoded!!))

    override fun deserialize(decoder: Decoder): OCSPResp = OCSPResp(Base64.decode(decoder.decodeString()))
}

object X509Serializer : KSerializer<X509CertificateHolder> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("X509Serializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: X509CertificateHolder) =
        encoder.encodeString(Base64.toBase64String(value.encoded!!))

    override fun deserialize(decoder: Decoder): X509CertificateHolder =
        X509CertificateHolder(Base64.decode(decoder.decodeString()))
}
