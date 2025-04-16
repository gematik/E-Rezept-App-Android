/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.nfc.card

import de.gematik.ti.erp.app.card.model.card.HealthCardVersion2
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert
import kotlin.test.Test

class Version2Test {
    @Test
    fun fromArray() {
        val version2: HealthCardVersion2 =
            HealthCardVersion2.of(
                Hex.decode("EF2BC003020000C103040302C21045474B47322020202020202020010304C403010000C503020000C703010000")
            )
        Assert.assertArrayEquals(
            Hex.decode("020000"),
            version2.fillingInstructionsEfAtrVersion
        ) // C5
        Assert.assertArrayEquals(
            Hex.decode(""),
            version2.fillingInstructionsEfEnvironmentSettingsVersion
        ) // C3
        Assert.assertArrayEquals(
            Hex.decode("010000"),
            version2.fillingInstructionsEfGdoVersion
        ) // C4
        Assert.assertArrayEquals(
            Hex.decode(""),
            version2.fillingInstructionsEfKeyInfoVersion
        ) // C6
        Assert.assertArrayEquals(
            Hex.decode("010000"),
            version2.fillingInstructionsEfLoggingVersion
        ) // C7
        Assert.assertArrayEquals(Hex.decode("020000"), version2.fillingInstructionsVersion) // C0
        Assert.assertArrayEquals(Hex.decode("040302"), version2.objectSystemVersion) // C1
        Assert.assertArrayEquals(
            Hex.decode("45474B47322020202020202020010304"),
            version2.productIdentificationObjectSystemVersion
        ) // C2
    }
}
