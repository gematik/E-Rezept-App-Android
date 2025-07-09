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

package de.gematik.ti.erp.app.pkv

import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.SaveGrantConsentDrawerShownUseCase
import de.gematik.ti.erp.app.consent.usecase.ShowGrantConsentDrawerUseCase
import de.gematik.ti.erp.app.invoice.repository.DefaultInvoiceRepository
import de.gematik.ti.erp.app.invoice.repository.InvoiceLocalDataSource
import de.gematik.ti.erp.app.invoice.repository.InvoiceRemoteDataSource
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.invoice.usecase.DeleteAllLocalInvoices
import de.gematik.ti.erp.app.invoice.usecase.DeleteInvoiceUseCase
import de.gematik.ti.erp.app.invoice.usecase.DownloadInvoicesUseCase
import de.gematik.ti.erp.app.invoice.usecase.GetInvoiceByTaskIdUseCase
import de.gematik.ti.erp.app.invoice.usecase.GetInvoicesByProfileUseCase
import de.gematik.ti.erp.app.invoice.usecase.SaveInvoiceUseCase
import de.gematik.ti.erp.app.pkv.presentation.ConsentController
import de.gematik.ti.erp.app.pkv.usecase.ShareInvoiceUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val pkvModule = DI.Module("pkvModule") {
    bindProvider { GetConsentUseCase(instance()) }
    bindProvider { GrantConsentUseCase(instance()) }
    bindProvider { RevokeConsentUseCase(instance()) }
    bindProvider { ShowGrantConsentDrawerUseCase(instance(), instance()) }
    bindProvider { SaveGrantConsentDrawerShownUseCase(instance()) }
    bindProvider { DownloadInvoicesUseCase(instance(), instance()) }
    bindProvider { DeleteInvoiceUseCase(instance(), instance()) }
    bindProvider { DeleteAllLocalInvoices(instance()) }
    bindProvider { GetInvoiceByTaskIdUseCase(instance()) }
    bindProvider { GetInvoicesByProfileUseCase(instance()) }
    bindProvider { SaveInvoiceUseCase(instance()) }
    bindProvider { ShareInvoiceUseCase(instance()) }
    bindProvider<InvoiceRepository> { DefaultInvoiceRepository(instance(), instance(), instance()) }
    bindProvider { InvoiceRemoteDataSource(instance()) }
    bindProvider { InvoiceLocalDataSource(instance()) }
    bindSingleton { ConsentController(instance(), instance(), instance(), instance()) }
}

val consentRepositoryModule = DI.Module("consentRepositoryModule", allowSilentOverride = true) {
    bindProvider<ConsentRepository> { DefaultConsentRepository(instance(), instance()) }
    bindProvider { ConsentLocalDataSource(instance()) }
    bindProvider { ConsentRemoteDataSource(instance()) }
}
