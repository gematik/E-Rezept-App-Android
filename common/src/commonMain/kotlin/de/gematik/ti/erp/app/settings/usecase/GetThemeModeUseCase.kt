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

package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.database.settings.ThemePreferencesDataStore
import de.gematik.ti.erp.app.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving the current theme mode setting.
 *
 * Returns a flow that emits the current theme mode (SYSTEM, LIGHT, or DARK)
 * from SharedPreferences.
 *
 * ## Usage:
 * ```kotlin
 * val getThemeModeUseCase = GetThemeModeUseCase(themeDataStore)
 * getThemeModeUseCase().collect { themeMode ->
 *     // Apply theme
 * }
 * ```
 *
 * @param themePreferencesDataStore DataStore for theme preferences
 */
class GetThemeModeUseCase(
    private val themePreferencesDataStore: ThemePreferencesDataStore
) {
    operator fun invoke(): Flow<ThemeMode> = themePreferencesDataStore.themeMode
}
