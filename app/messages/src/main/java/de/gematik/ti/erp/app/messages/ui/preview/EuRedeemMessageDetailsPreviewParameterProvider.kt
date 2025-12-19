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
package de.gematik.ti.erp.app.messages.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.messages.ui.preview.EuOrderMessageUiModelPreviewData.previewEuOrderCreatedMessages
import de.gematik.ti.erp.app.messages.ui.preview.EuOrderMessageUiModelPreviewData.previewEuOrderCreatedNoAccessCodeMessages
import de.gematik.ti.erp.app.messages.ui.preview.EuOrderMessageUiModelPreviewData.previewEuOrderRecreatedMessages
import de.gematik.ti.erp.app.messages.ui.preview.EuOrderMessageUiModelPreviewData.previewEuOrderRedeemedMessages

class EuRedeemMessageDetailsPreviewParameterProvider :
    PreviewParameterProvider<List<EuOrderMessageUiModel>> {

    override val values: Sequence<List<EuOrderMessageUiModel>> =
        sequenceOf(
            previewEuOrderCreatedMessages,
            previewEuOrderCreatedNoAccessCodeMessages,
            previewEuOrderRecreatedMessages,
            previewEuOrderRedeemedMessages
        )
}

val previewsForEuOrderMessageUiModel: List<List<EuOrderMessageUiModel>> = listOf(
    previewEuOrderRecreatedMessages,
    previewEuOrderRedeemedMessages,
    previewEuOrderCreatedMessages,
    previewEuOrderCreatedNoAccessCodeMessages
)

// ----------------------------------------------------------------------
// Preview Data
// ----------------------------------------------------------------------

object EuOrderMessageUiModelPreviewData {

    val previewEuOrderCreatedMessages = listOf(
        // ----------------------------------------------------
        // 1) CREATED (OLD) – no buttons
        // ----------------------------------------------------
        EuOrderMessageUiModel.AccessCodeCreated(
            id = "evt_created_1",
            underlyingEventIds = listOf(),
            orderId = "order-111",
            accessCode = "accessCode",
            dateTimeString = "02.05.25 um 18:57 Uhr",
            timestamp = null,
            isFirst = true,
            isLast = true,
            showButtons = true,
            taskIds = listOf("Gematidolor 100mg"),
            prescriptionNames = listOf("Gematidolor 100mg"),
            countryCode = "DE",
            title = "Willkommen in Spanien",
            flagEmoji = "\uD83C\uDDE7\uD83C\uDDEA",
            description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien abgerufen.",
            isRevoked = false,
            isUnread = false
        )
    )

    val previewEuOrderCreatedNoAccessCodeMessages = listOf(
        // ----------------------------------------------------
        // 1) CREATED (OLD) – no buttons
        // ----------------------------------------------------
        EuOrderMessageUiModel.AccessCodeCreated(
            id = "evt_created_1",
            underlyingEventIds = listOf(),
            orderId = "order-111",
            accessCode = "",
            dateTimeString = "02.05.25 um 18:57 Uhr",
            timestamp = null,
            isFirst = true,
            isLast = true,
            showButtons = false,
            taskIds = listOf("Gematidolor 100mg"),
            prescriptionNames = listOf("Gematidolor 100mg"),
            countryCode = "DE",
            title = "Willkommen in Spanien",
            flagEmoji = "\uD83C\uDDE7\uD83C\uDDEA",
            description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien abgerufen. " +
                "Spanien kann nicht mehr auf Ihre ausgewählten Rezepte zugreifen.",
            isRevoked = true,
            isUnread = false
        )
    )

    val previewEuOrderRedeemedMessages = listOf(
        // ----------------------------------------------------
        // 1) REDEEMED – final event
        // ----------------------------------------------------
        EuOrderMessageUiModel.TaskRedeemed(
            id = "evt_redeemed_1",
            underlyingEventIds = listOf(),
            orderId = "order-111",
            accessCode = "accessCode",
            dateTimeString = "02.05.25 um 18:57 Uhr",
            timestamp = null,
            isFirst = true,
            isLast = true,
            showButtons = false,
            taskIds = listOf("Gematidolor 100mg"),
            prescriptionNames = listOf("Gematidolor 100mg"),
            countryCode = "DE",
            title = "",
            description = "Die Apotheke in Spanien hat Ihr Rezept eingelöst.",
            isRevoked = false,
            isUnread = false
        )
    )
    val previewEuOrderRecreatedMessages = listOf(

        // ----------------------------------------------------
        // 3) RECREATED (LATEST) – with buttons
        // ----------------------------------------------------
        EuOrderMessageUiModel.AccessCodeRecreated(
            id = "evt_recreated_2",
            underlyingEventIds = listOf(),
            orderId = "order-111",
            accessCode = "accessCode",
            dateTimeString = "02.05.25 um 18:57 Uhr",
            timestamp = null,
            isFirst = false,
            isLast = false,
            showButtons = true,
            taskIds = listOf("Gematidolor 100mg"),
            prescriptionNames = listOf("Gematidolor 100mg"),
            countryCode = "DE",
            title = "Einlösecode generiert",
            description = "ie haben einen Code für die Einlösung Ihres Rezeptes in Spanien abgerufen. " +
                "Spanien konnte bis 19:48 Uhr auf Ihre ausgewählten Rezepte zugreifen.",
            isRevoked = false,
            isUnread = false
        ),

        // ----------------------------------------------------
        // 2) RECREATED (NOT LATEST) – no buttons
        // ----------------------------------------------------
        EuOrderMessageUiModel.AccessCodeRecreated(
            id = "evt_recreated_1",
            underlyingEventIds = listOf(),
            orderId = "order-111",
            accessCode = "accessCode",
            dateTimeString = "02.05.25 um 18:57 Uhr",
            timestamp = null,
            isFirst = false,
            isLast = false,
            showButtons = false,
            taskIds = listOf("Gematidolor 100mg"),
            prescriptionNames = listOf("Gematidolor 100mg"),
            countryCode = "DE",
            title = "Einlösecode generiert",
            description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien abgerufen.",
            isRevoked = false,
            isUnread = false
        ),

        // ----------------------------------------------------
        // 1) CREATED (OLD) – no buttons
        // ----------------------------------------------------
        EuOrderMessageUiModel.AccessCodeCreated(
            id = "evt_created_1",
            underlyingEventIds = listOf(),
            orderId = "order-111",
            accessCode = "accessCode",
            dateTimeString = "02.05.25 um 18:57 Uhr",
            timestamp = null,
            isFirst = false,
            isLast = true,
            showButtons = false,
            taskIds = listOf("Gematidolor 100mg"),
            prescriptionNames = listOf("Gematidolor 100mg"),
            countryCode = "DE",
            title = "Willkommen in Spanien",
            flagEmoji = "",
            description = "Sie haben einen Code für die Einlösung Ihres Rezeptes in Spanien abgerufen.",
            isRevoked = false,
            isUnread = false
        )
    )
}
