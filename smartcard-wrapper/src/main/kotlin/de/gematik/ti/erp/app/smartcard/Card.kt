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

package de.gematik.ti.erp.app.smartcard

import java.io.Closeable
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Objects
import javax.smartcardio.Card
import javax.smartcardio.CardChannel

class CardException internal constructor(cause: javax.smartcardio.CardException) : IOException(cause)

class Card internal constructor(private val reference: Card) : Closeable {
    init {
        reference.beginExclusive()
    }

    private val channel: CardChannel = reference.basicChannel

    fun transmit(command: ByteBuffer, response: ByteBuffer): Int =
        try {
            Objects.requireNonNull(channel).transmit(command, response)
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }

    override fun close() {
        try {
            reference.endExclusive()
            reference.disconnect(true)
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }
    }
}
