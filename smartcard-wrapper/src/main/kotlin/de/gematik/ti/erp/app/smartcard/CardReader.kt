package de.gematik.ti.erp.app.smartcard

import javax.smartcardio.CardTerminal

class CardReader internal constructor(private val reference: CardTerminal) {
    val name: String = reference.name

    val isCardPresent: Boolean
        get() = try {
            reference.isCardPresent
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }

    fun connect(): Card =
        try {
            Card(reference.connect("*"))
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }
}
