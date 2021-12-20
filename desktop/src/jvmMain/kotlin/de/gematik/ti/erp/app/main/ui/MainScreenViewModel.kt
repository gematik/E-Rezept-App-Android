package de.gematik.ti.erp.app.main.ui

import de.gematik.ti.erp.app.main.ui.model.MainScreenData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class MainScreenViewModel(
    private val zoomRange: ClosedFloatingPointRange<Float>,
    private val defaultZoom: Float,
    private val defaultDarkMode: Boolean
) {
    val defaultState = MainScreenData.State(zoomed = false, zoom = defaultZoom, darkMode = defaultDarkMode)
    private val state = MutableStateFlow(defaultState)

    fun screenState(): Flow<MainScreenData.State> = state

    private val _logout = MutableSharedFlow<Boolean>()
    val logout: Flow<Boolean>
        get() = _logout

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
        state.value = defaultState
        _logout.emit(true)
    }
}
