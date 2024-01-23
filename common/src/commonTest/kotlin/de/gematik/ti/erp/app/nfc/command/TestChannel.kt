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

package de.gematik.ti.erp.app.nfc.command

import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.card.model.card.IHealthCard
import de.gematik.ti.erp.app.card.model.command.CommandApdu
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.ResponseApdu
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class TestChannel : ICardChannel {
    private var lastCommandAPDU: CommandApdu? = null

    val lastCommandAPDUBytes: ByteArray
        get() = lastCommandAPDU?.bytes ?: ByteArray(0)

    override val card: IHealthCard = mockk()

    override val maxTransceiveLength: Int = 261

    override fun transmit(command: CommandApdu): ResponseApdu {
        lastCommandAPDU = command
        return ResponseApdu(byteArrayOf(0x90.toByte(), 0x00))
    }

    override val isExtendedLengthSupported: Boolean = true

    fun test(cmd: HealthCardCommand): ByteArray {
        runBlocking { cmd.executeOn(this@TestChannel) }
        return lastCommandAPDUBytes
    }
}
