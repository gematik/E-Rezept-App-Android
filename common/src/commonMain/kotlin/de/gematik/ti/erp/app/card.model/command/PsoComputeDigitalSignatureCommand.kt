/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.card.model.command

private const val CLA = 0x00
private const val INS = 0x2A

/**
 * Commands representing Compute Digital Signature in gemSpec_COS#14.8.2
 */
fun HealthCardCommand.Companion.psoComputeDigitalSignature(
    dataToBeSigned: ByteArray
) =
    HealthCardCommand(
        expectedStatus = psoComputeDigitalSignatureStatus,
        cla = CLA,
        ins = INS,
        p1 = 0x9E,
        p2 = 0x9A,
        data = dataToBeSigned,
        ne = EXPECT_ALL_WILDCARD
    )
