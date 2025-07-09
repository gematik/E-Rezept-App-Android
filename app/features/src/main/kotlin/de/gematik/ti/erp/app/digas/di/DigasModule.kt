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

package de.gematik.ti.erp.app.digas.di

import de.gematik.ti.erp.app.base.usecase.MarkNavigationTriggerConsumedUseCase
import de.gematik.ti.erp.app.base.usecase.ObserveNavigationTriggerUseCase
import de.gematik.ti.erp.app.base.usecase.TriggerNavigationUseCase
import de.gematik.ti.erp.app.diga.local.DigaLocalDataSource
import de.gematik.ti.erp.app.diga.repository.DefaultDigaRepository
import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.digas.domain.usecase.FetchInsuranceListUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.GetDigaByTaskIdUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.GetIknrUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateArchivedStatusUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateDigaIsNewUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateDigaStatusUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateIknrUseCase
import de.gematik.ti.erp.app.digas.presentation.DefaultDigasGraphController
import de.gematik.ti.erp.app.digas.presentation.DigasGraphController
import de.gematik.ti.erp.app.fhir.communication.DigaDispenseRequestBuilder
import de.gematik.ti.erp.app.insurance.usecase.FetchInsuranceProviderUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemDigaUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val digaModule = DI.Module("digaModule", allowSilentOverride = true) {
    bindProvider { DigaDispenseRequestBuilder() }

    bindProvider {
        FetchInsuranceProviderUseCase(
            instance(),
            instance(),
            instance()
        )
    }

    bindProvider {
        RedeemDigaUseCase(
            instance(),
            instance(),
            instance()
        )
    }

    bindProvider { GetDigaByTaskIdUseCase(instance(), instance()) }
    bindProvider { UpdateDigaStatusUseCase(instance(), instance()) }
    bindProvider { UpdateDigaIsNewUseCase(instance(), instance()) }
    bindProvider { MarkNavigationTriggerConsumedUseCase(instance()) }
    bindProvider { ObserveNavigationTriggerUseCase(instance()) }
    bindProvider { TriggerNavigationUseCase(instance()) }
    bindProvider { UpdateArchivedStatusUseCase(instance()) }
    bindProvider { UpdateIknrUseCase(instance()) }
    bindProvider { GetIknrUseCase(instance()) }
    bindProvider { FetchInsuranceListUseCase(instance(), instance()) }

    bindSingleton<DigasGraphController> {
        DefaultDigasGraphController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}

val digaRepositoryModule = DI.Module("digaRepositoryModule", allowSilentOverride = true) {
    bindProvider { DigaLocalDataSource(instance()) }
    bindProvider<DigaRepository> { DefaultDigaRepository(instance()) }
}
