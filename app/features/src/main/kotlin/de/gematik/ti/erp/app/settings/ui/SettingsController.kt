/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class SettingsController(
    private val settingsUseCase: SettingsUseCase,
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    getScreenShotsAllowedUseCase: GetScreenShotsAllowedUseCase,
    private val allowScreenshotsUseCase: AllowScreenshotsUseCase,
    private val scope: CoroutineScope,
    private val analytics: Analytics
) {

    private val analyticsFlow by lazy {
        isAnalyticsAllowedUseCase().map { SettingStatesData.AnalyticsState(it) }
    }

    val analyticsState
        @Composable
        get() = analyticsFlow.collectAsStateWithLifecycle(SettingStatesData.defaultAnalyticsState)

    fun changeAnalyticsState(boolean: Boolean) {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(boolean)
        }
    }

    private val authenticationModeFlow = settingsUseCase.authenticationMode.map {
        SettingStatesData.AuthenticationModeState(
            it
        )
    }

    val authenticationModeState
        @Composable
        get() = authenticationModeFlow.collectAsStateWithLifecycle(SettingStatesData.defaultAuthenticationState)

    private val zoomFlow = settingsUseCase.general.map { SettingStatesData.ZoomState(it.zoomEnabled) }

    val zoomState
        @Composable
        get() = zoomFlow.collectAsStateWithLifecycle(SettingStatesData.defaultZoomState)

    private val screenShotsAllowed =
        getScreenShotsAllowedUseCase.invoke()

    val screenShotsState
        @Composable
        get() = screenShotsAllowed.collectAsStateWithLifecycle(false)

    suspend fun onSelectDeviceSecurityAuthenticationMode() {
        settingsUseCase.saveAuthenticationMode(
            SettingsData.AuthenticationMode.DeviceSecurity
        )
    }

    fun onAllowScreenshots(allow: Boolean) = scope.launch {
        allowScreenshotsUseCase.invoke(allow)
    }

    suspend fun onEnableZoom() {
        settingsUseCase.saveZoomPreference(true)
    }

    suspend fun onDisableZoom() {
        settingsUseCase.saveZoomPreference(false)
    }

    @Requirement(
        "O.Purp_5#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Disable usage analytics."
    )
    fun onTrackingDisallowed() {
        analytics.setAnalyticsPreference(false)
    }

    private var insecureDevicePromptShown = false
    val showInsecureDevicePrompt = settingsUseCase
        .showInsecureDevicePrompt
        .map {
            // onb ...
            if (!insecureDevicePromptShown) {
                insecureDevicePromptShown = true
                it
            } else {
                false
            }
        }

    suspend fun onAcceptInsecureDevice() {
        settingsUseCase.acceptInsecureDevice()
    }
}

@Composable
fun rememberSettingsController(): SettingsController {
    val settingsUseCase by rememberInstance<SettingsUseCase>()
    val isAnalyticsAllowedUseCase by rememberInstance<IsAnalyticsAllowedUseCase>()
    val changeAnalyticsStateUseCase by rememberInstance<ChangeAnalyticsStateUseCase>()
    val getScreenShotsAllowedUseCase by rememberInstance<GetScreenShotsAllowedUseCase>()
    val allowScreenshotsUseCase by rememberInstance<AllowScreenshotsUseCase>()
    val analytics by rememberInstance<Analytics>()
    val scope = rememberCoroutineScope()

    return remember {
        SettingsController(
            settingsUseCase = settingsUseCase,
            isAnalyticsAllowedUseCase = isAnalyticsAllowedUseCase,
            changeAnalyticsStateUseCase = changeAnalyticsStateUseCase,
            getScreenShotsAllowedUseCase = getScreenShotsAllowedUseCase,
            allowScreenshotsUseCase = allowScreenshotsUseCase,
            analytics = analytics,
            scope = scope
        )
    }
}

object SettingStatesData {

    @Immutable
    data class AnalyticsState(
        val analyticsAllowed: Boolean
    )

    val defaultAnalyticsState = AnalyticsState(analyticsAllowed = false)

    @Immutable
    data class AuthenticationModeState(
        val authenticationMode: SettingsData.AuthenticationMode
    )

    val defaultAuthenticationState = AuthenticationModeState(SettingsData.AuthenticationMode.Unspecified)

    @Immutable
    data class ZoomState(
        val zoomEnabled: Boolean
    )

    val defaultZoomState = ZoomState(zoomEnabled = false)
}
