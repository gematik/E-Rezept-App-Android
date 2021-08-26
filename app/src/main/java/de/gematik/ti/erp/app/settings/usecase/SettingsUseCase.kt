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

import android.content.SharedPreferences
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.db.entities.HealthCardUser
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.settings.repository.HealthCardUserRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.ui.NEW_USER
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.app.KeyguardManager
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken

class SettingsUseCase @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val idpRepository: IdpRepository,
    private val userRepository: HealthCardUserRepository,
    @ApplicationPreferences
    private val appPrefs: SharedPreferences,
    private val demoUseCase: DemoUseCase
) {
    val settings =
        settingsRepository.settings()

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

    @OptIn(FlowPreview::class)
    fun healthCardUser(): Flow<List<HealthCardUser>> =
        demoUseCase.demoModeActive.flatMapConcat {
            if (it) {
                flowOf(
                    listOf(
                        HealthCardUser(
                            name = "Anna Vetter",
                            cardAccessNumber = "123456"
                        )
                    )
                )
            } else {
                userRepository.healthCardUser()
            }
        }

    suspend fun isPasswordValid(password: String): Boolean {
        return settingsRepository.loadPassword()?.let {
            settingsRepository.hashPasswordWithSalt(password, it.salt).contentEquals(it.hash)
        } ?: false
    }

    suspend fun saveHealthCardUser(user: HealthCardUser) {
        // TODO handle demo mode
        userRepository.saveHealthCardUser(user)
    }

    suspend fun clearIDPDataAndCAN() {
        idpRepository.invalidateWithUserCredentials()
    }

    data class Token(
        val accessToken: String? = null,
        val singleSignOnToken: SingleSignOnToken? = null
    )

    suspend fun getToken(): Token {
        return Token(idpRepository.decryptedAccessToken, idpRepository.getSingleSignOnToken())
    }

    suspend fun logout() {
        idpRepository.invalidateWithUserCredentials()
    }
}
