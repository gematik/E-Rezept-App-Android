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

package de.gematik.ti.erp.app.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.attestation.usecase.SafetynetUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import io.github.aakira.napier.Napier
import java.net.URI

class MainViewModel(
    private val settingsUseCase: SettingsUseCase,
    safetynetUseCase: SafetynetUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val idpUseCase: IdpUseCase
) : ViewModel() {
    val zoomEnabled = settingsUseCase.general.map { it.zoomEnabled }
    val authenticationMethod = settingsUseCase.authenticationMode
    var showOnboarding = runBlocking { settingsUseCase.showOnboarding.first() }

    private var insecureDevicePromptShown = false
    val showInsecureDevicePrompt = settingsUseCase
        .showInsecureDevicePrompt
        .map {
            if (showOnboarding) {
                false
            } else if (!insecureDevicePromptShown) {
                insecureDevicePromptShown = true
                it
            } else {
                false
            }
        }

    var showDataTermsUpdate = settingsUseCase.showDataTermsUpdate

    private var safetynetPromptShown = false
    val showSafetynetPrompt =
        safetynetUseCase.runSafetynetAttestation()
            .map {
                if (!it && !safetynetPromptShown) {
                    safetynetPromptShown = true
                    false
                } else {
                    true
                }
            }

    fun onAcceptInsecureDevice() {
        viewModelScope.launch {
            settingsUseCase.acceptInsecureDevice()
        }
    }

    fun acceptUpdatedDataTerms() {
        viewModelScope.launch {
            settingsUseCase.acceptUpdatedDataTerms()
        }
    }

    fun dataProtectionVersionAcceptedOn() =
        settingsUseCase.general.map { it.dataProtectionVersionAcceptedOn }

    val hasActiveProfileToken = profilesUseCase.activeProfile
        .map {
            it.ssoTokenScope != null
        }

    suspend fun onExternAppAuthorizationResult(uri: URI): Result<Unit> =
        runCatching {
            Napier.d("Authenticate external ...")
            idpUseCase.authenticateWithExternalAppAuthorization(uri)
            Napier.d("... authenticated")
        }
}
