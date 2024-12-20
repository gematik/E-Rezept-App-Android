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

package de.gematik.ti.erp.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em

@Suppress("LongMethod")
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        AppColorsThemeDark
    } else {
        AppColorsThemeLight
    }

    val sizes = remember { SizeDefaults }

    val fontFamily = remember { fonts }

    val typoColors = AppTypographyColors(
        body1l = colors.neutral600,
        body2l = colors.neutral600,
        subtitle1l = colors.neutral600,
        subtitle2l = colors.neutral600,
        captionl = colors.neutral600
    )

    val materialTypo = MaterialTheme.typography.copy(
        h1 = MaterialTheme.typography.h1.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W700
        ),
        h2 = MaterialTheme.typography.h2.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W700
        ),
        h3 = MaterialTheme.typography.h3.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W700
        ),
        h4 = MaterialTheme.typography.h4.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W700
        ),
        h5 = MaterialTheme.typography.h5.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W700
        ),
        h6 = MaterialTheme.typography.h6.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em
        ),
        subtitle1 = MaterialTheme.typography.subtitle1.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W500
        ),
        subtitle2 = MaterialTheme.typography.subtitle2.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em,
            fontWeight = FontWeight.W500
        ),
        body1 = MaterialTheme.typography.body1.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em
        ),
        body2 = MaterialTheme.typography.body2.copy(
            fontFamily = fontFamily,
            lineHeight = 1.5.em
        )
    )

    MaterialTheme(
        typography = materialTypo,
        colors = Colors(
            primary = colors.primary600,
            primaryVariant = colors.primary600,
            secondary = colors.primary600,
            secondaryVariant = colors.primary600,
            background = colors.neutral025,
            surface = colors.neutral000,
            error = colors.red500,
            onPrimary = colors.neutral000,
            onSecondary = colors.neutral000,
            onBackground = colors.neutral900,
            onSurface = colors.neutral900,
            onError = colors.red900,
            isLight = !darkTheme
        ),
        content = {
            val typo =
                AppTypography(
                    body1l = MaterialTheme.typography.body1.copy(
                        fontFamily = fontFamily,
                        color = typoColors.body1l,
                        lineHeight = 1.5.em
                    ),
                    body2l = MaterialTheme.typography.body2.copy(
                        fontFamily = fontFamily,
                        color = typoColors.body2l,
                        lineHeight = 1.5.em
                    ),
                    subtitle1l = MaterialTheme.typography.subtitle1.copy(
                        fontFamily = fontFamily,
                        color = typoColors.subtitle1l,
                        lineHeight = 1.5.em
                    ),
                    subtitle2l = MaterialTheme.typography.subtitle2.copy(
                        fontFamily = fontFamily,
                        color = typoColors.subtitle2l,
                        lineHeight = 1.5.em
                    ),
                    caption1l = MaterialTheme.typography.caption.copy(
                        fontFamily = fontFamily,
                        color = typoColors.captionl,
                        lineHeight = 1.5.em
                    ),
                    h1 = materialTypo.h1,
                    h2 = materialTypo.h2,
                    h3 = materialTypo.h3,
                    h4 = materialTypo.h4,
                    h5 = materialTypo.h5,
                    h6 = materialTypo.h6,
                    subtitle1 = materialTypo.subtitle1,
                    subtitle2 = materialTypo.subtitle2,
                    body1 = materialTypo.body1,
                    body2 = materialTypo.body2,
                    button = materialTypo.button,
                    caption1 = materialTypo.caption,
                    caption2 = materialTypo.caption.copy(fontWeight = FontWeight.Medium),
                    overline = materialTypo.overline
                )

            CompositionLocalProvider(
                LocalAppColors provides colors,
                LocalAppTypographyColors provides typoColors,
                LocalAppTypography provides typo,
                LocalSizes provides sizes,
                LocalFonts provides fonts,
                content = content
            )
        }
    )
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        get() = LocalAppTypography.current

    val DebugColor = Color(0xFFD71F5F)
}

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

val LocalAppTypographyColors = staticCompositionLocalOf<AppTypographyColors> {
    error("No AppTypographyColors provided")
}

val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("No AppTypography provided")
}

val LocalSizes = staticCompositionLocalOf<SizeDefaults> {
    error("No sized provided")
}

val LocalFonts = staticCompositionLocalOf<FontFamily> {
    error("No font provided")
}
