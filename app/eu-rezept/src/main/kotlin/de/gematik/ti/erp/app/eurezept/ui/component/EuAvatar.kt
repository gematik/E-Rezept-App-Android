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

package de.gematik.ti.erp.app.eurezept.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
fun EuAvatar(
    profile: ProfilesUseCaseData.Profile,
    size: Dp,
    emptyIcon: ImageVector = Icons.Rounded.PersonOutline,
    modifier: Modifier = Modifier
) {
    val profileColors = getProfileColor(profile.color)

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = profileColors.backgroundColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (profile.avatar) {
                ProfilesData.Avatar.PersonalizedImage -> {
                    if (profile.image != null) {
                        profile.image?.let {
                            PersonalizedImage(
                                imageData = it,
                                backgroundColor = profileColors.backgroundColor
                            )
                        }
                    } else {
                        Icon(
                            imageVector = emptyIcon,
                            contentDescription = null,
                            tint = AppTheme.colors.neutral600
                        )
                    }
                }
                else -> {
                    val imageResource = getAvatarImageResource(profile.avatar)
                    if (imageResource != 0) {
                        Image(
                            painter = painterResource(id = imageResource),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(profileColors.backgroundColor)
                        )
                    } else {
                        Icon(
                            imageVector = emptyIcon,
                            contentDescription = null,
                            tint = AppTheme.colors.neutral600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalizedImage(
    imageData: ByteArray,
    backgroundColor: Color
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, imageData) {
        value = try {
            BitmapFactory.decodeByteArray(imageData, 0, imageData.size).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        )
    }
}

@Composable
private fun getProfileColor(colorName: ProfilesData.ProfileColorNames): ProfileColor {
    return when (colorName) {
        ProfilesData.ProfileColorNames.SPRING_GRAY -> ProfileColor(
            backgroundColor = AppTheme.colors.neutral200
        )
        ProfilesData.ProfileColorNames.SUN_DEW -> ProfileColor(
            backgroundColor = AppTheme.colors.yellow200
        )
        ProfilesData.ProfileColorNames.PINK -> ProfileColor(
            backgroundColor = AppTheme.colors.red200
        )
        ProfilesData.ProfileColorNames.TREE -> ProfileColor(
            backgroundColor = AppTheme.colors.green200
        )
        ProfilesData.ProfileColorNames.BLUE_MOON -> ProfileColor(
            backgroundColor = AppTheme.colors.primary200
        )
    }
}

private data class ProfileColor(
    val backgroundColor: Color
)

private fun getAvatarImageResource(avatar: ProfilesData.Avatar): Int {
    return when (avatar) {
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

@LightDarkPreview(name = "Avatar")
@Composable
private fun AvatarColorsPreview() {
    PreviewTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            Box(
                modifier = Modifier
                    .size(SizeDefaults.sixfold)
                    .background(AppTheme.colors.neutral200, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                EuAvatar(
                    profile = createPreviewProfile(
                        avatar = ProfilesData.Avatar.BoyWithHealthCard,
                        color = ProfilesData.ProfileColorNames.SUN_DEW
                    ),
                    size = SizeDefaults.fivefold
                )
            }
            Text(
                text = "Max Mustermann",
                style = AppTheme.typography.h6,
                fontSize = SizeDefaults.double.value.sp
            )
        }
    }
}

private fun createPreviewProfile(
    avatar: ProfilesData.Avatar,
    color: ProfilesData.ProfileColorNames
): ProfilesUseCaseData.Profile {
    return ProfilesUseCaseData.Profile(
        id = "preview-id",
        name = "Preview User",
        insurance = ProfileInsuranceInformation(
            insuranceType = ProfilesUseCaseData.InsuranceType.GKV
        ),
        isActive = true,
        color = color,
        avatar = avatar,
        image = null,
        lastAuthenticated = null,
        ssoTokenScope = null
    )
}
