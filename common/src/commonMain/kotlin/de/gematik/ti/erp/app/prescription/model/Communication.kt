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

package de.gematik.ti.erp.app.prescription.model

import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Communication(
    val taskId: String,
    val communicationId: String,
    val orderId: String,
    val profile: CommunicationProfile,
    val sentOn: Instant,
    val sender: String,
    val recipient: String,
    val payload: String?,
    val consumed: Boolean
)

enum class CommunicationProfile {
    ErxCommunicationDispReq, ErxCommunicationReply, InApp;

    fun toEntityValue() = when (this) {
        ErxCommunicationDispReq ->
            CommunicationProfileV1.ErxCommunicationDispReq

        ErxCommunicationReply ->
            CommunicationProfileV1.ErxCommunicationReply

        InApp -> InApp
    }.name
}
