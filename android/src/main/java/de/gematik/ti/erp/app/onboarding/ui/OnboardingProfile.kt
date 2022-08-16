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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.utils.sanitizeProfileName

@Composable
fun OnboardingProfile(
    modifier: Modifier = Modifier,
    isReturningUser: Boolean = false,
    profileName: String,
    onProfileNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    var profileNameError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OnboardingLazyColumn(
        state = lazyListState,
        modifier = modifier
            .visualTestTag(TestTag.Onboarding.ProfileScreen)
            .fillMaxSize()
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.doctor_blue_circle),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.XXLarge
                    )
                    .fillMaxWidth()
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics(mergeDescendants = true) {}
            ) {
                Text(
                    text = stringResource(R.string.onboarding_profile_header),
                    style = AppTheme.typography.h4,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = PaddingDefaults.Medium, top = PaddingDefaults.XXLarge)
                )
                Text(
                    text = stringResource(R.string.onboarding_profile_information),
                    style = AppTheme.typography.body1l,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = PaddingDefaults.XLarge)
                )
            }
        }
        item {
            val inputDescription = stringResource(R.string.on_boarding_profil_input_description)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics(mergeDescendants = true) {}
            ) {
                InputField(
                    modifier = Modifier
                        .visualTestTag(TestTag.Onboarding.Profile.ProfileField)
                        .fillMaxWidth()
                        .scrollOnFocus(2, lazyListState)
                        .semantics { contentDescription = inputDescription },
                    value = profileName,
                    onValueChange = {
                        val name = sanitizeProfileName(it.trimStart())
                        onProfileNameChange(name)
                        profileNameError = name.isEmpty()
                    },
                    onSubmit = {
                        if (!profileNameError) {
                            focusManager.clearFocus()
                            onNext()
                        }
                    },
                    label = {
                        Text(stringResource(R.string.onboarding_profile_input_name))
                    },
                    isError = profileNameError,
                    colors = TextFieldDefaults.outlinedTextFieldColors()
                )
                if (profileNameError) {
                    SpacerSmall()
                    Text(
                        text = stringResource(R.string.edit_profile_empty_profile_name),
                        color = AppTheme.colors.red600,
                        style = AppTheme.typography.caption1,
                        modifier = Modifier.padding(
                            start = PaddingDefaults.Medium
                        )
                    )
                }
            }
        }
        item {
            if (isReturningUser) {
                SpacerXLarge()
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
}
