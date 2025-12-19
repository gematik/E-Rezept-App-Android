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

package de.gematik.ti.erp.app.messages.mappers

import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.timestate.TimeState
import kotlinx.datetime.Instant

internal fun EuOrderMessageUiModel.toInAppMessage(
    title: String,
    threadStart: Instant?,
    threadEnd: Instant?,
    hasUnreadTasks: Boolean
): InAppMessage {
    val timeState = this.timestamp?.let { TimeState.ShowDate(it) } ?: TimeState.ShowDate(Instant.DISTANT_PAST)

    return InAppMessage(
        id = id,
        from = title,
        text = buildMessageText(),
        timeState = timeState,
        prescriptionsCount = taskIds.size,
        tag = "",
        isUnread = hasUnreadTasks,
        lastMessage = null,
        messageProfile = CommunicationProfile.EuOrder,
        version = "",
        threadOrderId = orderId,
        threadStart = threadStart,
        threadEnd = threadEnd
    )
}

private fun EuOrderMessageUiModel.buildMessageText(): String {
    return buildString {
        append(title)
        description?.let {
            append("\n\n")
            append(it)
        }
    }
}
