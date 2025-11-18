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

package de.gematik.ti.erp.app.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

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

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    group = "light",
    heightDp = 1400,
    showBackground = true
)
internal annotation class LightLongPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "dark",
    heightDp = 1400,
    showBackground = true
)
annotation class DarkLongPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
private annotation class LightPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
private annotation class DarkPreview

@LightPreview
@DarkPreview
annotation class LightDarkPreview(val name: String = "")

@LightLongPreview
@DarkLongPreview
annotation class LightDarkLongPreview(val name: String = "")

@BigFontLightPreview
@BigFontDarkPreview
annotation class BigFontPreview(val name: String = "")
