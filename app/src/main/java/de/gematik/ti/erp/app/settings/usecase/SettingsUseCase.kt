/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.db.entities.HealthCardUser
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.settings.repository.HealthCardUserRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val idpRepository: IdpRepository,
    private val userRepository: HealthCardUserRepository,
    private val demoUseCase: DemoUseCase
) {
    val settings =
        settingsRepository.settings().map {
            if (it.isEmpty()) {
                Settings(
                    authenticationMethod = SettingsAuthenticationMethod.Unspecified
                )
            } else {
                it.first()
            }
        }

    suspend fun saveSettings(settings: Settings) {
        settingsRepository.saveSettings(settings)
    }

    @OptIn(FlowPreview::class)
    fun healthCardUser(): Flow<List<HealthCardUser>> =
        demoUseCase.demoModeActive.flatMapConcat {
            if (it) {
                flowOf(
                    listOf(
                        HealthCardUser(
                            name = "Anna Vetter",
                            cardAccessNumber = "123456"
                        )
                    )
                )
            } else {
                userRepository.healthCardUser()
            }
        }

    suspend fun saveHealthCardUser(user: HealthCardUser) {
        // TODO handle demo mode
        userRepository.saveHealthCardUser(user)
    }

    suspend fun clearIDPDataAndCAN() {
        idpRepository.invalidateWithUserCredentials()
    }

    suspend fun logout() {
        idpRepository.invalidateWithUserCredentials()
    }
}
