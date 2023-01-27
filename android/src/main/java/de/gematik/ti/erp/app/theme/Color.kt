/*
 * Copyright (c) 2023 gematik GmbH
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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val green100: Color,
    val green200: Color,
    val green300: Color,
    val green400: Color,
    val green500: Color,
    val green600: Color,
    val green700: Color,
    val green800: Color,
    val green900: Color,

    val neutral000: Color,
    val neutral025: Color,
    val neutral050: Color,
    val neutral100: Color,
    val neutral200: Color,
    val neutral300: Color,
    val neutral400: Color,
    val neutral500: Color,
    val neutral600: Color,
    val neutral700: Color,
    val neutral800: Color,
    val neutral900: Color,
    val neutral999: Color,

    val primary100: Color,
    val primary200: Color,
    val primary300: Color,
    val primary400: Color,
    val primary500: Color,
    val primary600: Color,
    val primary700: Color,
    val primary800: Color,
    val primary900: Color,

    val red100: Color,
    val red200: Color,
    val red300: Color,
    val red400: Color,
    val red500: Color,
    val red600: Color,
    val red700: Color,
    val red800: Color,
    val red900: Color,

    val yellow100: Color,
    val yellow200: Color,
    val yellow300: Color,
    val yellow400: Color,
    val yellow500: Color,
    val yellow600: Color,
    val yellow700: Color,
    val yellow800: Color,
    val yellow900: Color,

    val scanOverlayErrorOutline: Color,
    val scanOverlayErrorFill: Color,

    val scanOverlaySavedOutline: Color,
    val scanOverlaySavedFill: Color,

    val scanOverlayHoldOutline: Color,
    val scanOverlayHoldFill: Color
)

val AppColorsThemeLight = AppColors(
    green100 = Color(0xFFEBFFF0),
    green200 = Color(0xFFC6F6D5),
    green300 = Color(0xFF9AE6B4),
    green400 = Color(0xFF68D391),
    green500 = Color(0xFF48BB78),
    green600 = Color(0xFF38A169),
    green700 = Color(0xFF2F855A),
    green800 = Color(0xFF276749),
    green900 = Color(0xFF22543D),

    neutral000 = Color(0xFFFFFFFF),
    neutral025 = Color(0xFFFDFDFD),
    neutral050 = Color(0xFFFAFAFA),
    neutral100 = Color(0xFFF5F5F5),
    neutral200 = Color(0xFFEEEEEE),
    neutral300 = Color(0xFFE0E0E0),
    neutral400 = Color(0xFFBDBDBD),
    neutral500 = Color(0xFF9E9E9E),
    neutral600 = Color(0xFF757575),
    neutral700 = Color(0xFF616161),
    neutral800 = Color(0xFF424242),
    neutral900 = Color(0xFF212121),
    neutral999 = Color(0xFF000000),

    primary100 = Color(0xFFEBF8FF),
    primary200 = Color(0xFFBEE3F8),
    primary300 = Color(0xFF90CDF4),
    primary400 = Color(0xFF63B3ED),
    primary500 = Color(0xFF4299E1),
    primary600 = Color(0xFF3182CE),
    primary700 = Color(0xFF2B6CB0),
    primary800 = Color(0xFF2C5282),
    primary900 = Color(0xFF2A4365),

    red100 = Color(0xFFFFEBEB),
    red200 = Color(0xFFFED7D7),
    red300 = Color(0xFFFEB2B2),
    red400 = Color(0xFFFC8181),
    red500 = Color(0xFFF56565),
    red600 = Color(0xFFE53E3E),
    red700 = Color(0xFFC53030),
    red800 = Color(0xFF9B2C2C),
    red900 = Color(0xFF742A2A),

    yellow100 = Color(0xFFFFFFEB),
    yellow200 = Color(0xFFFEFCBF),
    yellow300 = Color(0xFFFAF089),
    yellow400 = Color(0xFFF6E05E),
    yellow500 = Color(0xFFECC94B),
    yellow600 = Color(0xFFD69E2E),
    yellow700 = Color(0xFFB7791F),
    yellow800 = Color(0xFF975A16),
    yellow900 = Color(0xFF744210),

    scanOverlayErrorOutline = Color(0xFFF86A6A),
    scanOverlayErrorFill = Color(0x33E12D39),
    scanOverlaySavedOutline = Color(0xFF00FF64),
    scanOverlaySavedFill = Color(0x3300D353),
    scanOverlayHoldOutline = Color(0xFFFFFFFF),
    scanOverlayHoldFill = Color(0x26FFFFFF)
)

val AppColorsThemeDark = AppColors(
    green100 = Color(0x7F22543D),
    green200 = Color(0xFF22543D),
    green300 = Color(0xFF276749),
    green400 = Color(0xFF2F855A),
    green500 = Color(0xFF38A169),
    green600 = Color(0xFF48BB78),
    green700 = Color(0xFF68D391),
    green800 = Color(0xFF9AE6B4),
    green900 = Color(0xFFC6F6D5),

    neutral000 = Color(0xFF000000),
    neutral025 = Color(0xFF0C0C0C),
    neutral050 = Color(0xFF212121),
    neutral100 = Color(0xFF424242),
    neutral200 = Color(0xFF616161),
    neutral300 = Color(0xFF757575),
    neutral400 = Color(0xFF9E9E9E),
    neutral500 = Color(0xFFBDBDBD),
    neutral600 = Color(0xFFE0E0E0),
    neutral700 = Color(0xFFEEEEEE),
    neutral800 = Color(0xFFF5F5F5),
    neutral900 = Color(0xFFFAFAFA),
    neutral999 = Color(0xFFFFFFFF),

    primary100 = Color(0x7F33517A),
    primary200 = Color(0xFF2A4365),
    primary300 = Color(0xFF2C5282),
    primary400 = Color(0xFF2B6CB0),
    primary500 = Color(0xFF3182CE),
    primary600 = Color(0xFF4299E1),
    primary700 = Color(0xFF63B3ED),
    primary800 = Color(0xFF90CDF4),
    primary900 = Color(0xFFBEE3F8),

    red100 = Color(0x7F742A2A),
    red200 = Color(0xFF742A2A),
    red300 = Color(0xFF9B2C2C),
    red400 = Color(0xFFC53030),
    red500 = Color(0xFFE53E3E),
    red600 = Color(0xFFF56565),
    red700 = Color(0xFFFC8181),
    red800 = Color(0xFFFEB2B2),
    red900 = Color(0xFFFED7D7),

    yellow100 = Color(0x4CECC94B),
    yellow200 = Color(0xFF744210),
    yellow300 = Color(0xFF975A16),
    yellow400 = Color(0xFFB7791F),
    yellow500 = Color(0xFFD69E2E),
    yellow600 = Color(0xFFECC94B),
    yellow700 = Color(0xFFF6E05E),
    yellow800 = Color(0xFFFAF089),
    yellow900 = Color(0xFFFEFCBF),

    scanOverlayErrorOutline = Color(0xFFF86A6A),
    scanOverlayErrorFill = Color(0x33E12D39),
    scanOverlaySavedOutline = Color(0xFF00FF64),
    scanOverlaySavedFill = Color(0x3300D353),
    scanOverlayHoldOutline = Color(0xFFFFFFFF),
    scanOverlayHoldFill = Color(0x26FFFFFF)
)
