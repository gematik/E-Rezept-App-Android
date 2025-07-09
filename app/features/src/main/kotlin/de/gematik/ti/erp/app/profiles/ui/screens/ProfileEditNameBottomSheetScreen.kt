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

package de.gematik.ti.erp.app.profiles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileEditNameController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.preview.ProfileEditData
import de.gematik.ti.erp.app.profiles.ui.preview.ProfilePreviewParameterProvider
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.isKeyboardVisible
import de.gematik.ti.erp.app.utils.extensions.sanitizeProfileName
import de.gematik.ti.erp.app.utils.compose.Center as ComposeCenter

class ProfileEditNameBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = false) {

    @Composable
    override fun Content() {
        val profileId = remember { navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID) }
        val controller = rememberProfileEditNameController(profileId)
        val combinedProfileData by controller.combinedProfile.collectAsStateWithLifecycle()
        val keyboardController = LocalSoftwareKeyboardController.current

        UiStateMachine(
            state = combinedProfileData,
            onEmpty = { ComposeCenter { ErrorScreenComponent(noMaxSize = true) } },
            onLoading = { FullScreenLoadingIndicator() },
            onError = { ComposeCenter { ErrorScreenComponent(noMaxSize = true) } }
        ) { data ->
            ProfileEditNameBottomSheetScreenContent(
                existingProfiles = data.profiles,
                profileToEdit = data.selectedProfile,
                keyboardController = keyboardController,
                addProfile = profileId == null,
                onUpdate = { _, newName -> controller.updateProfileName(newName) },
                onAdd = controller::addNewProfile,
                onCancel = navController::popBackStack
            )
        }
    }
}

/**
 * @param profileToEdit is used to get the profile to edit
 * @param existingProfiles: List<ProfilesUseCaseData.Profile> is used to get the list of existing profiles
 * @param keyboardController: SoftwareKeyboardController is used to control the keyboard
 * @param addProfile: Boolean is used to differentiate between adding a new profile and editing an existing one
 * @param onCancel: () -> Unit is used to dismiss the bottomsheet
 */
@Suppress("CyclomaticComplexMethod")
@Composable
internal fun ProfileEditNameBottomSheetScreenContent(
    existingProfiles: List<ProfilesUseCaseData.Profile>,
    profileToEdit: ProfilesUseCaseData.Profile?,
    keyboardController: SoftwareKeyboardController?,
    addProfile: Boolean = false,
    onUpdate: (ProfileIdentifier, String) -> Unit,
    onAdd: (String) -> Unit,
    onCancel: () -> Unit,
    initialTextValue: TextFieldValue = TextFieldValue(
        text = profileToEdit?.name.orEmpty(),
        selection = TextRange((profileToEdit?.name.orEmpty()).length)
    ),
    initialHasUserInteracted: Boolean = false
) {
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }

    var textValue by remember { mutableStateOf(initialTextValue) }
    var hasUserInteracted by remember { mutableStateOf(initialHasUserInteracted) }
    var isSaving by remember { mutableStateOf(false) }

    val showError by remember(textValue, hasUserInteracted) {
        derivedStateOf { textValue.text.isEmpty() && hasUserInteracted }
    }
    val canSave by remember(textValue, isSaving) {
        derivedStateOf { textValue.text.isNotEmpty() && !isSaving }
    }

    val onSave = {
        if (canSave) {
            isSaving = true
            when {
                addProfile -> onAdd(textValue.text)
                else -> profileToEdit?.let { onUpdate(it.id, textValue.text) }
            }
            onCancel()
            keyboardController?.hide()
        }
    }

    DisposableEffect(profileToEdit) {
        focusRequester.requestFocus()
        if (!view.isKeyboardVisible) keyboardController?.show()
        onDispose { keyboardController?.hide() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerLarge()

        ErezeptOutlineText(
            modifier = Modifier
                .testTag(TestTag.Main.MainScreenBottomSheet.ProfileNameField)
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(SizeDefaults.one),
            value = textValue,
            singleLine = true,
            enabled = !isSaving,
            onValueChange = { changedValue ->
                if (!isSaving) {
                    hasUserInteracted = true
                    textValue = changedValue.copy(
                        text = changedValue.text.sanitizeProfileName()
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions {
                if (canSave) onSave()
            },
            placeholder = { Text(stringResource(R.string.profile_edit_name_place_holder)) },
            isError = showError,
            trailingIcon = {
                Crossfade(
                    label = "",
                    targetState = textValue.text.isNotEmpty() && !isSaving
                ) { shouldShow ->
                    if (shouldShow) {
                        IconButton(
                            onClick = {
                                textValue = TextFieldValue(text = "", selection = TextRange(0))
                                hasUserInteracted = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        )

        AnimatedVisibility(showError) {
            Text(
                text = stringResource(R.string.edit_profile_empty_profile_name),
                color = AppTheme.colors.red600,
                style = AppTheme.typography.caption1,
                modifier = Modifier.padding(
                    top = PaddingDefaults.Medium,
                    start = PaddingDefaults.Medium
                )
            )
        }

        SpacerLarge()

        PrimaryButton(
            modifier = Modifier.testTag(TestTag.Main.MainScreenBottomSheet.SaveProfileNameButton),
            enabled = canSave,
            onClick = onSave
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(SizeDefaults.double),
                    color = AppTheme.colors.neutral000,
                    strokeWidth = SizeDefaults.quarter
                )
            } else {
                Text(stringResource(R.string.profile_bottom_sheet_save))
            }
        }
    }
}

@LightDarkPreview
@Composable
fun ProfileEditNameBottomSheetScreenContentPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) editProfilePreviewParameter: ProfileEditData
) {
    PreviewAppTheme {
        ProfileEditNameBottomSheetScreenContent(
            existingProfiles = listOf(editProfilePreviewParameter.profile),
            profileToEdit = editProfilePreviewParameter.profile,
            keyboardController = null,
            addProfile = false,
            onUpdate = { _, _ -> },
            onAdd = { },
            onCancel = { },
            initialTextValue = TextFieldValue(
                text = editProfilePreviewParameter.profile.name,
                selection = TextRange(editProfilePreviewParameter.profile.name.length)
            ),
            initialHasUserInteracted = editProfilePreviewParameter.initialHasUserInteracted
        )
    }
}
