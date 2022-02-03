/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.profiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.theme.AppTheme

@Immutable
data class ProfileColor(val textColor: Color, val colorName: String, val backGroundColor: Color, val borderColor: Color)

@Composable
fun profileColor(profileColorNames: ProfileColorNames): ProfileColor {

    return when (profileColorNames) {
        ProfileColorNames.SPRING_GRAY -> ProfileColor(
            textColor = AppTheme.colors.neutral700,
            colorName = stringResource(R.string.profile_color_name_gray),
            backGroundColor = AppTheme.colors.neutral200,
            borderColor = AppTheme.colors.neutral400,
        )
        ProfileColorNames.SUN_DEW -> ProfileColor(
            textColor = AppTheme.colors.yellow700,
            colorName = stringResource(R.string.profile_color_sun_dew),
            backGroundColor = AppTheme.colors.yellow200,
            borderColor = AppTheme.colors.yellow400
        )
        ProfileColorNames.PINK -> ProfileColor(
            textColor = AppTheme.colors.red700,
            colorName = stringResource(R.string.profile_color_name_pink),
            backGroundColor = AppTheme.colors.red200,
            borderColor = AppTheme.colors.red400
        )
        ProfileColorNames.TREE -> ProfileColor(
            textColor = AppTheme.colors.green700,
            colorName = stringResource(R.string.profile_color_name_tree),
            backGroundColor = AppTheme.colors.green200,
            borderColor = AppTheme.colors.green400
        )
        ProfileColorNames.BLUE_MOON -> ProfileColor(
            textColor = AppTheme.colors.primary700,
            colorName = stringResource(R.string.profile_color_name_moon),
            backGroundColor = AppTheme.colors.primary200,
            borderColor = AppTheme.colors.primary400
        )
    }
}

@Composable
fun connectionTextColor(profileSsoToken: SingleSignOnToken?) = if (profileSsoToken?.isValid() == true) {
    AppTheme.colors.green600
} else {
    AppTheme.colors.neutral600
}
