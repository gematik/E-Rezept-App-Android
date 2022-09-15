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

package de.gematik.ti.erp.app.settings.repository

import androidx.room.withTransaction
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.PasswordEntity
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.prescription.repository.LocalDataSource
import de.gematik.ti.erp.app.secureRandomInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import java.security.MessageDigest
import java.time.LocalDate
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val db: AppDatabase,
    private val localDataSource: LocalDataSource,
) {
    fun settings(): Flow<Settings> {
        return db.settingsDao().getSettings().onStart {
            db.withTransaction {
                if (!db.settingsDao().isNotEmpty()) {
                    db.settingsDao().insertSettings(
                        Settings(
                            authenticationMethod = SettingsAuthenticationMethod.Unspecified,
                            authenticationFails = 0,
                            zoomEnabled = false
                        )
                    )
                }
            }
        }
    }

    suspend fun savePharmacySearch(
        name: String,
        locationEnabled: Boolean,
        filterReady: Boolean,
        filterDeliveryService: Boolean,
        filterOnlineService: Boolean,
        filterOpenNow: Boolean
    ) {
        db.settingsDao().updatePharmacySearch(
            name = name,
            locationEnabled = locationEnabled,
            filterReady = filterReady,
            filterDeliveryService = filterDeliveryService,
            filterOnlineService = filterOnlineService,
            filterOpenNow = filterOpenNow
        )
    }

    suspend fun saveZoomPreference(enabled: Boolean) {
        db.settingsDao().updateZoom(enabled)
    }

    suspend fun saveAuthenticationMethod(authenticationMethod: SettingsAuthenticationMethod) {
        db.settingsDao().updateAuthenticationMethod(authenticationMethod, null, null)
    }

    suspend fun incrementNumberOfAuthenticationFailures() {
        db.settingsDao().incrementNumberOfAuthenticationFailures()
    }

    suspend fun resetNumberOfAuthenticationFailures() {
        db.settingsDao().resetNumberOfAuthenticationFailures()
    }

    suspend fun acceptInsecureDevice() {
        db.settingsDao().acceptInsecureDevice()
    }

    suspend fun savePasswordAsAuthenticationMethod(password: String) {
        val salt = ByteArray(32).apply {
            secureRandomInstance().nextBytes(this)
        }

        val hash = hashPasswordWithSalt(password, salt)

        db.settingsDao().updateAuthenticationMethod(SettingsAuthenticationMethod.Password, hash = hash, salt = salt)
    }

    fun hashPasswordWithSalt(password: String, salt: ByteArray): ByteArray {
        val combined = password.toByteArray() + salt

        return MessageDigest.getInstance("SHA-256").digest(combined)
    }

    suspend fun loadPassword(): PasswordEntity? =
        db.settingsDao().getSettings().first().password

    suspend fun updatedDataTermsAccepted(date: LocalDate) {
        db.settingsDao().acceptDataProtectionVersion(date)
    }
}
