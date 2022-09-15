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

package de.gematik.ti.erp.app.cardwall.model.nfc.command

import de.gematik.ti.erp.app.cardwall.model.nfc.card.EncryptedPinFormat2
import de.gematik.ti.erp.app.cardwall.model.nfc.card.Password

/**
 * Command representing Verify Secret Command gemSpec_COS#14.6.6
 */

private const val CLA = 0x00
private const val INS = 0x20
private const val MODE_VERIFICATION_DATA = 0x00

/**
 * Use case Change Password Secret (Pin) gemSpec_COS#14.6.6.1
 */
fun HealthCardCommand.Companion.verifyPin(
    password: Password,
    dfSpecific: Boolean,
    pin: EncryptedPinFormat2
) =
    HealthCardCommand(
        expectedStatus = verifyStatus,
        cla = CLA,
        ins = INS,
        p1 = MODE_VERIFICATION_DATA,
        p2 = password.calculateKeyReference(dfSpecific),
        data = pin.bytes
    )
