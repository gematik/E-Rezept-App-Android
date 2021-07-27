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

package de.gematik.ti.erp.app.cardwall.model.nfc.exchange

import de.gematik.ti.erp.app.cardwall.model.nfc.card.EncryptedPinFormat2
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.Password
import de.gematik.ti.erp.app.cardwall.model.nfc.cardobjects.Mf
import de.gematik.ti.erp.app.cardwall.model.nfc.command.HealthCardCommand
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseStatus
import de.gematik.ti.erp.app.cardwall.model.nfc.command.executeSuccessfulOn
import de.gematik.ti.erp.app.cardwall.model.nfc.command.select
import de.gematik.ti.erp.app.cardwall.model.nfc.command.verifyPin

fun NfcCardSecureChannel.verifyPin(pin: String): ResponseStatus {
    HealthCardCommand.select(selectParentElseRoot = false, readFirst = false)
        .executeSuccessfulOn(this)

    val password = Password(Mf.MrPinHome.PWID)

    val response =
        HealthCardCommand.verifyPin(password, false, EncryptedPinFormat2(pin))
            .executeOn(this)

    require(
        when (response.status) {
            ResponseStatus.SUCCESS,
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_01,
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_02,
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 ->
                true
            else ->
                false
        }
    ) { "Verify pin command failed with status: ${response.status}" }

    return response.status
}
