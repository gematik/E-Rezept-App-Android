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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.firstCharOfForeNameSurName

@Composable
fun Avatar(
    modifier: Modifier,
    name: String,
    profileColor: ProfileColor,
    ssoStatusColor: Color?,
    active: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.body2
) {
    val text = remember(name) { firstCharOfForeNameSurName(name) }
    Box(modifier = modifier.fillMaxSize().aspectRatio(1f), contentAlignment = Alignment.Center) {
        CircleBox(
            profileColor.backGroundColor,
            border = if (active) BorderStroke(2.dp, profileColor.borderColor) else null,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = textStyle,
            color = profileColor.textColor,
            textAlign = TextAlign.Center,
        )
        if (ssoStatusColor != null) {
            CircleBox(
                backgroundColor = ssoStatusColor,
                border = BorderStroke(2.dp, MaterialTheme.colors.background),
                modifier = Modifier.size(16.dp).align(Alignment.BottomEnd).offset(4.dp, 4.dp)
            )
        }
    }
}

@Composable
private fun CircleBox(
    backgroundColor: Color,
    modifier: Modifier,
    border: BorderStroke? = null
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .aspectRatio(1f)
            .background(backgroundColor)
            .then(
                border?.let { Modifier.border(border, CircleShape) } ?: Modifier
            )
    )
}

@Preview
@Composable
private fun AvatarPreview() {
    AppTheme {
        Avatar(
            modifier = Modifier.size(36.dp), name = "Ina Müller",
            profileColor = ProfileColor(
                textColor = AppTheme.colors.red700,
                colorName = stringResource(R.string.profile_color_name_pink),
                backGroundColor = AppTheme.colors.red200,
                borderColor = AppTheme.colors.red400
            ),
            ssoStatusColor = null, active = false
        )
    }
}

@Preview
@Composable
private fun AvatarWithSSOPreview() {
    AppTheme {
        Avatar(
            modifier = Modifier.size(36.dp), name = "Ina Müller",
            profileColor = ProfileColor(
                textColor = AppTheme.colors.red700,
                colorName = stringResource(R.string.profile_color_name_pink),
                backGroundColor = AppTheme.colors.red200,
                borderColor = AppTheme.colors.red400
            ),
            ssoStatusColor = Color.Green, active = false
        )
    }
}
