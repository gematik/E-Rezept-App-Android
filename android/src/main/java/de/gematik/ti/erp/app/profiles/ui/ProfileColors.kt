package de.gematik.ti.erp.app.profiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.ProfileColors
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.theme.AppTheme

@Immutable
data class ProfileColor(val textColor: Color, val colorName: String, val backGroundColor: Color, val borderColor: Color)

@Composable
fun profileColor(profileColors: ProfileColors): ProfileColor {

    return when (profileColors) {
        ProfileColors.SPRING_GRAY -> ProfileColor(
            textColor = AppTheme.colors.neutral700,
            colorName = stringResource(R.string.profile_color_name_gray),
            backGroundColor = AppTheme.colors.neutral200,
            borderColor = AppTheme.colors.neutral400,
        )
        ProfileColors.SUN_DEW -> ProfileColor(
            textColor = AppTheme.colors.yellow700,
            colorName = stringResource(R.string.profile_color_sun_dew),
            backGroundColor = AppTheme.colors.yellow200,
            borderColor = AppTheme.colors.yellow400
        )
        ProfileColors.PINK -> ProfileColor(
            textColor = AppTheme.colors.red700,
            colorName = stringResource(R.string.profile_color_name_pink),
            backGroundColor = AppTheme.colors.red200,
            borderColor = AppTheme.colors.red400
        )
        ProfileColors.TREE -> ProfileColor(
            textColor = AppTheme.colors.green700,
            colorName = stringResource(R.string.profile_color_name_tree),
            backGroundColor = AppTheme.colors.green200,
            borderColor = AppTheme.colors.green400
        )
        ProfileColors.BLUE_MOON -> ProfileColor(
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
