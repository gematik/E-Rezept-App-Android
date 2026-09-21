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

package de.gematik.ti.erp.app.messages.mapper

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.messages.model.ChangeLogMessage
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.timestate.getTimeState
import kotlinx.datetime.Instant

fun ChangeLogMessage.toInternalMessage(
    changeLogSender: String,
    changeLogTag: String,
    changeLogTimeStamp: Instant,
    language: String
): InternalMessageErpModel {
    return InternalMessageErpModel(
        id = this@toInternalMessage.id,
        version = this@toInternalMessage.version,
        time = changeLogTimeStamp,
        sender = changeLogSender,
        tag = changeLogTag,
        text = this@toInternalMessage.text ?: "",
        isUnread = true,
        messageProfile = CommunicationErpModel.CommunicationProfile.InApp,
        languageCode = language
    )
}

fun InternalMessageErpModel.toInAppMessage(): InAppMessage {
    return InAppMessage(
        id = id,
        from = sender,
        text = text,
        timeState = getTimeState(time),
        prescriptionsCount = 0,
        tag = tag,
        isUnread = isUnread,
        lastMessage = null,
        messageProfile = messageProfile,
        version = version
    )
}
