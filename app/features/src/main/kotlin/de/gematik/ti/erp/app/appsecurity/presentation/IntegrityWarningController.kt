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
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.appsecurity.usecase.AcceptIntegrityRiskUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.AcceptRiskEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class IntegrityWarningController(
    private val acceptIntegrityRiskUseCase: AcceptIntegrityRiskUseCase,
    private val scope: CoroutineScope
) {
    fun acceptRiskForSession() {
        scope.launch {
            acceptIntegrityRiskUseCase.invoke(acceptRiskEnum = AcceptRiskEnum.AcceptForSession)
        }
    }

    fun acceptRiskPermanent() {
        scope.launch {
            acceptIntegrityRiskUseCase.invoke(acceptRiskEnum = AcceptRiskEnum.AcceptPermanent)
        }
    }
}

@Composable
fun rememberIntegrityWarningController(): IntegrityWarningController {
    val acceptIntegrityRiskUseCase: AcceptIntegrityRiskUseCase by rememberInstance()
    val scope = rememberCoroutineScope()

    return remember {
        IntegrityWarningController(
            acceptIntegrityRiskUseCase = acceptIntegrityRiskUseCase,
            scope = scope
        )
    }
}
