/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.cardwall.model.nfc.tagobjects

import de.gematik.ti.erp.app.utils.Bytes.padData
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.params.KeyParameter
import java.io.ByteArrayOutputStream

private const val DO_8E_TAG = 0x0E
private const val MAC_SIZE = 8
private const val BLOCK_SIZE = 16

/**
 * Mac object with TAG 8E (cryptographic checksum)
 *
 *
 * @param header byte array with extracted header from plain CommandApdu
 * @param commandDataOutput ByteArrayOutputStream with extracted data and expected length from plain CommandApdu
 * @param kMac byte array with Session key for message authentication
 * @param ssc byte array with send sequence counter
 */
class MacObject(
    private val header: ByteArray? = null,
    private val commandOutput: ByteArrayOutputStream,
    private val kMac: ByteArray,
    private val ssc: ByteArray
) {
    private var _mac: ByteArray = ByteArray(BLOCK_SIZE)
    val mac: ByteArray
        get() = _mac.copyOf()

    val taggedObject: DERTaggedObject
        get() =
            DERTaggedObject(false, DO_8E_TAG, DEROctetString(_mac))

    init {
        calculateMac()
    }

    private fun calculateMac() {
        val cbcMac = getCMac(ssc, kMac)

        if (header != null) {
            val paddedHeader = padData(header, BLOCK_SIZE)
            cbcMac.update(paddedHeader, 0, paddedHeader.size)
        }
        if (commandOutput.size() > 0) {
            val paddedData = padData(commandOutput.toByteArray(), BLOCK_SIZE)
            cbcMac.update(paddedData, 0, paddedData.size)
        }
        cbcMac.doFinal(_mac, 0)

        _mac = _mac.copyOfRange(0, MAC_SIZE)
    }

    private fun getCMac(secureMessagingSSC: ByteArray, kMac: ByteArray): CMac =
        CMac(AESEngine()).apply {
            init(KeyParameter(kMac))
            update(secureMessagingSSC, 0, secureMessagingSSC.size)
        }
}
