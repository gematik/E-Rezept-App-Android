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

package de.gematik.ti.erp.app.pharmacy.repository.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.math.abs

private const val EPSILON = 1e-6

enum class RoleCode {
    OUT_PHARM, MOBL, PHARM
}

data class PharmacySearchResult(
    val pharmacies: List<Pharmacy>,
    val bundleId: String,
    val bundleResultCount: Int
)

@Parcelize
data class Location(
    val latitude: Double,
    val longitude: Double
) : Parcelable {

    fun distanceInMeters(other: Location): Double {
        val distance = FloatArray(1)
        android.location.Location.distanceBetween(
            this.latitude,
            this.longitude,
            other.latitude,
            other.longitude,
            distance
        )
        return distance[0].toDouble()
    }

    /**
     * @see distanceInMeters
     */
    operator fun minus(other: Location) = distanceInMeters(other)

    override fun equals(other: Any?): Boolean =
        if (other == null || other !is Location) {
            false
        } else {
            abs(this.latitude - other.latitude) < EPSILON && abs(this.longitude - other.longitude) < EPSILON
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
    val city: String,
)

@Parcelize
data class PharmacyContacts(
    val phone: String,
    val mail: String,
    val url: String,
) : Parcelable

data class Pharmacy(
    val name: String,
    val address: PharmacyAddress,
    val location: Location,
    val contacts: PharmacyContacts,
    val provides: List<PharmacyService>,
    val telematikId: String,
    val roleCode: List<RoleCode>,
    val ready: Boolean
)

interface PharmacyService : Parcelable {
    val openingHours: OpeningHours

    fun isOpenAt(tm: OffsetDateTime) = openingHours.isOpenAt(tm)

    fun isAllDayOpen(day: DayOfWeek) = openingHours[day]?.any { it.isAllDayOpen() } ?: false

    fun openUntil(tm: OffsetDateTime): LocalTime? {
        val localTm = tm.toLocalTime()
        return openingHours[tm.dayOfWeek]?.find {
            it.isOpenAt(localTm)
        }?.closingTime
    }

    fun opensAt(tm: OffsetDateTime): LocalTime? {
        val localTm = tm.toLocalTime()
        return openingHours[tm.dayOfWeek]?.find {
            it.openingTime >= localTm
        }?.openingTime
    }
}

// data class OnlinePharmacyService(
//    val name: String, override val openingHours: List<OpeningHours>
// ) : PharmacyService

@Parcelize
data class DeliveryPharmacyService(
    val name: String,
    override val openingHours: OpeningHours
) : PharmacyService

@Parcelize
data class EmergencyPharmacyService(
    val name: String,
    override val openingHours: OpeningHours
) : PharmacyService

@Parcelize
data class LocalPharmacyService(
    val name: String,
    override val openingHours: OpeningHours
) : PharmacyService

@Parcelize
data class OpeningHours(val openingTime: Map<DayOfWeek, List<OpeningTime>>) :
    Parcelable,
    Map<DayOfWeek, List<OpeningTime>> by openingTime

@Parcelize
data class OpeningTime(
    val openingTime: LocalTime,
    val closingTime: LocalTime
) : Parcelable {
    fun isOpenAt(tm: LocalTime) = tm in openingTime..closingTime
    fun isAllDayOpen() = openingTime == LocalTime.MIN && closingTime == LocalTime.MAX
}

fun OpeningHours.isOpenAt(tm: OffsetDateTime) =
    get(tm.dayOfWeek)?.any {
        it.isOpenAt(tm.toLocalTime())
    } ?: false

fun Map.Entry<DayOfWeek, List<OpeningTime>>.isOpenToday(tm: OffsetDateTime) =
    key == tm.dayOfWeek && value.isNotEmpty()
