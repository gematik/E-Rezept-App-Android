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

package de.gematik.ti.erp.app.nfc.model.exchange

import de.gematik.ti.erp.app.nfc.model.card.EncryptedPinFormat2
import de.gematik.ti.erp.app.nfc.model.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.nfc.model.card.Password
import de.gematik.ti.erp.app.nfc.model.cardobjects.Mf
import de.gematik.ti.erp.app.nfc.model.command.HealthCardCommand
import de.gematik.ti.erp.app.nfc.model.command.ResponseStatus
import de.gematik.ti.erp.app.nfc.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.nfc.model.command.select
import de.gematik.ti.erp.app.nfc.model.command.verifyPin
import io.github.aakira.napier.Napier

fun NfcCardSecureChannel.verifyPin(pin: String): ResponseStatus {
    HealthCardCommand.select(selectParentElseRoot = false, readFirst = false)
        .executeSuccessfulOn(this)

    val password = Password(Mf.MrPinHome.PWID)

    Napier.d("Verify pin")

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

    Napier.d("Pin verified with status ${response.status}")

    return response.status
}
