package de.gematik.ti.erp.app.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Stable
class Navigation(startDestination: Destination) {
    private val observableBackStack = mutableStateListOf(startDestination)

    val backStackEntries: List<Destination>
        get() = observableBackStack

    val currentBackStackEntry: Destination?
        get() = observableBackStack.lastOrNull()

    fun back() {
        if (observableBackStack.isNotEmpty()) {
            observableBackStack.removeLast()
        }
    }

    fun navigate(destination: Destination) {
        observableBackStack.add(destination)
    }

    fun navigate(destination: Destination, clearBackStack: Boolean) {
        if (clearBackStack) {
            observableBackStack.clear()
        }
        observableBackStack.add(destination)
    }
}

interface Destination

@Composable
fun rememberNavigation(startDestination: Destination): Navigation {
    return remember { Navigation(startDestination) }
}
