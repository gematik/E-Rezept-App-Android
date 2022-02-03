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

package de.gematik.ti.erp.app.settings.usecase

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.ui.NEW_USER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

const val DEFAULT_PROFILE_NAME = ""
val DATA_PROTECTION_LAST_UPDATED: LocalDate = LocalDate.parse(BuildKonfig.DATA_PROTECTION_LAST_UPDATED)

class SettingsUseCase @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    @ApplicationPreferences
    private val appPrefs: SharedPreferences,
) {
    val settings = settingsRepository.settings()

    val zoomEnabled =
        settings.map { it.zoomEnabled }

    val showInsecureDevicePrompt =
        settings.map {
            val deviceSecured =
                (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceSecure

            if (!deviceSecured) {
                !it.userHasAcceptedInsecureDevice
            } else {
                false
            }
        }

    val authenticationMethod =
        settings.map { it.authenticationMethod }

    // TODO move to database
    var isNewUser: Boolean
        get() = appPrefs.getBoolean(NEW_USER, true)
        set(v) {
            appPrefs.edit().putBoolean(NEW_USER, v).apply()
        }

    var showDataTermsUpdate: Flow<Boolean> =
        settings.map {
            it.dataProtectionVersionAccepted < DATA_PROTECTION_LAST_UPDATED
        }

    val pharmacySearch =
        settings.map { it.pharmacySearch }

    suspend fun savePharmacySearch(
        name: String,
        locationEnabled: Boolean,
        filterReady: Boolean,
        filterDeliveryService: Boolean,
        filterOnlineService: Boolean,
        filterOpenNow: Boolean
    ) {
        settingsRepository.savePharmacySearch(
            name = name,
            locationEnabled = locationEnabled,
            filterReady = filterReady,
            filterDeliveryService = filterDeliveryService,
            filterOnlineService = filterOnlineService,
            filterOpenNow = filterOpenNow
        )
    }

    suspend fun saveAuthenticationMethod(authenticationMethod: SettingsAuthenticationMethod) {
        settingsRepository.saveAuthenticationMethod(authenticationMethod)
    }

    suspend fun savePasswordAsAuthenticationMethod(password: String) {
        settingsRepository.savePasswordAsAuthenticationMethod(password)
    }

    suspend fun saveZoomPreference(enabled: Boolean) {
        settingsRepository.saveZoomPreference(enabled)
    }

    suspend fun incrementNumberOfAuthenticationFailures() =
        settingsRepository.incrementNumberOfAuthenticationFailures()

    suspend fun resetNumberOfAuthenticationFailures() =
        settingsRepository.resetNumberOfAuthenticationFailures()

    suspend fun acceptInsecureDevice() =
        settingsRepository.acceptInsecureDevice()

    suspend fun isPasswordValid(password: String): Boolean {
        return settingsRepository.loadPassword()?.let {
            settingsRepository.hashPasswordWithSalt(password, it.salt).contentEquals(it.hash)
        } ?: false
    }

    suspend fun updatedDataTermsAccepted(date: LocalDate) {
        settingsRepository.updatedDataTermsAccepted(date)
    }

    fun dataProtectionVersionAccepted(): Flow<LocalDate> = settings.map {
        it.dataProtectionVersionAccepted
    }
}
