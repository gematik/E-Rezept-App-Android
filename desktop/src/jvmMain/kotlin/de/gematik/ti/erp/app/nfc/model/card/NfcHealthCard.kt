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

package de.gematik.ti.erp.app.nfc.model.card

import de.gematik.ti.erp.app.nfc.model.command.CommandApdu
import de.gematik.ti.erp.app.nfc.model.command.ResponseApdu
import de.gematik.ti.erp.app.smartcard.Card
import de.gematik.ti.erp.app.smartcard.CardReader
import java.nio.ByteBuffer

class NfcHealthCard private constructor(val card: Card) {
    private val buffer = ByteBuffer.allocate(1024)

    fun transmit(apduCommand: CommandApdu): ResponseApdu {
        buffer.clear()

        val n = card.transmit(ByteBuffer.wrap(apduCommand.bytes), buffer)
        return ResponseApdu(buffer.array().copyOfRange(0, n))
    }

    companion object {
        fun connect(reader: CardReader): NfcCardChannel =
            NfcCardChannel(isExtendedLengthSupported = true, NfcHealthCard(reader.connect()))
    }
}
