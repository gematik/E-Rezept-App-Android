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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.theme.AppTheme

@Immutable
data class ProfileColor(val textColor: Color, val colorName: String, val backGroundColor: Color, val borderColor: Color)

@Composable
fun ProfilesData.ProfileColorNames.color(): ProfileColor {
    return profileColor(this)
}

@Composable
fun profileColor(profileColorNames: ProfilesData.ProfileColorNames): ProfileColor {
    return when (profileColorNames) {
        ProfilesData.ProfileColorNames.SPRING_GRAY -> ProfileColor(
            textColor = AppTheme.colors.neutral700,
            colorName = stringResource(R.string.profile_color_name_gray),
            backGroundColor = AppTheme.colors.neutral200,
            borderColor = AppTheme.colors.neutral400
        )

        ProfilesData.ProfileColorNames.SUN_DEW -> ProfileColor(
            textColor = AppTheme.colors.yellow700,
            colorName = stringResource(R.string.profile_color_sun_dew),
            backGroundColor = AppTheme.colors.yellow200,
            borderColor = AppTheme.colors.yellow400
        )

        ProfilesData.ProfileColorNames.PINK -> ProfileColor(
            textColor = AppTheme.colors.red700,
            colorName = stringResource(R.string.profile_color_name_pink),
            backGroundColor = AppTheme.colors.red200,
            borderColor = AppTheme.colors.red400
        )

        ProfilesData.ProfileColorNames.TREE -> ProfileColor(
            textColor = AppTheme.colors.green700,
            colorName = stringResource(R.string.profile_color_name_tree),
            backGroundColor = AppTheme.colors.green200,
            borderColor = AppTheme.colors.green400
        )

        ProfilesData.ProfileColorNames.BLUE_MOON -> ProfileColor(
            textColor = AppTheme.colors.primary700,
            colorName = stringResource(R.string.profile_color_name_moon),
            backGroundColor = AppTheme.colors.primary200,
            borderColor = AppTheme.colors.primary400
        )
    }
}
