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

@file:Suppress("UnstableCollections")

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DriveEta
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.InTransportETA
import de.gematik.ti.erp.app.communication.model.payload.InTransportPosition
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.mapper.getDeliveryDescriptionText
import de.gematik.ti.erp.app.messages.mapper.getDeliveryStatusText
import de.gematik.ti.erp.app.pharmacy.model.PositionErpModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import kotlinx.datetime.Instant.Companion.fromEpochSeconds

internal data class DeliveryServiceAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Delivery
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17444&m=dev
// MVP is everything without a maps implementation. In MVP maps is replaced by a clickable text which opens Maps intent with lat+long or something else
@Composable
internal fun DeliveryMessageCard(
    time: String,
    title: String,
    description: String?,
    isCourier: Boolean,
    deliveryStatus: CommunicationDeliveryStatusErpModel,
    deliverySummaryTitle: String? = null,
    deliverySummaryText: String? = null,
    actionLabel: String? = null,
    hint: String? = null,
    isPharmacyOpen: Boolean? = null,
    serviceActions: List<DeliveryServiceAction> = emptyList(),
    onClickAction: () -> Unit = {}
) {
    val cardIcon = if (isCourier) Icons.Rounded.DriveEta else Icons.Rounded.LocalShipping
    val hasHint = !hint.isNullOrBlank()
    val hasTrackingAction = !actionLabel.isNullOrBlank()
    val showIncidentFallback = deliveryStatus == CommunicationDeliveryStatusErpModel.Incident &&
        !hasHint &&
        !hasTrackingAction &&
        deliverySummaryTitle == null &&
        deliverySummaryText == null

    CommResV3MessageCard(
        cardIcon = if (deliveryStatus != CommunicationDeliveryStatusErpModel.Delivered) cardIcon else Icons.Rounded.Check,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = title,
        content = {
            if (showIncidentFallback) {
                description?.let { DeliveryIncidentDescription(text = it) }
                isPharmacyOpen?.let {
                    SpacerMedium()
                    DeliveryIncidentAvailabilityText(isPharmacyOpen = it)
                }
                if (serviceActions.isNotEmpty()) {
                    SpacerMedium()
                    DeliveryIncidentServiceActions(actions = serviceActions)
                }
            } else {
                if (deliverySummaryTitle != null && deliverySummaryText != null) {
                    SpacerMedium()
                    CommResV3DeliveryContent(
                        title = deliverySummaryTitle,
                        text = deliverySummaryText
                    )
                }
                description?.let {
                    SpacerMedium()
                    CommResV3TextContent(text = it)
                }
                actionLabel?.let {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                        CommResV3ActionContent(
                            text = actionLabel,
                            onClickAction = onClickAction
                        )
                    }
                }
            }
        },
        hintContent = hint?.takeUnless { it.isBlank() }?.let {
            { CommResV3HintContent(hint = it) }
        }
    )
}

@Composable
internal fun DeliveryStatusMessageCard(
    time: String,
    payload: CommunicationReplyDeliveryStatusPayloadErpModel,
    phoneNumber: String? = null,
    mailAddress: String? = null,
    isPharmacyOpen: Boolean? = null,
    onClickPhone: ((String) -> Unit)? = null,
    onClickMail: ((String) -> Unit)? = null,
    onClickLocation: ((PositionErpModel) -> Unit)? = null
) {
    val statusText = payload.deliveryStatus.getDeliveryStatusText()
    val statusDescription = payload.deliveryStatus.getDeliveryDescriptionText(payload)
    val etaText = payload.inTransportETA?.toDeliveryEtaText()
    val position = payload.inTransportPosition?.toPositionErpModel()
    val hint = payload.text?.takeUnless { it.isBlank() }
    val serviceActions = if (
        payload.deliveryStatus == CommunicationDeliveryStatusErpModel.Incident &&
        hint == null &&
        position == null
    ) {
        buildList {
            phoneNumber?.let { phone ->
                add(
                    DeliveryServiceAction(
                        label = stringResource(R.string.pharmacy_contact_phone_two_lines),
                        icon = Icons.Outlined.Phone,
                        onClick = { onClickPhone?.invoke(phone) }
                    )
                )
            }
            mailAddress?.let { mail ->
                add(
                    DeliveryServiceAction(
                        label = stringResource(R.string.legal_notice_email_text),
                        icon = Icons.Outlined.MailOutline,
                        onClick = { onClickMail?.invoke(mail) }
                    )
                )
            }
        }
    } else {
        emptyList()
    }

    DeliveryMessageCard(
        time = time,
        title = statusText,
        isCourier = true,
        deliveryStatus = payload.deliveryStatus,
        description = statusDescription,
        deliverySummaryTitle = etaText?.let { stringResource(R.string.message_card_delivery_eta_label) },
        deliverySummaryText = etaText,
        actionLabel = position?.let { stringResource(R.string.message_card_delivery_action_tracking) },
        isPharmacyOpen = isPharmacyOpen,
        serviceActions = serviceActions,
        onClickAction = {
            position?.let { coordinates ->
                onClickLocation?.invoke(coordinates)
            }
        },
        hint = hint
    )
}

@Composable
internal fun DeliveryLocationMessageCard(
    time: String,
    title: String,
    description: String,
    deliverySummaryTitle: String,
    locationLabel: String,
    actionLabel: String,
    deliveryStatus: CommunicationDeliveryStatusErpModel,
    onClickLocation: () -> Unit
) {
    DeliveryMessageCard(
        time = time,
        title = title,
        isCourier = false,
        description = description,
        deliverySummaryTitle = deliverySummaryTitle,
        deliverySummaryText = locationLabel,
        actionLabel = actionLabel,
        onClickAction = onClickLocation,
        deliveryStatus = deliveryStatus
    )
}

@Composable
private fun InTransportETA.toDeliveryEtaText(): String {
    val formatter = rememberErpTimeFormatter()
    val fromInstant = remember(this) { fromEpochSeconds(from) }
    val toInstant = remember(this) { fromEpochSeconds(to) }

    return stringResource(
        R.string.message_card_delivery_eta_today,
        formatter.time(fromInstant),
        formatter.time(toInstant)
    )
}

private fun InTransportPosition.toPositionErpModel() = PositionErpModel(
    latitude = latitude,
    longitude = longitude
)

@Composable
private fun DeliveryIncidentDescription(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.body2l,
        color = AppTheme.colors.neutral700,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeliveryIncidentAvailabilityText(isPharmacyOpen: Boolean) {
    val prefix = stringResource(R.string.message_card_delivery_incident_pharmacy_prefix)
    val statusText = stringResource(
        if (isPharmacyOpen) {
            R.string.message_card_delivery_incident_pharmacy_open
        } else {
            R.string.message_card_delivery_incident_pharmacy_closed
        }
    )

    Text(
        text = buildAnnotatedString {
            append(prefix)
            pushStyle(
                SpanStyle(
                    color = if (isPharmacyOpen) AppTheme.colors.green700 else AppTheme.colors.red700,
                    fontWeight = FontWeight.Bold
                )
            )
            append(statusText)
            pop()
            append(".")
        },
        style = AppTheme.typography.body2l,
        color = AppTheme.colors.neutral700,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DeliveryIncidentServiceActions(
    actions: List<DeliveryServiceAction>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        actions.take(2).forEach { action ->
            DeliveryIncidentServiceActionButton(
                action = action,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeliveryIncidentServiceActionButton(
    action: DeliveryServiceAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(
                role = Role.Button,
                onClick = action.onClick
            ),
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral600),
        shape = RoundedCornerShape(SizeDefaults.double),
        backgroundColor = AppTheme.colors.neutral000,
        elevation = SizeDefaults.quarter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SizeDefaults.one, vertical = SizeDefaults.one),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier
                    .size(SizeDefaults.fourfold)
                    .align(Alignment.CenterHorizontally)
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action.label,
                    style = AppTheme.typography.subtitle2,
                    color = AppTheme.colors.neutral900,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
