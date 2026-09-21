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
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    emptyIcon: ImageVector,
    imageData: ProfileImageDataErpModel,
    active: Boolean = false,
    iconModifier: Modifier
) {
    val currentSelectedColors = profileColor(profileColorNames = imageData.color)
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
            color = currentSelectedColors.backgroundColor,
            border = if (active) BorderStroke(SizeDefaults.quarter, currentSelectedColors.borderColor) else null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    image = imageData.image,
                    profileColor = imageData.color.color(),
                    emptyIcon = emptyIcon,
                    modifier = iconModifier,
                    avatar = imageData.avatar
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
    avatar: Avatar
) {
    when (avatar) {
        Avatar.PersonalizedImage -> {
            if (image != null) {
                BitmapImage(
                    modifier = Modifier.background(profileColor.backgroundColor),
                    image = image
                )
            } else {
                Icon(
                    imageVector = emptyIcon,
                    tint = AppTheme.colors.neutral700,
                    contentDescription = null
                )
            }
        }

        else -> {
            val imageResource = extractImageResource(useSmallImages, avatar)
            if (imageResource == 0) {
                Icon(
                    modifier = modifier.background(profileColor.backgroundColor),
                    imageVector = emptyIcon,
                    tint = AppTheme.colors.neutral700,
                    contentDescription = null
                )
            } else {
                Image(
                    modifier = Modifier
                        .testTag(avatar.name)
                        .fillMaxSize()
                        .background(profileColor.backgroundColor),
                    painter = painterResource(id = imageResource),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun Avatar.toDescription(): String =
    when (this) {
        Avatar.FemaleDoctor -> stringResource(R.string.female_doctor)
        Avatar.WomanWithHeadScarf -> stringResource(R.string.woman_with_headscarf)
        Avatar.Grandfather -> stringResource(R.string.grandfather)
        Avatar.BoyWithHealthCard -> stringResource(R.string.boy_with_health_card)
        Avatar.OldManOfColor -> stringResource(R.string.old_man_of_color)
        Avatar.WomanWithPhone -> stringResource(R.string.woman_with_phone)
        Avatar.Grandmother -> stringResource(R.string.grandmother)
        Avatar.ManWithPhone -> stringResource(R.string.man_with_phone)
        Avatar.WheelchairUser -> stringResource(R.string.wheelchair_user)
        Avatar.Baby -> stringResource(R.string.baby)
        Avatar.MaleDoctorWithPhone -> stringResource(R.string.male_doctor_with_phone)
        Avatar.FemaleDoctorWithPhone -> stringResource(R.string.female_doctor_with_phone)
        Avatar.FemaleDeveloper -> stringResource(R.string.female_developer)
        Avatar.PersonalizedImage -> stringResource(R.string.personalized_image)
    }

@Suppress("ComplexMethod")
@Composable
private fun extractImageResource(
    useSmallImages: Boolean? = false,
    figure: Avatar
) = if (useSmallImages == true) {
    when (figure) {
        Avatar.FemaleDoctor -> R.drawable.femal_doctor_small_portrait
        Avatar.WomanWithHeadScarf -> R.drawable.woman_with_head_scarf_small_portrait
        Avatar.Grandfather -> R.drawable.grand_father_small_portrait
        Avatar.BoyWithHealthCard -> R.drawable.boy_with_health_card_small_portrait
        Avatar.OldManOfColor -> R.drawable.old_man_of_color_small_portrait
        Avatar.WomanWithPhone -> R.drawable.woman_with_phone_small_portrait
        Avatar.Grandmother -> R.drawable.grand_mother_small_portrait
        Avatar.ManWithPhone -> R.drawable.man_with_phone_small_portrait
        Avatar.WheelchairUser -> R.drawable.wheel_chair_user_small_portrait
        Avatar.Baby -> R.drawable.baby_small_portrait
        Avatar.MaleDoctorWithPhone -> R.drawable.doctor_with_phone_small_portrait
        Avatar.FemaleDoctorWithPhone -> R.drawable.femal_doctor_with_phone_small_portrait
        Avatar.FemaleDeveloper -> R.drawable.femal_developer_small_portrait
        else -> 0
    }
} else {
    when (figure) {
        Avatar.FemaleDoctor -> R.drawable.femal_doctor_portrait
        Avatar.WomanWithHeadScarf -> R.drawable.woman_with_head_scarf_portrait
        Avatar.Grandfather -> R.drawable.grand_father_portrait
        Avatar.BoyWithHealthCard -> R.drawable.boy_with_health_card_portrait
        Avatar.OldManOfColor -> R.drawable.old_man_of_color_portrait
        Avatar.WomanWithPhone -> R.drawable.woman_with_phone_portrait
        Avatar.Grandmother -> R.drawable.grand_mother_portrait
        Avatar.ManWithPhone -> R.drawable.man_with_phone_portrait
        Avatar.WheelchairUser -> R.drawable.wheel_chair_user_portrait
        Avatar.Baby -> R.drawable.baby_portrait
        Avatar.MaleDoctorWithPhone -> R.drawable.doctor_with_phone_portrait
        Avatar.FemaleDoctorWithPhone -> R.drawable.femal_doctor_with_phone_portrait
        Avatar.FemaleDeveloper -> R.drawable.femal_developer_portrait
        else -> 0
    }
}

@Preview
@Composable
private fun AvatarPreview() {
    AppTheme {
        Avatar(
            modifier = Modifier.size(SizeDefaults.fourfoldAndHalf),
            imageData = ProfileImageDataErpModel(
                image = null,
                avatar = Avatar.PersonalizedImage,
                color = ProfileColorNames.SUN_DEW
            ),
            active = false,
            iconModifier = Modifier.size(SizeDefaults.doubleHalf),
            emptyIcon = Icons.Rounded.AddAPhoto
        )
    }
}
