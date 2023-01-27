/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.orders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.redeem.ui.createBitMatrix
import de.gematik.ti.erp.app.redeem.ui.drawDataMatrix
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MessageSheetContent(
    order: OrderUseCaseData.Order?,
    message: OrderUseCaseData.Message?,
    onClickClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium)
            .padding(bottom = PaddingDefaults.XLarge)
            .navigationBarsPadding()
    ) {
        IconButton(
            onClick = onClickClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(AppTheme.colors.neutral100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, null, tint = AppTheme.colors.neutral600)
            }
        }
        SpacerMedium()
        order?.let {
            message?.let {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (message.type) {
                        OrderUseCaseData.Message.Type.All ->
                            AllSheetContent(message)

                        OrderUseCaseData.Message.Type.Link ->
                            LinkSheetContent(message)

                        OrderUseCaseData.Message.Type.Code ->
                            CodeSheetContent(message)

                        OrderUseCaseData.Message.Type.Text ->
                            TextSheetContent(message)

                        OrderUseCaseData.Message.Type.Empty ->
                            EmptySheetContent(order.pharmacy.pharmacyName())
                    }
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
        message.code?.let {
            Box(
                Modifier
                    .background(AppTheme.colors.neutral100, RoundedCornerShape(16.dp))
                    .padding(PaddingDefaults.Medium)
            ) {
                DataMatrixCode(payload = message.code, modifier = Modifier.size(144.dp))
            }
            CodeLabel(code = message.code)
        }
        message.message?.let {
            TextSheetContent(message)
        }
        message.link?.let {
            LinkSheetContent(message)
        }
    }
}

@Composable
fun LinkSheetContent(
    message: OrderUseCaseData.Message
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxWidth(),
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
            onClick = {
                message.link?.let { uriHandler.openUri(it) }
            }
        ) {
            Text(stringResource(R.string.orders_open_cart_link))
        }
    }
}

@Composable
fun TextSheetContent(
    message: OrderUseCaseData.Message
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.neutral100, RoundedCornerShape(16.dp))
                .padding(PaddingDefaults.Medium)
        ) {
            Text(
                message.message ?: "",
                style = AppTheme.typography.body2
            )
        }
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
        dateFormatter.format(LocalDateTime.ofInstant(message.sentOn, ZoneId.systemDefault()))
    }

@Composable
fun CodeSheetContent(
    message: OrderUseCaseData.Message
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        message.code?.let {
            DataMatrixCode(payload = message.code, modifier = Modifier.size(144.dp))
            SpacerMedium()
            CodeLabel(code = message.code)
        }
        SpacerSmall()
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
}

@Composable
private fun CodeLabel(
    code: String
) {
    Box(
        Modifier
            .background(AppTheme.colors.neutral100, RoundedCornerShape(8.dp))
            .padding(horizontal = PaddingDefaults.ShortMedium, vertical = PaddingDefaults.ShortMedium / 2)
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
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawDataMatrix(matrix)
        )
    }
}
