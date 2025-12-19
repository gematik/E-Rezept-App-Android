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

package de.gematik.ti.erp.app.eurezept.model

import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuAccessCodeEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuOrderEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuTaskEventLogEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuTaskEventLogFactory
import de.gematik.ti.erp.app.fhir.FhirEuRedeemAccessCodeResponseErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.toRealmList
import kotlinx.datetime.Clock.System.now
import java.util.UUID
import kotlin.time.ExperimentalTime

private fun EuAccessCode.toEuAccessCodeEntityV1() =
    EuAccessCodeEntityV1().apply {
        accessCode = this@toEuAccessCodeEntityV1.accessCode
        countryCode = this@toEuAccessCodeEntityV1.countryCode
        validUntil = this@toEuAccessCodeEntityV1.validUntil.toRealmInstant()
        createdAt = this@toEuAccessCodeEntityV1.createdAt.toRealmInstant()
        profileId = this@toEuAccessCodeEntityV1.profileIdentifier
    }

fun EuAccessCodeEntityV1.toEuAccessCode() =
    EuAccessCode(
        accessCode = this@toEuAccessCode.accessCode,
        countryCode = this@toEuAccessCode.countryCode,
        validUntil = this@toEuAccessCode.validUntil.toInstant(),
        createdAt = this@toEuAccessCode.createdAt.toInstant(),
        profileIdentifier = this@toEuAccessCode.profileId
    )

fun FhirEuRedeemAccessCodeResponseErpModel.toEuAccessCode(
    profileId: ProfileIdentifier
) = EuAccessCode(
    accessCode = this@toEuAccessCode.accessCode,
    countryCode = this@toEuAccessCode.countryCode,
    validUntil = this@toEuAccessCode.validUntil.value,
    createdAt = this@toEuAccessCode.createdAt.value,
    profileIdentifier = profileId
)

fun FhirEuRedeemAccessCodeResponseErpModel.toModel(
    profileId: ProfileIdentifier,
    relatedTaskIds: List<String>,
    orderId: String = UUID.randomUUID().toString()
) = EuOrder(
    orderId = orderId,
    profileId = profileId,
    countryCode = this@toModel.countryCode,
    createdAt = this@toModel.createdAt.value,
    euAccessCode = this.toEuAccessCode(profileId),
    events = emptyList(), // we do not have the events in the response, its built only at db level
    relatedTaskIds = relatedTaskIds
)

@OptIn(ExperimentalTime::class)
fun EuOrder.toEuOrderEntityV1(event: EuEventType): EuOrderEntityV1 =
    EuOrderEntityV1().apply {
        val newEvents = this@toEuOrderEntityV1.toEuTaskEventLogEntityV1(event)
        orderId = this@toEuOrderEntityV1.orderId
        profileId = this@toEuOrderEntityV1.profileId
        countryCode = this@toEuOrderEntityV1.countryCode
        createdAt = this@toEuOrderEntityV1.createdAt.toRealmInstant()
        lastModifiedAt = now().toRealmInstant()
        euAccessCode = this@toEuOrderEntityV1.euAccessCode?.toEuAccessCodeEntityV1()
        taskEvents = newEvents.toRealmList()
        relatedTaskIds = this@toEuOrderEntityV1.relatedTaskIds.toRealmList()
    }

fun EuOrder.toEuOrderEntityV1(
    eventType: EuEventType,
    affectedTaskIds: List<String>
): EuOrderEntityV1 {
    val newEvents = affectedTaskIds.map { tid ->
        EuTaskEventLogEntityV1().apply {
            id = UUID.randomUUID().toString()
            orderId = this@toEuOrderEntityV1.orderId
            taskId = tid
            event = eventType.name
            createdAt = now().toRealmInstant()
        }
    }

    return EuOrderEntityV1().apply {
        orderId = this@toEuOrderEntityV1.orderId
        profileId = this@toEuOrderEntityV1.profileId
        countryCode = this@toEuOrderEntityV1.countryCode
        createdAt = this@toEuOrderEntityV1.createdAt.toRealmInstant()
        lastModifiedAt = now().toRealmInstant()
        euAccessCode = this@toEuOrderEntityV1.euAccessCode?.toEuAccessCodeEntityV1()
        relatedTaskIds = this@toEuOrderEntityV1.relatedTaskIds.toRealmList()
        taskEvents = newEvents.toRealmList()
    }
}

fun EuOrder.toEuTaskEventLogEntityV1(event: EuEventType): List<EuTaskEventLogEntityV1> =
    EuTaskEventLogFactory.buildEvents(
        orderId = this@toEuTaskEventLogEntityV1.orderId,
        taskIds = this@toEuTaskEventLogEntityV1.relatedTaskIds,
        event = event.name
    )

fun EuOrderEntityV1.toModel() = EuOrder(
    orderId = this@toModel.orderId,
    profileId = this@toModel.profileId,
    countryCode = this@toModel.countryCode,
    createdAt = this@toModel.createdAt.toInstant(),
    euAccessCode = this@toModel.euAccessCode?.toEuAccessCode(),
    events = this@toModel.taskEvents.map { taskEventLog -> taskEventLog.toModel() },
    relatedTaskIds = this@toModel.relatedTaskIds.toList()
)

private fun EuTaskEventLogEntityV1.toModel(): EuTaskEvent {
    val type = event.let {
        try {
            EuEventType.valueOf(it)
        } catch (e: Exception) {
            Napier.e("Unknown EuTaskEvent type: $it", e)
            EuEventType.UNKNOWN
        }
    }
    return EuTaskEvent(
        id = this.id,
        type = type,
        createdAt = this.createdAt.toInstant(),
        taskId = this.taskId,
        isUnread = this.isUnread
    )
}
