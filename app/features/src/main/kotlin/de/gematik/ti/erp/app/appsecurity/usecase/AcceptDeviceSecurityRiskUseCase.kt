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

package de.gematik.ti.erp.app.appsecurity.usecase

import de.gematik.ti.erp.app.appsecurity.AppSecuritySession
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcceptDeviceSecurityRiskUseCase(
    private val repository: SettingsRepository,
    private val session: AppSecuritySession,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(acceptRiskEnum: AcceptRiskEnum) {
        withContext(dispatcher) {
            when (acceptRiskEnum) {
                AcceptRiskEnum.AcceptForSession -> session.acceptDeviceSecurityForSession()
                AcceptRiskEnum.AcceptPermanent -> repository.acceptInsecureDevice()
            }
        }
    }
}
