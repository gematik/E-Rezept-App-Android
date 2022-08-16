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

package de.gematik.ti.erp.app.card.model.exchange

import de.gematik.ti.erp.app.card.model.card.EncryptedPinFormat2
import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.PasswordReference
import de.gematik.ti.erp.app.card.model.cardobjects.Mf
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.command.unlockEgk
import de.gematik.ti.erp.app.card.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.card.model.command.select
import de.gematik.ti.erp.app.card.model.command.verifyPin

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

fun ICardChannel.unlockEgk(changeSecret: Boolean, puk: String, newSecret: String): ResponseStatus {
    HealthCardCommand.select(selectParentElseRoot = false, readFirst = false)
        .executeSuccessfulOn(this)

    val passwordReference = PasswordReference(Mf.MrPinHome.PWID)

    val response =
        HealthCardCommand.unlockEgk(
            changeSecret = changeSecret,
            passwordReference = passwordReference,
            dfSpecific = false,
            puk = EncryptedPinFormat2(puk),
            newSecret = if (changeSecret) {
                EncryptedPinFormat2(newSecret)
            } else { null }
        ).executeSuccessfulOn(this)

    println("Response: $response")

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
