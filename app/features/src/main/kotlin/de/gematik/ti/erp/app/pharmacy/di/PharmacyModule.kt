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

package de.gematik.ti.erp.app.pharmacy.di

import de.gematik.ti.erp.app.pharmacy.presentation.DefaultPharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.repository.DefaultPharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.DefaultShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.PreviewMapCoordinatesRepository
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.repository.datasource.DefaultFavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.DefaultOftenUsePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.FavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.OftenUsedPharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.PreviewMapCoordinatesDataSource
import de.gematik.ti.erp.app.pharmacy.ui.components.GooglePharmacyMap
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyMap
import de.gematik.ti.erp.app.pharmacy.usecase.ChangePharmacyFavoriteStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.DeleteOverviewPharmacyUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetLocationUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetOverviewPharmaciesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetPreviewMapCoordinatesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.IsPharmacyFavoriteUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyDirectRedeemUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyMapsUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.SaveShippingContactUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.SetPreviewMapCoordinatesUseCase
import de.gematik.ti.erp.app.redeem.repository.datasource.DefaultRedeemLocalDataSource
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val pharmacyModule = DI.Module("pharmacyModule", allowSilentOverride = true) {
    bindSingleton<PharmacyGraphController> {
        DefaultPharmacyGraphController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider { GetShippingContactValidationUseCase() }
    bindProvider { PharmacyDirectRedeemUseCase(instance()) } // TODO: Only used in debug process on testing
    bindProvider { PharmacyMapsUseCase(instance(), instance(), instance()) }
    bindProvider { PharmacySearchUseCase(instance(), instance(), instance(), instance(), instance()) }
    bindProvider { PharmacyOverviewUseCase(instance(), instance()) }
    bindProvider { GetOverviewPharmaciesUseCase(instance()) }
    bindProvider { GetOrderStateUseCase(instance(), instance(), instance()) }
    bindProvider { GetLocationUseCase(instance()) }
    bindProvider { GetPreviewMapCoordinatesUseCase(instance()) }
    bindProvider { SetPreviewMapCoordinatesUseCase(instance()) }
    bindProvider { IsPharmacyFavoriteUseCase(instance()) }
    bindProvider { ChangePharmacyFavoriteStateUseCase(instance()) }
    bindProvider { SaveShippingContactUseCase(instance()) }
    bindProvider { GetPharmacyByTelematikIdUseCase(instance()) }
    bindProvider { DeleteOverviewPharmacyUseCase(instance()) }
    bindProvider<PharmacyMap> { GooglePharmacyMap() }
}

val pharmacyRepositoryModule = DI.Module("pharmacyRepositoryModule", allowSilentOverride = true) {
    bindProvider { PharmacyRemoteDataSource(instance(), instance()) }
    bindProvider<RedeemLocalDataSource> { DefaultRedeemLocalDataSource(instance()) }
    bindProvider<FavouritePharmacyLocalDataSource> { DefaultFavouritePharmacyLocalDataSource(instance()) }
    bindProvider<OftenUsedPharmacyLocalDataSource> { DefaultOftenUsePharmacyLocalDataSource(instance()) }
    bindSingleton { PreviewMapCoordinatesDataSource() }
    bindProvider { PreviewMapCoordinatesRepository(instance()) }
    bindProvider<PharmacyRepository> { DefaultPharmacyRepository(instance(), instance(), instance(), instance()) }
    bindProvider<ShippingContactRepository> { DefaultShippingContactRepository(instance(), instance()) }
}
