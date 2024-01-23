/*
 * Copyright (c) 2024 gematik GmbH
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.LaunchedEffect
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.PasswordTextField
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedLinkString
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import kotlinx.coroutines.launch
import java.util.Locale
@Suppress("LongMethod")
@Composable
fun UserAuthenticationScreen() {
    val authentication = rememberAuthenticationController()
    val scope = rememberCoroutineScope()
    var showAuthPrompt by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var initiallyHandledAuthPrompt by rememberSaveable { mutableStateOf(false) }
    val authenticationState by authentication.authenticationState
    val navBarInsetsPadding = WindowInsets.systemBars.asPaddingValues()
    val paddingModifier = if (navBarInsetsPadding.calculateBottomPadding() <= PaddingDefaults.Medium) {
        Modifier.statusBarsPadding()
    } else {
        Modifier.systemBarsPadding()
    }
    // clear underlying text input focus
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus(true)
        if (!initiallyHandledAuthPrompt && authenticationState.nrOfAuthFailures == 0) {
            showAuthPrompt = true
        }
        initiallyHandledAuthPrompt = true
    }

    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(paddingModifier)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = PaddingDefaults.Medium)
                    .padding(horizontal = PaddingDefaults.Medium)
                    .align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(R.drawable.ic_onboarding_logo_flag),
                    null,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Icon(
                    painterResource(R.drawable.ic_onboarding_logo_gematik),
                    null,
                    tint = AppTheme.colors.primary900
                )
            }

            if (showError) {
                AuthenticationScreenErrorContent(
                    showAuthPromptOnClick = { showAuthPrompt = true }
                )
            } else {
                AuthenticationScreenContent(
                    showAuthPromptOnClick = { showAuthPrompt = true },
                    state = authenticationState
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (showError) {
                AuthenticationScreenErrorBottomContent(
                    state = authenticationState
                )
            } else {
                Image(
                    painterResource(R.drawable.crew),
                    null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Medium),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }

    if (showAuthPrompt) {
        when (authenticationState.authenticationMethod) {
            is SettingsData.AuthenticationMode.Password ->
                PasswordPrompt(
                    authentication,
                    onAuthenticated = {
                        showAuthPrompt = false
                        scope.launch { authentication.onAuthenticated() }
                    },
                    onCancel = {
                        showAuthPrompt = false
                    },
                    onAuthenticationError = {
                        scope.launch { authentication.onFailedAuthentication() }
                        showAuthPrompt = false
                        showError = true
                    }
                )
            else ->
                BiometricPrompt(
                    title = stringResource(R.string.auth_prompt_headline),
                    description = "",
                    negativeButton = stringResource(R.string.auth_prompt_cancel),
                    onAuthenticated = {
                        showAuthPrompt = false
                        scope.launch { authentication.onAuthenticated() }
                    },
                    onCancel = {
                        showAuthPrompt = false
                    },
                    onAuthenticationError = {
                        scope.launch { authentication.onFailedAuthentication() }
                        showAuthPrompt = false
                        showError = true
                    },
                    onAuthenticationSoftError = {
                        scope.launch { authentication.onFailedAuthentication() }
                    }
                )
        }
    }
}

@Composable
private fun AuthenticationScreenErrorContent(
    showAuthPromptOnClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Image(
            painterResource(R.drawable.woman_red_shirt_circle_red),
            null,
            alignment = Alignment.Center
        )
        SpacerMedium()
        Text(
            stringResource(R.string.auth_subtitle_error),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            stringResource(R.string.auth_info_error),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.body1l
        )
        SpacerLarge()
        PrimaryButton(
            onClick = showAuthPromptOnClick,
            elevation = ButtonDefaults.elevation(8.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(
                horizontal = PaddingDefaults.Large,
                vertical = PaddingDefaults.ShortMedium
            )
        ) {
            Icon(Icons.Rounded.LockOpen, null)
            SpacerTiny()
            SpacerSmall()
            Text(stringResource(R.string.auth_button))
        }
    }
}

@Composable
private fun AuthenticationScreenErrorBottomContent(state: AuthenticationStateData.AuthenticationState) {
    Column(
        modifier = Modifier
            .background(color = AppTheme.colors.neutral100)
            .padding(
                bottom = PaddingDefaults.Large,
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium
            )
            .fillMaxWidth()
    ) {
        val uriHandler = LocalUriHandler.current
        val link = annotatedLinkString(
            stringResource(R.string.auth_link_to_gematik_q_and_a),
            stringResource(R.string.auth_link_to_gematik_helptext)
        )
        when (state.authenticationMethod) {
            SettingsData.AuthenticationMode.DeviceSecurity ->
                Text(
                    text = stringResource(R.string.auth_failed_biometry_info),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
            else ->
                ClickableTaggedText(
                    annotatedStringResource(R.string.auth_failed_password_info, link),
                    style = AppTheme.typography.body2l.merge(TextStyle(textAlign = TextAlign.Center)),
                    onClick = { range ->
                        uriHandler.openUri(range.item)
                    }
                )
        }
    }
}

@Composable
private fun AuthenticationScreenContent(
    showAuthPromptOnClick: () -> Unit,
    state: AuthenticationStateData.AuthenticationState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.nrOfAuthFailures > 0) {
            HintCard(
                modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
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
        } else {
            Spacer(modifier = Modifier.height(80.dp))
        }

        Text(
            stringResource(R.string.auth_headline),
            style = AppTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        SpacerMedium()
        Text(
            stringResource(R.string.auth_subtitle),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            stringResource(R.string.auth_information),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.body1l
        )
        SpacerLarge()
        PrimaryButton(
            onClick = showAuthPromptOnClick,
            elevation = ButtonDefaults.elevation(8.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(
                horizontal = PaddingDefaults.Large,
                vertical = PaddingDefaults.ShortMedium
            )
        ) {
            Icon(Icons.Rounded.LockOpen, null)
            SpacerTiny()
            SpacerSmall()
            Text(stringResource(R.string.auth_button))
        }
    }
}

@Composable
private fun PasswordPrompt(
    authenticationState: AuthenticationController,
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit,
    onAuthenticationError: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        buttons = {
            if (BuildKonfig.INTERNAL) {
                OutlinedDebugButton("SKIP", onClick = onAuthenticated)
            }
            TextButton(
                onClick = onCancel
            ) {
                Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
            }
            TextButton(
                enabled = password.isNotEmpty(),
                onClick = {
                    coroutineScope.launch {
                        if (authenticationState.isPasswordValid(password)) {
                            onAuthenticated()
                        } else {
                            onAuthenticationError()
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.auth_prompt_check_password).uppercase(Locale.getDefault()))
            }
        },
        title = null,
        text = {
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
        }
    )
}
