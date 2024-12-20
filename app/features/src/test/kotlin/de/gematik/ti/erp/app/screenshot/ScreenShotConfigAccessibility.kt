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

package de.gematik.ti.erp.app.screenshot

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import java.util.Locale

enum class ScreenShotConfigAccessibility(
    override val deviceConfig: DeviceConfig,
    override val theme: String = "Theme.ERezApp"
) : ScreenshotConfig {
    // German
    LARGE_SCREEN_LIGHT_GERMAN(
        deviceConfig = DeviceConfig.PIXEL_6_PRO.copy(
            softButtons = false,
            nightMode = NightMode.NOTNIGHT,
            locale = Locale.GERMAN.toLanguageTag()
        )
    ),

    // English
    LARGE_SCREEN_LIGHT_ENGLISH(
        deviceConfig = DeviceConfig.PIXEL_6_PRO.copy(
            softButtons = false,
            nightMode = NightMode.NOTNIGHT,
            locale = Locale.ENGLISH.toLanguageTag()
        )
    )
}
