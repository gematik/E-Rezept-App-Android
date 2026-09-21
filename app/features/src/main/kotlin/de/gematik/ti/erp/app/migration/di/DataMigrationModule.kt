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

package de.gematik.ti.erp.app.migration.di

import de.gematik.ti.erp.app.database.di.ModuleTags
import de.gematik.ti.erp.app.migration.presentation.DataMigrationViewModel
import de.gematik.ti.erp.app.migration.usecase.ClearMigratedPharmacyAndShippingInfoUseCase
import de.gematik.ti.erp.app.migration.usecase.CompleteMigrationUseCase
import de.gematik.ti.erp.app.migration.usecase.StartMigrationUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val dataMigrationModule = DI.Module("dataMigrationModule") {
    bindSingleton {
        DataMigrationViewModel(
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider { StartMigrationUseCase(instance()) }
    bindProvider { CompleteMigrationUseCase(instance(tag = ModuleTags.SETTINGS_V2), instance(), instance()) }
    bindProvider {
        ClearMigratedPharmacyAndShippingInfoUseCase(
            instance(tag = ModuleTags.PHARMACY_V2),
            instance(tag = ModuleTags.SHIPPING_INFO_V2)
        )
    }
}
