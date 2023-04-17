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

package de.gematik.ti.erp.app.pkv

import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.usecase.ConsentUseCase
import de.gematik.ti.erp.app.invoice.repository.InvoiceLocalDataSource
import de.gematik.ti.erp.app.invoice.repository.InvoiceRemoteDataSource
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.invoice.usecase.InvoiceUseCase

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val pkvModule = DI.Module("pkvModule") {
    bindProvider { ConsentUseCase(instance()) }
    bindProvider { ConsentRepository(instance(), instance()) }
    bindProvider { ConsentRemoteDataSource(instance()) }

    bindProvider { InvoiceUseCase(instance(), instance()) }
    bindProvider { InvoiceRepository(instance(), instance(), instance()) }
    bindProvider { InvoiceRemoteDataSource(instance()) }
    bindProvider { InvoiceLocalDataSource(instance()) }
}