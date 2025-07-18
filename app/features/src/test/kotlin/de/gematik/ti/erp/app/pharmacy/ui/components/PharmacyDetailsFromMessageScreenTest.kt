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

package de.gematik.ti.erp.app.pharmacy.ui.components

import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacySheetFromMessagesParameterProvider
import de.gematik.ti.erp.app.screenshot.BaseAccessibilityTest
import de.gematik.ti.erp.app.screenshot.BaseScreenshotTest
import de.gematik.ti.erp.app.screenshot.ScreenshotConfig
import org.junit.Test

class PharmacyDetailsFromMessageScreenTest(config: ScreenshotConfig) : BaseScreenshotTest(config) {
    @Test
    fun screenShotTest() {
        val testParameters = PharmacySheetFromMessagesParameterProvider().values.toList()
        testParameters.forEachIndexed { index, pharmacy ->
            paparazzi.snapshot("parameter_$index") {
                PharmacyDetailsScreenFromMessagePreview(pharmacy = pharmacy)
            }
        }
    }
}

class PharmacyDetailsFromMessageScreenAccessibilityTest(config: ScreenshotConfig) : BaseAccessibilityTest(config) {
    @Test
    fun screenShotTest() {
        val testParameters = PharmacySheetFromMessagesParameterProvider().values.toList()
        testParameters.forEachIndexed { index, pharmacy ->
            paparazzi.accessibilitySnapshot("parameter_$index") {
                PharmacyDetailsScreenFromMessagePreview(pharmacy)
            }
        }
    }
}
