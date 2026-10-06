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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.InTransportETA
import de.gematik.ti.erp.app.communication.model.payload.InTransportPosition
import de.gematik.ti.erp.app.communication.model.payload.PaymentMethod
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pharmacy.ui.components.MockMap
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.utils.SpacerMedium
import kotlinx.datetime.Instant

@LightDarkPreview
@Composable
fun CommResV3OrderedMessageCardsPreview() {
    PreviewTheme {
        Column {
            OrderedReservationMessageCard(
                date = "24.12.2026",
                pharmacyName = "Apotheke",
                prescriptions = listOf(previewTask()),
                {},
                {}
            )
            SpacerMedium()
            OrderedShippingMessageCard(
                date = "24.12.2026",
                pharmacyName = "Apotheke",
                prescriptions = listOf(previewTask()),
                {},
                {}
            )
            SpacerMedium()
            OrderedDeliveryMessageCard(
                date = "24.12.2026",
                pharmacyName = "Apotheke",
                prescriptions = listOf(previewTask()),
                {},
                {}
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3CompletedMessageCardsPreview() {
    PreviewTheme {
        Column {
            RedeemedReservationMessageCard("18:00 Uhr")
            SpacerMedium()
            RedeemedShippingMessageCard("18:00 Uhr")
            SpacerMedium()
            RedeemedDeliveryMessageCard("18:00 Uhr")
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3InvoiceCardsPreview() {
    PreviewTheme {
        Column {
            InvoiceMessageCard("18:00 Uhr", {})
            SpacerMedium()
            DeletedInvoiceMessageCard("18:00 Uhr")
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3PickUpCodeCardsPreview() {
    PreviewTheme {
        Column {
            PickUpCodeMessageCard("18:00 Uhr", message = "Bitte kommen", {})
            SpacerMedium()
            PickUpCodeMessageCard("18:00 Uhr", message = null, {})
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3ReservationStatePreview() {
    PreviewTheme {
        Column {
            ImmediatlyReservationStateMessageCard("18:00 Uhr")
            SpacerMedium()
            SameDayReservationStateMessageCard("18:00 Uhr")
            SpacerMedium()
            NextDayReservationStateMessageCard("18:00 Uhr")
            SpacerMedium()
            NextDayAMReservationStateMessageCard("18:00 Uhr")
            SpacerMedium()
            NextDayPMReservationStateMessageCard("18:00 Uhr")
            SpacerMedium()
            UnknownReservationStateMessageCard("18:00 Uhr")
            SpacerMedium()
            NotAvailableReservationStateMessageCard("18:00 Uhr")
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3EmptyCardsPreview() {
    PreviewTheme {
        EmptyMessageCard("18:00 Uhr")
    }
}

@LightDarkPreview
@Composable
fun CommResV3LinkCardsPreview() {
    PreviewTheme {
        Column {
            LinkMessageCard("18:00 Uhr", message = "Bitte kommen", {})
            SpacerMedium()
            LinkMessageCard("18:00 Uhr", message = null, {})
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3MessageCardsPreview() {
    PreviewTheme {
        Column {
            SentMessageCard("18:00 Uhr", message = "Bitte kommen")
            SpacerMedium()
            ReceivedMessageCard("18:00 Uhr", message = "jetzt", sender = "Apotheke")
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3PaymentCardsPreview() {
    PreviewTheme {
        Column {
            PaymentMessageCard(
                time = "18:00 Uhr",
                title = "Zahlung ausstehend",
                description = "Für Ihre Bestellung fällt folgender Betrag an:",
                isPaymentRequired = true,
                paymentSummaryText = "24,50 €",
                paymentNotice = "Sie können den Betrag wie gewohnt vor Ort bezahlen oder bequem " +
                    "vorab online. Folgende Online-Zahlungsoptionen bietet die Apotheke an:",
                paymentMethods = listOf(
                    PaymentMethodTile("PayPal", PaymentMethodTileType.PayPal, {}),
                    PaymentMethodTile("Barzahlung", PaymentMethodTileType.Cash, {}),
                    PaymentMethodTile("Bankkonto", PaymentMethodTileType.BankAccount, {}),
                    PaymentMethodTile("Kreditkarte", PaymentMethodTileType.Creditcard, {}),
                    PaymentMethodTile("Unbekannt", PaymentMethodTileType.Unknown, {})
                )
            )
            SpacerMedium()
            PaymentMessageCard(
                time = "18:30 Uhr",
                title = "Zahlung erforderlich",
                description = "Für Ihre Bestellung fällt folgender Betrag an:",
                isPaymentRequired = true,
                paymentSummaryText = "24,50 €",
                paymentNotice = "Bitte zahlen Sie den Betrag direkt in der Apotheke."
            )
            SpacerMedium()
            PaymentMessageCard(
                time = "18:15 Uhr",
                title = "Keine Zahlung erforderlich",
                description = "Für Ihre Bestellung fällt folgender Betrag an:",
                isPaymentRequired = false,
                paymentSummaryTitle = "Diese Bestellung ist für Sie",
                paymentSummaryText = "kostenlos",
                hint = "Glück gehabt!"
            )
            SpacerMedium()
            PaymentMessageCard(
                time = "18:30 Uhr",
                title = "Zahlung erforderlich",
                description = "Für Ihre Bestellung fällt folgender Betrag an:",
                isPaymentRequired = true,
                paymentSummaryText = "24,50 €",
                paymentNotice = "Bitte zahlen Sie den Betrag direkt in der Apotheke."
            )
            SpacerMedium()
            PaymentMessageCard(
                time = "18:45 Uhr",
                title = "Zahlung erforderlich",
                description = "Für Ihre Bestellung fällt folgender Betrag an:",
                isPaymentRequired = true,
                paymentSummaryText = "24,50 €",
                paymentNotice = "Bitte zahlen Sie den Betrag direkt in der Apotheke."
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3PaymentInfoCardsPreview() {
    PreviewTheme {
        Column {
            PaymentInfoMessageCard(
                time = "18:00 Uhr",
                payload = CommunicationReplyPaymentInfoPayloadErpModel(
                    totalAmount = 2450.0,
                    paymentMethods = listOf(
                        PaymentMethod(
                            paymentMethod = "cash",
                            url = "https://gematik.de"
                        ),
                        PaymentMethod(
                            paymentMethod = "paypal",
                            url = "https://paypal.com"
                        )
                    ),
                    text = "Sie können den Betrag wie gewohnt vor Ort bezahlen oder bequem vorab online.",
                    transactionID = "1"
                ),
                onClickPaymentMethod = {}
            )
            SpacerMedium()
            PaymentInfoMessageCard(
                time = "18:15 Uhr",
                payload = CommunicationReplyPaymentInfoPayloadErpModel(
                    totalAmount = 0.0,
                    paymentMethods = emptyList(),
                    text = "Diese Bestellung ist für Sie kostenlos.",
                    transactionID = "2"
                ),
                onClickPaymentMethod = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3DeliveryCardsPreview() {
    PreviewTheme {
        Column {
            DeliveryMessageCard(
                time = "18:00 Uhr",
                title = "Lieferung unterwegs",
                description = "Ihre Apotheke hat den Botendienst für Ihre Bestellung gestartet.",
                isCourier = true,
                deliverySummaryTitle = "Lieferfenster",
                deliverySummaryText = "Heute zwischen 18:30 und 19:00 Uhr",
                hint = "Bitte stellen Sie sicher, dass jemand die Lieferung entgegennehmen kann.",
                deliveryStatus = CommunicationDeliveryStatusErpModel.InTransport
            )
            SpacerMedium()
            DeliveryStatusMessageCard(
                time = "17:48 Uhr",
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-1",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.InTransport,
                    inTransportETA = InTransportETA(
                        from = 1735736400,
                        to = 1735741800
                    ),
                    inTransportPosition = InTransportPosition(
                        latitude = 52.522529939635795,
                        longitude = 13.387595793605172
                    ),
                    text = "Ihre Lieferung ist unterwegs."
                ),
                pharmacyMap = MockMap(),
                onClickLocation = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
fun CommResV3DeliveryStatusCardsPreview() {
    PreviewTheme {
        Column {
            DeliveryStatusPreviewCard(
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-preparing",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.PreparedWaiting,
                    text = "Ihre Bestellung wird für die Lieferung vorbereitet."
                )
            )
            SpacerMedium()
            DeliveryStatusPreviewCard(
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-in-transport",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.InTransport,
                    inTransportETA = InTransportETA(
                        from = 1735736400,
                        to = 1735741800
                    ),
                    inTransportPosition = InTransportPosition(
                        latitude = 52.522529939635795,
                        longitude = 13.387595793605172
                    ),
                    text = "Verfolgen Sie Ihre Bestellung auf der Webseite des Versanddienstleisters:"
                )
            )
            SpacerMedium()
            DeliveryStatusPreviewCard(
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-delivered",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.Delivered,
                    text = "Ihre Medikamente sind angekommen. Gute Genesung!"
                )
            )
            SpacerMedium()
            DeliveryStatusPreviewCard(
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-incident-tracking",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.Incident,
                    inTransportPosition = InTransportPosition(
                        latitude = 52.522529939635795,
                        longitude = 13.387595793605172
                    ),
                    text = "Verfolgen Sie Ihre Bestellung auf der Webseite des Versanddienstleisters:"
                )
            )
            SpacerMedium()
            DeliveryStatusPreviewCard(
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-incident-fallback",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.Incident
                ),
                phoneNumber = "030 1234567",
                mailAddress = "kontakt@apotheke.de",
                isPharmacyOpen = true
            )
            SpacerMedium()
            DeliveryStatusPreviewCard(
                payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                    transactionID = "delivery-status-unknown",
                    deliveryStatus = CommunicationDeliveryStatusErpModel.Unknown,
                    text = "Zum aktuellen Lieferstatus liegen noch keine Informationen vor."
                )
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun DeliveryStatusPreparingMessageCardPreview() {
    DeliveryStatusPreviewCard(
        payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            transactionID = "delivery-status-preparing",
            deliveryStatus = CommunicationDeliveryStatusErpModel.PreparedWaiting,
            text = "Ihre Bestellung wird für die Lieferung vorbereitet."
        )
    )
}

@LightDarkPreview
@Composable
private fun DeliveryStatusInTransportMessageCardPreview() {
    DeliveryStatusPreviewCard(
        payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            transactionID = "delivery-status-in-transport",
            deliveryStatus = CommunicationDeliveryStatusErpModel.InTransport,
            inTransportETA = InTransportETA(
                from = 1735736400,
                to = 1735741800
            ),
            inTransportPosition = InTransportPosition(
                latitude = 52.522529939635795,
                longitude = 13.387595793605172
            ),
            text = "Verfolgen Sie Ihre Bestellung auf der Webseite des Versanddienstleisters:"
        )
    )
}

@LightDarkPreview
@Composable
private fun DeliveryStatusDeliveredMessageCardPreview() {
    DeliveryStatusPreviewCard(
        payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            transactionID = "delivery-status-delivered",
            deliveryStatus = CommunicationDeliveryStatusErpModel.Delivered,
            text = "Ihre Medikamente sind angekommen. Gute Genesung!"
        )
    )
}

@LightDarkPreview
@Composable
private fun DeliveryStatusIncidentMessageCardPreview() {
    DeliveryStatusPreviewCard(
        payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            transactionID = "delivery-status-incident",
            deliveryStatus = CommunicationDeliveryStatusErpModel.Incident
        ),
        phoneNumber = "030 1234567",
        mailAddress = "kontakt@apotheke.de",
        isPharmacyOpen = true
    )
}

@LightDarkPreview
@Composable
private fun DeliveryStatusIncidentFallbackMessageCardPreview() {
    PreviewTheme {
        DeliveryStatusMessageCard(
            time = "17:48 Uhr",
            payload = CommunicationReplyDeliveryStatusPayloadErpModel(
                transactionID = "delivery-status-incident-fallback",
                deliveryStatus = CommunicationDeliveryStatusErpModel.Incident
            ),
            phoneNumber = "030 1234567",
            mailAddress = "kontakt@apotheke.de",
            isPharmacyOpen = true,
            onClickPhone = {},
            onClickMail = {},
            onClickLocation = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun DeliveryStatusUnknownMessageCardPreview() {
    DeliveryStatusPreviewCard(
        payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            transactionID = "delivery-status-unknown",
            deliveryStatus = CommunicationDeliveryStatusErpModel.Unknown,
            text = "Zum aktuellen Lieferstatus liegen noch keine Informationen vor."
        )
    )
}

@Composable
fun DeliveryStatusPreviewCard(
    payload: CommunicationReplyDeliveryStatusPayloadErpModel,
    phoneNumber: String? = null,
    mailAddress: String? = null,
    isPharmacyOpen: Boolean? = null
) {
    PreviewTheme {
        DeliveryStatusMessageCard(
            time = "17:48 Uhr",
            payload = payload,
            phoneNumber = phoneNumber,
            mailAddress = mailAddress,
            isPharmacyOpen = isPharmacyOpen,
            pharmacyMap = MockMap(),
            onClickLocation = {}
        )
    }
}

@LightDarkPreview
@Composable
fun CommResV3EuCardsPreview() {
    PreviewTheme {
        Column {
            EuAccessCodeCreatedMessageCard(
                time = "17:48 Uhr",
                title = "Willkommen in Spanien 🇪🇸",
                description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien " +
                    "abgerufen. Spanien kann bis 19:48 Uhr auf Ihre ausgewählten Rezepte " +
                    "zugreifen.",
                onClickShowCode = {},
                onClickRevokeAccess = {}
            )
            SpacerMedium()
            EuAccessCodeRevokedMessageCard(
                time = "17:48 Uhr",
                title = "Willkommen in Spanien 🇪🇸",
                description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien " +
                    "abgerufen. Spanien kann nicht mehr auf Ihre ausgewählten Rezepte " +
                    "zugreifen."
            )
            SpacerMedium()
            EuAccessCodeGeneratedMessageCard(
                time = "17:48 Uhr",
                title = "Einlösecode generiert",
                description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien abgerufen."
            )
            SpacerMedium()
            EuPrescriptionRemovedMessageCard(
                time = "17:48 Uhr",
                description = stringResource(R.string.eu_messages_prescription_removed_body)
            )
            SpacerMedium()
            EuPrescriptionAddedMessageCard(
                time = "17:48 Uhr",
                description = stringResource(R.string.eu_messages_prescription_added_body)
            )
            SpacerMedium()
            EuPrescriptionRedeemedMessageCard(
                time = "18:00 Uhr",
                title = "Apotheke Barcelona",
                description = stringResource(R.string.eu_messages_code_used_by_pharmacy_message_body, "Spanien")
            )
        }
    }
}

private fun previewTask() = TaskErpModel.Scanned(
    profileId = "1",
    taskId = "1",
    redeemedOn = null,
    accessCode = "1",
    name = "Medis",
    isEuRedeemable = false,
    communications = emptyList(),
    scannedOn = Instant.DISTANT_PAST,
    index = 1
)
