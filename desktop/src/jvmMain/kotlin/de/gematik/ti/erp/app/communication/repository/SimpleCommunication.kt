/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.repository

import java.time.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class SimpleCommunication {
    abstract val id: String
    abstract val profile: CommunicationProfile
    abstract val sent: LocalDateTime?
}

data class SimpleCommunicationWithPharmacy(
    override val id: String,
    override val profile: CommunicationProfile,
    override val sent: LocalDateTime?,
    val basedOnTaskWithId: String,
    val telematicsId: String, // pharmacy id
    val userId: String, // refer to KVNR (e.g. X123456789)
    val payload: CommunicationPayloadInbox?
) : SimpleCommunication()

enum class CommunicationProfile {
    DispenseRequest, Reply
}

@Serializable
data class CommunicationPayloadInbox(
    @SerialName("version") val version: String = "1",
    @SerialName("supplyOptionsType") val supplyOptionsType: CommunicationSupplyOption,
    @SerialName("info_text") val infoText: String,
    @SerialName("url") val url: String?,
    @SerialName("pickUpCodeHR") val pickUpCodeHR: String?,
    @SerialName("pickUpCodeDMC") val pickUpCodeDMC: String?
)

@Serializable
enum class CommunicationSupplyOption {
    @SerialName("onPremise")
    OnPremise,

    @SerialName("shipment")
    Shipment,

    @SerialName("delivery")
    Delivery
}
