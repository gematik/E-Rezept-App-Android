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

package de.gematik.ti.erp.app.demomode.di

import de.gematik.ti.erp.app.authentication.mapper.PromptAuthenticationProvider
import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.mapper.authentication.DemoPromptAuthenticationProvider
import de.gematik.ti.erp.app.demomode.repository.orders.DemoCommunicationRepository
import de.gematik.ti.erp.app.demomode.repository.orders.DemoDownloadCommunicationResource
import de.gematik.ti.erp.app.demomode.repository.pharmacy.DemoPharmacyLocalDataSource
import de.gematik.ti.erp.app.demomode.repository.prescriptions.DemoPrescriptionsRepository
import de.gematik.ti.erp.app.demomode.repository.prescriptions.DemoTaskRepository
import de.gematik.ti.erp.app.demomode.repository.profiles.DemoProfilesRepository
import de.gematik.ti.erp.app.demomode.repository.protocol.DemoAuditEventsRepository
import de.gematik.ti.erp.app.demomode.usecase.idp.DemoIdpUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val demoModeModule = DI.Module("demoModeModule") {
    bindProvider { DemoDownloadCommunicationResource(instance()) }
    // only data source for demo mode
    bindSingleton { DemoModeDataSource() }
}

fun DI.MainBuilder.demoModeOverrides() {
    bindProvider<ProfileRepository>(overrides = true) { DemoProfilesRepository(instance()) }
    bindProvider<PrescriptionRepository>(overrides = true) { DemoPrescriptionsRepository(instance()) }
    bindProvider<AuditEventsRepository>(overrides = true) { DemoAuditEventsRepository(instance()) }
    bindProvider<PharmacyLocalDataSource>(overrides = true) { DemoPharmacyLocalDataSource(instance()) }
    bindProvider<CommunicationRepository>(overrides = true) { DemoCommunicationRepository(instance(), instance()) }
    bindProvider<TaskRepository>(overrides = true) { DemoTaskRepository() }
    bindProvider<IdpUseCase>(overrides = true) { DemoIdpUseCase(instance()) }
    // these two are added for future functions
    bindProvider<PromptAuthenticationProvider>(overrides = true) { DemoPromptAuthenticationProvider() }
}
