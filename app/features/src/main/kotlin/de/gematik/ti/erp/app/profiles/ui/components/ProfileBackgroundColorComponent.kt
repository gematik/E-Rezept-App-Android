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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.ui.screens.ColorPicker
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun ColumnScope.ProfileBackgroundColorComponent(
    color: ProfilesData.ProfileColorNames,
    onColorPicked: (ProfilesData.ProfileColorNames) -> Unit
) {
    SpacerXXLarge()
    SpacerMedium()
    Text(
        modifier = Modifier
            .padding(PaddingDefaults.Tiny)
            .semanticsHeading(),
        color = AppTheme.colors.neutral900,
        text = stringResource(R.string.edit_profile_background_color),
        style = AppTheme.typography.h6
    )
    SpacerLarge()
    ColorPicker(
        modifier = Modifier.padding(top = PaddingDefaults.Tiny),
        profileColorName = color,
        onSelectProfileColor = onColorPicked
    )
}

@LightDarkPreview
@Composable
fun ProfileBackgroundColorComponentPreview() {
    PreviewAppTheme {
        Column {
            ProfileBackgroundColorComponent(
                color = ProfilesData.ProfileColorNames.BLUE_MOON,
                onColorPicked = {}
            )
        }
    }
}
