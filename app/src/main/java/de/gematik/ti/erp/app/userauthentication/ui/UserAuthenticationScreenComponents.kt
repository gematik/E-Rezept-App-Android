/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.userauthentication.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.settings.ui.PasswordTextField
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedLinkString
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UserAuthenticationScreen(userAuthViewModel: UserAuthenticationViewModel = hiltViewModel()) {
    val flag = painterResource(R.drawable.ic_onboarding_logo_flag)
    val gematik = painterResource(R.drawable.ic_onboarding_logo_gematik)
    val context = LocalContext.current

    var showAuthPrompt by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    var initiallyHandledAuthPrompt by rememberSaveable { mutableStateOf(false) }
    val state by produceState(userAuthViewModel.defaultState) {
        userAuthViewModel.screenState().collect {
            value = it
            if (!initiallyHandledAuthPrompt && it.nrOfAuthFailures == 0) {
                showAuthPrompt = true
            }
            initiallyHandledAuthPrompt = true
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, top = 40.dp)
                    .align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(flag, null, modifier = Modifier.padding(end = 10.dp))
                Icon(gematik, null, tint = AppTheme.colors.primary900)
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = PaddingDefaults.Large)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!showError && state.nrOfAuthFailures > 0) {
                    HintCard(
                        modifier = Modifier.padding(
                            top = PaddingDefaults.Large
                        ),
                        properties = HintCardDefaults.flatProperties(
                            backgroundColor = AppTheme.colors.red100
                        ),
                        image = {
                            HintSmallImage(
                                painterResource(R.drawable.oh_no_girl_hint_red),
                                innerPadding = it
                            )
                        },
                        title = { Text(stringResource(R.string.auth_error_failed_auths_headline)) },
                        body = {
                            Text(
                                annotatedPluralsResource(
                                    R.plurals.auth_error_failed_auths_info,
                                    state.nrOfAuthFailures,
                                    AnnotatedString(state.nrOfAuthFailures.toString())
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }
                if (!showError) {
                    Text(
                        stringResource(R.string.auth_headline),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight(700)),
                        modifier = Modifier.padding(top = 80.dp)
                    )
                    SpacerMedium()
                } else {
                    Image(
                        painterResource(R.drawable.woman_red_shirt_circle_red),
                        null,
                        modifier = Modifier.padding(top = 40.dp, start = 56.dp, end = 56.dp)
                    )
                }
                Text(
                    stringResource(if (showError) R.string.auth_subtitle_error else R.string.auth_subtitle),
                    style = MaterialTheme.typography.subtitle1
                )
                SpacerTiny()
                Text(
                    stringResource(if (showError) R.string.auth_info_error else R.string.auth_info),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1,
                    color = AppTheme.typographyColors.subtitle1l
                )
                SpacerLarge()
                Button(
                    onClick = {
                        showAuthPrompt = true
                    },
                    elevation = ButtonDefaults.elevation(8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.LockOpen, null)
                    SpacerTiny()
                    Text(stringResource(R.string.auth_button))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showError) {
                Column(
                    modifier = Modifier
                        .background(color = AppTheme.colors.neutral100)
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    val uriHandler = LocalUriHandler.current
                    val phoneContact = stringResource(R.string.auth_hotlinephone_contact)
                    val color = AppTheme.colors.primary600

                    val link = annotatedLinkString(
                        stringResource(R.string.auth_link_to_gematik),
                        stringResource(R.string.auth_link_to_gematik_text)
                    )
                    val annotatedPhoneText =
                        providePhoneString(phoneContact, phoneContact, "PHONE", linkColor = color)

                    ClickableTaggedText(
                        annotatedStringResource(R.string.auth_more_hotline, annotatedPhoneText),
                        style = AppTheme.typography.subtitle2l.merge(TextStyle(textAlign = TextAlign.Center)),
                        onClick = {
                            context.handleIntent(providePhoneIntent(phoneContact))
                        }
                    )
                    SpacerSmall()
                    ClickableTaggedText(
                        annotatedStringResource(R.string.auth_more_web, link),
                        style = AppTheme.typography.subtitle2l.merge(TextStyle(textAlign = TextAlign.Center)),
                        onClick = { range ->
                            uriHandler.openUri(range.item)
                        }
                    )
                }
            } else {
                Image(
                    painterResource(R.drawable.crew),
                    null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }

    if (showAuthPrompt) {
        when (state.authenticationMethod) {
            SettingsAuthenticationMethod.Password ->
                PasswordPrompt(
                    userAuthViewModel,
                    onAuthenticated = {
                        showAuthPrompt = false
                        userAuthViewModel.onAuthenticated()
                    },
                    onCancel = {
                        showAuthPrompt = false
                    },
                    onAuthenticationError = {
                        userAuthViewModel.onFailedAuthentication()
                        showAuthPrompt = false
                        showError = true
                    }
                )
            else ->
                BiometricPrompt(
                    authenticationMethod = state.authenticationMethod,
                    title = stringResource(R.string.auth_prompt_headline),
                    description = "",
                    negativeButton = stringResource(R.string.auth_prompt_cancel),
                    onAuthenticated = {
                        showAuthPrompt = false
                        userAuthViewModel.onAuthenticated()
                    },
                    onCancel = {
                        showAuthPrompt = false
                    },
                    onAuthenticationError = {
                        userAuthViewModel.onFailedAuthentication()
                        showAuthPrompt = false
                        showError = true
                    },
                    onAuthenticationSoftError = {
                        userAuthViewModel.onFailedAuthentication()
                    }
                )
        }
    }
}

@Composable
private fun PasswordPrompt(
    viewModel: UserAuthenticationViewModel,
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit,
    onAuthenticationError: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onCancel
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(PaddingDefaults.Small)
        ) {
            Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                PasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    allowAutofill = true,
                    allowVisiblePassword = true,
                    label = {
                        Text(stringResource(R.string.auth_prompt_enter_password))
                    },
                    onSubmit = {}
                )
                SpacerLarge()
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onCancel
                    ) {
                        Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
                    }
                    TextButton(
                        enabled = password.isNotEmpty(),
                        onClick = {
                            coroutineScope.launch {
                                if (viewModel.isPasswordValid(password)) {
                                    onAuthenticated()
                                } else {
                                    onAuthenticationError()
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.auth_prompt_check_password).uppercase(Locale.getDefault()))
                    }
                }
            }
        }
    }
}

fun providePhoneString(
    text: String,
    annotation: String = text,
    tag: String,
    start: Int = 0,
    end: Int = text.length,
    linkColor: Color
) =
    buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                color = linkColor,
                fontWeight = FontWeight.Bold
            ),
            start = start, end = end
        )
        addStringAnnotation(
            tag = tag,
            annotation = annotation,
            start = start,
            end = end
        )
    }
