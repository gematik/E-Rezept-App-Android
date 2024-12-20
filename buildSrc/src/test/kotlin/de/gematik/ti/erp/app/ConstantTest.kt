/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app

import de.gematik.ti.erp.app.utils.extractMajorVersion
import de.gematik.ti.erp.app.utils.extractRCVersion
import de.gematik.ti.erp.app.utils.extractVersion
import de.gematik.ti.erp.app.utils.isValidVersionCode
import org.junit.Assert
import org.junit.Test

class ConstantTest {

    @Test
    fun isValidVersionCodeTest() {
        Assert.assertEquals(true, "1.19.1-RC2".isValidVersionCode())
        Assert.assertEquals(true, "1.19.1-RC2-bfg87ibedf".isValidVersionCode())
        Assert.assertEquals(true, "9.100.10-RC10".isValidVersionCode())
        Assert.assertEquals(false, "R9.100.10-RC10".isValidVersionCode())
        Assert.assertEquals(false, "R9.100.10".isValidVersionCode())
        Assert.assertEquals(false, "R1.1.1-RC".isValidVersionCode())
    }

    @Test
    fun extractVersionTest() {
        Assert.assertEquals("1.19.1", "R1.19.1-RC2".extractVersion())
        Assert.assertEquals("9.100.10", "R9.100.10-RC10".extractVersion())
    }

    @Test
    fun extractRCVersionTest() {
        Assert.assertEquals("1.19.1-RC2", "R1.19.1-RC2".extractRCVersion())
        Assert.assertEquals("9.100.10-RC10", "R9.100.10-RC10".extractRCVersion())
    }

    @Test
    fun extractMajorVersionTest() {
        Assert.assertEquals("1.19", "1.19.1-RC2".extractMajorVersion())
    }
}
