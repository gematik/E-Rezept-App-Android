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

package de.gematik.ti.erp.app.pkv

import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.SaveGrantConsentDrawerShownUseCase
import de.gematik.ti.erp.app.consent.usecase.ShowGrantConsentUseCase
import de.gematik.ti.erp.app.invoice.repository.InvoiceLocalDataSource
import de.gematik.ti.erp.app.invoice.repository.InvoiceRemoteDataSource
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.invoice.usecase.GetInvoiceByTaskIdUseCase
import de.gematik.ti.erp.app.invoice.usecase.InvoiceUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val pkvModule = DI.Module("pkvModule") {
    bindProvider { GetConsentUseCase(instance()) }
    bindProvider { GrantConsentUseCase(instance()) }
    bindProvider { RevokeConsentUseCase(instance()) }
    bindProvider { ShowGrantConsentUseCase(instance(), instance()) }
    bindProvider { SaveGrantConsentDrawerShownUseCase(instance()) }

    bindProvider { DefaultConsentRepository(instance(), instance()) }
    bindProvider { ConsentLocalDataSource(instance()) }
    bindProvider { ConsentRemoteDataSource(instance()) }

    bindProvider { InvoiceUseCase(instance(), instance()) }
    bindProvider { InvoiceRepository(instance(), instance(), instance()) }
    bindProvider { InvoiceRemoteDataSource(instance()) }
    bindProvider { InvoiceLocalDataSource(instance()) }
    bindProvider { GetInvoiceByTaskIdUseCase(instance()) }
}

val consentRepositoryModule = DI.Module("consentRepositoryModule", allowSilentOverride = true) {
    bindProvider<ConsentRepository> { DefaultConsentRepository(instance(), instance()) }
}
