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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.model.EditProfilePictureSelectionState
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenBottomPopUpNames
import de.gematik.ti.erp.app.mainscreen.presentation.AppController
import de.gematik.ti.erp.app.mainscreen.ui.components.EditProfilePicture
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import java.util.UUID

@Stable
sealed class MainScreenBottomSheetContentState {
    @Stable
    class EditProfilePicture(
        val popUp: MainScreenBottomPopUpNames.EditProfilePicture = MainScreenBottomPopUpNames.EditProfilePicture,
        val profile: ProfilesUseCaseData.Profile
    ) : MainScreenBottomSheetContentState()

    @Stable
    class EditProfileName(
        val popUp: MainScreenBottomPopUpNames.EditProfileName = MainScreenBottomPopUpNames.EditProfileName,
        val profile: ProfilesUseCaseData.Profile
    ) : MainScreenBottomSheetContentState()

    @Stable
    class AddProfile(
        val popUp: MainScreenBottomPopUpNames.AddProfile = MainScreenBottomPopUpNames.AddProfile
    ) : MainScreenBottomSheetContentState()

    @Stable
    class Welcome(
        val popUp: MainScreenBottomPopUpNames.Welcome = MainScreenBottomPopUpNames.Welcome
    ) : MainScreenBottomSheetContentState()

    @Stable
    class GrantConsent(
        val popUp: MainScreenBottomPopUpNames.GrantConsent = MainScreenBottomPopUpNames.GrantConsent
    ) : MainScreenBottomSheetContentState()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreenBottomSheetContentState(
    mainNavController: NavController,
    appController: AppController,
    profile: ProfilesUseCaseData.Profile,
    existingProfiles: List<ProfilesUseCaseData.Profile>,
    infoContentState: MainScreenBottomSheetContentState?,
    keyboardController: SoftwareKeyboardController?,
    onGrantConsent: () -> Unit,
    onUpdateProfileName: (ProfileIdentifier, String) -> Unit,
    onAddProfileName: (String) -> Unit,
    onClearPersonalizedImage: (ProfileIdentifier) -> Unit,
    onPickImage: (EditProfilePictureSelectionState) -> Unit,
    onSelectAvatar: (ProfileIdentifier, ProfilesData.Avatar) -> Unit,
    onSelectProfileColor: (ProfilesUseCaseData.Profile, ProfilesData.ProfileColorNames) -> Unit,
    onCancel: () -> Unit
) {
    val title = when (infoContentState) {
        is MainScreenBottomSheetContentState.EditProfilePicture ->
            stringResource(R.string.mainscreen_bottom_sheet_edit_profile_image)

        is MainScreenBottomSheetContentState.EditProfileName ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)

        is MainScreenBottomSheetContentState.AddProfile ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)

        else -> null
    }

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title?.let {
            SpacerMedium()
            Text(
                text = it,
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral999
            )
            SpacerMedium()
        }
        LazyColumn(
            Modifier.fillMaxWidth()
        ) {
            item {
                infoContentState?.let {
                    when (it) {
                        is MainScreenBottomSheetContentState.EditProfilePicture ->
                            EditProfilePicture(
                                profile = it.profile,
                                clearPersonalizedImage = {
                                    onClearPersonalizedImage(it.profile.id)
                                },
                                onPickImage = onPickImage,
                                onSelectAvatar = { avatar ->
                                    onSelectAvatar(it.profile.id, avatar)
                                },
                                onSelectProfileColor = { color ->
                                    onSelectProfileColor(it.profile, color)
                                }
                            )

                        is MainScreenBottomSheetContentState.EditProfileName -> {
                            ProfileSheetContent(
                                key = UUID.randomUUID().toString(),
                                existingProfiles = existingProfiles,
                                keyboardController = keyboardController,
                                addProfile = false,
                                profileToEdit = it.profile,
                                onUpdate = onUpdateProfileName,
                                onAdd = onAddProfileName,
                                onCancel = onCancel
                            )
                        }

                        is MainScreenBottomSheetContentState.AddProfile ->
                            ProfileSheetContent(
                                key = UUID.randomUUID().toString(),
                                existingProfiles = existingProfiles,
                                keyboardController = keyboardController,
                                addProfile = true,
                                profileToEdit = null,
                                onUpdate = onUpdateProfileName,
                                onAdd = onAddProfileName,
                                onCancel = onCancel
                            )

                        is MainScreenBottomSheetContentState.Welcome ->
                            ConnectBottomSheetContent(
                                onClickConnect = {
                                    appController.welcomeDrawerShown()
                                    mainNavController.navigate(
                                        CardWallRoutes.CardWallIntroScreen.path(profile.id)
                                    )
                                },
                                onCancel = {
                                    appController.welcomeDrawerShown()
                                    onCancel()
                                }
                            )

                        is MainScreenBottomSheetContentState.GrantConsent ->
                            GrantConsentBottomSheetContent(
                                onClickGrantConsent = onGrantConsent,
                                onCancel = onCancel
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectBottomSheetContent(onClickConnect: () -> Unit, onCancel: () -> Unit) {
    ConnectBottomSheet(
        header = stringResource(R.string.mainscreen_welcome_drawer_header),
        info = stringResource(R.string.mainscreen_welcome_drawer_info),
        image = painterResource(R.drawable.man_phone_blue_circle),
        connectButtonText = stringResource(R.string.mainscreen_connect_bottomsheet_connect),
        cancelButtonText = stringResource(R.string.mainscreen_connect_bottomsheet_connect_later),
        onClickConnect = onClickConnect,
        onCancel = onCancel
    )
}

@Composable
private fun GrantConsentBottomSheetContent(onClickGrantConsent: () -> Unit, onCancel: () -> Unit) {
    ConnectBottomSheet(
        header = stringResource(R.string.give_consent_bottom_sheet_header),
        info = stringResource(R.string.give_consent_bottom_sheet_info),
        image = painterResource(R.drawable.pharmacist_circle_blue),
        connectButtonText = stringResource(R.string.give_consent_bottom_sheet_activate),
        cancelButtonText = stringResource(R.string.give_consent_bottom_sheet_activate_later),
        onClickConnect = onClickGrantConsent,
        onCancel = onCancel
    )
}

@Composable
private fun ConnectBottomSheet(
    header: String,
    info: String,
    image: Painter,
    connectButtonText: String,
    cancelButtonText: String,
    onClickConnect: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerSmall()
        Image(
            image,
            null
        )
        Text(
            header,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            text = info,
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()
        PrimaryButton(
            modifier = Modifier
                .testTag(TestTag.Main.MainScreenBottomSheet.GetConsentButton),
            onClick = onClickConnect,
            contentPadding = PaddingValues(
                vertical = 13.dp,
                horizontal = 48.dp
            )
        ) {
            Text(
                connectButtonText
            )
        }
        SpacerMedium()
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.Main.MainScreenBottomSheet.ConnectLaterButton),
            contentPadding = PaddingValues(
                vertical = 13.dp
            )
        ) {
            Text(
                cancelButtonText
            )
        }
    }
}
