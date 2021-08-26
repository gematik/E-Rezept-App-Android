/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        AppColorsThemeDark
    } else {
        AppColorsThemeLight
    }

    val typoColors = AppTypographyColors(
        body2l = colors.neutral600,
        subtitle1l = colors.neutral600,
        subtitle2l = colors.neutral600,
        captionl = colors.neutral600,
    )

    MaterialTheme(
        typography = MaterialTheme.typography.copy(
            h1 = MaterialTheme.typography.h1.copy(lineHeight = 1.5.em),
            h2 = MaterialTheme.typography.h2.copy(lineHeight = 1.5.em),
            h3 = MaterialTheme.typography.h3.copy(lineHeight = 1.5.em),
            h4 = MaterialTheme.typography.h4.copy(lineHeight = 1.5.em),
            h5 = MaterialTheme.typography.h5.copy(lineHeight = 1.5.em),
            h6 = MaterialTheme.typography.h6.copy(lineHeight = 1.5.em),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(lineHeight = 1.5.em, fontWeight = FontWeight.W500),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(lineHeight = 1.5.em, fontWeight = FontWeight.W500),
            body1 = MaterialTheme.typography.body1.copy(lineHeight = 1.5.em),
            body2 = MaterialTheme.typography.body2.copy(lineHeight = 1.5.em),
        ),
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
            onBackground = colors.neutral999,
            onSurface = colors.neutral999,
            onError = colors.red900,
            isLight = !darkTheme
        ),
        content = {
            val typo =
                AppTypography(
                    body2l = MaterialTheme.typography.body2.copy(color = typoColors.body2l, lineHeight = 1.5.em),
                    subtitle1l = MaterialTheme.typography.subtitle1.copy(
                        color = typoColors.subtitle1l,
                        lineHeight = 1.5.em
                    ),
                    subtitle2l = MaterialTheme.typography.subtitle2.copy(
                        color = typoColors.subtitle2l,
                        lineHeight = 1.5.em
                    ),
                    captionl = MaterialTheme.typography.caption.copy(color = typoColors.captionl, lineHeight = 1.5.em)
                )

            CompositionLocalProvider(
                LocalAppColors provides colors,
                LocalAppTypographyColors provides typoColors,
                LocalAppTypography provides typo,
                content = content
            )
        }
    )
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current

    val typographyColors: AppTypographyColors
        @Composable
        get() = LocalAppTypographyColors.current

    val typography: AppTypography
        @Composable
        get() = LocalAppTypography.current

    val framePadding = PaddingValues(16.dp)
}

private
val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

private
val LocalAppTypographyColors = staticCompositionLocalOf<AppTypographyColors> {
    error("No AppTypographyColors provided")
}

private
val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("No AppTypography provided")
}
