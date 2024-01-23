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

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun Avatar(
    modifier: Modifier,
    emptyIcon: ImageVector,
    profile: ProfilesUseCaseData.Profile,
    ssoStatusColor: Color?,
    active: Boolean = false,
    iconModifier: Modifier
) {
    val currentSelectedColors = profileColor(profileColorNames = profile.color)

    Box(
        modifier = modifier
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
                    modifier = iconModifier,
                    avatar = profile.avatar
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
            modifier = Modifier.size(36.dp),
            profile = ProfilesUseCaseData.Profile(
                id = "",
                name = "",
                insurance = de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.NONE
                ),
                active = false,
                color = ProfilesData.ProfileColorNames.SUN_DEW,
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null,
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
            modifier = Modifier.size(36.dp),
            profile = ProfilesUseCaseData.Profile(
                id = "",
                name = "",
                insurance = de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.NONE
                ),
                active = false,

                color = ProfilesData.ProfileColorNames.SUN_DEW,
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null,
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

@Composable
fun ChooseAvatar(
    modifier: Modifier = Modifier,
    useSmallImages: Boolean? = false,
    profile: ProfilesUseCaseData.Profile,
    emptyIcon: ImageVector,
    showPersonalizedImage: Boolean = true,
    avatar: ProfilesData.Avatar
) {
    val imageResource = extractImageResource(useSmallImages, avatar)

    when (avatar) {
        ProfilesData.Avatar.PersonalizedImage -> {
            if (showPersonalizedImage) {
                if (profile.image != null) {
                    BitmapImage(profile)
                } else {
                    Icon(
                        emptyIcon,
                        modifier = modifier,
                        contentDescription = null,
                        tint = AppTheme.colors.neutral600
                    )
                }
            }
        }

        else -> {
            if (imageResource == 0) {
                Icon(
                    emptyIcon,
                    modifier = modifier,
                    contentDescription = null,
                    tint = AppTheme.colors.neutral600
                )
            } else {
                Image(
                    painterResource(id = imageResource),
                    null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Suppress("ComplexMethod")
@Composable
private fun extractImageResource(
    useSmallImages: Boolean? = false,
    figure: ProfilesData.Avatar
) = if (useSmallImages == true) {
    when (figure) {
        ProfilesData.Avatar.FemaleDoctor -> R.drawable.femal_doctor_small_portrait
        ProfilesData.Avatar.WomanWithHeadScarf -> R.drawable.woman_with_head_scarf_small_portrait
        ProfilesData.Avatar.Grandfather -> R.drawable.grand_father_small_portrait
        ProfilesData.Avatar.BoyWithHealthCard -> R.drawable.boy_with_health_card_small_portrait
        ProfilesData.Avatar.OldManOfColor -> R.drawable.old_man_of_color_small_portrait
        ProfilesData.Avatar.WomanWithPhone -> R.drawable.woman_with_phone_small_portrait
        ProfilesData.Avatar.Grandmother -> R.drawable.grand_mother_small_portrait
        ProfilesData.Avatar.ManWithPhone -> R.drawable.man_with_phone_small_portrait
        ProfilesData.Avatar.WheelchairUser -> R.drawable.wheel_chair_user_small_portrait
        ProfilesData.Avatar.Baby -> R.drawable.baby_small_portrait
        ProfilesData.Avatar.MaleDoctorWithPhone -> R.drawable.doctor_with_phone_small_portrait
        ProfilesData.Avatar.FemaleDoctorWithPhone -> R.drawable.femal_doctor_with_phone_small_portrait
        ProfilesData.Avatar.FemaleDeveloper -> R.drawable.femal_developer_small_portrait
        else -> 0
    }
} else {
    when (figure) {
        ProfilesData.Avatar.FemaleDoctor -> R.drawable.femal_doctor_portrait
        ProfilesData.Avatar.WomanWithHeadScarf -> R.drawable.woman_with_head_scarf_portrait
        ProfilesData.Avatar.Grandfather -> R.drawable.grand_father_portrait
        ProfilesData.Avatar.BoyWithHealthCard -> R.drawable.boy_with_health_card_portrait
        ProfilesData.Avatar.OldManOfColor -> R.drawable.old_man_of_color_portrait
        ProfilesData.Avatar.WomanWithPhone -> R.drawable.woman_with_phone_portrait
        ProfilesData.Avatar.Grandmother -> R.drawable.grand_mother_portrait
        ProfilesData.Avatar.ManWithPhone -> R.drawable.man_with_phone_portrait
        ProfilesData.Avatar.WheelchairUser -> R.drawable.wheel_chair_user_portrait
        ProfilesData.Avatar.Baby -> R.drawable.baby_portrait
        ProfilesData.Avatar.MaleDoctorWithPhone -> R.drawable.doctor_with_phone_portrait
        ProfilesData.Avatar.FemaleDoctorWithPhone -> R.drawable.femal_doctor_with_phone_portrait
        ProfilesData.Avatar.FemaleDeveloper -> R.drawable.femal_developer_portrait
        else -> 0
    }
}

@Composable
private fun BitmapImage(profile: ProfilesUseCaseData.Profile) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, profile) {
        value = profile.image?.let {
            BitmapFactory.decodeByteArray(profile.image, 0, it.size).asImageBitmap()
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
