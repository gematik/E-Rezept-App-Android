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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.extensions.circularBorder

@Composable
fun ProfileAvatarSection(
    profile: ProfilesUseCaseData.Profile,
    onClickEditAvatar: () -> Unit
) {
    val selectedColor = profileColor(profileColorNames = profile.color)
    SpacerLarge()
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        SpacerTiny()
        Surface(
            modifier =
            Modifier
                .size(SizeDefaults.eighteenfold)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = selectedColor.backGroundColor
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .circularBorder(selectedColor.borderColor)
                    .clickable(onClick = onClickEditAvatar),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    modifier = Modifier.size(SizeDefaults.triple),
                    image = profile.image,
                    profileColor = profile.color.color(),
                    avatar = profile.avatar
                )
            }
        }
        SpacerSmall()
        TextButton(
            modifier =
            Modifier
                .align(Alignment.CenterHorizontally)
                .testTag(TestTag.Profile.EditProfileImageButton),
            onClick = onClickEditAvatar
        ) {
            Text(text = stringResource(R.string.edit_profile_avatar), textAlign = TextAlign.Center)
        }
    }
}
