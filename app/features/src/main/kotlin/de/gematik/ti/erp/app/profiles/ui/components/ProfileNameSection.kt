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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.containsProfileWithName
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.ErrorText
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.extensions.sanitizeProfileName
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.launch

@Composable
fun ProfileNameSection(
    profileState: UiState<ProfileCombinedData>,
    color: Color,
    keyboardController: SoftwareKeyboardController?,
    onUpdateProfileName: (String) -> Unit
) {
    val initialProfileName = profileState.data?.selectedProfile?.name ?: ""
    var profileName by remember(profileState) { mutableStateOf(initialProfileName) }
    var profileNameValid by remember { mutableStateOf(true) }
    var textFieldEnabled by remember { mutableStateOf(false) }
    val editProfileNameDescription = annotatedStringResource(
        R.string.edit_profile_name_button,
        profileName
    ).toString()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(textFieldEnabled) {
        if (textFieldEnabled) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column {
        Row(
            modifier = Modifier.padding(PaddingDefaults.Medium)
        ) {
            if (!textFieldEnabled) {
                val txt =
                    buildAnnotatedString {
                        append(profileName)
                        append(" ")
                        appendInlineContent("edit", "edit")
                    }
                val inlineContent =
                    mapOf(
                        "edit" to
                            InlineTextContent(
                                Placeholder(
                                    width = 0.em,
                                    height = 0.em,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                                )
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = AppTheme.colors.neutral400)
                            }
                    )
                DynamicText(
                    txt,
                    style = AppTheme.typography.h5,
                    inlineContent = inlineContent,
                    modifier =
                    Modifier
                        .clickable {
                            textFieldEnabled = true
                        }
                        .testTag(TestTag.Profile.EditProfileNameButton)
                        .clearAndSetSemantics {
                            role = Role.Button
                            contentDescription = editProfileNameDescription
                        }
                )
            } else {
                ProfileEditBasicTextField(
                    modifier =
                    Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag(TestTag.Profile.NewProfileNameField),
                    enabled = textFieldEnabled,
                    initialProfileName = initialProfileName,
                    onChangeProfileName = { name: String, isValid: Boolean ->
                        profileName = name
                        profileNameValid = isValid
                    },
                    color = color,
                    profiles = profileState.data?.profiles ?: emptyList(),
                    onDone = {
                        if (profileNameValid) {
                            onUpdateProfileName(profileName.trim())
                            textFieldEnabled = false
                            scope.launch { keyboardController?.hide() }
                        }
                    }
                )
            }
        }

        if (!profileNameValid) {
            SpacerTiny()
            val errorText =
                stringResource(R.string.edit_profile_empty_profile_name)

            ErrorText(
                text = errorText,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
    }
}

@Composable
private fun ProfileEditBasicTextField(
    modifier: Modifier,
    enabled: Boolean,
    textStyle: TextStyle = AppTheme.typography.h5,
    color: Color,
    initialProfileName: String,
    onChangeProfileName: (String, Boolean) -> Unit,
    profiles: List<ProfilesUseCaseData.Profile>,
    onDone: () -> Unit
) {
    var profileNameState by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialProfileName,
                selection = TextRange(initialProfileName.length)
            )
        )
    }

    val mergedTextStyle = textStyle.merge(TextStyle(color = color))

    BasicTextField(
        value = profileNameState,
        onValueChange = {
            val name = it.text.trimStart().sanitizeProfileName()
            profileNameState =
                TextFieldValue(
                    text = name,
                    selection = it.selection,
                    composition = it.composition
                )

            onChangeProfileName(
                name,
                name.trim().equals(initialProfileName, true) ||
                    !profiles.containsProfileWithName(name) &&
                    name.isNotEmpty()
            )
        },
        enabled = enabled,
        singleLine = !enabled,
        textStyle = mergedTextStyle,
        modifier = modifier,
        cursorBrush = SolidColor(color),
        keyboardOptions =
        KeyboardOptions(
            autoCorrect = false,
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}
