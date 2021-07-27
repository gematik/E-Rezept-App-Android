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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedLinkString
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent

@Composable
fun UserAuthenticationScreen(userAuthViewModel: UserAuthenticationViewModel) {
    val flag = painterResource(R.drawable.ic_onboarding_logo_flag)
    val gematik = painterResource(R.drawable.ic_onboarding_logo_gematik)
    val context = LocalContext.current

    var showBiometricPrompt by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    Scaffold {
        Column(modifier = Modifier.fillMaxSize()) {
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
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(top = 64.dp, bottom = 24.dp).fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Large).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (showError) {
                        Image(
                            painterResource(R.drawable.woman_red_shirt_circle_red),
                            null,
                            modifier = Modifier.padding(top = 40.dp, start = 80.dp, end = 80.dp)
                        )
                    } else {
                        Text(
                            stringResource(R.string.auth_headline),
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.padding(top = 80.dp)
                        )
                        SpacerMedium()
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
                            showBiometricPrompt = true
                        },
                        elevation = ButtonDefaults.elevation(8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Rounded.LockOpen, null)
                        SpacerTiny()
                        Text(stringResource(R.string.auth_button))
                    }
                }

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
    }

    val descriptionText =
        if (userAuthViewModel.screenState.authenticationMethod == SettingsAuthenticationMethod.Biometrics) {
            stringResource(R.string.auth_prompt_info_biometrics)
        } else {
            ""
        }

    if (showBiometricPrompt) {
        BiometricPrompt(
            authenticationMode = userAuthViewModel.screenState.authenticationMethod,
            title = stringResource(R.string.auth_prompt_headline),
            description = descriptionText,
            negativeButton = stringResource(R.string.auth_prompt_cancel),
            onAuthenticated = {
                showBiometricPrompt = false
                userAuthViewModel.onAuthenticated()
            },
            onCancel = {
                showBiometricPrompt = false
            },
            onAuthenticationError = {
                showBiometricPrompt = false
                showError = true
            }
        )
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
