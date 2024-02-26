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
import de.gematik.ti.erp.app.settings.usecase.GetAuthenticationModeUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveDeviceSecurityUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class DeviceSecuritySettingsController(
    private val saveDeviceSecurityUseCase: SaveDeviceSecurityUseCase,
    getAuthenticationModeUseCase: GetAuthenticationModeUseCase,
    private val scope: CoroutineScope
) {
    private val authenticationModeFlow = getAuthenticationModeUseCase.invoke().map {
        SettingStatesData.AuthenticationModeState(
            it
        )
    }

    val authenticationModeState
        @Composable
        get() = authenticationModeFlow.collectAsStateWithLifecycle(SettingStatesData.defaultAuthenticationState)

    fun onSelectDeviceSecurityAuthenticationMode() {
        scope.launch {
            saveDeviceSecurityUseCase.invoke()
        }
    }
}

@Composable
fun rememberDeviceSecuritySettingsController(): DeviceSecuritySettingsController {
    val saveDeviceSecurityUseCase by rememberInstance<SaveDeviceSecurityUseCase>()
    val getAuthenticationModeUseCase by rememberInstance<GetAuthenticationModeUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        DeviceSecuritySettingsController(
            saveDeviceSecurityUseCase = saveDeviceSecurityUseCase,
            getAuthenticationModeUseCase = getAuthenticationModeUseCase,
            scope = scope
        )
    }
}
