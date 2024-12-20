/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.cardwall.model.nfc.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import de.gematik.ti.erp.app.card.model.card.IHealthCard
import de.gematik.ti.erp.app.card.model.command.CommandApdu
import de.gematik.ti.erp.app.card.model.command.ResponseApdu
import io.github.aakira.napier.Napier

private const val ISO_DEP_TIMEOUT = 2500

class NfcHealthCard private constructor(val isoDep: IsoDep) : IHealthCard {

    override fun transmit(apduCommand: CommandApdu): ResponseApdu {
        Napier.d("transceive ----")
        val resp = ResponseApdu(isoDep.transceive(apduCommand.bytes))
        Napier.d("transceived ----")
        return resp
    }

    companion object {
        fun connect(tag: Tag): NfcCardChannel {
            val isoDep =
                IsoDep.get(tag).apply {
                    Napier.d("Try isoDep connect ...")
                    connect()
                    Napier.d("... isoDep connected")
                    Napier.d("isoDep maxTransceiveLength: $maxTransceiveLength")
                    Napier.d("isoDep timeout: $timeout")
                    timeout = ISO_DEP_TIMEOUT
                    Napier.d("isoDep timeout set to: $timeout")
                }

            val healthCard = NfcHealthCard(isoDep)

            return NfcCardChannel(
                isoDep.isExtendedLengthApduSupported,
                healthCard
            )
        }
    }
}
