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

package de.gematik.ti.erp.app.messages.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.messages.ui.models.CommunicationReply
import de.gematik.ti.erp.app.messages.ui.models.ErrorUIMessage
import de.gematik.ti.erp.app.messages.ui.models.UIMessage
import de.gematik.ti.erp.app.messages.usecase.ERROR
import de.gematik.ti.erp.app.messages.usecase.LOCAL
import de.gematik.ti.erp.app.messages.usecase.SHIPMENT
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.flow.collect
import java.lang.StringBuilder

@ExperimentalMaterialApi
@Composable
fun MessageScreen(mainNavController: NavController, viewModel: MessageViewModel) {
    val result by produceState(initialValue = listOf<CommunicationReply>()) {
        viewModel.fetchCommunications().collect { value = it }
    }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    if (result.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().testTag("message_screen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.messages_empty_screen),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.testTag("emptyMessagesHeader")
            )
            Spacer16()
            Text(
                stringResource(id = R.string.messages_empty_screen_info),
                style = AppTheme.typography.body2l,
                modifier = Modifier.testId("msgs_txt_empty_list")
            )
        }
    } else {
        LazyColumn(
            Modifier
                .padding(start = PaddingDefaults.Tiny, end = PaddingDefaults.Medium)
                .testTag("lazyColumn")
        ) {
            item {
                SpacerMedium()
            }
            items(items = result) { message ->
                when (message) {
                    is UIMessage -> {
                        Message(
                            message,
                            { viewModel.messageAcknowledged(message.copy(consumed = true)) }
                        ) {
                            when (message.supplyOptionsType) {
                                LOCAL -> {
                                    mainNavController.navigate(
                                        MainNavigationScreens.PickUpCode.path(
                                            pickUpCodeHR = message.pickUpCodeHR,
                                            pickUpCodeDMC = message.pickUpCodeDMC
                                        )
                                    )
                                }
                                SHIPMENT -> {
                                    message.url?.let { url ->
                                        uriHandler.openUri(url)
                                    }
                                }
                            }
                        }
                    }
                    is ErrorUIMessage -> {
                        val mailTo = stringResource(id = R.string.messages_contact_mail_address)
                        val subject = stringResource(id = R.string.messages_contact_email_subject)
                        val body = stringResource(id = R.string.messages_contact_email_body)
                        val errorCode =
                            stringResource(id = R.string.messages_contact_email_error_code)
                        val dataInfo =
                            stringResource(id = R.string.messages_contact_email_data_transparency)
                        val emailBody =
                            generateBody(
                                body,
                                dataInfo,
                                errorCode,
                                message.message ?: "",
                                message.timeStamp
                            )
                        Message(
                            message = message,
                            onRowClick = { viewModel.messageAcknowledged(message.copy(consumed = true)) }
                        ) {
                            email(mailTo, subject, emailBody, context)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Message(
    message: CommunicationReply,
    onRowClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val icon = when (message.supplyOptionsType) {
        SHIPMENT -> Icons.Filled.OpenInBrowser
        ERROR -> Icons.Default.KeyboardArrowRight
        else -> Icons.Default.QrCode
    }
    val color = if (message.consumed) Color.Transparent else AppTheme.colors.primary500
    val infoText =
        if (message is UIMessage) message.message else stringResource(id = (message as ErrorUIMessage).displayText)
    Row(
        modifier = Modifier.clickable {
            onRowClick()
        }
    ) {
        NewMessageDot(color)
        Spacer8()
        Column {
            Text(stringResource(id = message.header), style = MaterialTheme.typography.subtitle1)
            Spacer8()
            Text(
                infoText
                    ?: stringResource(id = R.string.communication_info_text_not_available),
                style = MaterialTheme.typography.body1
            )
            Spacer8()
            if (message.actionText != -1) {
                Spacer8()
                Row {
                    Text(
                        modifier = Modifier.clickable {
                            onRowClick()
                            onActionClick()
                        },
                        text = stringResource(id = message.actionText),
                        color = AppTheme.colors.primary600
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = icon,
                        tint = AppTheme.colors.primary600,
                        contentDescription = ""
                    )
                }
                Spacer8()
            }
            Divider(
                modifier = Modifier.padding(
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Medium
                )
            )
        }
    }
}

@Composable
fun NewMessageDot(color: Color) {
    Canvas(
        modifier = Modifier
            .padding(start = PaddingDefaults.Tiny, top = 5.dp)
            .size(12.dp),
        onDraw = {
            drawCircle(color = color)
        }
    )
}

fun email(address: String, subject: String, message: String, context: Context) {
    val intent = emailIntent(address, subject, message)
    if (canHandleIntent(intent, context.packageManager)) {
        context.startActivity(intent)
    }
}

private fun emailIntent(address: String, subject: String, body: String) = Intent().apply {
    data = (Uri.parse("mailto:"))
    action = Intent.ACTION_SENDTO
    putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
    putExtra(Intent.EXTRA_SUBJECT, subject)
    putExtra(Intent.EXTRA_TEXT, body)
}

private fun generateBody(
    firstPart: String,
    secondPart: String,
    errorCode: String,
    message: String,
    time: String
): String {
    val body = StringBuilder()
        .append(firstPart)
        .append("\n\n")
        .append(secondPart)
        .append("\n\n")
        .append("----------------")
        .append("\n\n")
        .append(errorCode)
        .append("\n\n")
        .append(message)
        .append("\n\n")
        .append("App Version Code: ${BuildConfig.VERSION_CODE}")
        .append("\n\n")
        .append("OS Version: ${System.getProperty("os.version")}")
        .append("\n\n")
        .append("Device Info: ${Build.MODEL}")
        .append("\n\n")
        .append("Server Timestamp: $time")

    return body.toString()
}
