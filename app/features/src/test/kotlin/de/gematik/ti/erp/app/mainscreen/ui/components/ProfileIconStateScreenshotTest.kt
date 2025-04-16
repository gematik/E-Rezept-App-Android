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

package de.gematik.ti.erp.app.mainscreen.ui.components

import app.cash.paparazzi.Paparazzi
import de.gematik.ti.erp.app.prescription.ui.ProfileStatePreviews
import de.gematik.ti.erp.app.prescription.ui.preview.AvatarPreviewParameterProvider
import org.junit.Rule
import org.junit.Test

/**
 * Adding paparazzi directly since this is not a direct component or a screen,
 * it is built to show the different states together as an example of how they should look
 */
class ProfileIconStateScreenshotTest {

    @get: Rule
    val paparazzi = Paparazzi(
        validateAccessibility = true,
        maxPercentDifference = 0.2
    )

    @Test
    fun screenShotTest() {
        val testParameters = AvatarPreviewParameterProvider().values.toList()
        testParameters.forEach { param ->
            paparazzi.snapshot(param.description) {
                ProfileStatePreviews(param)
            }
        }
    }
}
