/*
 * Copyright (c) 2024 gematik GmbH
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
            Workarounds.`workaround for MacOSX Big Sur And Monterey - PCSC not found bug`()

            CardFactory()
        }
    }
}
