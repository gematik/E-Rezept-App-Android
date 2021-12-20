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

import de.gematik.ti.erp.app.cardwall.model.nfc.command.CommandApdu
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseApdu
import java.io.Closeable

class NfcCardChannel internal constructor(
    override val isExtendedLengthSupported: Boolean,
    private val nfcHealthCard: NfcHealthCard,
) : ICardChannel, Closeable {
    override val card: NfcHealthCard
        get() = nfcHealthCard

    override val maxTransceiveLength = card.isoDep.maxTransceiveLength

    /**
     * Returns the responseApdu after transmitting a commandApdu
     */
    override fun transmit(command: CommandApdu): ResponseApdu =
        nfcHealthCard.transceive(command)

    override fun close() {
        card.isoDep.close()
    }
}
