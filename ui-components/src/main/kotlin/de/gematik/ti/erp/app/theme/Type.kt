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

@file:Suppress("LongParameterList")

package de.gematik.ti.erp.app.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Immutable
data class AppTypographyColors(
    val body1l: Color,
    val body2l: Color,
    val subtitle1l: Color,
    val subtitle2l: Color,
    val captionl: Color
)

@Immutable
class AppTypography(
    // overloaded fonts with different lighter color
    val body1l: TextStyle,
    val body2l: TextStyle,
    val subtitle1l: TextStyle,
    val subtitle2l: TextStyle,
    val caption1l: TextStyle,
    // material theme default fonts
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val subtitle1: TextStyle,
    val subtitle2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val button: TextStyle,
    val caption1: TextStyle,
    val caption2: TextStyle,
    val overline: TextStyle
)
