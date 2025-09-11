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

package de.gematik.ti.erp.app.di.pharmacy

import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.PreviewMapCoordinatesRepository
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.repository.datasource.PreviewMapCoordinatesDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.DefaultFavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.DefaultOftenUsePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.FavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.OftenUsedPharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.FhirVzdRemoteDataSource
import de.gematik.ti.erp.app.redeem.repository.datasource.DefaultRedeemLocalDataSource
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import de.gematik.ti.erp.app.repository.MockShippingContactRepository
import de.gematik.ti.erp.app.repository.pharmacy.MockPharmacyRepository
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val mockPharmacyRepositoryModule = DI.Module("mockPharmacyModule") {
    bindProvider { FhirVzdRemoteDataSource(instance()) }
    bindProvider<RedeemLocalDataSource> { DefaultRedeemLocalDataSource(instance()) }
    bindProvider<OftenUsedPharmacyLocalDataSource> { DefaultOftenUsePharmacyLocalDataSource(instance()) }
    bindProvider<FavouritePharmacyLocalDataSource> { DefaultFavouritePharmacyLocalDataSource(instance()) }
    bindSingleton { PreviewMapCoordinatesDataSource() }
    bindProvider { PreviewMapCoordinatesRepository(instance()) }
    bindProvider<PharmacyRepository> { MockPharmacyRepository(instance(), instance(), instance()) }
    bindProvider<ShippingContactRepository> { MockShippingContactRepository() }
}
