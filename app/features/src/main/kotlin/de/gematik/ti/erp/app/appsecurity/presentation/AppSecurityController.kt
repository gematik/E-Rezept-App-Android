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

package de.gematik.ti.erp.app.appsecurity.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appsecurity.usecase.GetDeviceSecurityUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.IntegrityUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.IsIntegrityRiskAcceptedUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.kodein.di.compose.rememberInstance

class AppSecurityController(
    private val integrityUseCase: IntegrityUseCase,
    private val isIntegrityRiskAcceptedUseCase: IsIntegrityRiskAcceptedUseCase,
    private val deviceSecurityUseCase: GetDeviceSecurityUseCase
) {
    @Requirement(
        "O.Arch_6#2",
        "O.Resi_2#2",
        "O.Resi_3#2",
        "O.Resi_4#2",
        "O.Resi_5#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Check device integrity."
    )
    suspend fun checkIntegrityRisk() =
        combine(
            integrityUseCase.runIntegrityAttestation(),
            isIntegrityRiskAcceptedUseCase.invoke()
        ) { isAttested, isRiskAccepted ->
            Napier.d(
                tag = "AppSecurity check",
                message = "integrity check: isAttested $isAttested"
            )
            Napier.d(
                tag = "AppSecurity check",
                message = "integrity check: isRiskAccepted $isRiskAccepted"
            )
            return@combine when {
                !isAttested && !isRiskAccepted -> false
                else -> true
            }
        }.first()

    @Requirement(
        "O.Plat_1#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Check for insecure Devices on first screen when the app is started."
    )
    suspend fun checkDeviceSecurityRisk(): Boolean = deviceSecurityUseCase.invoke().first()
}

@Composable
fun rememberAppSecurityController(): AppSecurityController {
    val integrityUseCase: IntegrityUseCase by rememberInstance()
    val isIntegrityRiskAcceptedUseCase: IsIntegrityRiskAcceptedUseCase by rememberInstance()
    val deviceSecurityUseCase: GetDeviceSecurityUseCase by rememberInstance()

    return remember {
        AppSecurityController(
            integrityUseCase = integrityUseCase,
            isIntegrityRiskAcceptedUseCase = isIntegrityRiskAcceptedUseCase,
            deviceSecurityUseCase = deviceSecurityUseCase
        )
    }
}
