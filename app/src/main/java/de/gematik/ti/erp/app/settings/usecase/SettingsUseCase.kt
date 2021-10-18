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

package de.gematik.ti.erp.app.settings.usecase

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.db.entities.Profile
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.ui.NEW_USER
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

const val DEFAULT_PROFILE_NAME = ""

class SettingsUseCase @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val idpRepository: IdpRepository,
    private val profilesRepository: ProfilesRepository,
    @ApplicationPreferences
    private val appPrefs: SharedPreferences,
    private val demoUseCase: DemoUseCase,
    private val profilesUseCase: ProfilesUseCase
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

    @ExperimentalCoroutinesApi
    @OptIn(FlowPreview::class)
    fun profiles(): Flow<List<SettingsScreen.UIProfile>> =
        demoUseCase.demoModeActive.flatMapConcat {
            if (it) {
                flowOf(
                    listOf(
                        SettingsScreen.UIProfile(
                            id = 0,
                            name = "Anna Vetter",
                            active = false
                        )
                    )
                )
            } else {
                profilesRepository.activeProfile().filterNotNull().flatMapLatest { activeProfile ->
                    profilesRepository.profiles().map { profiles ->
                        profiles.map { profile ->
                            val active = activeProfile.profileName == profile.name
                            SettingsScreen.UIProfile(profile.id, profile.name, active)
                        }
                    }
                }
            }
        }

    suspend fun isPasswordValid(password: String): Boolean {
        return settingsRepository.loadPassword()?.let {
            settingsRepository.hashPasswordWithSalt(password, it.salt).contentEquals(it.hash)
        } ?: false
    }

    suspend fun saveProfile(profile: Profile) {
        // TODO handle demo mode
        profilesRepository.saveProfile(profile)
    }

    suspend fun clearIDPDataAndCAN(profileName: String) {
        idpRepository.invalidateWithUserCredentials(profileName)
    }

    data class Token(
        val accessToken: String? = null,
        val singleSignOnToken: SingleSignOnToken? = null
    )

    suspend fun getToken(): Token {
        val activeProfileName = profilesUseCase.activeProfileName().first()
        return Token(
            idpRepository.decryptedAccessToken,
            idpRepository.getSingleSignOnToken(activeProfileName)
        )
    }

    suspend fun logout() {
        val activeProfileName = profilesUseCase.activeProfileName().first()
        idpRepository.invalidateWithUserCredentials(activeProfileName)
    }

    fun isProfileSetupCompleted() =
        profilesUseCase.activeProfileName().map {
            it != DEFAULT_PROFILE_NAME
        }

    suspend fun overwriteDefaultProfileName(profileName: String) {
        profilesUseCase.updateProfileName(DEFAULT_PROFILE_NAME, profileName)
    }

    suspend fun activateProfile(profileName: String) {
        profilesUseCase.insertActiveProfile(profileName)
    }
}
