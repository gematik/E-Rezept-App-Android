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

package de.gematik.ti.erp.app.card.model.card

import de.gematik.ti.erp.app.card.model.command.CommandApdu
import de.gematik.ti.erp.app.card.model.command.ResponseApdu

/**
 * Interface to a (logical) channel of a smart card.
 * A channel object is used to send commands to and to receive answers from a smartcard.
 * This is done by sending so called A-PDUs [CommandApdu] to smartcard. A smartcard returns
 * a [ResponseApdu]
 */
interface ICardChannel {
    /**
     * Returns the Card this channel is associated with.
     */
    val card: IHealthCard

    /**
     * Max transceive length
     */
    val maxTransceiveLength: Int

    /**
     * Transmits the specified [CommandApdu] to the associated smartcard and returns the
     * [ResponseApdu].
     *
     * The CLA byte of the [CommandApdu] is automatically adjusted to match the channel number of this card channel
     * since the channel number is coded into CLA byte of a command APDU according to ISO 7816-4.
     *
     * Implementations should transparently handle artifacts of the transmission protocol.
     *
     * The ResponseAPDU returned by this method is the result after this processing has been performed.
     */
    fun transmit(command: CommandApdu): ResponseApdu

    /**
     * Identify whether a channel supports APDU extended length commands and
     * appropriate responses
     */
    val isExtendedLengthSupported: Boolean
}
