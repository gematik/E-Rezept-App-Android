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

package de.gematik.ti.erp.app.prescription.di

import de.gematik.ti.erp.app.prescription.repository.PrescriptionLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.DesktopPrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionMapper
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindInstance
import org.kodein.di.bindings.Scope
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

fun prescriptionModule(scope: Scope<Any?>) = DI.Module("Prescription Module") {
    bind { scoped(scope).singleton { PrescriptionRemoteDataSource(instance()) } }
    bind { scoped(scope).singleton { PrescriptionLocalDataSource() } }
    bind { scoped(scope).singleton { DesktopPrescriptionRepository(instance(), instance(), instance(), instance()) } }
    bind { scoped(scope).singleton { PrescriptionUseCase(instance(), instance()) } }
    bindInstance { PrescriptionMapper() }
}
