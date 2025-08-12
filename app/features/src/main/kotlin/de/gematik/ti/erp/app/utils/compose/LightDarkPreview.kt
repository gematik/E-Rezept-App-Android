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

package de.gematik.ti.erp.app.utils.compose

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    uiMode = UI_MODE_NIGHT_NO,
    group = "light",
    showBackground = true
)
internal annotation class LightPreview

@Preview(
    uiMode = UI_MODE_NIGHT_YES,
    group = "dark",
    showBackground = true
)
internal annotation class DarkPreview

@Preview(
    name = "Long Light",
    group = "light",
    heightDp = 1400,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightLongPreview

@Preview(
    name = "Long Dark",
    group = "dark",
    heightDp = 1400,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkLongPreview

@Preview(
    name = "Big Font Light",
    group = "dark",
    fontScale = 2f,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkBigFontPreview

@Preview(
    name = "Big Font Light",
    group = "light",
    fontScale = 2f,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightBigFontPreview

@LightBigFontPreview
@DarkBigFontPreview
internal annotation class LightDarkBigFontPreview

@Preview(
    name = "Large Font Light",
    group = "dark",
    fontScale = 3f,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkLargeFontPreview

@Preview(
    name = "Large Font Light",
    group = "light",
    fontScale = 3f,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightLargeFontPreview

@LightLargeFontPreview
@DarkLargeFontPreview
internal annotation class LightDarkLargeFontPreview

@LightPreview
@DarkPreview
internal annotation class LightDarkPreview

@LightLongPreview
@DarkLongPreview
internal annotation class LightDarkLongPreview

@Preview(
    name = "Phone Landscape",
    group = "dark",
    widthDp = 891,
    heightDp = 411,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkPhoneLandscapePreview

@Preview(
    name = "Phone Landscape",
    group = "light",
    widthDp = 891,
    heightDp = 411,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightPhoneLandscapePreview

@LightPhoneLandscapePreview
@DarkPhoneLandscapePreview
internal annotation class LightDarkPhoneLandscapePreview

// ---
@Preview(
    name = "Tablet Portrait Dark",
    group = "dark",
    widthDp = 800,
    heightDp = 1280,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkTabletPortraitPreview

@Preview(
    name = "Tablet Portrait Light",
    group = "light",
    widthDp = 800,
    heightDp = 1280,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightTabletPortraitPreview

@LightTabletPortraitPreview
@DarkTabletPortraitPreview
internal annotation class LightDarkTabletPortraitPreview

@Preview(
    name = "Tablet Landscape Dark",
    group = "dark",
    device = TABLET,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkTabletLandscapePreview

@Preview(
    name = "Tablet Landscape Light",
    group = "light",
    device = TABLET,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightTabletLandscapePreview

@LightTabletLandscapePreview
@DarkTabletLandscapePreview
internal annotation class LightDarkTabletLandscapePreview

@Preview(
    name = "Square Dark",
    group = "dark",
    widthDp = 900,
    heightDp = 900,
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true
)
internal annotation class DarkSquarePreview

@Preview(
    name = "Square Light",
    group = "light",
    widthDp = 900,
    heightDp = 900,
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true
)
internal annotation class LightSquarePreview

@LightSquarePreview
@DarkSquarePreview
internal annotation class LightDarkSquarePreview

@LightDarkPreview
@LightDarkPhoneLandscapePreview
@LightDarkTabletPortraitPreview
@LightDarkTabletLandscapePreview
@LightDarkLongPreview
@LightDarkBigFontPreview
@LightDarkLargeFontPreview
@LightDarkSquarePreview
internal annotation class LightDarkAllPreview
