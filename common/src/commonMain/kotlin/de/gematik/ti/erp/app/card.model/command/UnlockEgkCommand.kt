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

import de.gematik.ti.erp.app.card.model.card.EncryptedPinFormat2
import de.gematik.ti.erp.app.cardwall.model.nfc.card.PasswordReference
import kotlinx.serialization.Serializable

private const val CLA = 0x00
private const val UNLOCK_EGK_INS = 0x2C
private const val MODE_VERIFICATION_DATA_NEW_SECRET = 0x00
private const val MODE_VERIFICATION_DATA = 0x01

@Serializable
enum class UnlockMethod {
    ChangeReferenceData,
    ResetRetryCounterWithNewSecret,
    ResetRetryCounter,
    None
}

/**
 * Use case unlock eGK with/without Secret (Pin) gemSpec_COS#14.6.5.1 und gemSpec_COS#14.6.5.2
 */
fun HealthCardCommand.Companion.unlockEgk(
    unlockMethod: String,
    passwordReference: PasswordReference,
    dfSpecific: Boolean,
    puk: EncryptedPinFormat2,
    newSecret: EncryptedPinFormat2?
) =
    HealthCardCommand(
        expectedStatus = unlockEgkStatus,
        cla = CLA,
        ins = UNLOCK_EGK_INS,
        p1 = if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret.name) {
            MODE_VERIFICATION_DATA_NEW_SECRET
        } else {
            MODE_VERIFICATION_DATA
        },
        p2 = passwordReference.calculateKeyReference(dfSpecific),
        data = if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret.name) {
            puk.bytes + (newSecret?.bytes ?: byteArrayOf())
        } else {
            puk.bytes
        }
    )
