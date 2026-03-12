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

package de.gematik.ti.erp.app.database.room.v2.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsRoomEntity(
    @PrimaryKey val id: Int = 1,
    val zoomEnabled: Boolean,
    val welcomeDrawerShown: Boolean,
    val timeEpochMillis: Long,
    val mainScreenTooltipsShown: Boolean,
    val pharmacySearch_name: String?,
    val pharmacySearch_locationEnabled: Boolean?,
    val pharmacySearch_filterReady: Boolean?,
    val pharmacySearch_filterDeliveryService: Boolean?,
    val pharmacySearch_filterOnlineService: Boolean?,
    val pharmacySearch_filterOpenNow: Boolean?,
    val userHasAcceptedInsecureDevice: Boolean,
    val userHasAcceptedIntegrityNotOk: Boolean,
    val dataProtectionVersionAcceptedEpochMillis: Long,
    val latestAppVersionName: String,
    val latestAppVersionCode: Int,
    val onboardingLatestAppVersionName: String,
    val onboardingLatestAppVersionCode: Int,
    val mlKitAccepted: Boolean,
    val screenshotsAllowed: Boolean,
    val trackingAllowed: Boolean
) {
    companion object {
        /**
         * Example instance with sensible defaults mirroring legacy v1 SettingsEntity defaults.
         * timeEpochMillis/dataProtectionVersionAcceptedEpochMillis correspond to 2021-10-15T00:00:00Z.
         */
        val Example: SettingsRoomEntity = SettingsRoomEntity(
            id = 1,
            zoomEnabled = false,
            welcomeDrawerShown = false,
            timeEpochMillis = 1634256000000, // 2021-10-15T00:00:00Z
            mainScreenTooltipsShown = false,
            pharmacySearch_name = null,
            pharmacySearch_locationEnabled = false,
            pharmacySearch_filterReady = false,
            pharmacySearch_filterDeliveryService = false,
            pharmacySearch_filterOnlineService = false,
            pharmacySearch_filterOpenNow = false,
            userHasAcceptedInsecureDevice = false,
            userHasAcceptedIntegrityNotOk = false,
            dataProtectionVersionAcceptedEpochMillis = 1634256000000, // 2021-10-15T00:00:00Z
            latestAppVersionName = "",
            latestAppVersionCode = -1,
            onboardingLatestAppVersionName = "",
            onboardingLatestAppVersionCode = -1,
            mlKitAccepted = false,
            screenshotsAllowed = false,
            trackingAllowed = false
        )

        /** Convenience builder for a fresh default settings row. */
        fun example(): SettingsRoomEntity = Example
    }
}
