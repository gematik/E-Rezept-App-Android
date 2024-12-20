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

package de.gematik.ti.erp.app.utils.compose

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    group = "light",
    showBackground = true
)
internal annotation class LightPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "dark",
    showBackground = true
)
internal annotation class DarkPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "dark",
    showBackground = true,
    fontScale = 2f
)
internal annotation class BigFontDarkPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    group = "light",
    showBackground = true,
    fontScale = 2f
)
internal annotation class BigFontLightPreview

@BigFontLightPreview
@BigFontDarkPreview
internal annotation class BigFontPreview

@LightPreview
@DarkPreview
internal annotation class LightDarkPreview
