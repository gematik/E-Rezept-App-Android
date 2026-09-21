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

package de.gematik.ti.erp.app.database.room.v2.eurezept

import de.gematik.ti.erp.app.database.room.v2.euredeem.EuAccessCodeEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderWithRelations
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuTaskEventEntity
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.eurezept.model.EuTaskEventErpModel
import io.github.aakira.napier.Napier

fun EuAccessCodeErpModel.toEntity() = EuAccessCodeEntity(
    accessCode = accessCode,
    countryCode = countryCode,
    validUntil = validUntil,
    createdAt = createdAt,
    profileId = profileIdentifier
)

fun EuAccessCodeEntity.toModel() = EuAccessCodeErpModel(
    accessCode = accessCode,
    countryCode = countryCode,
    validUntil = validUntil,
    createdAt = createdAt,
    profileIdentifier = profileId
)

fun EuOrderErpModel.toEntity(lastModifiedAt: kotlinx.datetime.Instant?) = EuOrderEntity(
    orderId = orderId,
    countryCode = countryCode,
    createdAt = createdAt,
    lastModifiedAt = lastModifiedAt,
    profileId = profileId,
    euAccessCodeCode = euAccessCode?.accessCode,
    relatedTaskIds = relatedTaskIds
)

fun EuTaskEventErpModel.toEntity(orderId: String) = EuTaskEventEntity(
    id = id,
    orderId = orderId,
    type = type.name,
    taskId = taskId,
    createdAt = createdAt,
    isUnread = isUnread
)

fun EuTaskEventEntity.toModel(): EuTaskEventErpModel {
    val eventType = try {
        EuEventType.valueOf(type)
    } catch (e: Exception) {
        Napier.e("Unknown EuTaskEvent type: $type", e)
        EuEventType.UNKNOWN
    }
    return EuTaskEventErpModel(
        id = id,
        type = eventType,
        taskId = taskId,
        createdAt = createdAt,
        isUnread = isUnread
    )
}

fun EuOrderWithRelations.toModel() = EuOrderErpModel(
    orderId = order.orderId,
    countryCode = order.countryCode,
    createdAt = order.createdAt,
    lastModifiedAt = order.lastModifiedAt,
    profileId = order.profileId,
    euAccessCode = accessCode?.toModel(),
    events = events
        .sortedWith(compareBy<EuTaskEventEntity> { it.createdAt }.thenBy { it.id })
        .map { it.toModel() },
    relatedTaskIds = order.relatedTaskIds
)
