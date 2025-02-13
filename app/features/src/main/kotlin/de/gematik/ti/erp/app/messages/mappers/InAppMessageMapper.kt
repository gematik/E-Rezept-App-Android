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

package de.gematik.ti.erp.app.messages.mappers

import de.gematik.ti.erp.app.db.entities.v1.InAppMessageEntity
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.LocalInAppJsonMessage
import de.gematik.ti.erp.app.messages.domain.model.getTimeState
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Instant

fun LocalInAppJsonMessage.toInAppMessage(from: String, tag: String, timeStamp: Instant): InAppMessage {
    return InAppMessage(
        id = id,
        from = from,
        timeState = getTimeState(timeStamp),
        text = text,
        tag = tag,
        isUnread = true,
        lastMessage = null,
        messageProfile = CommunicationProfile.InApp,
        version = version
    )
}

fun InAppMessage.toEntity(): InAppMessageEntity {
    return InAppMessageEntity().apply {
        @PrimaryKey
        id = this@toEntity.id
        version = this@toEntity.version
        isUnRead = true
    }
}
