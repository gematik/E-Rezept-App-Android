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

package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettings(): Flow<Settings>

    @Query("SELECT COUNT(*) FROM settings LIMIT 1")
    fun isNotEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSettings(settings: Settings)

    @Query(
        """UPDATE settings SET
        pharmacySearch_name = :name,
        pharmacySearch_locationEnabled = :locationEnabled,
        pharmacySearch_filterReady = :filterReady,
        pharmacySearch_filterDeliveryService = :filterDeliveryService,
        pharmacySearch_filterOnlineService = :filterOnlineService,
        pharmacySearch_filterOpenNow = :filterOpenNow
    """
    )
    suspend fun updatePharmacySearch(
        name: String,
        locationEnabled: Boolean,
        filterReady: Boolean,
        filterDeliveryService: Boolean,
        filterOnlineService: Boolean,
        filterOpenNow: Boolean
    )

    @Query("UPDATE settings SET authenticationMethod = :authenticationMethod, password_salt = :salt, password_hash = :hash")
    suspend fun updateAuthenticationMethod(
        authenticationMethod: SettingsAuthenticationMethod,
        salt: ByteArray? = null,
        hash: ByteArray? = null
    )

    @Query("UPDATE settings SET zoomEnabled = :enabled")
    suspend fun updateZoom(enabled: Boolean)

    @Query("UPDATE settings SET authenticationFails = authenticationFails + 1")
    suspend fun incrementNumberOfAuthenticationFailures()

    @Query("UPDATE settings SET authenticationFails = 0")
    suspend fun resetNumberOfAuthenticationFailures()

    @Query("UPDATE settings SET userHasAcceptedInsecureDevice = 1")
    suspend fun acceptInsecureDevice()

    @Query("UPDATE settings SET dataProtectionVersionAccepted = :date")
    suspend fun acceptDataProtectionVersion(date: LocalDate)
}
