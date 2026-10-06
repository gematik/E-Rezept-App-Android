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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DriveEta
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.InTransportETA
import de.gematik.ti.erp.app.communication.model.payload.InTransportPosition
import de.gematik.ti.erp.app.core.LocalDi
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.mapper.getDeliveryDescriptionText
import de.gematik.ti.erp.app.messages.mapper.getDeliveryStatusText
import de.gematik.ti.erp.app.pharmacy.model.PositionErpModel
import de.gematik.ti.erp.app.pharmacy.ui.components.GooglePharmacyMap
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyMap
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyProperties
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacySettings
import de.gematik.ti.erp.app.pharmacy.ui.components.PositionState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import kotlinx.datetime.Instant.Companion.fromEpochSeconds
import org.kodein.di.direct
import org.kodein.di.instanceOrNull

internal data class DeliveryServiceAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Delivery
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17444&m=dev
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
    position: PositionErpModel? = null,
    pharmacyMap: PharmacyMap? = null,
    serviceActions: List<DeliveryServiceAction> = emptyList(),
    onClickAction: () -> Unit = {}
) {
    val di = runCatching { LocalDi.current }.getOrNull()
    val pharmacyMapFromDi = if (pharmacyMap == null && di != null) {
        di.direct.instanceOrNull<PharmacyMap>()
    } else {
        pharmacyMap
    }
    val resolvedPharmacyMap = pharmacyMap ?: pharmacyMapFromDi

    val cardIcon = if (isCourier) Icons.Outlined.DriveEta else Icons.Outlined.LocalShipping
    val hasHint = !hint.isNullOrBlank()
    val hasTrackingAction = !actionLabel.isNullOrBlank()
    val showIncidentFallback = deliveryStatus == CommunicationDeliveryStatusErpModel.Incident &&
        !hasHint &&
        !hasTrackingAction &&
        deliverySummaryTitle == null &&
        deliverySummaryText == null &&
        position == null

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
                position?.let { pos ->
                    SpacerMedium()
                    DeliveryMapContent(
                        position = pos,
                        pharmacyMap = resolvedPharmacyMap ?: GooglePharmacyMap(),
                        onClick = onClickAction
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
    supplyOption: CommunicationSupplyOptionTypeErpModel? = null,
    phoneNumber: String? = null,
    mailAddress: String? = null,
    isPharmacyOpen: Boolean? = null,
    pharmacyCoordinates: PositionErpModel? = null,
    pharmacyMap: PharmacyMap? = null,
    onClickPhone: ((String) -> Unit)? = null,
    onClickMail: ((String) -> Unit)? = null,
    onClickLocation: ((PositionErpModel) -> Unit)? = null
) {
    val statusText = payload.deliveryStatus.getDeliveryStatusText()
    val statusDescription = payload.deliveryStatus.getDeliveryDescriptionText(payload)
    val etaText = payload.inTransportETA?.toDeliveryEtaText()
    val position = payload.inTransportPosition?.toPositionErpModel(fallbackCoordinates = pharmacyCoordinates)
    val hint = payload.text?.takeUnless { it.isBlank() }
    val isCourier = supplyOption == CommunicationSupplyOptionTypeErpModel.DELIVERY
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
        isCourier = isCourier,
        deliveryStatus = payload.deliveryStatus,
        description = statusDescription,
        deliverySummaryTitle = etaText?.let { stringResource(R.string.message_card_delivery_eta_label) },
        deliverySummaryText = etaText,
        actionLabel = position?.let { stringResource(R.string.message_card_delivery_action_tracking) },
        isPharmacyOpen = isPharmacyOpen,
        position = position,
        pharmacyMap = pharmacyMap,
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
internal fun DeliveryMapContent(
    position: PositionErpModel,
    pharmacyMap: PharmacyMap = GooglePharmacyMap(),
    onClick: (() -> Unit)? = null
) {
    val positionState = remember(position) {
        PositionState(position = position, zoom = 14f)
    }
    val markerIcon = remember {
        runCatching {
            BitmapDescriptorFactory.fromResource(R.drawable.maps_marker_red)
        }.getOrNull()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(SizeDefaults.double))
    ) {
        pharmacyMap.Map(
            modifier = Modifier.fillMaxSize(),
            isFullScreen = false,
            positionState = positionState,
            settings = PharmacySettings.Default,
            properties = PharmacyProperties.Default,
            contentPaddingValues = PaddingValues(),
            onZoomStateChanged = {},
            onClick = onClick,
            content = {
                Marker(
                    state = MarkerState(LatLng(position.latitude, position.longitude)),
                    icon = markerIcon
                )
            }
        )
    }
}

@Composable
private fun InTransportETA.toDeliveryEtaText(): String? {
    val from = from ?: return null
    val to = to ?: return null
    if (to < from) return null

    val formatter = rememberErpTimeFormatter()
    val fromInstant = remember(from) { fromEpochSeconds(from) }
    val toInstant = remember(to) { fromEpochSeconds(to) }

    val zoneId = formatter.zoneId
    val fromDate = remember(from, zoneId) { java.time.Instant.ofEpochSecond(from).atZone(zoneId).toLocalDate() }
    val toDate = remember(to, zoneId) { java.time.Instant.ofEpochSecond(to).atZone(zoneId).toLocalDate() }
    // A window spanning several days can't be expressed as a single date with a time range
    if (fromDate != toDate) return null

    val fromTime = formatter.time(fromInstant)
    val toTime = formatter.time(toInstant)

    return if (fromDate == java.time.LocalDate.now(zoneId)) {
        stringResource(R.string.message_card_delivery_eta_today, fromTime, toTime)
    } else {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(formatter.locale, "dMMMM")
        val dateText = fromDate.format(java.time.format.DateTimeFormatter.ofPattern(pattern, formatter.locale))
        stringResource(R.string.message_card_delivery_eta_date, dateText, fromTime, toTime)
    }
}

private const val DefaultFallbackLongitude = 13.387595793605172

private fun InTransportPosition.toPositionErpModel(
    fallbackCoordinates: PositionErpModel? = null
): PositionErpModel? {
    val lat = latitude?.takeIf { it != 0.0 } ?: fallbackCoordinates?.latitude?.takeIf { it != 0.0 } ?: return null
    val long = longitude?.takeIf { it != 0.0 } ?: fallbackCoordinates?.longitude?.takeIf { it != 0.0 } ?: DefaultFallbackLongitude
    return PositionErpModel(
        latitude = lat,
        longitude = long
    )
}

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
