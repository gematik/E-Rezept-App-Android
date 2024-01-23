/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.common.common.theme

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
data class AppTypography(
    val body1l: TextStyle,
    val body2l: TextStyle,
    val subtitle1l: TextStyle,
    val subtitle2l: TextStyle,
    val captionl: TextStyle,
    val overlinel: TextStyle
)
