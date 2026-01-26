/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.settings.model.ThemeMode
import de.gematik.ti.erp.app.settings.usecase.GetThemeModeUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveThemeModeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.rememberInstance

/**
 * Controller for managing theme settings.
 *
 * Provides access to the current theme mode and allows changing it.
 */
class SettingsThemeScreenController(
    getThemeModeUseCase: GetThemeModeUseCase,
    private val saveThemeModeUseCase: SaveThemeModeUseCase
) : Controller() {

    val selectedTheme: StateFlow<ThemeMode> = getThemeModeUseCase()
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), ThemeMode.SYSTEM)

    /**
     * Updates the app theme mode.
     *
     * @param themeMode The new theme mode to apply
     */
    fun onThemeSelected(themeMode: ThemeMode) {
        saveThemeModeUseCase(themeMode)
    }
}

@Composable
fun rememberSettingsThemeScreenController(): SettingsThemeScreenController {
    val getThemeModeUseCase by rememberInstance<GetThemeModeUseCase>()
    val saveThemeModeUseCase by rememberInstance<SaveThemeModeUseCase>()
    return remember {
        SettingsThemeScreenController(
            getThemeModeUseCase = getThemeModeUseCase,
            saveThemeModeUseCase = saveThemeModeUseCase
        )
    }
}
