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

package de.gematik.ti.erp.app.common.theme

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.common.common.theme.AppTypography
import de.gematik.ti.erp.app.common.common.theme.AppTypographyColors

@Composable
fun DesktopAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        AppColorsThemeDark
    } else {
        AppColorsThemeLight
    }

    val fontFamily = FontFamily(
        Font("fonts/NotoSans-Bold.ttf", weight = FontWeight.Bold),
        Font("fonts/NotoSans-Medium.ttf", weight = FontWeight.Medium),
        Font("fonts/NotoSans-Regular.ttf", weight = FontWeight.Normal),
        Font("fonts/NotoSans-SemiBold.ttf", weight = FontWeight.SemiBold)
    )

    DesktopMaterialTheme(
        typography = MaterialTheme.typography.copy(
            h1 = MaterialTheme.typography.h1.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
            h2 = MaterialTheme.typography.h2.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
            h3 = MaterialTheme.typography.h3.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
            h4 = MaterialTheme.typography.h4.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
            h5 = MaterialTheme.typography.h5.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
            h6 = MaterialTheme.typography.h6.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
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
            body1 = MaterialTheme.typography.body1.copy(fontFamily = fontFamily, lineHeight = 1.5.em),
            body2 = MaterialTheme.typography.body2.copy(fontFamily = fontFamily, lineHeight = 1.5.em)
        ),
        colors = Colors(
            primary = colors.primary600,
            primaryVariant = colors.primary600,
            secondary = colors.primary600,
            secondaryVariant = colors.primary600,
            background = colors.neutral050,
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
                    body1l = MaterialTheme.typography.body1.copy(
                        fontFamily = fontFamily,
                        color = colors.neutral600,
                        lineHeight = 1.5.em
                    ),
                    body2l = MaterialTheme.typography.body2.copy(
                        fontFamily = fontFamily,
                        color = colors.neutral600,
                        lineHeight = 1.5.em
                    ),
                    subtitle1l = MaterialTheme.typography.subtitle1.copy(
                        fontFamily = fontFamily,
                        color = colors.neutral600,
                        lineHeight = 1.5.em
                    ),
                    subtitle2l = MaterialTheme.typography.subtitle2.copy(
                        fontFamily = fontFamily,
                        color = colors.neutral600,
                        lineHeight = 1.5.em
                    ),
                    captionl = MaterialTheme.typography.caption.copy(
                        fontFamily = fontFamily,
                        color = colors.neutral600,
                        lineHeight = 1.5.em
                    ),
                    overlinel = MaterialTheme.typography.overline.copy(
                        fontFamily = fontFamily,
                        color = colors.neutral600,
                        lineHeight = 1.5.em
                    )
                )

            CompositionLocalProvider(
                LocalAppColors provides colors,
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

    val typography: AppTypography
        @Composable
        get() = LocalAppTypography.current

    val DebugColor = Color(0xFFD71F5F)
}

private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

private val LocalAppTypographyColors = staticCompositionLocalOf<AppTypographyColors> {
    error("No AppTypographyColors provided")
}

private val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("No AppTypography provided")
}
