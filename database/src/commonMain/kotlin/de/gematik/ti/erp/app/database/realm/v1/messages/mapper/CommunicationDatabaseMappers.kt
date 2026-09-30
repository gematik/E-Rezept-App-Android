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

package de.gematik.ti.erp.app.database.realm.v1.messages.mapper

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.datetime.Clock

object CommunicationDatabaseMappers {

    fun CommunicationErpModel.toDatabaseModel(): CommunicationEntityV1 {
        return CommunicationEntityV1().apply {
            this.profile = this@toDatabaseModel.profile.toEntityValue() ?: CommunicationProfileV1.ErxCommunicationDispReq
            this.taskId = this@toDatabaseModel.taskId
            this.communicationId = this@toDatabaseModel.communicationId
            this.orderId = this@toDatabaseModel.orderId
            this.sentOn = this@toDatabaseModel.timeStamp?.toRealmInstant() ?: Clock.System.now().toRealmInstant()
            this.sender = this@toDatabaseModel.senderTelematikId
            this.recipient = this@toDatabaseModel.recipient
            this.payload = this@toDatabaseModel.payload?.let {
                SafeJson.value.encodeToString(CommunicationPayloadErpModel.serializer(), it)
            }.orEmpty()
            this.consumed = this@toDatabaseModel.consumed
            this.pharmacyName = this@toDatabaseModel.pharmacyName
        }
    }

    fun FhirReplyCommunicationEntryErpModel.toDatabaseModel(): CommunicationEntityV1 {
        return CommunicationEntityV1()
            .apply {
                this.profile = CommunicationProfileV1.ErxCommunicationReply
                this.taskId = this@toDatabaseModel.taskId ?: ""
                this.communicationId = this@toDatabaseModel.id
                this.orderId = this@toDatabaseModel.orderId ?: ""
                this.sentOn = this@toDatabaseModel.sent?.value?.toRealmInstant() ?: Clock.System.now().toRealmInstant()
                this.sender = this@toDatabaseModel.sender?.identifier ?: ""
                this.recipient = this@toDatabaseModel.recipient?.identifier ?: ""
                this.payload = this@toDatabaseModel.payload.toString()
                this.consumed = false
                this.pharmacyName = this@toDatabaseModel.pharmacyName
            }
    }

    fun FhirDispenseCommunicationEntryErpModel.toDatabaseModel(): CommunicationEntityV1 {
        return CommunicationEntityV1()
            .apply {
                this.profile = CommunicationProfileV1.ErxCommunicationDispReq
                this.taskId = this@toDatabaseModel.taskId ?: ""
                this.communicationId = this@toDatabaseModel.id
                this.orderId = this@toDatabaseModel.orderId ?: ""
                this.sentOn = this@toDatabaseModel.sent?.value?.toRealmInstant() ?: Clock.System.now().toRealmInstant()
                this.sender = this@toDatabaseModel.sender?.identifier ?: ""
                this.recipient = this@toDatabaseModel.recipient?.identifier ?: ""
                this.payload = this@toDatabaseModel.payload.toString()
                this.consumed = false
                this.pharmacyName = this@toDatabaseModel.pharmacyName
            }
    }
}
