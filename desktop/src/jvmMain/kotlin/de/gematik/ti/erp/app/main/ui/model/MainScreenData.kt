package de.gematik.ti.erp.app.main.ui.model

object MainScreenData {
    data class State(
        val zoomed: Boolean,
        val zoom: Float,
        val darkMode: Boolean
    )
}
