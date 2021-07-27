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

package de.gematik.ti.erp.app.cardwall.model.nfc.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import de.gematik.ti.erp.app.cardwall.model.nfc.command.CommandApdu
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseApdu
import timber.log.Timber

private const val ISO_DEP_TIMEOUT = 2000
private const val RETRY_TIMEOUT = 1000

class NfcHealthCard private constructor(val isoDep: IsoDep) {

    fun transceive(apduCommand: CommandApdu): ResponseApdu {
        val tm = System.currentTimeMillis()
        while (!isCardPresent && tm - System.currentTimeMillis() < RETRY_TIMEOUT) {
            // nop
        }
        Timber.d("transceive ----")
        val resp = ResponseApdu(isoDep.transceive(apduCommand.bytes))
        Timber.d("transceived ----")
        return resp
    }

    /**
     * Returns if card is present
     *
     * @return true if IsoDep not null and IsoDep is connected false if IsoDep is null or IsoDep is not connected
     * @throws CardException
     */
    private val isCardPresent: Boolean
        get() {
            var result: Boolean
            isoDep.let {
                result = isoDep.isConnected
                Timber.d("isCardPresent() = %s", result)
            }
            return result
        }

    companion object {
        fun connect(tag: Tag): NfcCardChannel {
            val isoDep =
                IsoDep.get(tag).apply {
                    Timber.d("Try isoDep connect ...")
                    connect()
                    Timber.d("... isoDep connected")
                    Timber.d("isoDep maxTransceiveLength: %s", maxTransceiveLength)
                    Timber.d("isoDep timeout: %s", timeout)
                    timeout = ISO_DEP_TIMEOUT
                    Timber.d("isoDep timeout set to: %s", timeout)
                }

            val healthCard = NfcHealthCard(isoDep)

            return NfcCardChannel(
                isoDep.isExtendedLengthApduSupported,
                healthCard
            )
        }
    }
}
