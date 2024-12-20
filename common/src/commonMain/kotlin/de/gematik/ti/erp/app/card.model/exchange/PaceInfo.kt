/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.card.model.exchange

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.card.model.CardUtilities
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DLSet
import org.bouncycastle.jce.ECNamedCurveTable

private const val PARAMETER256 = 13
private const val PARAMETER384 = 16
private const val PARAMETER512 = 17

/**
 * Extracts PACE Information from CardAccess
 */
class PaceInfo(cardAccess: ByteArray) {
    private val protocol: ASN1ObjectIdentifier
    private val parameterID: Int

    /**
     * Returns PACE info protocol bytes
     */
    val paceInfoProtocolBytes: ByteArray =
        ASN1InputStream(cardAccess).use { asn1InputStream ->
            val app = asn1InputStream.readObject() as DLSet
            val seq = app.getObjectAt(0) as ASN1Sequence
            protocol = seq.getObjectAt(0) as ASN1ObjectIdentifier
            parameterID = (seq.getObjectAt(2) as ASN1Integer).value.toInt()

            protocol.encoded.let {
                it.copyOfRange(2, it.size)
            }
        }

    /**
     * PACE info protocol ID
     */
    val protocolID: String = protocol.id

    @Requirement(
        "O.Cryp_5#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Brainpool curves P256 used",
        codeLines = 4
    )
    private val ecNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(
        when (parameterID) {
            PARAMETER256 -> "BrainpoolP256r1"
            PARAMETER384 -> "BrainpoolP384r1"
            PARAMETER512 -> "BrainpoolP512r1"
            else -> ""
        }
    )

    val ecCurve = ecNamedCurveParameterSpec.curve
    val ecPointG = ecNamedCurveParameterSpec.g

    fun convertECPoint(ecPoint: ByteArray) =
        CardUtilities.byteArrayToECPoint(ecPoint, ecCurve)
}
