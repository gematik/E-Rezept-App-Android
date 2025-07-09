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

package de.gematik.ti.erp.app.card.model.command

import de.gematik.ti.erp.app.cardwall.model.nfc.card.PasswordReference

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
fun HealthCardCommand.Companion.getPinStatus(password: PasswordReference, dfSpecific: Boolean) =
    HealthCardCommand(
        expectedStatus = pinStatus,
        cla = CLA,
        ins = INS,
        p1 = NO_MEANING,
        p2 = password.calculateKeyReference(dfSpecific)
    )
