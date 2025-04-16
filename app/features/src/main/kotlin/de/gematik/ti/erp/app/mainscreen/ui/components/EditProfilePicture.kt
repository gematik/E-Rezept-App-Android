/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.mainscreen.model.EditProfilePictureSelectionState
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.ui.components.ProfileBackgroundColorComponent
import de.gematik.ti.erp.app.profiles.ui.components.showProfileImageSelectorDialog
import de.gematik.ti.erp.app.profiles.ui.screens.AvatarPicker
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileImage
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.CenterColumn
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

@Suppress("ComplexMethod")
@Composable
internal fun EditProfilePicture(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickImage: (EditProfilePictureSelectionState) -> Unit,
    onSelectAvatar: (ProfilesData.Avatar) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    val dialog = LocalDialog.current
    var editableProfile by remember(profile.id) { mutableStateOf(profile) }

    Column(modifier = Modifier.fillMaxSize()) {
        SpacerMedium()
        ProfileImage(
            selectedProfile = editableProfile,
            onClickDeleteAvatar = {
                editableProfile = editableProfile.copy(
                    avatar = ProfilesData.Avatar.PersonalizedImage,
                    image = null
                )
                clearPersonalizedImage()
            }
        )

        SpacerXXLarge()
        AvatarPicker(
            profile = editableProfile,
            currentAvatar = editableProfile.avatar,
            onPickPersonalizedImage = {
                showProfileImageSelectorDialog(
                    dialogScaffold = dialog,
                    onPickEmojiImage = {
                        onPickImage(EditProfilePictureSelectionState.Emoji)
                    },
                    onPickPersonalizedImage = {
                        onPickImage(EditProfilePictureSelectionState.PersonalizedImage)
                    },
                    onPickCamera = {
                        onPickImage(EditProfilePictureSelectionState.Camera)
                    }
                )
            },
            onSelectAvatar = {
                editableProfile = editableProfile.copy(avatar = it)
                onSelectAvatar(it)
            }
        )
        SpacerSmall()
        CenterColumn {
            ProfileBackgroundColorComponent(
                color = editableProfile.color,
                onColorPicked = {
                    editableProfile = editableProfile.copy(color = it)
                    onSelectProfileColor(it)
                }
            )
        }
    }
}
