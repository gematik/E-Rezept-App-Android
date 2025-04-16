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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.idp

import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.jose4j.jws.JsonWebSignatureAlgorithm

open class EcdsaUsingShaAlgorithmExtending(
    id: String?,
    javaAlgo: String?,
    curveName: String?,
    signatureByteLength: Int
) : EcdsaUsingShaAlgorithm(id, javaAlgo, curveName, signatureByteLength),
    JsonWebSignatureAlgorithm {
    class EcdsaBP256R1UsingSha256 : EcdsaUsingShaAlgorithmExtending(
        AlgorithmIdentifiersExtending.BRAINPOOL256_USING_SHA256,
        "SHA256withECDSA",
        EllipticCurvesExtending.BP_256,
        64
    )

    class EcdsaBP384R1UsingSha384 : EcdsaUsingShaAlgorithmExtending(
        AlgorithmIdentifiersExtending.BRAINPOOL384_USING_SHA384,
        "SHA384withECDSA",
        EllipticCurvesExtending.BP_384,
        64
    )

    class EcdsaBP512R1UsingSha512 : EcdsaUsingShaAlgorithmExtending(
        AlgorithmIdentifiersExtending.BRAINPOOL512_USING_SHA512,
        "SHA512withECDSA",
        EllipticCurvesExtending.BP_512,
        64
    )
}
