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

package de.gematik.ti.erp.app.messages.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.MessageTimeState
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import kotlinx.datetime.Instant

class MessageDetailInAppPreviewParameterProvider : PreviewParameterProvider<List<InAppMessage>> {
    override val values = sequenceOf(
        inAppPreview
    )
}

const val IN_APP_MESSAGE =
    "Nutzen Sie ab sofort die E-Rezept App, um Ihre Entscheidung zur Organspende im **digitalen Organspenderegister** festzuhalten – sicher und " +
        "#unkompliziert.\u2028\n\nAktuell warten 9.192 Menschen in Deutschland dringend auf ein Organ – jede Entscheidung zählt und kann Leben retten.\n\n"

private val inAppPreview = listOf(
    InAppMessage(
        id = "123",
        from = "Team",
        text = IN_APP_MESSAGE,
        timeState = MessageTimeState.ShowDate(Instant.parse("2024-11-08T15:20:00Z")),
        prescriptionsCount = 0,
        tag = "Version 1.26.0",
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = "1.26.0"
    ),
    InAppMessage(
        id = "123",
        from = "Team",
        text = IN_APP_MESSAGE,
        timeState = MessageTimeState.ShowDate(Instant.parse("2024-11-08T15:20:00Z")),
        prescriptionsCount = 0,
        tag = "Version 1.27.0",
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = "1.27.0"
    )
)
