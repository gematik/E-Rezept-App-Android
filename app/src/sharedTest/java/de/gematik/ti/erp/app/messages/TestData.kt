/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.messages

import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.attestation.SafetynetResult
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import de.gematik.ti.erp.app.messages.ui.models.ErrorUIMessage
import de.gematik.ti.erp.app.messages.ui.models.UIMessage

fun testUIMessage() =
    UIMessage(
        "communicationId",
        "onPremise",
        R.string.communication_shipment_inbox_header,
        "this is a test message",
        pickUpCodeHR = "hrPickup",
        pickUpCodeDMC = "dmcPickup",
        consumed = false
    )

fun testErrorUIMessage() =
    ErrorUIMessage(
        "communicationId",
        "none",
        R.string.communication_error_inbox_header,
        "the message that was sent",
        R.string.communication_error_inbox_display_text,
        "some time stamp",
        R.string.communication_error_action_text,
        false
    )

fun communicationOnPremise() =
    Communication(
        "id",
        CommunicationProfile.ErxCommunicationReply,
        "time",
        "taskId",
        "telematiksId",
        "kbvUserId",
        "{\"version\": \"1\",\"supplyOptionsType\": \"onPremise\",\"info_text\": \"Wir möchten Sie informieren, dass Ihre bestellten Medikamente zur Abholung bereitstehen. Den Abholcode finden Sie anbei.\",\"pickUpCodeHR\": \"12341234\",\"pickUpCodeDMC\": \"465465465f6s4g6df54gs65dfg\",\"url\": \"\"}",
        false
    )

fun listOfCommunicationsRead() =
    listOf(communicationOnPremise().copy(consumed = true))

fun listOfCommunicationsUnread() =
    listOf(communicationOnPremise())

fun safetynetAttestationEntity() =
    SafetynetAttestationEntity(id = 0, jws = "", ourNonce = "".toByteArray())

fun listOfAttestationEntities() =
    listOf(safetynetAttestationEntity())

fun safetynetResult() = SafetynetResult("")
