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

package de.gematik.ti.erp.app.demomode.di

import android.content.res.AssetManager
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.data.FunnyAppNameProvider
import de.gematik.ti.erp.app.demomode.navigation.triggers.DemoNavigationTriggerDataStore
import de.gematik.ti.erp.app.demomode.repository.consent.DemoConsentRepository
import de.gematik.ti.erp.app.demomode.repository.diga.DemoDigaInformationRepository
import de.gematik.ti.erp.app.demomode.repository.diga.DemoDigaRepository
import de.gematik.ti.erp.app.demomode.repository.eurezept.DemoEuRepository
import de.gematik.ti.erp.app.demomode.repository.orders.DemoCommunicationRepository
import de.gematik.ti.erp.app.demomode.repository.orders.DemoDownloadCommunicationResource
import de.gematik.ti.erp.app.demomode.repository.orders.DemoInternalMessagesRepository
import de.gematik.ti.erp.app.demomode.repository.pharmacy.DemoPharmacyRepository
import de.gematik.ti.erp.app.demomode.repository.pharmacy.DemoRedeemLocalDataSource
import de.gematik.ti.erp.app.demomode.repository.pharmacy.DemoShippingContactRepository
import de.gematik.ti.erp.app.demomode.repository.prescriptions.DemoTaskOperationsRepository
import de.gematik.ti.erp.app.demomode.repository.prescriptions.DemoTaskSyncRepository
import de.gematik.ti.erp.app.demomode.repository.profiles.DemoProfilesRepository
import de.gematik.ti.erp.app.demomode.repository.protocol.DemoAuditEventsRepository
import de.gematik.ti.erp.app.demomode.usecase.idp.DemoIdpUseCase
import de.gematik.ti.erp.app.diga.repository.DigaInformationRepository
import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.prescription.repository.TaskSyncRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val demoModeModule = DI.Module("demoModeModule") {
    bindProvider { DemoDownloadCommunicationResource(instance()) }
    bindSingleton { FunnyAppNameProvider() }
    // only data source for demo mode
    bindSingleton { DemoModeDataSource(instance()) }
}

fun DI.MainBuilder.demoModeOverrides() {
    bindProvider<ProfileRepository>(overrides = true) { DemoProfilesRepository(instance()) }
    bindSingleton<ConsentRepository>(overrides = true) { DemoConsentRepository() }
    bindProvider<TaskOperationsRepository>(overrides = true) { DemoTaskOperationsRepository(instance()) }
    bindProvider<DigaRepository>(overrides = true) { DemoDigaRepository(instance()) }
    bindProvider<DigaInformationRepository>(overrides = true) { DemoDigaInformationRepository() }
    bindProvider<EuRepository>(overrides = true) { DemoEuRepository(instance()) }
    bindProvider<AuditEventsRepository>(overrides = true) { DemoAuditEventsRepository(instance()) }
    bindProvider<RedeemLocalDataSource>(overrides = true) { DemoRedeemLocalDataSource(instance()) }
    bindProvider<CommunicationRepository>(overrides = true) { DemoCommunicationRepository(instance(), instance()) }
    bindProvider<InternalMessagesRepository>(overrides = true) { DemoInternalMessagesRepository(instance()) }
    bindProvider<TaskSyncRepository>(overrides = true) { DemoTaskSyncRepository() }
    bindProvider<IdpUseCase>(overrides = true) { DemoIdpUseCase(instance()) }
    bindProvider<PharmacyRepository>(overrides = true) {
        val assetManager = instance<AssetManager>()
        DemoPharmacyRepository(instance(), instance(), assetManager)
    }
    bindProvider<ShippingContactRepository>(overrides = true) { DemoShippingContactRepository() }
    bindSingleton<NavigationTriggerDataStore>(overrides = true) { DemoNavigationTriggerDataStore() }
}
