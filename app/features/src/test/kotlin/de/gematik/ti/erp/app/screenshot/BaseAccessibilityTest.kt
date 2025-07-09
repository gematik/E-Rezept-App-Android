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

package de.gematik.ti.erp.app.screenshot

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.accessibility.AccessibilityRenderExtension
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
abstract class BaseAccessibilityTest(
    config: ScreenshotConfig,
    renderingMode: SessionParams.RenderingMode = SessionParams.RenderingMode.NORMAL
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<ScreenshotConfig> = ScreenShotConfigAccessibility.entries
    }

    @get: Rule
    val paparazzi = Paparazzi(
        deviceConfig = config.deviceConfig,
        theme = config.theme,
        validateAccessibility = false,
        maxPercentDifference = ScreenshotTestDifference.DIFFERENCE,
        renderingMode = renderingMode,
        renderExtensions = setOf(AccessibilityRenderExtension())
    )

    fun Paparazzi.accessibilitySnapshot(
        name: String? = null,
        composable: @Composable () -> Unit
    ) {
        this.snapshot("accessibility-$name") { composable() }
    }
}
