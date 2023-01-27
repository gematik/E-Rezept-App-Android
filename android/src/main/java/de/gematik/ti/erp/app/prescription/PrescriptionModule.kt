/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.prescription

import de.gematik.ti.erp.app.prescription.repository.LocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.RemoteDataSource
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeProcessor
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeScanner
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.RefreshPrescriptionUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val prescriptionModule = DI.Module("prescriptionModule") {
    bindProvider { TwoDCodeProcessor() }
    bindProvider { TwoDCodeScanner(instance()) }
    bindProvider { TwoDCodeValidator() }
    bindSingleton { LocalDataSource(instance()) }
    bindSingleton { PrescriptionRepository(instance(), instance(), instance()) }
    bindSingleton { RemoteDataSource(instance()) }
    bindSingleton { PrescriptionUseCase(instance(), instance(), instance()) }
    bindSingleton { RefreshPrescriptionUseCase(instance(), instance(), instance(), instance()) }
}
