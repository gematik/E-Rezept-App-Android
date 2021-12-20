package de.gematik.ti.erp.app.smartcard

import javax.smartcardio.TerminalFactory

class CardFactory private constructor() {
    private val factory: TerminalFactory by lazy {
        TerminalFactory.getInstance("PC/SC", null)
    }

    val readers: List<CardReader>
        get() = try {
            factory.terminals().list().map { CardReader(it) }
        } catch (e: javax.smartcardio.CardException) {
            throw CardException(e)
        }

    companion object {
        val instance: CardFactory by lazy {
            Workarounds.`workaround for MacOSX Big Sur - PCSC not found bug`()

            CardFactory()
        }
    }
}
