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

import android.app.KeyguardManager
import android.content.Context
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appsecurity.AppSecuritySession
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetDeviceSecurityUseCase(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val appSecuritySession: AppSecuritySession,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Requirement(
        "O.Plat_1#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Check for insecure Devices."
    )
    operator fun invoke(): Flow<Boolean> =
        settingsRepository.general
            .map {
                val isDeviceSecure = (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                    .isDeviceSecure

                Napier.d(
                    tag = "AppSecurity check",
                    message = "Device security check: isDeviceSecure $isDeviceSecure"
                )

                val riskAccepted =
                    it.userHasAcceptedInsecureDevice || appSecuritySession.isDeviceSecurityAcceptedForSession()

                Napier.d(
                    tag = "AppSecurity check",
                    message = "Device security check: riskAccepted $riskAccepted"
                )

                val result = when {
                    !isDeviceSecure && !riskAccepted -> false
                    else -> true
                }
                Napier.d(
                    tag = "AppSecurity check",
                    message = "Device security check: result $result"
                )
                result
            }.flowOn(dispatcher)
}
