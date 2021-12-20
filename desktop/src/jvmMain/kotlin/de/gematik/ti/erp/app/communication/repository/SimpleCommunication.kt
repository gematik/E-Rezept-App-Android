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
