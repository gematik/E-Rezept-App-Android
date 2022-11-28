/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy

import de.gematik.ti.erp.app.pharmacy.repository.PharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyDirectRedeemUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyMapsUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val pharmacyModule = DI.Module("pharmacyModule") {
    bindProvider { PharmacyRemoteDataSource(instance(), instance()) }
    bindProvider { PharmacyLocalDataSource(instance()) }
    bindProvider { PharmacyRepository(instance(), instance(), instance()) }
    bindProvider { ShippingContactRepository(instance(), instance()) }
    bindProvider { PharmacyDirectRedeemUseCase(instance()) }
    bindProvider { PharmacyMapsUseCase(instance(), instance(), instance()) }
    bindProvider { PharmacySearchUseCase(instance(), instance(), instance(), instance(), instance()) }
    bindProvider { PharmacyOverviewUseCase(instance(), instance()) }
}
