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

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetZoomStateUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveZoomPreferenceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class AccessibilitySettingsController(
    getScreenShotsAllowedUseCase: GetScreenShotsAllowedUseCase,
    private val allowScreenshotsUseCase: AllowScreenshotsUseCase,
    private val saveZoomPreferenceUseCase: SaveZoomPreferenceUseCase,
    getZoomStateUseCase: GetZoomStateUseCase,
    private val scope: CoroutineScope
) {
    private val zoomFlow = getZoomStateUseCase.invoke().map { SettingStatesData.ZoomState(it) }

    val zoomState
        @Composable
        get() = zoomFlow.collectAsStateWithLifecycle(SettingStatesData.defaultZoomState)

    private val screenShotsAllowed =
        getScreenShotsAllowedUseCase.invoke()

    val screenShotsState
        @Composable
        get() = screenShotsAllowed.collectAsStateWithLifecycle(false)

    fun onAllowScreenshots(allow: Boolean) = scope.launch {
        allowScreenshotsUseCase.invoke(allow)
    }

    fun onEnableZoom() {
        scope.launch {
            saveZoomPreferenceUseCase.invoke(true)
        }
    }

    fun onDisableZoom() {
        scope.launch {
            saveZoomPreferenceUseCase.invoke(false)
        }
    }
}

@Composable
fun rememberAccessibilitySettingsController(): AccessibilitySettingsController {
    val getScreenShotsAllowedUseCase by rememberInstance<GetScreenShotsAllowedUseCase>()
    val allowScreenshotsUseCase by rememberInstance<AllowScreenshotsUseCase>()
    val getZoomStateUseCase by rememberInstance<GetZoomStateUseCase>()
    val saveZoomPreferenceUseCase by rememberInstance<SaveZoomPreferenceUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        AccessibilitySettingsController(
            getScreenShotsAllowedUseCase = getScreenShotsAllowedUseCase,
            allowScreenshotsUseCase = allowScreenshotsUseCase,
            saveZoomPreferenceUseCase = saveZoomPreferenceUseCase,
            getZoomStateUseCase = getZoomStateUseCase,
            scope = scope
        )
    }
}
