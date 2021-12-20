/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.nfc.model.command

import de.gematik.ti.erp.app.nfc.model.card.Password

/**
 * Command representing Get Pin Status Command gemSpec_COS#14.6.4
 */

private const val CLA = 0x80
private const val INS = 0x20
private const val NO_MEANING = 0x00

/**
 * Use case Get Pin Status gemSpec_COS#14.6.4.1
 *
 * @param password the arguments for the Get Pin Status command
 * @param dfSpecific whether or not the password object specifies a Global or DF-specific.
 * true = DF-Specific, false = global
 */
fun HealthCardCommand.Companion.getPinStatus(password: Password, dfSpecific: Boolean) =
    HealthCardCommand(
        expectedStatus = pinStatus,
        cla = CLA,
        ins = INS,
        p1 = NO_MEANING,
        p2 = password.calculateKeyReference(dfSpecific)
    )
