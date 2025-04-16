/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.redeem

import de.gematik.ti.erp.app.redeem.presentation.DefaultOnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.usecase.GetDMCodesForLocalRedeemUseCase
import de.gematik.ti.erp.app.redeem.usecase.GetRedeemableTasksForDmCodesUseCase
import de.gematik.ti.erp.app.redeem.usecase.HasRedeemableTasksUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnDirectUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnLoggedInUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemScannedTasksUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val redeemModule = DI.Module("redeemModule") {
    bindProvider { HasRedeemableTasksUseCase(instance()) }
    bindProvider { GetDMCodesForLocalRedeemUseCase() }
    bindProvider { RedeemScannedTasksUseCase(instance()) }
    bindProvider { GetRedeemableTasksForDmCodesUseCase(instance()) }
    bindProvider { RedeemPrescriptionsOnLoggedInUseCase(instance(), instance()) }
    bindProvider { RedeemPrescriptionsOnDirectUseCase(instance(), instance()) }
    bindSingleton<OnlineRedeemGraphController> {
        DefaultOnlineRedeemGraphController(
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}
