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

package de.gematik.ti.erp.app.database.room.v2.task.mappers

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.communication.model.toCommunicationProfile
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationPayloadParser
import kotlinx.datetime.Clock

/**
 * Convert FHIR communication entry models to Room entities.
 */
internal fun FhirReplyCommunicationEntryErpModel.toErpCommunicationEntity(
    task: ErpTaskEntity,
    profileId: String,
    orderId: String? = null,
    insurantId: String? = null
): ErpCommunicationEntity {
    return ErpCommunicationEntity(
        communicationId = id,
        orderId = orderId ?: "",
        taskId = task.taskId,
        profileId = profileId,
        telematikId = sender?.identifier ?: "",
        kvnr = recipient?.identifier ?: "",
        consumed = false,
        payload = payload?.let { CommunicationPayloadParser.extract(it, isRequest = false) },
        profile = CommunicationProfileV1.ErxCommunicationReply,
        recipient = recipient?.identifier ?: "",
        insuranceId = insurantId,
        timeStamp = sent?.value ?: Clock.System.now(),
        pharmacyName = pharmacyName
    )
}

internal fun FhirDispenseCommunicationEntryErpModel.toErpCommunicationEntity(
    task: ErpTaskEntity,
    profileId: String,
    insurantId: String? = null
): ErpCommunicationEntity {
    return ErpCommunicationEntity(
        communicationId = id,
        orderId = orderId ?: "",
        taskId = task.taskId,
        profileId = profileId,
        telematikId = sender?.identifier ?: "",
        kvnr = recipient?.identifier ?: "",
        consumed = false,
        payload = payload?.let { CommunicationPayloadParser.extract(it, isRequest = true) },
        profile = CommunicationProfileV1.ErxCommunicationDispReq,
        recipient = recipient?.identifier ?: "",
        insuranceId = insurantId,
        timeStamp = sent?.value ?: Clock.System.now(),
        pharmacyName = pharmacyName
    )
}

internal fun CommunicationErpModel.toErpCommunicationEntity(): ErpCommunicationEntity =
    ErpCommunicationEntity(
        communicationId = communicationId,
        orderId = orderId,
        taskId = taskId,
        profileId = profileId ?: "",
        telematikId = senderTelematikId,
        kvnr = recipient,
        consumed = consumed,
        payload = payload,
        profile = profile.toEntityValue() ?: CommunicationProfileV1.ErxCommunicationDispReq,
        recipient = recipient,
        // TODO CommResV3 CleanUp of Migration: DB Insurance is the wrong name here, should be insurant or profileId
        insuranceId = profileId,
        timeStamp = timeStamp ?: Clock.System.now(),
        pharmacyName = pharmacyName
    )

fun CommunicationEntityV1.toErpModel(profileId: String? = null): CommunicationErpModel {
    return CommunicationErpModel(
        communicationId = communicationId,
        orderId = orderId,
        taskId = taskId,
        senderTelematikId = sender,
        consumed = consumed,
        payload = payload?.let { CommunicationPayloadParser.extract(it, isRequest = profile == CommunicationProfileV1.ErxCommunicationDispReq) },
        recipient = recipient,
        profile = profile.toCommunicationProfile(),
        profileId = profileId ?: this.parent?.parent?.id,
        timeStamp = this.sentOn.toInstant(),
        pharmacyName = pharmacyName
    )
}
