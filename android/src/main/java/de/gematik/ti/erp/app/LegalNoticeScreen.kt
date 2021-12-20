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

package de.gematik.ti.erp.app

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent

@Composable
fun LegalNoticeWithScaffold(navigation: NavHostController) {
    val header = stringResource(id = R.string.legal_notice_menu)
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                title = header,
                onBack = { navigation.popBackStack() }
            )
        }
    ) { innerPadding ->
        LegalNoticeScreen(Modifier.padding(innerPadding))
    }
}

@Composable
fun LegalNoticeScreen(modifier: Modifier) {
    val issuer = stringResource(id = R.string.legal_notice_issuer)
    val address = stringResource(id = R.string.legal_notice_address)
    val info = stringResource(id = R.string.legal_notice_info)
    val responsibleForHeader = stringResource(id = R.string.legal_notice_responsible_header)
    val responsibleForName = stringResource(id = R.string.legal_notice_responsible_name)
    val hintHeader = stringResource(id = R.string.legal_notice_hint_header)
    val hint = stringResource(id = R.string.legal_notice_hint)

    val logo = painterResource(R.drawable.ic_logo)
    val logoText = stringResource(id = R.string.legal_notice_logo_text)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = issuer,
            modifier = modifier
                .padding(top = 24.dp),
            style = MaterialTheme.typography.h6
        )
        Text(text = address, modifier = modifier)
        Text(text = info, modifier = modifier)

        Text(
            text = responsibleForHeader,
            modifier = modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.h6
        )
        Text(text = responsibleForName, modifier = modifier)

        Contact(modifier = modifier)

        Text(
            text = hintHeader,
            modifier = modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.h6
        )
        Text(text = hint, modifier = modifier)

        Image(
            logo, null,
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = logoText,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
fun Contact(modifier: Modifier) {
    val context = LocalContext.current
    val contactHeader = stringResource(id = R.string.legal_notice_contact_header)
    Text(
        text = contactHeader,
        modifier = modifier.padding(top = 24.dp),
        style = MaterialTheme.typography.h6
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
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
                            Toast.makeText(context, noEmailClientText, Toast.LENGTH_SHORT).show()
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
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
            start = start, end = end
        )
        addStringAnnotation(
            tag = tag,
            annotation = annotation,
            start = start,
            end = end
        )
    }
