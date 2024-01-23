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
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun Avatar(
    avatarModifier: Modifier,
    emptyIcon: ImageVector,
    profile: ProfilesUseCaseData.Profile,
    ssoStatusColor: Color?,
    active: Boolean = false,
    iconModifier: Modifier
) {
    val currentSelectedColors = profileColor(profileColorNames = profile.color)

    Box(
        modifier = avatarModifier
            .fillMaxSize()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            shape = CircleShape,
            color = currentSelectedColors.backGroundColor,
            border = if (active) BorderStroke(2.dp, currentSelectedColors.borderColor) else null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    profile = profile,
                    emptyIcon = emptyIcon,
                    iconModifier = iconModifier,
                    figure = profile.avatarFigure
                )
            }
        }
        if (ssoStatusColor != null) {
            CircleBox(
                backgroundColor = ssoStatusColor,
                border = BorderStroke(2.dp, MaterialTheme.colors.background),
                modifier = Modifier
                    .size(PaddingDefaults.Medium)
                    .align(Alignment.BottomEnd)
                    .offset(PaddingDefaults.Tiny, PaddingDefaults.Tiny)
            )
        }
    }
}

@Composable
fun CircleBox(
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
            avatarModifier = Modifier.size(36.dp),
            profile = ProfilesUseCaseData.Profile(
                id = "",
                name = "",
                insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.NONE
                ),
                active = false,
                color = ProfilesData.ProfileColorNames.SUN_DEW,
                avatarFigure = ProfilesData.AvatarFigure.PersonalizedImage,
                personalizedImage = null,
                lastAuthenticated = null,
                ssoTokenScope = null
            ),
            ssoStatusColor = null,
            active = false,
            iconModifier = Modifier.size(20.dp),
            emptyIcon = Icons.Rounded.AddAPhoto
        )
    }
}

@Preview
@Composable
private fun AvatarWithSSOPreview() {
    AppTheme {
        Avatar(
            avatarModifier = Modifier.size(36.dp),
            profile = ProfilesUseCaseData.Profile(
                id = "",
                name = "",
                insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.NONE
                ),
                active = false,

                color = ProfilesData.ProfileColorNames.SUN_DEW,
                avatarFigure = ProfilesData.AvatarFigure.PersonalizedImage,
                personalizedImage = null,
                lastAuthenticated = null,
                ssoTokenScope = null
            ),
            ssoStatusColor = Color.Green,
            active = false,
            iconModifier = Modifier.size(20.dp),
            emptyIcon = Icons.Rounded.AddAPhoto
        )
    }
}
