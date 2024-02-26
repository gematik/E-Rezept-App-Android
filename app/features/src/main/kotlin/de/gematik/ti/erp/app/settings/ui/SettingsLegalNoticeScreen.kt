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

package de.gematik.ti.erp.app.settings.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.compose.shortToast

class SettingsLegalNoticeScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()

        AnimatedElevationScaffold(
            listState = listState,
            navigationMode = NavigationBarMode.Back,
            topBarTitle = stringResource(id = R.string.legal_notice_menu),
            onBack = navController::popBackStack
        ) { innerPadding ->
            LegalNoticeScreenContent(
                innerPadding,
                listState
            )
        }
    }
}

@Composable
private fun LegalNoticeScreenContent(
    innerPadding: PaddingValues,
    listState: LazyListState
) {
    LazyColumn(
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        state = listState
    ) {
        item {
            IssuerSection()
        }
        item {
            ResponsibilitySection()
        }
        item {
            ContactSection()
        }
        item {
            HintSection()
        }
        item {
            LogoSection()
        }
    }
}

@Composable
private fun IssuerSection() {
    SpacerXLarge()
    Text(
        text = stringResource(id = R.string.legal_notice_issuer),
        style = AppTheme.typography.h6
    )
    SpacerSmall()
    Text(text = stringResource(id = R.string.legal_notice_address))
    SpacerSmall()
    Text(text = stringResource(id = R.string.legal_notice_info))
}

@Composable
private fun ResponsibilitySection() {
    SpacerLarge()
    Text(
        text = stringResource(id = R.string.legal_notice_responsible_header),
        style = AppTheme.typography.h6
    )
    SpacerSmall()
    Text(text = stringResource(id = R.string.legal_notice_responsible_name))
}

@Composable
private fun HintSection() {
    SpacerLarge()
    Text(
        text = stringResource(id = R.string.legal_notice_hint_header),
        style = AppTheme.typography.h6
    )
    SpacerSmall()
    Text(text = stringResource(id = R.string.legal_notice_hint))
}

@Composable
private fun LogoSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerLarge()
        Image(
            painterResource(R.drawable.ic_logo),
            null
        )
        SpacerSmall()
        Text(
            text = stringResource(id = R.string.legal_notice_logo_text),
            style = AppTheme.typography.body2
        )
    }
}

@Composable
private fun ContactSection() {
    val context = LocalContext.current
    SpacerLarge()
    Text(
        text = stringResource(id = R.string.legal_notice_contact_header),
        style = AppTheme.typography.h6
    )
    LinkToWeb(
        linkInfo = stringResource(id = R.string.menu_legal_notice_url_info),
        link = stringResource(id = R.string.menu_legal_notice_url),
        icon = Icons.Outlined.Feedback
    )
    EmailContact(
        context,
        emailText = stringResource(id = R.string.legal_notice_email_text),
        emailAddress = stringResource(id = R.string.legal_notice_email),
        emailIcon = Icons.Outlined.Email
    )
    PhoneContact(
        context,
        phoneInfo = stringResource(id = R.string.technical_hotline_info),
        phoneContact = stringResource(id = R.string.technical_hotline_contact),
        icon = Icons.Outlined.PhoneInTalk
    )
}

@Composable
fun PhoneContact(context: Context, phoneInfo: String, phoneContact: String, icon: ImageVector) {
    val color = AppTheme.colors.primary500
    val annotatedPhoneText =
        provideLinkForString(phoneInfo, phoneContact, "phone", linkColor = color)

    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        modifier = Modifier.padding(top = PaddingDefaults.Medium)
    ) {
        Icon(icon, null, tint = color)
        ClickableText(
            text = annotatedPhoneText,
            onClick = {
                annotatedPhoneText
                    .getStringAnnotations("phone", it, it)
                    .firstOrNull()?.let {
                        context.handleIntent(providePhoneIntent(phoneContact))
                    }
            }
        )
    }
}

@Composable
fun EmailContact(
    context: Context,
    emailText: String,
    emailAddress: String,
    emailIcon: ImageVector
) {
    val color = AppTheme.colors.primary500
    val annotatedEmail =
        provideLinkForString(emailText, annotation = emailAddress, "email", linkColor = color)
    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        modifier = Modifier.padding(top = PaddingDefaults.Medium)
    ) {
        Icon(emailIcon, null, tint = color)
        val noEmailClientText = stringResource(R.string.contact_email_no_client)
        ClickableText(
            text = annotatedEmail,
            onClick = {
                annotatedEmail
                    .getStringAnnotations("email", it, it)
                    .firstOrNull()?.let { _ ->
                        val intent = provideEmailIntent(emailAddress)
                        if (canHandleIntent(intent, context.packageManager)) {
                            context.startActivity(intent)
                        } else {
                            context.shortToast(noEmailClientText)
                        }
                    }
            }
        )
    }
}

@Composable
fun LinkToWeb(linkInfo: String, link: String, icon: ImageVector) {
    val color = AppTheme.colors.primary500
    val uriHandler = LocalUriHandler.current
    val annotatedLink =
        provideLinkForString(linkInfo, annotation = link, tag = "URL", linkColor = color)
    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        modifier = Modifier.padding(top = PaddingDefaults.Medium)
    ) {
        Icon(icon, null, tint = color)

        ClickableText(
            text = annotatedLink,
            onClick = {
                annotatedLink
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            }
        )
    }
}

fun provideLinkForString(
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
                textDecoration = TextDecoration.Underline
            ),
            start = start,
            end = end
        )
        addStringAnnotation(
            tag = tag,
            annotation = annotation,
            start = start,
            end = end
        )
    }
