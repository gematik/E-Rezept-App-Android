/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.ti.erp.app.attestation.usecase.IntegrityUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(
    private val integrityUseCase: IntegrityUseCase,
    private val settingsUseCase: SettingsUseCase
) : ViewModel() {
    val zoomEnabled = settingsUseCase.general.map { it.zoomEnabled }
    val authenticationMethod = settingsUseCase.authenticationMode
    var showOnboarding = runBlocking { settingsUseCase.showOnboarding.first() }
    var showWelcomeDrawer = runBlocking { settingsUseCase.showWelcomeDrawer }

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

    var integrityPromptShown = false

    fun checkDeviceIntegrity() = integrityUseCase.runIntegrityAttestation().map {
        if (!it && !integrityPromptShown) {
            integrityPromptShown = true
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

    fun acceptMlKit() {
        viewModelScope.launch {
            settingsUseCase.acceptMlKit()
        }
    }

    fun acceptUpdatedDataTerms() {
        viewModelScope.launch {
            settingsUseCase.acceptUpdatedDataTerms()
        }
    }

    suspend fun welcomeDrawerShown() {
        settingsUseCase.welcomeDrawerShown()
    }

    suspend fun mainScreenTooltipsShown() {
        settingsUseCase.mainScreenTooltipsShown()
    }

    fun showMainScreenToolTips(): Flow<Boolean> = settingsUseCase.general
        .map { !it.mainScreenTooltipsShown && it.welcomeDrawerShown }

    fun dataProtectionVersionAcceptedOn() =
        settingsUseCase.general.map { it.dataProtectionVersionAcceptedOn }

    fun mlKitNotAccepted() =
        settingsUseCase.general.map { !it.mlKitAccepted }
}
