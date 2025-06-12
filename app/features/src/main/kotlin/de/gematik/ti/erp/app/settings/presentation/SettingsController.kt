/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.featuretoggle.datasource.FeatureToggleDataStore
import de.gematik.ti.erp.app.featuretoggle.datasource.Features
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetZoomStateUseCase
import de.gematik.ti.erp.app.settings.usecase.HasValidDigasUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveZoomPreferenceUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SettingsController(
    getProfilesUseCase: GetProfilesUseCase,
    getScreenShotsAllowedUseCase: GetScreenShotsAllowedUseCase,
    getZoomStateUseCase: GetZoomStateUseCase,
    featureToggleDataStore: FeatureToggleDataStore,
    private val allowScreenshotsUseCase: AllowScreenshotsUseCase,
    private val saveZoomPreferenceUseCase: SaveZoomPreferenceUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val endpointHelper: EndpointHelper,
    private val hasValidDigasUseCase: HasValidDigasUseCase
) : Controller() {

    private val zoomFlow = getZoomStateUseCase.invoke().map { SettingStatesData.ZoomState(it) }
    private val screenShotsAllowedFlow = getScreenShotsAllowedUseCase.invoke()

    val profiles: StateFlow<List<ProfilesUseCaseData.Profile>> = getProfilesUseCase()
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), emptyList())

    val zoomState: StateFlow<SettingStatesData.ZoomState> = zoomFlow.stateIn(
        controllerScope,
        SharingStarted.WhileSubscribed(),
        SettingStatesData.defaultZoomState
    )

    val screenShotsState: StateFlow<Boolean> = screenShotsAllowedFlow.stateIn(
        controllerScope,
        SharingStarted.WhileSubscribed(),
        false
    )

    private val _hasValidDigas: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val hasValidDigas: StateFlow<Boolean> = _hasValidDigas

    val allowScreenshotsEvent = ComposableEvent<Unit>()

    val isMedicationPlanEnabled: StateFlow<Boolean> =
        featureToggleDataStore.isFeatureEnabled(Features.MEDICATION_PLAN)
            .stateIn(
                controllerScope,
                SharingStarted.WhileSubscribed(),
                false
            )

    init {
        hasValidDigas()
    }

    fun onAllowScreenshots(allow: Boolean) = viewModelScope.launch {
        if (allow) {
            allowScreenshotsEvent.trigger(Unit)
        } else {
            allowScreenshotsUseCase.invoke(false)
        }
    }

    val intentEvent = ComposableEvent<String>()

    fun confirmAllowScreenshots() = viewModelScope.launch {
        allowScreenshotsUseCase.invoke(true)
    }

    fun onToggleEnableZoom(enabled: Boolean) {
        viewModelScope.launch {
            saveZoomPreferenceUseCase.invoke(enabled)
        }
    }

    fun createOrganDonationRegisterIntent() {
        controllerScope.launch {
            val profile = getActiveProfileUseCase.invoke().first()
            var url = endpointHelper.getOrganDonationRegisterInfoHost()

            profile.let {
                val token = profile.ssoTokenScope

                if (token is IdpData.ExternalAuthenticationToken) {
                    val iss = token.authenticatorId
                    val issSafe = URLEncoder.encode(iss, StandardCharsets.UTF_8.toString())
                    url = "${endpointHelper.getOrganDonationRegisterIntentHost()}?iss=$issSafe"
                }
            }
            intentEvent.trigger(url)
        }
    }

    private fun hasValidDigas() {
        controllerScope.launch {
            getActiveProfileUseCase()
                .firstOrNull()
                ?.let { profile ->
                    hasValidDigasUseCase(profile.id).collect { isValid ->
                        _hasValidDigas.value = isValid
                    }
                }
        }
    }
}

@Composable
fun rememberSettingsController(): SettingsController {
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getScreenShotsAllowedUseCase by rememberInstance<GetScreenShotsAllowedUseCase>()
    val allowScreenshotsUseCase by rememberInstance<AllowScreenshotsUseCase>()
    val getZoomStateUseCase by rememberInstance<GetZoomStateUseCase>()
    val saveZoomPreferenceUseCase by rememberInstance<SaveZoomPreferenceUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val endpointHelper by rememberInstance<EndpointHelper>()
    val featureToggleDataStore by rememberInstance<FeatureToggleDataStore>()
    val hasValidDigasUseCase by rememberInstance<HasValidDigasUseCase>()

    return remember {
        SettingsController(
            getProfilesUseCase = getProfilesUseCase,
            getScreenShotsAllowedUseCase = getScreenShotsAllowedUseCase,
            allowScreenshotsUseCase = allowScreenshotsUseCase,
            saveZoomPreferenceUseCase = saveZoomPreferenceUseCase,
            getZoomStateUseCase = getZoomStateUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            endpointHelper = endpointHelper,
            featureToggleDataStore = featureToggleDataStore,
            hasValidDigasUseCase = hasValidDigasUseCase
        )
    }
}
