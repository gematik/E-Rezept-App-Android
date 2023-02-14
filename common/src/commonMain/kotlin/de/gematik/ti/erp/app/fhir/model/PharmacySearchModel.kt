/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
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

data class Location(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Haversine distance between two points on a sphere.
     */
    fun distanceInMeters(other: Location): Double {
        val dLat = toRadians(other.latitude - this.latitude)
        val dLon = toRadians(other.longitude - this.longitude)
        val lat1 = toRadians(this.latitude)
        val lat2 = toRadians(other.latitude)
        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        return EarthRadiusInMeter * c
    }

    private fun toRadians(deg: Double) = deg / 180.0 * PI
    operator fun minus(other: Location) = distanceInMeters(other)
    override fun equals(other: Any?): Boolean =
        if (other == null || other !is Location) {
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

data class PharmacyAddress(
    val lines: List<String>,
    val postalCode: String,
    val city: String
)

data class PharmacyContacts(
    val phone: String,
    val mail: String,
    val url: String
)

data class Pharmacy(
    val name: String,
    val address: PharmacyAddress,
    val location: Location,
    val contacts: PharmacyContacts,
    val provides: List<PharmacyService>,
    val telematikId: String,
    val ready: Boolean
)

sealed interface PharmacyService

interface TemporalPharmacyService : PharmacyService {
    val openingHours: OpeningHours
    fun isOpenAt(tm: LocalDateTime) = openingHours.isOpenAt(tm)
    fun isAllDayOpen(day: DayOfWeek) = openingHours[day]?.any { it.isAllDayOpen() } ?: false
    fun openUntil(tm: LocalDateTime): LocalTime? {
        val localTm = tm.time
        return openingHours[tm.dayOfWeek]?.find {
            it.isOpenAt(localTm)
        }?.closingTime
    }

    fun opensAt(tm: LocalDateTime): LocalTime? {
        val localTm = tm.time
        return openingHours[tm.dayOfWeek]?.find {
            if (it.openingTime == null) {
                true
            } else {
                it.openingTime >= localTm
            }
        }?.openingTime
    }
}

data class OnlinePharmacyService(
    val name: String
) : PharmacyService

data class PickUpPharmacyService(
    val name: String
) : PharmacyService

data class DeliveryPharmacyService(
    val name: String,
    override val openingHours: OpeningHours
) : TemporalPharmacyService

data class EmergencyPharmacyService(
    val name: String,
    override val openingHours: OpeningHours
) : TemporalPharmacyService

data class LocalPharmacyService(
    val name: String,
    override val openingHours: OpeningHours
) : TemporalPharmacyService

data class OpeningHours(val openingTime: Map<DayOfWeek, List<OpeningTime>>) :
    Map<DayOfWeek, List<OpeningTime>> by openingTime

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
