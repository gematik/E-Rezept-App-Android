/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun Avatar(
    modifier: Modifier,
    emptyIcon: ImageVector,
    profile: ProfilesUseCaseData.Profile,
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
            border = if (active) BorderStroke(SizeDefaults.quarter, currentSelectedColors.borderColor) else null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    image = profile.image,
                    profileColor = profile.color.color(),
                    emptyIcon = emptyIcon,
                    modifier = iconModifier,
                    avatar = profile.avatar
                )
            }
        }
    }
}

@Composable
fun ChooseAvatar(
    modifier: Modifier = Modifier,
    useSmallImages: Boolean? = false,
    image: ByteArray?,
    profileColor: ProfileColor,
    emptyIcon: ImageVector,
    avatar: ProfilesData.Avatar
) {
    when (avatar) {
        ProfilesData.Avatar.PersonalizedImage -> {
            if (image != null) {
                BitmapImage(
                    modifier = Modifier.background(profileColor.backGroundColor),
                    image = image
                )
            } else {
                Icon(
                    imageVector = emptyIcon,
                    tint = AppTheme.colors.neutral600,
                    contentDescription = null
                )
            }
        }

        else -> {
            val imageResource = extractImageResource(useSmallImages, avatar)
            if (imageResource == 0) {
                Icon(
                    modifier = modifier.background(profileColor.backGroundColor),
                    imageVector = emptyIcon,
                    tint = AppTheme.colors.neutral600,
                    contentDescription = null
                )
            } else {
                Image(
                    modifier = Modifier
                        .testTag(avatar.name)
                        .fillMaxSize()
                        .background(profileColor.backGroundColor),
                    painter = painterResource(id = imageResource),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ProfilesData.Avatar.toDescription(): String =
    when (this) {
        ProfilesData.Avatar.FemaleDoctor -> stringResource(R.string.female_doctor)
        ProfilesData.Avatar.WomanWithHeadScarf -> stringResource(R.string.woman_with_headscarf)
        ProfilesData.Avatar.Grandfather -> stringResource(R.string.grandfather)
        ProfilesData.Avatar.BoyWithHealthCard -> stringResource(R.string.boy_with_health_card)
        ProfilesData.Avatar.OldManOfColor -> stringResource(R.string.old_man_of_color)
        ProfilesData.Avatar.WomanWithPhone -> stringResource(R.string.woman_with_phone)
        ProfilesData.Avatar.Grandmother -> stringResource(R.string.grandmother)
        ProfilesData.Avatar.ManWithPhone -> stringResource(R.string.man_with_phone)
        ProfilesData.Avatar.WheelchairUser -> stringResource(R.string.wheelchair_user)
        ProfilesData.Avatar.Baby -> stringResource(R.string.baby)
        ProfilesData.Avatar.MaleDoctorWithPhone -> stringResource(R.string.male_doctor_with_phone)
        ProfilesData.Avatar.FemaleDoctorWithPhone -> stringResource(R.string.female_doctor_with_phone)
        ProfilesData.Avatar.FemaleDeveloper -> stringResource(R.string.female_developer)
        ProfilesData.Avatar.PersonalizedImage -> stringResource(R.string.personalized_image)
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

@Preview
@Composable
private fun AvatarPreview() {
    AppTheme {
        Avatar(
            modifier = Modifier.size(SizeDefaults.fourfoldAndHalf),
            profile = ProfilesUseCaseData.Profile(
                id = "",
                name = "",
                insurance = ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.NONE
                ),
                isActive = false,
                color = ProfilesData.ProfileColorNames.SUN_DEW,
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null,
                lastAuthenticated = null,
                ssoTokenScope = null
            ),
            active = false,
            iconModifier = Modifier.size(SizeDefaults.doubleHalf),
            emptyIcon = Icons.Rounded.AddAPhoto
        )
    }
}
