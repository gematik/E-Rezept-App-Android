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

package de.gematik.ti.erp.app.main.ui

import de.gematik.ti.erp.app.main.ui.model.MainScreenData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.system.exitProcess

class MainScreenViewModel(
    private val zoomRange: ClosedFloatingPointRange<Float>,
    private val defaultZoom: Float,
    private val defaultDarkMode: Boolean
) {
    val defaultState = MainScreenData.State(zoomed = false, zoom = defaultZoom, darkMode = defaultDarkMode)
    private val state = MutableStateFlow(defaultState)

    fun screenState(): Flow<MainScreenData.State> = state

    fun onZoom(step: Float) {
        state.value = state.value.copy(zoomed = true, zoom = (state.value.zoom + step).coerceIn(zoomRange))
    }

    fun onZoomIn() {
        state.value = state.value.copy(zoomed = true, zoom = zoomRange.endInclusive)
    }

    fun onZoomOut() {
        state.value = state.value.copy(zoomed = false, zoom = defaultZoom)
    }

    fun onEnableDarkMode() {
        state.value = state.value.copy(darkMode = true)
    }

    fun onDisableDarkMode() {
        state.value = state.value.copy(darkMode = false)
    }

    suspend fun onLogout() {
        exitProcess(0)
    }
}
