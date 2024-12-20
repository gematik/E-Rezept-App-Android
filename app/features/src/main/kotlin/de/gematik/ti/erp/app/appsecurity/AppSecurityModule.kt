/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.appsecurity

import de.gematik.ti.erp.app.appsecurity.usecase.AcceptDeviceSecurityRiskUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.AcceptIntegrityRiskUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.GetDeviceSecurityUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.IntegrityUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.IsIntegrityRiskAcceptedUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val appSecurityModule = DI.Module("appSecurityModule") {
    bindSingleton { AppSecuritySession() }
    bindProvider { IntegrityUseCase(instance()) }
    bindProvider { GetDeviceSecurityUseCase(instance(), instance(), instance()) }
    bindProvider { AcceptIntegrityRiskUseCase(instance(), instance()) }
    bindProvider { AcceptDeviceSecurityRiskUseCase(instance(), instance()) }
    bindProvider { IsIntegrityRiskAcceptedUseCase(instance(), instance()) }
}
