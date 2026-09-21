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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.pharmacy.model

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

@Serializable
data class PositionErpModel(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Haversine distance between two points on a sphere.
     */
    private fun distanceInMeters(other: PositionErpModel): Double {
        val dLat = toRadians(other.latitude - this.latitude)
        val dLon = toRadians(other.longitude - this.longitude)
        val lat1 = toRadians(this.latitude)
        val lat2 = toRadians(other.latitude)
        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        return EarthRadiusInMeter * c
    }

    private fun toRadians(deg: Double) = deg / 180.0 * PI

    operator fun minus(other: PositionErpModel) = distanceInMeters(other)

    override fun equals(other: Any?): Boolean =
        if (other == null || other !is PositionErpModel) {
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
