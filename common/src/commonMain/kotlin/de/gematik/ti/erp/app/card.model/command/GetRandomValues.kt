/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.Requirement

/**
 * Commands representing Get Random command in gemSpec_COS#14.9.5
 */

private const val CLA = 0x80
private const val INS = 0x84
private const val NO_MEANING = 0x00

/**
 * Use case Get Random gemSpec_COS#14.9.5.1
 */
@Requirement(
    "GS-A_4367#5",
    "GS-A_4368#5",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Random numbers are generated using the RNG of the health card." +
        "This generator fulfills BSI-TR-03116#3.4 PTG.2 required by gemSpec_COS#14.9.5.1"
)
fun HealthCardCommand.Companion.getRandomValues(
    length: Int
) =
    HealthCardCommand(
        expectedStatus = getRandomValuesStatus,
        cla = CLA,
        ins = INS,
        p1 = NO_MEANING,
        p2 = NO_MEANING,
        ne = length
    )
