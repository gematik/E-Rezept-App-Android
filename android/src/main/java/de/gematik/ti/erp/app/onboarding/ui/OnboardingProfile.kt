/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ProfileNameInputField
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer32
import de.gematik.ti.erp.app.utils.compose.Spacer8

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OnboardingProfile(
    modifier: Modifier = Modifier,
    isReturningUser: Boolean = false,
    profileName: String,
    onProfileNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val header = stringResource(R.string.onboarding_profile_header)
    val info = stringResource(R.string.onboarding_profile_info)

    Column(
        modifier = modifier
            .testTag("onboarding/profilePage")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PaddingDefaults.Large, vertical = PaddingDefaults.XXLarge)
    ) {

        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))

        var profileNameError by remember { mutableStateOf(false) }
        val keyboardController = LocalSoftwareKeyboardController.current

        ProfileNameInputField(
            modifier = Modifier
                .testTag("onboarding/profile_text_input")
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = profileName,
            onValueChange = {
                onProfileNameChange(it)
                profileNameError = it.isEmpty()
            },
            onSubmit = {
                if (!profileNameError) {
                    keyboardController?.hide()
                    onNext()
                }
            },
            label = {
                Text(stringResource(R.string.onboarding_profile_input_name))
            },
            isError = profileNameError,
            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = AppTheme.colors.neutral999)
        )

        Spacer8()
        if (profileNameError) {
            Text(
                text = stringResource(R.string.edit_profile_empty_profile_name),
                color = AppTheme.colors.red600,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
            Spacer16()
        }

        Text(
            text = info,
            style = MaterialTheme.typography.body2,
            color = AppTheme.colors.neutral600,
        )
        if (isReturningUser) {
            Spacer32()
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onNext() },
                    enabled = profileName.isNotEmpty()
                ) {
                    Text(text = stringResource(id = R.string.profile_setup_save).uppercase())
                }
            }
        }
    }
}
