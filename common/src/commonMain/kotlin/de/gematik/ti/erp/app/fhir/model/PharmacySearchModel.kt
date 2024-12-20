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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.fhir.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EqEpsilon = 1e-6
private const val EarthRadiusInMeter = 6371e3

data class PharmacyServices(
    val pharmacies: List<Pharmacy>,
    val bundleId: String,
    val bundleResultCount: Int
)

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
}

@Serializable
data class PharmacyAddress(
    val lines: List<String>,
    val postalCode: String,
    val city: String
)

@Serializable
data class PharmacyContacts(
    val phone: String,
    val mail: String,
    val url: String,
    val pickUpUrl: String,
    val deliveryUrl: String,
    val onlineServiceUrl: String
)

data class Pharmacy(
    val id: String,
    val name: String,
    val address: PharmacyAddress,
    val coordinates: Coordinates? = null,
    val contacts: PharmacyContacts,
    val provides: List<PharmacyService>,
    val telematikId: String
)

@Serializable
data class OpeningHours(val openingTime: Map<DayOfWeek, List<OpeningTime>>) :
    Map<DayOfWeek, List<OpeningTime>> by openingTime

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
}

fun OpeningHours.isOpenAt(tm: LocalDateTime) =
    get(tm.dayOfWeek)?.any {
        it.isOpenAt(tm.time)
    } ?: false

fun Map.Entry<DayOfWeek, List<OpeningTime>>.isOpenToday(tm: LocalDateTime) =
    key == tm.dayOfWeek && value.isNotEmpty()
