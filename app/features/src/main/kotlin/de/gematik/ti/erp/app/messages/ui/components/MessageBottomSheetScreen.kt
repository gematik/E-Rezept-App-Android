/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutesBackStackEntryArguments
import de.gematik.ti.erp.app.messages.ui.preview.MessageSheetsPreviewData
import de.gematik.ti.erp.app.messages.ui.preview.MessagesPreviewParameterProvider
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ClickableAnnotatedText
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.copyToClipboardWithHaptic
import de.gematik.ti.erp.app.utils.compose.createBitMatrix
import de.gematik.ti.erp.app.utils.compose.createPhoneNumberAnnotations
import de.gematik.ti.erp.app.utils.compose.drawDataMatrix
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.letNotNull
import de.gematik.ti.erp.app.utils.letNotNullOnCondition
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MessageBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val arguments = MessagesRoutesBackStackEntryArguments(navBackStackEntry)

        arguments.getOrderDetail()?.let { orderDetail ->
            arguments.getSelectedMessage()?.let { message ->
                MessageBottomSheetScreenContent(
                    order = orderDetail,
                    message = message,
                    onClickClose = { navController.popBackStack() }
                )
            }
        } ?: run {
            ErrorScreenComponent()
        }
    }
}

@Composable
fun MessageBottomSheetScreenContent(
    order: OrderUseCaseData.OrderDetail?,
    message: OrderUseCaseData.Message?,
    onClickClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(bottom = PaddingDefaults.Large, top = PaddingDefaults.Medium)
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Small),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onClickClose,
                modifier = Modifier
                    .padding(top = SizeDefaults.one)
            ) {
                Box(
                    Modifier
                        .size(SizeDefaults.fourfold)
                        .background(AppTheme.colors.neutral100, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = AppTheme.colors.neutral600)
                }
            }
        }
        SpacerMedium()
        if (order == null && message == null) {
            ErrorScreenComponent(noMaxSize = true)
        }

        letNotNull(order, message) { order, message ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag(TestTag.Orders.Messages.Content)
            ) {
                when (message.type) {
                    OrderUseCaseData.Message.Type.All -> AllSheetContent(message)
                    OrderUseCaseData.Message.Type.Link -> LinkSheetContent(message)
                    OrderUseCaseData.Message.Type.PickUpCodeDMC,
                    OrderUseCaseData.Message.Type.PickUpCodeHR -> CodeSheetContent(message)
                    OrderUseCaseData.Message.Type.Text -> TextSheetContent(message)
                    OrderUseCaseData.Message.Type.Empty -> EmptySheetContent(order.pharmacy.pharmacyName())
                }
            }
        }
    }
}

@Composable
private fun AllSheetContent(
    message: OrderUseCaseData.Message
) {
    Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)) {
        CodeSheetContent(message)
        message.content?.let { TextSheetContent(message) }
        message.link?.let { LinkSheetContent(message) }
    }
}

@Composable
fun LinkSheetContent(
    message: OrderUseCaseData.Message
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTag.Orders.Messages.Link),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (message.type != OrderUseCaseData.Message.Type.All) {
            Text(
                stringResource(R.string.orders_cart_ready),
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            SpacerSmall()
            Text(
                stringResource(R.string.orders_cart_ready_info),
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            SpacerLarge()
        }
        PrimaryButtonSmall(
            modifier = Modifier.testTag(TestTag.Orders.Messages.LinkButton),
            onClick = {
                message.link?.let { uriHandler.openUriWhenValid(it) }
            }
        ) {
            Text(stringResource(R.string.orders_open_cart_link))
        }
    }
}

@Composable
fun TextSheetContent(
    message: OrderUseCaseData.Message,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(true) { testTag = TestTag.Orders.Messages.Text }
    ) {
        val messageText = message.content ?: ""

        ClickableAnnotatedText(
            text = createPhoneNumberAnnotations(
                text = messageText,
                textColor = Color.White,
                phoneNumberColor = AppTheme.colors.primary700
            ),
            style = AppTheme.typography.body2,
            onClick = { annotation ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${annotation.item}")
                }
                context.startActivity(intent)
            },
            onLongPress = {
                copyToClipboardWithHaptic(
                    text = messageText,
                    clipboardManager = clipboardManager,
                    hapticFeedback = hapticFeedback
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AppTheme.colors.neutral100,
                    shape = RoundedCornerShape(PaddingDefaults.Medium)
                )
                .padding(PaddingDefaults.Medium)
        )

        SpacerSmall()
        Text(
            sentOn(message),
            style = AppTheme.typography.caption1l,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun sentOn(message: OrderUseCaseData.Message): String =
    remember(message) {
        val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        dateFormatter.format(message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }

@Composable
fun CodeSheetContent(
    message: OrderUseCaseData.Message
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letNotNullOnCondition(
            first = message.pickUpCodeDMC,
            condition = { message.pickUpCodeDMC?.isNotBlank() == true }
        ) { code ->
            DataMatrixCode(payload = code, modifier = Modifier.size(SizeDefaults.eighteenfold))
            SpacerMedium()
        }

        letNotNullOnCondition(
            first = message.pickUpCodeHR,
            condition = { message.pickUpCodeHR?.isNotBlank() == true }
        ) { code ->
            CodeLabel(code = code)
            SpacerSmall()
        }

        if (message.pickUpCodeHR.isNotNullOrEmpty() || message.pickUpCodeDMC.isNotNullOrEmpty()) {
            OrdersCodeInfo()
        }
    }
}

@Composable
fun OrdersCodeInfo() {
    Text(
        stringResource(R.string.orders_code_title),
        style = AppTheme.typography.subtitle1,
        textAlign = TextAlign.Center
    )
    SpacerSmall()
    Text(
        stringResource(R.string.orders_code_info),
        style = AppTheme.typography.body2l,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CodeLabel(
    code: String
) {
    Box(
        Modifier
            .background(AppTheme.colors.neutral100, RoundedCornerShape(SizeDefaults.one))
            .padding(horizontal = PaddingDefaults.ShortMedium, vertical = PaddingDefaults.ShortMedium / 2)
            .semantics(true) { testTag = TestTag.Orders.Messages.CodeLabelContent }
    ) {
        Text(
            code,
            style = AppTheme.typography.subtitle2l,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun EmptySheetContent(pharmacyName: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .testTag(TestTag.Orders.Messages.Empty),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.orders_no_message_title),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            stringResource(R.string.orders_no_message, pharmacyName),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DataMatrixCode(payload: String, modifier: Modifier) {
    val matrix = remember(payload) { createBitMatrix(payload) }

    Box(
        modifier = Modifier
            .then(modifier)
            .background(Color.White)
            .padding(PaddingDefaults.Small)
            .testTag(TestTag.Orders.Messages.Code)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawDataMatrix(matrix)
        )
    }
}

@Composable
fun OrderUseCaseData.Pharmacy.pharmacyName() =
    name.ifBlank {
        stringResource(R.string.orders_generic_pharmacy_name)
    }

@LightDarkPreview
@Composable
fun MessageBottomSheetScreenContentNoOrderEmptyPreview() {
    PreviewAppTheme {
        MessageBottomSheetScreenContent(
            order = null,
            message = null,
            onClickClose = {}
        )
    }
}

@LightDarkPreview
@Composable
fun MessageBottomSheetScreenContentPreview(
    @PreviewParameter(MessagesPreviewParameterProvider::class) message: OrderUseCaseData.Message?
) {
    PreviewAppTheme {
        MessageBottomSheetScreenContent(
            order = MessageSheetsPreviewData.ORDER_DETAIL_PREVIEW,
            message = message,
            onClickClose = {}
        )
    }
}
