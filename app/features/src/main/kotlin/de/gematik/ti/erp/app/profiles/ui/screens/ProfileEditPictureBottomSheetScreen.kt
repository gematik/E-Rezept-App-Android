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

package de.gematik.ti.erp.app.profiles.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileEditPictureController
import de.gematik.ti.erp.app.profiles.ui.components.ProfileBackgroundColorComponent
import de.gematik.ti.erp.app.profiles.ui.components.ProfileImageSelectorDialog
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.CenterColumn
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

class ProfileEditPictureBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val dialog = LocalDialog.current
        val profileId = remember { navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID) }
        val controller = rememberProfileEditPictureController(profileId)
        val profileData by controller.profile.collectAsStateWithLifecycle()
        val imageTypeDialogEvent by lazy { ComposableEvent<Unit>() }

        ProfileImageSelectorDialog(
            composableEvent = imageTypeDialogEvent,
            dialogScaffold = dialog,
            onPickEmojiImage = {
                profileId?.let { profileId ->
                    navController.navigate(
                        ProfileRoutes.ProfileImageEmojiScreen.path(profileId)
                    )
                }
            },
            onPickPersonalizedImage = {
                profileId?.let { profileId ->
                    navController.navigate(
                        ProfileRoutes.ProfileImageCropperScreen.path(profileId)
                    )
                }
            },
            onPickCamera = {
                profileId?.let { profileId ->
                    navController.navigate(
                        ProfileRoutes.ProfileImageCameraScreen.path(profileId)
                    )
                }
            }
        )

        UiStateMachine(
            state = profileData,
            onError = {
                ErrorScreenComponent()
            },
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onContent = { profile ->
                ProfileEditAvatarScreenContent(
                    profile = profile,
                    clearPersonalizedImage = {
                        controller.clearPersonalizedImage()
                    },
                    onPickPersonalizedImage = {
                        imageTypeDialogEvent.trigger()
                    },
                    onSelectAvatar = {
                        controller.updateAvatar(it)
                    },
                    onSelectProfileColor = {
                        controller.updateProfileColor(it)
                    }
                )
            }
        )
    }
}

@Composable
private fun ProfileEditAvatarScreenContent(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.Avatar) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    var editableProfile by remember(profile.id) { mutableStateOf(profile) }
    Column(
        modifier = Modifier.wrapContentSize()
    ) {
        SpacerMedium()
        ProfileImage(editableProfile) {
            editableProfile = editableProfile.copy(
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null
            )
            clearPersonalizedImage()
        }

        SpacerXXLarge()
        AvatarPicker(
            profile = editableProfile,
            currentAvatar = editableProfile.avatar,
            onPickPersonalizedImage = onPickPersonalizedImage,
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

@Composable
@LightDarkPreview
fun ProfileEditAvatarScreenContentPreview() {
    PreviewAppTheme {
        ProfileEditAvatarScreenContent(
            profile = ProfilesUseCaseData.Profile(
                id = "1",
                name = "Max Mustermann",
                insurance = ProfileInsuranceInformation(
                    insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                ),
                isActive = true,
                color = ProfilesData.ProfileColorNames.SPRING_GRAY,
                lastAuthenticated = null,
                ssoTokenScope = null,
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null
            ),
            clearPersonalizedImage = {},
            onPickPersonalizedImage = {},
            onSelectAvatar = {},
            onSelectProfileColor = {}
        )
    }
}
