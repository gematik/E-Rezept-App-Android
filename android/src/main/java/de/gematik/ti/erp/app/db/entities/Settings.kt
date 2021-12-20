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

package de.gematik.ti.erp.app.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SettingsAuthenticationMethod {
    HealthCard,
    DeviceSecurity,

    @Deprecated("Keep for older app versions migrating to a newer one with mandatory app protection.")
    Biometrics,

    @Deprecated("Keep for older app versions migrating to a newer one with mandatory app protection.")
    DeviceCredentials,
    Password,

    @Deprecated("Keep for older app versions migrating to a newer one with mandatory app protection.")
    None,
    Unspecified
}

data class PasswordEntity(
    val salt: ByteArray,
    val hash: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasswordEntity

        if (!salt.contentEquals(other.salt)) return false
        if (!hash.contentEquals(other.hash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = salt.contentHashCode()
        result = 31 * result + hash.contentHashCode()
        return result
    }
}

data class PharmacySearch(
    val name: String,
    val locationEnabled: Boolean,
    val filterReady: Boolean,
    val filterDeliveryService: Boolean,
    val filterOnlineService: Boolean,
    val filterOpenNow: Boolean
)

@Entity(tableName = "settings")
data class Settings(
    val authenticationMethod: SettingsAuthenticationMethod,
    val authenticationFails: Int,
    val zoomEnabled: Boolean,
    @Embedded(prefix = "password_")
    val password: PasswordEntity? = null,
    @Embedded(prefix = "pharmacySearch_")
    val pharmacySearch: PharmacySearch = PharmacySearch(
        name = "",
        locationEnabled = false,
        filterReady = false,
        filterDeliveryService = false,
        filterOnlineService = false,
        filterOpenNow = false
    ),
    val userHasAcceptedInsecureDevice: Boolean = false
) {
    @PrimaryKey
    var id: Long = 0
}
