package de.gematik.ti.erp.app.smartcard

import java.io.Closeable
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Objects
import javax.smartcardio.Card
import javax.smartcardio.CardChannel

class CardException internal constructor(cause: javax.smartcardio.CardException) : IOException(cause)

class Card internal constructor(private val reference: Card) : Closeable {
    private val channel: CardChannel = reference.basicChannel

    fun transmit(command: ByteBuffer, response: ByteBuffer): Int =
        try {
            Objects.requireNonNull(channel).transmit(command, response)
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }

    override fun close() {
        try {
            reference.disconnect(false)
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }
    }
}
