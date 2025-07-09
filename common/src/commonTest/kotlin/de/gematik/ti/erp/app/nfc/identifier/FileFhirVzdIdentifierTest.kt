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

package de.gematik.ti.erp.app.nfc.identifier

import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier
import org.junit.Assert
import kotlin.test.Test

class FileIdentifierTest {
    @Test
    fun shouldConstructEqualObjects() {
        val fi1 = FileIdentifier(FID_INTEGER)
        val fi2 = FileIdentifier(FID_HEX_STRING)
        Assert.assertArrayEquals(fi1.getFid(), fi2.getFid())
    }

    @Test
    fun shouldConvertToEqualObject() {
        val fi1 = FileIdentifier(FID_HEX_STRING)
        val fi2 = FileIdentifier(fi1.getFid())
        Assert.assertArrayEquals(fi1.getFid(), fi2.getFid())
    }

    companion object {
        private const val FID_HEX_STRING = "011C"
        private const val FID_INTEGER = 0x011C
    }
}
