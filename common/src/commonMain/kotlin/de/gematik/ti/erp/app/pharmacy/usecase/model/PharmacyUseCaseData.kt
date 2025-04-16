/*
 * Copyright 2025, gematik GmbH
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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.pharmacy.usecase.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPositionErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningHoursErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningTimeErpModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OpeningTime.Companion.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyServiceSerializationType.DeliveryPharmacyServiceType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyServiceSerializationType.EmergencyPharmacyServiceType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyServiceSerializationType.LocalPharmacyServiceType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyServiceSerializationType.OnlinePharmacyServiceType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyServiceSerializationType.PickUpPharmacyServiceType
import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter.Companion.toTextFilter
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EqEpsilon = 1e-6
private const val EarthRadiusInMeter = 6371e3

object PharmacyUseCaseData {
    @Requirement(
        "A_20285#5",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "The initial filter[Filter] are based on the UI of the app which is then mapped into the filter[PharmacyFilter]."
    )
    // can go into the new file with search data
    @Immutable
    data class Filter(
        val nearBy: Boolean = false,
        val deliveryService: Boolean = false,
        val onlineService: Boolean = false,
        val openNow: Boolean = false,
        val directRedeem: Boolean = false
    ) {
        fun isAnySet(): Boolean =
            nearBy || deliveryService || onlineService || openNow || directRedeem
    }

    @Serializable
    data class OpeningHours(
        val openingTime: Map<DayOfWeek, List<OpeningTime>>
    ) : Map<DayOfWeek, List<OpeningTime>> by openingTime {
        companion object {
            fun OpeningHoursErpModel.toModel() = OpeningHours(openingTime = openingTime.toModel())
        }
    }

    @Serializable
    data class OpeningTime(
        val openingTime: LocalTime?,
        val closingTime: LocalTime?
    ) {
        fun isOpenAt(tm: LocalTime) =
            when {
                openingTime == null && closingTime != null -> tm <= closingTime
                openingTime != null && closingTime == null -> tm >= openingTime
                openingTime == null && closingTime == null -> true
                openingTime != null && closingTime != null -> tm in openingTime..closingTime
                else -> error("Unreachable")
            }

        fun isAllDayOpen() = openingTime == null && closingTime == null

        companion object {
            fun Map<DayOfWeek, List<OpeningTimeErpModel>>.toModel(): Map<DayOfWeek, List<OpeningTime>> =
                mapValues { (_, erpList) ->
                    erpList.map { OpeningTime(it.openingTime, it.closingTime) }
                }
        }
    }

    fun OpeningHours.isOpenAt(tm: LocalDateTime) =
        get(tm.dayOfWeek)?.any {
            it.isOpenAt(tm.time)
        } ?: false

    fun Map.Entry<DayOfWeek, List<OpeningTime>>.isOpenToday(tm: LocalDateTime) =
        key == tm.dayOfWeek && value.isNotEmpty()

    /*
  Since kotlinx.serialization does not support PolymorphicSerializer of sealed interfaces
  out of the box we need to add a type to let the serializer know the difference if it is
  a sealed class or sealed interface.
*/
    enum class PharmacyServiceSerializationType {
        OnlinePharmacyServiceType,
        PickUpPharmacyServiceType,
        DeliveryPharmacyServiceType,
        EmergencyPharmacyServiceType,
        LocalPharmacyServiceType
    }

    @Serializable(with = PharmacyServiceSerializer::class)
    sealed interface PharmacyService {

        val name: String
        val type: PharmacyServiceSerializationType

        // shipment
        @Serializable
        data class OnlinePharmacyService(
            override val name: String,
            override val type: PharmacyServiceSerializationType = OnlinePharmacyServiceType
        ) : PharmacyService

        @Serializable
        data class PickUpPharmacyService(
            override val name: String,
            override val type: PharmacyServiceSerializationType = PickUpPharmacyServiceType
        ) : PharmacyService

        // courier service
        @Serializable
        data class DeliveryPharmacyService(
            override val name: String,
            override val openingHours: OpeningHours,
            override val type: PharmacyServiceSerializationType = DeliveryPharmacyServiceType
        ) : TemporalPharmacyService, PharmacyService

        @Serializable
        data class EmergencyPharmacyService(
            override val name: String,
            override val openingHours: OpeningHours,
            override val type: PharmacyServiceSerializationType = EmergencyPharmacyServiceType
        ) : TemporalPharmacyService, PharmacyService

        @Serializable
        data class LocalPharmacyService(
            override val name: String,
            override val openingHours: OpeningHours,
            override val type: PharmacyServiceSerializationType = LocalPharmacyServiceType
        ) : TemporalPharmacyService, PharmacyService
    }

    object PharmacyServiceSerializer : JsonContentPolymorphicSerializer<PharmacyService>(PharmacyService::class) {
        override fun selectDeserializer(element: JsonElement): KSerializer<out PharmacyService> {
            element.jsonObject["type"]?.jsonPrimitive?.content?.let { classType ->
                return when (PharmacyServiceSerializationType.valueOf(classType)) {
                    OnlinePharmacyServiceType -> PharmacyService.OnlinePharmacyService.serializer()
                    PickUpPharmacyServiceType -> PharmacyService.PickUpPharmacyService.serializer()
                    DeliveryPharmacyServiceType -> PharmacyService.DeliveryPharmacyService.serializer()
                    EmergencyPharmacyServiceType -> PharmacyService.EmergencyPharmacyService.serializer()
                    LocalPharmacyServiceType -> PharmacyService.LocalPharmacyService.serializer()
                }
            }
                ?: throw SerializationException(
                    "PharmacyServiceSerializer: key 'type' not found or does not matches any module type"
                )
        }
    }

    interface TemporalPharmacyService {
        val openingHours: OpeningHours
        fun isOpenAt(tm: LocalDateTime) = openingHours.isOpenAt(tm)
        fun isAllDayOpen(day: java.time.DayOfWeek) = openingHours[day]?.any { it.isAllDayOpen() } ?: false
        fun openUntil(localDateTime: LocalDateTime): LocalTime? {
            val localTime = localDateTime.time
            return openingHours[localDateTime.dayOfWeek]?.find {
                it.isOpenAt(localTime)
            }?.closingTime
        }

        fun opensAt(localDateTime: LocalDateTime): LocalTime? {
            val localTime = localDateTime.time
            return openingHours[localDateTime.dayOfWeek]?.find {
                if (it.openingTime == null) {
                    true
                } else {
                    it.openingTime >= localTime
                }
            }?.openingTime
        }
    }

    @Serializable
    data class PharmacyContact(
        val phone: String,
        val mail: String,
        val url: String,
        // required only for zuweisung-ohne-ti
        val pickUpUrl: String,
        val deliveryUrl: String,
        val onlineServiceUrl: String
    ) {
        companion object {
            fun FhirContactInformationErpModel.toModel() =
                PharmacyContact(
                    phone = phone,
                    mail = mail,
                    url = url,
                    pickUpUrl = pickUpUrl ?: "",
                    deliveryUrl = deliveryUrl ?: "",
                    onlineServiceUrl = onlineServiceUrl ?: ""
                )
        }
    }

    @Serializable
    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    ) {
        /**
         * Haversine distance between two points on a sphere.
         */
        private fun distanceInMeters(other: Coordinates): Double {
            val dLat = toRadians(other.latitude - this.latitude)
            val dLon = toRadians(other.longitude - this.longitude)
            val lat1 = toRadians(this.latitude)
            val lat2 = toRadians(other.latitude)
            val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
            val c = 2 * asin(sqrt(a))
            return EarthRadiusInMeter * c
        }

        private fun toRadians(deg: Double) = deg / 180.0 * PI
        operator fun minus(other: Coordinates) = distanceInMeters(other)
        override fun equals(other: Any?): Boolean =
            if (other == null || other !is Coordinates) {
                false
            } else {
                abs(this.latitude - other.latitude) < EqEpsilon && abs(this.longitude - other.longitude) < EqEpsilon
            }

        override fun hashCode(): Int {
            var result = latitude.hashCode()
            result = 31 * result + longitude.hashCode()
            return result
        }

        companion object {
            fun FhirPositionErpModel.toModel() =
                Coordinates(
                    latitude = this.latitude,
                    longitude = this.longitude
                )
        }
    }

    @Serializable
    @Immutable
    data class Pharmacy(
        val id: String,
        val name: String,
        val address: String?,
        val coordinates: Coordinates?,
        val distance: Double?,
        val contact: PharmacyContact, // Telecom
        val provides: List<PharmacyService>,
        val openingHours: OpeningHours?,
        val telematikId: String
    ) {
        val isPickupService
            get() = provides.any { it is PharmacyService.PickUpPharmacyService }

        val isDeliveryService
            get() = provides.any { it is PharmacyService.DeliveryPharmacyService }

        val isOnlineService
            get() = provides.any { it is PharmacyService.OnlinePharmacyService }

        val directRedeemUrlsNotPresent: Boolean
            get() {
                val hasNoPickupContact = contact.pickUpUrl.isEmpty()
                val hasNoDeliveryContact = contact.deliveryUrl.isEmpty()
                val hasNoOnlineServiceContact = contact.onlineServiceUrl.isEmpty()
                return listOf(
                    hasNoPickupContact,
                    hasNoDeliveryContact,
                    hasNoOnlineServiceContact
                ).all { it }
            }

        @Stable
        fun singleLineAddress(): String =
            if (address.isNullOrEmpty()) {
                ""
            } else {
                address.replace("\n", ", ")
            }
    }

    // can go into the new file with search data
    sealed class LocationMode {

        @Immutable
        data object Disabled : LocationMode()

        @Immutable
        data class Enabled(
            val coordinates: Coordinates,
            val radiusInMeter: Double = DefaultRadiusInMeter
        ) : LocationMode()
    }

    // search data could be in its own file
    @Immutable
    data class SearchData(
        val name: String,
        val filter: Filter,
        val locationMode: LocationMode
    ) {
        companion object {
            fun SearchData.toPharmacyFilter() =
                PharmacyFilter(
                    textFilter = name.toTextFilter(),
                    locationFilter = if (locationMode is LocationMode.Enabled) {
                        LocationFilter(
                            latitude = locationMode.coordinates.latitude,
                            longitude = locationMode.coordinates.longitude
                        )
                    } else {
                        null
                    },
                    serviceFilter = ServiceFilter(
                        courier = filter.deliveryService,
                        shipment = filter.onlineService,
                        pickup = filter.nearBy
                    )
                )
        }
    }

    @Immutable
    data class MapsSearchData(
        val name: String,
        val filter: Filter,
        val locationMode: LocationMode,
        val coordinates: Coordinates?
    ) {
        companion object {
            fun MapsSearchData.toPharmacyFilter(forcedRadius: Double?) =
                PharmacyFilter(
                    textFilter = name.toTextFilter(),
                    locationFilter = when {
                        locationMode is LocationMode.Enabled ->
                            LocationFilter(
                                latitude = locationMode.coordinates.latitude,
                                longitude = locationMode.coordinates.longitude,
                                radius = forcedRadius ?: DefaultRadiusInMeter
                            )

                        coordinates != null ->
                            LocationFilter(
                                latitude = coordinates.latitude,
                                longitude = coordinates.longitude,
                                radius = forcedRadius ?: DefaultRadiusInMeter
                            )

                        else -> null
                    },
                    serviceFilter = ServiceFilter(
                        courier = filter.deliveryService,
                        shipment = filter.onlineService,
                        pickup = filter.nearBy
                    )
                )
        }
    }

    // maybe these should be placed somewhere else
    @Immutable
    data class PrescriptionInOrder(
        val taskId: String,
        val accessCode: String,
        val title: String?,
        val isSelfPayerPrescription: Boolean,
        val index: Int?,
        val timestamp: Instant,
        val substitutionsAllowed: Boolean,
        val isScanned: Boolean
    )

    @Immutable
    data class ShippingContact(
        val name: String,
        val line1: String,
        val line2: String,
        val postalCode: String,
        val city: String,
        val telephoneNumber: String,
        val mail: String,
        val deliveryInformation: String
    ) {
        @Stable
        fun address() = listOf(
            line1,
            line2,
            postalCode,
            city
        ).filter { it.isNotBlank() }

        @Stable
        fun other() = listOf(
            telephoneNumber,
            mail,
            deliveryInformation
        ).filter { it.isNotBlank() }

        @Stable
        fun isEmpty() = address().isEmpty() && other().isEmpty()

        companion object {
            val EmptyShippingContact = ShippingContact(
                name = "",
                line1 = "",
                line2 = "",
                postalCode = "",
                city = "",
                telephoneNumber = "",
                mail = "",
                deliveryInformation = ""
            )
        }
    }

    @Immutable
    data class OrderState(
        val prescriptionsInOrder: List<PrescriptionInOrder>,
        val selfPayerPrescriptionIds: List<String>,
        val contact: ShippingContact
    ) {
        val selfPayerPrescriptionNames = prescriptionsInOrder
            .filter { it.taskId in this.selfPayerPrescriptionIds }
            .mapNotNull { it.title }

        companion object {
            val Empty = OrderState(
                prescriptionsInOrder = emptyList(),
                selfPayerPrescriptionIds = emptyList(),
                contact = ShippingContact.EmptyShippingContact
            )
        }
    }
}
