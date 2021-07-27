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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class PollingUseCaseDelegate @Inject constructor(
    private val demoDelegate: PollingUseCaseDemo,
    private val productionDelegate: PollingUseCaseProduction,
    private val demoUseCase: DemoUseCase
) : PollingUseCase {
    private val delegate: PollingUseCase
        get() = if (demoUseCase.isDemoModeActive) demoDelegate else productionDelegate

    override val doRefresh: Flow<Unit> =
        demoUseCase.demoModeActive.flatMapLatest {
            delegate.doRefresh
        }

    override suspend fun refreshNow() = delegate.refreshNow()
}
