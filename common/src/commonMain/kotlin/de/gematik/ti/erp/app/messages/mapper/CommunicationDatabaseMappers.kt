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

import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirReplyCommunicationEntryErpModel
import kotlinx.datetime.Clock

object CommunicationDatabaseMappers {

    fun FhirReplyCommunicationEntryErpModel.toDatabaseModel() = CommunicationEntityV1()
        .apply {
            this.profile = CommunicationProfileV1.ErxCommunicationReply
            this.taskId = this@toDatabaseModel.taskId ?: ""
            this.communicationId = this@toDatabaseModel.id
            this.orderId = this@toDatabaseModel.orderId ?: ""
            this.sentOn = this@toDatabaseModel.sent?.value?.toRealmInstant() ?: Clock.System.now().toRealmInstant()
            this.sender = this@toDatabaseModel.sender?.identifier ?: ""
            this.recipient = this@toDatabaseModel.recipient?.identifier ?: ""
            this.payload = this@toDatabaseModel.payload.text.toString()
            this.consumed = false
        }

    fun FhirDispenseCommunicationEntryErpModel.toDatabaseModel() = CommunicationEntityV1()
        .apply {
            this.profile = CommunicationProfileV1.ErxCommunicationDispReq
            this.taskId = this@toDatabaseModel.taskId ?: ""
            this.communicationId = this@toDatabaseModel.id
            this.orderId = this@toDatabaseModel.orderId ?: ""
            this.sentOn = this@toDatabaseModel.sent?.value?.toRealmInstant() ?: Clock.System.now().toRealmInstant()
            this.sender = this@toDatabaseModel.sender?.identifier ?: ""
            this.recipient = this@toDatabaseModel.recipient?.identifier ?: ""
            this.payload = this@toDatabaseModel.payload.contentString.toString()
            this.consumed = false
        }
}
