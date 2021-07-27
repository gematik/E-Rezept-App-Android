/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.model.nfc.card

import okio.ByteString.Companion.decodeHex
import org.junit.Assert
import org.junit.Test

class Version2Test {
    @Test
    fun fromArray() {
        val version2: HealthCardVersion2 =
            HealthCardVersion2.of("EF2BC003020000C103040302C21045474B47322020202020202020010304C403010000C503020000C703010000".decodeHex().toByteArray())
        Assert.assertArrayEquals(
            "020000".decodeHex().toByteArray(),
            version2.fillingInstructionsEfAtrVersion
        ) // C5
        Assert.assertArrayEquals(
            "".decodeHex().toByteArray(),
            version2.fillingInstructionsEfEnvironmentSettingsVersion
        ) // C3
        Assert.assertArrayEquals(
            "010000".decodeHex().toByteArray(),
            version2.fillingInstructionsEfGdoVersion
        ) // C4
        Assert.assertArrayEquals(
            "".decodeHex().toByteArray(),
            version2.fillingInstructionsEfKeyInfoVersion
        ) // C6
        Assert.assertArrayEquals(
            "010000".decodeHex().toByteArray(),
            version2.fillingInstructionsEfLoggingVersion
        ) // C7
        Assert.assertArrayEquals("020000".decodeHex().toByteArray(), version2.fillingInstructionsVersion) // C0
        Assert.assertArrayEquals("040302".decodeHex().toByteArray(), version2.objectSystemVersion) // C1
        Assert.assertArrayEquals(
            "45474B47322020202020202020010304".decodeHex().toByteArray(),
            version2.productIdentificationObjectSystemVersion
        ) // C2
    }
}
