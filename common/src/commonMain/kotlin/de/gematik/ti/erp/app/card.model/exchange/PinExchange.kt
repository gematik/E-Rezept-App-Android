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

package de.gematik.ti.erp.app.card.model.exchange

import de.gematik.ti.erp.app.card.model.card.EncryptedPinFormat2
import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.card.model.cardobjects.Mf
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.card.model.command.changeReferenceData
import de.gematik.ti.erp.app.card.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.card.model.command.select
import de.gematik.ti.erp.app.card.model.command.unlockEgk
import de.gematik.ti.erp.app.card.model.command.verifyPin
import de.gematik.ti.erp.app.cardwall.model.nfc.card.PasswordReference

fun ICardChannel.verifyPin(pin: String): ResponseStatus {
    HealthCardCommand.select(selectParentElseRoot = false, readFirst = false)
        .executeSuccessfulOn(this)

    val passwordReference = PasswordReference(Mf.MrPinHome.PWID)

    val response =
        HealthCardCommand.verifyPin(
            passwordReference = passwordReference,
            dfSpecific = false,
            pin = EncryptedPinFormat2(pin)
        ).executeOn(this)

    require(
        when (response.status) {
            ResponseStatus.SUCCESS,
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_01,
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_02,
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_03,
            ResponseStatus.PASSWORD_BLOCKED ->
                true
            else ->
                false
        }
    ) { "Verify pin command failed with status: ${response.status}" }

    return response.status
}

fun ICardChannel.unlockEgk(
    unlockMethod: String,
    puk: String,
    oldSecret: String,
    newSecret: String
): ResponseStatus {
    HealthCardCommand.select(selectParentElseRoot = false, readFirst = false)
        .executeSuccessfulOn(this)

    val passwordReference = PasswordReference(Mf.MrPinHome.PWID)

    val response = if (unlockMethod == UnlockMethod.ChangeReferenceData.name) {
        HealthCardCommand.changeReferenceData(
            passwordReference = passwordReference,
            dfSpecific = false,
            oldSecret = EncryptedPinFormat2(oldSecret),
            newSecret = EncryptedPinFormat2(newSecret)
        ).executeSuccessfulOn(this)
    } else {
        HealthCardCommand.unlockEgk(
            unlockMethod = unlockMethod,
            passwordReference = passwordReference,
            dfSpecific = false,
            puk = EncryptedPinFormat2(puk),
            newSecret = if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret.name) {
                EncryptedPinFormat2(newSecret)
            } else { null }
        ).executeSuccessfulOn(this)
    }

    require(
        when (response.status) {
            ResponseStatus.SUCCESS ->
                true
            else ->
                false
        }
    ) { "Change secret command failed with status: ${response.status}" }

    return response.status
}
