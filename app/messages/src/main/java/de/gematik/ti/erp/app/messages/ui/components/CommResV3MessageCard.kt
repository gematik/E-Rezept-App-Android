/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

@file:Suppress("UnusedPrivateMember", "UnstableCollections")

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.ClickableText
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.PrimaryOutlinedButton

// Shared base layouts for CommResV3 message cards
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-19310&m=dev

// Base Layouts
@Composable
internal fun CommResV3MessageCard(
    cardIcon: ImageVector,
    cardIconTint: Color,
    cardIconBackgroundColor: Color,
    time: String,
    title: String,
    showTime: Boolean = true,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit),
    hintContent: (@Composable ColumnScope.() -> Unit)? = null,
    actionContent: (@Composable ColumnScope.() -> Unit)? = null,
    prescriptionsContent: (@Composable ColumnScope.() -> Unit)? = null
) = CommResV3MessageCardBase(
    iconContent = {
        Box(
            modifier = Modifier
                .size(SizeDefaults.fourfold)
                .background(color = cardIconBackgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = cardIcon,
                contentDescription = null,
                tint = cardIconTint
            )
        }
    },
    time = time,
    title = title,
    showTime = showTime,
    modifier = modifier,
    content = content,
    hintContent = hintContent,
    actionContent = actionContent,
    prescriptionsContent = prescriptionsContent
)

@Composable
internal fun CommResV3MessageCard(
    cardImage: Painter,
    time: String,
    title: String,
    showTime: Boolean = true,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit),
    hintContent: (@Composable ColumnScope.() -> Unit)? = null,
    actionContent: (@Composable ColumnScope.() -> Unit)? = null,
    prescriptionsContent: (@Composable ColumnScope.() -> Unit)? = null
) = CommResV3MessageCardBase(
    iconContent = {
        Box(
            modifier = Modifier.size(SizeDefaults.fourfold),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = cardImage,
                contentDescription = null
            )
        }
    },
    time = time,
    title = title,
    showTime = showTime,
    modifier = modifier,
    content = content,
    hintContent = hintContent,
    actionContent = actionContent,
    prescriptionsContent = prescriptionsContent
)

@Composable
private fun CommResV3MessageCardBase(
    iconContent: @Composable () -> Unit,
    time: String,
    title: String,
    showTime: Boolean = true,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit),
    hintContent: (@Composable ColumnScope.() -> Unit)? = null,
    actionContent: (@Composable ColumnScope.() -> Unit)? = null,
    prescriptionsContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        border = BorderStroke(
            width = SizeDefaults.eighth,
            color = AppTheme.colors.neutral200
        ),
        shape = RoundedCornerShape(SizeDefaults.double),
        backgroundColor = AppTheme.colors.neutral000,
        elevation = SizeDefaults.one
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MessageCardMainSection(
                iconContent = iconContent,
                time = time,
                title = title,
                showTime = showTime,
                content = content
            )
            if (hintContent != null || actionContent != null) {
                MessageCardHintAndActionSection(
                    hintContent = hintContent,
                    actionContent = actionContent
                )
            }
            prescriptionsContent?.let {
                prescriptionsContent()
            }
        }
    }
}

@Composable
private fun ColumnScope.MessageCardMainSection(
    iconContent: @Composable () -> Unit,
    time: String,
    title: String,
    showTime: Boolean,
    content: (@Composable ColumnScope.() -> Unit)
) {
    iconContent()

    SpacerMedium()

    if (showTime) {
        Text(
            text = time,
            style = AppTheme.typography.caption1,
            fontStyle = FontStyle.Italic,
            color = AppTheme.colors.neutral700,
            textAlign = TextAlign.Center
        )
    }
    Text(
        text = title,
        style = AppTheme.typography.subtitle1,
        color = AppTheme.colors.neutral900,
        textAlign = TextAlign.Center
    )

    content()
}

@Composable
private fun MessageCardHintAndActionSection(
    hintContent: (@Composable ColumnScope.() -> Unit)?,
    actionContent: (@Composable ColumnScope.() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        SpacerMedium()
        Divider(Modifier.fillMaxWidth())
        SpacerMedium()

        hintContent?.let {
            hintContent()
        }

        actionContent?.let {
            hintContent?.let {
                SpacerTiny()
            }
            actionContent()
        }
    }
}

@Composable
internal fun CommResV3ReceivedMessageBubbleCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    affectedPrescriptionsContent: (ColumnScope.() -> Unit)?
) {
    Card(
        modifier = modifier.padding(end = PaddingDefaults.XXLarge),
        border = BorderStroke(
            width = SizeDefaults.eighth,
            color = AppTheme.colors.primary300
        ),
        shape = RoundedCornerShape(
            topStart = SizeDefaults.double,
            topEnd = SizeDefaults.double,
            bottomEnd = SizeDefaults.double,
            bottomStart = SizeDefaults.half
        ),
        backgroundColor = AppTheme.colors.primary100,
        elevation = SizeDefaults.one
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                title,
                style = MaterialTheme.typography.caption,
                color = AppTheme.colors.primary900
            )
            Text(
                message,
                style = MaterialTheme.typography.body1,
                color = AppTheme.colors.neutral999
            )

            affectedPrescriptionsContent?.let {
                SpacerMedium()
                affectedPrescriptionsContent()
            }
        }
    }
}

@Composable
internal fun CommResV3SentMessageBubbleCard(
    time: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(start = PaddingDefaults.XXLarge),
        border = BorderStroke(
            width = SizeDefaults.eighth,
            color = AppTheme.colors.primary700
        ),
        shape = RoundedCornerShape(
            topStart = SizeDefaults.double,
            topEnd = SizeDefaults.double,
            bottomStart = SizeDefaults.double,
            bottomEnd = SizeDefaults.half
        ),
        backgroundColor = AppTheme.colors.primary700,
        elevation = SizeDefaults.one
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                message,
                style = MaterialTheme.typography.body1,
                color = AppTheme.colors.neutral000
            )
            Text(
                time,
                style = MaterialTheme.typography.caption,
                color = AppTheme.colors.neutral000,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// Top Section Layouts
@Composable
internal fun CommResV3TextContent(
    text: String
) {
    Text(
        text = text,
        style = AppTheme.typography.body2l,
        color = AppTheme.colors.neutral700,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun CommResV3ClickableTextContent(
    text: String,
    pharmacyName: String,
    onClickPharmacy: () -> Unit
) {
    ClickableText(
        leadingText = text,
        linkText = pharmacyName,
        onClick = onClickPharmacy,
        textStyle = AppTheme.typography.body2
    )
}

@Composable
internal fun CommResV3DeliveryContent(
    title: String,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SizeDefaults.double))
            .background(AppTheme.colors.green100)
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.green900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerTiny()
        Text(
            text = text,
            style = AppTheme.typography.subtitle1,
            color = AppTheme.colors.green900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun CommResV3PaymentContent(
    title: String? = null,
    details: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SizeDefaults.double))
            .background(AppTheme.colors.neutral100)
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title?.let {
            Text(
                text = it,
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.neutral700,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerTiny()
        }
        Text(
            text = details,
            style = AppTheme.typography.subtitle1,
            color = AppTheme.colors.neutral900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun CommResV3PaymentMethodContent(
    paymentMethods: List<PaymentMethodTile>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.oneQuarter)
    ) {
        paymentMethods.toPaymentMethodRows().forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SizeDefaults.oneQuarter)
            ) {
                row.forEach { paymentMethod ->
                    CommResV3PaymentMethodTile(
                        paymentMethod = paymentMethod,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommResV3PaymentMethodTile(
    paymentMethod: PaymentMethodTile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(70.dp)
            .clickable(
                enabled = true,
                onClick = paymentMethod.onClick,
                role = Role.Button
            ),
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
        shape = RoundedCornerShape(SizeDefaults.double),
        backgroundColor = AppTheme.colors.neutral000,
        elevation = SizeDefaults.quarter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PaddingDefaults.Small,
                    vertical = SizeDefaults.one
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SizeDefaults.triple),
                contentAlignment = Alignment.Center
            ) {
                PaymentMethodTileMark(paymentMethod = paymentMethod)
            }
            Spacer(modifier = Modifier.height(SizeDefaults.half))
            Text(
                text = paymentMethod.label,
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral900,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PaymentMethodTileMark(paymentMethod: PaymentMethodTile) {
    when (paymentMethod.type) {
        PaymentMethodTileType.PayPal -> {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.payment_method_paypal),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 44.dp)
            )
        }

        PaymentMethodTileType.Cash,
        PaymentMethodTileType.Unknown -> {
            Icon(
                imageVector = Icons.Outlined.Euro,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.doubleHalf)
            )
        }

        PaymentMethodTileType.BankAccount -> {
            Icon(
                imageVector = Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.doubleHalf)
            )
        }

        PaymentMethodTileType.Creditcard -> {
            Icon(
                imageVector = Icons.Outlined.CreditCard,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.doubleHalf)
            )
        }
    }
}

private fun List<PaymentMethodTile>.toPaymentMethodRows(): List<List<PaymentMethodTile>> =
    when (size) {
        0 -> emptyList()
        4 -> listOf(take(2), drop(2))
        else -> chunked(3)
    }

// Hint Section Layouts
@Composable
internal fun CommResV3HintContent(
    hint: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Hinweis der Apotheke:",
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral700
        )
        SpacerTiny()
        Text(
            text = hint,
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral900
        )
    }
}

@Composable
internal fun CommResV3ActionContent(
    text: String,
    enabled: Boolean = true,
    tint: Color = AppTheme.colors.primary700,
    onClickAction: () -> Unit
) {
    MessageActionButton(
        text = text,
        enabled = enabled,
        tint = tint,
        onClick = onClickAction
    )
}

// Prescription Section Layouts
@Composable
internal fun CommResV3OrderedPrescriptionsContent(
    prescriptions: List<TaskErpModel>,
    onClickPrescription: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SpacerMedium()
        Divider(Modifier.fillMaxWidth())
        SpacerMedium()
        Text(
            text = stringResource(R.string.message_card_ordered_prescriptions),
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral700
        )
        prescriptions.forEach { prescription ->
            SpacerTiny()
            PrimaryOutlinedButton(
                onClick = { onClickPrescription(prescription.taskId) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(prescription.name ?: "")
                    SpacerTiny()
                    Icon(Icons.Outlined.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
internal fun CommResV3AffectedPrescriptionsContent(
    prescriptions: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SpacerMedium()
        Divider(Modifier.fillMaxWidth())
        SpacerMedium()
        Text(
            text = stringResource(R.string.message_card_affected_prescriptions),
            style = AppTheme.typography.caption1,
            color = AppTheme.colors.neutral700
        )
        prescriptions.forEach { prescription ->
            SpacerTiny()
            Text(
                modifier = Modifier
                    .background(color = AppTheme.colors.primary100, RoundedCornerShape(SizeDefaults.double))
                    .padding(horizontal = PaddingDefaults.Small, vertical = PaddingDefaults.Tiny),
                text = prescription,
                style = AppTheme.typography.caption1,
                maxLines = 1,
                color = AppTheme.colors.primary900
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CommResV3MessageCardPreview() {
    PreviewTheme {
        Column {
            CommResV3MessageCard(
                cardIcon = Icons.Rounded.LocalShipping,
                cardIconTint = AppTheme.colors.green700,
                cardIconBackgroundColor = AppTheme.colors.green100,
                time = "23 Feb 2025 • 14:35",
                title = "Ihre Bestellung ist unterwegs",
                content = {
                    Text(
                        text = "Bar",
                        style = AppTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                hintContent = {
                    CommResV3HintContent(
                        "Bitte kommen Sie morgen zwischen 12 und 13 Uhr."
                    )
                },
                actionContent = {
                    CommResV3ActionContent("Abholcode") { }
                },
                prescriptionsContent = {
                    CommResV3AffectedPrescriptionsContent(
                        listOf("Medikament", "Hilfe")
                    )
                }
            )
            SpacerMedium()
            CommResV3SentMessageBubbleCard(
                time = "18:00",
                message = "Wann soll ich kommen?"
            )
            SpacerMedium()
            CommResV3ReceivedMessageBubbleCard(
                message = "Jetzt",
                title = "Apotheke um 18:01",
                affectedPrescriptionsContent = null
            )
        }
    }
}
