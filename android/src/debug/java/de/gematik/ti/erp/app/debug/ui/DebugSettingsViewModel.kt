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

package de.gematik.ti.erp.app.debug.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.jakewharton.processphoenix.ProcessPhoenix
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.App
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.debug.data.DebugSettingsData
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.ui.NEW_USER
import de.gematik.ti.erp.app.vau.repository.VauRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val DEBUG_SETTINGS_STATE = "DEBUG_SETTINGS_STATE"

@HiltViewModel
class DebugSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val endpointHelper: EndpointHelper,
    @ApplicationPreferences
    private val sharedPreferences: SharedPreferences,
    private val cardWallUseCase: CardWallUseCase,
    private val hintUseCase: HintUseCase,
    private val demoUseCase: DemoUseCase,
    private val prescriptionUseCase: PrescriptionUseCase,
    private val vauRepository: VauRepository,
    private val idpRepository: IdpRepository,
    private val profilesUseCase: ProfilesUseCase,
    private val featureToggleManager: FeatureToggleManager
) : BaseViewModel() {

    var debugSettingsData by mutableStateOf(
        savedStateHandle.get<DebugSettingsData>(DEBUG_SETTINGS_STATE) ?: createDebugSettingsData()
    )

    private fun createDebugSettingsData() = DebugSettingsData(
        endpointHelper.eRezeptServiceUri,
        endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.BASE_SERVICE_URI),
        endpointHelper.idpServiceUri,
        endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.IDP_SERVICE_URI),
        "",
        true,
        cardWallUseCase.deviceHasNFCAndAndroidMOrHigher,
        false,
        cardWallUseCase.cardWallIntroIsAccepted,
        false,
        activeProfileName = ""
    )

    suspend fun state() {
        val it = profilesUseCase.activeProfileName().first()
        updateState(
            debugSettingsData.copy(
                cardAccessNumberIsSet = cardWallUseCase.cardAccessNumberWasSaved().first(),
                activeProfileName = it,
                bearerToken = idpRepository.decryptedAccessTokenMap.value[it] ?: ""
            )
        )
    }

    fun updateState(debugSettingsData: DebugSettingsData) {
        this.debugSettingsData = debugSettingsData
        savedStateHandle[DEBUG_SETTINGS_STATE] = debugSettingsData
    }

    fun restartWithOnboarding() {
        sharedPreferences.edit().putBoolean(NEW_USER, true).commit()
        restart()
    }

    fun changeBearerToken(activeProfileName: String) {
        idpRepository.decryptedAccessTokenMap.update {
            it + (activeProfileName to debugSettingsData.bearerToken)
        }
        updateState(debugSettingsData.copy(bearerTokenIsSet = true))
    }

    fun breakSSOToken() = runBlocking {
        val activeProfileName = profilesUseCase.activeProfileName().first()
        idpRepository.getSingleSignOnToken(activeProfileName).first()?.let {
            val newToken = when (it) {
                is SingleSignOnToken.AlternateAuthenticationToken ->
                    it.copy(token = it.token.removeRange(0..2))
                is SingleSignOnToken.AlternateAuthenticationWithoutToken ->
                    it
                is SingleSignOnToken.DefaultToken ->
                    it.copy(token = it.token.removeRange(0..2))
            }

            idpRepository.setSingleSignOnToken(
                activeProfileName,
                newToken
            )
        }
    }

    fun saveAndRestartApp() {
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.BASE_SERVICE_URI,
            debugSettingsData.eRezeptServiceURL,
            debugSettingsData.eRezeptActive
        )
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.IDP_SERVICE_URI,
            debugSettingsData.idpUrl,
            debugSettingsData.idpActive
        )

        viewModelScope.launch {
            idpRepository.invalidateWithUserCredentials(profilesUseCase.activeProfileName().first())
            vauRepository.invalidate()
        }

        restart()
    }

    suspend fun resetCardAccessNumber() {
        cardWallUseCase.setCardAccessNumber(null)
        updateState(debugSettingsData.copy(cardAccessNumberIsSet = false))
    }

    fun resetCardWallIntro() {
        cardWallUseCase.cardWallIntroIsAccepted = false
        updateState(debugSettingsData.copy(cardWallIntroIsAccepted = false))
    }

    fun resetHints() {
        hintUseCase.resetAllHints()
    }

    fun allowNfc(value: Boolean) {
        cardWallUseCase.deviceHasNFCAndAndroidMOrHigher = value
        updateState(debugSettingsData.copy(fakeNFCCapabilities = value))
    }

    fun refreshPrescriptions() {
        if (demoUseCase.isDemoModeActive) {
            demoUseCase.authTokenReceived.value = true
        }
        viewModelScope.launch {
            prescriptionUseCase.downloadTasks(profilesUseCase.activeProfileName().first())
        }
    }

    fun features() = featureToggleManager.features

    fun featuresState() =
        featureToggleManager.featuresState()

    fun toggleFeature(feature: Features) {
        viewModelScope.launch {
            val key = booleanPreferencesKey(feature.featureName)
            featureToggleManager.toggleFeature(key)
        }
    }

    fun activeProfileName() =
        profilesUseCase.activeProfileName()

    private fun restart() {
        Thread.sleep(500)
        ProcessPhoenix.triggerRebirth(
            App.appContext, Intent(App.appContext, MainActivity::class.java)
        )
    }
}
