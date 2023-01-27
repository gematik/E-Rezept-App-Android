/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.card.model.identifier

import org.bouncycastle.util.encoders.Hex
import java.nio.ByteBuffer

/**
 * A file identifier may reference any file. It consists of two bytes. The value '3F00'
 * is reserved for referencing the MF. The value 'FFFF' is reserved for future use. The value '3FFF' is reserved
 * (see below and 7.4.1). The value '0000' is reserved (see 7.2.2 and 7.4.1). In order to unambiguously select
 * any file by its identifier, all EFs and DFs immediately under a given DF shall have different file identifiers.
 * @see "ISO/IEC 7816-4"
 */
class FileIdentifier {
    private val fid: Int

    constructor(fid: ByteArray) {
        require(fid.size == 2) { "requested length of byte array for a File Identifier value is 2 but was " + fid.size }
        val b = ByteBuffer.allocate(Int.SIZE_BYTES)
        for (i in fid.indices) {
            b.put(fid.size + i, fid[i])
        }
        this.fid = b.int
        sanityCheck()
    }

    constructor(fid: Int) {
        this.fid = fid
        sanityCheck()
    }

    constructor(hexFid: String) : this(Hex.decode(hexFid))

    fun getFid(): ByteArray {
        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES)
        return buffer.putShort(fid.toShort()).array()
    }

    private fun sanityCheck() {
        // gemSpec_COS#N006.700, N006.900
        require(!((fid < 0x1000 || fid > 0xFEFF) && fid != 0x011C || fid == 0x3FFF)) {
            "File Identifier is out of range: 0x" + Hex.toHexString(getFid())
        }
    }
}
