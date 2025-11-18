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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.analytics.di.cardCommunicationAnalyticsModule
import de.gematik.ti.erp.app.appsecurity.appSecurityModule
import de.gematik.ti.erp.app.appupdate.di.appUpdateModule
import de.gematik.ti.erp.app.cardunlock.di.cardUnlockModule
import de.gematik.ti.erp.app.cardwall.cardWallModule
import de.gematik.ti.erp.app.database.di.databaseModule
import de.gematik.ti.erp.app.debugsettings.di.debugSettingsModule
import de.gematik.ti.erp.app.di.datasource.mockDataSourceModule
import de.gematik.ti.erp.app.di.pharmacy.mockPharmacyRepositoryModule
import de.gematik.ti.erp.app.di.prescription.mockPrescriptionRepositoryModule
import de.gematik.ti.erp.app.di.prescription.mockTaskRepositoryModule
import de.gematik.ti.erp.app.di.profile.mockProfileRepositoryModule
import de.gematik.ti.erp.app.di.settings.mockSettingsRepositoryModule
import de.gematik.ti.erp.app.digas.di.digaModule
import de.gematik.ti.erp.app.digas.di.digaRepositoryModule
import de.gematik.ti.erp.app.eurezept.di.euModule
import de.gematik.ti.erp.app.idp.idpModule
import de.gematik.ti.erp.app.idp.idpUseCaseModule
import de.gematik.ti.erp.app.logger.di.loggerModule
import de.gematik.ti.erp.app.messages.di.messageRepositoryModule
import de.gematik.ti.erp.app.messages.di.messagesModule
import de.gematik.ti.erp.app.mlkit.mlKitModule
import de.gematik.ti.erp.app.mock.BuildConfig
import de.gematik.ti.erp.app.onboarding.di.onboardingModule
import de.gematik.ti.erp.app.orderhealthcard.di.orderHealthCardModule
import de.gematik.ti.erp.app.pharmacy.di.pharmacyModule
import de.gematik.ti.erp.app.pkv.consentRepositoryModule
import de.gematik.ti.erp.app.pkv.pkvModule
import de.gematik.ti.erp.app.prescription.prescriptionModule
import de.gematik.ti.erp.app.prescription.prescriptionRepositoryModule
import de.gematik.ti.erp.app.prescription.taskModule
import de.gematik.ti.erp.app.profiles.profilesModule
import de.gematik.ti.erp.app.protocol.auditEventsModule
import de.gematik.ti.erp.app.protocol.auditEventsRepositoryModule
import de.gematik.ti.erp.app.redeem.redeemModule
import de.gematik.ti.erp.app.settings.settingsModule
import de.gematik.ti.erp.app.timeouts.di.timeoutsSharedPrefsModule
import de.gematik.ti.erp.app.userauthentication.di.userAuthenticationModule
import de.gematik.ti.erp.app.vau.vauModule
import org.kodein.di.DI

val mockFeatureModule = DI.Module("featureModule", allowSilentOverride = true) {
    importAll(
        userAuthenticationModule,
        applicationControllerModule,
        onboardingModule,
        dispatchersModule,
        cardWallModule,
        appSecurityModule,
        clientBuilderModule,
        networkModule,
        fhirVzdNetworkModule,
        loggerModule,
        realmModule,
        idpModule,
        idpUseCaseModule,
        messagesModule,
        orderHealthCardModule,
        pharmacyModule,
        redeemModule,
        profilesModule,
        auditEventsModule,
        taskModule,
        settingsModule,
        vauModule,
        cardUnlockModule,
        pkvModule,
        digaModule,
        euModule,
        // shared-prefs modules
        timeoutsSharedPrefsModule,
        // other modules
        cardCommunicationAnalyticsModule,
        debugSettingsModule,
        mlKitModule,
        appUpdateModule,
        prescriptionModule,
        // repositories
        prescriptionRepositoryModule,
        consentRepositoryModule,
        auditEventsRepositoryModule,
        messageRepositoryModule,
        digaRepositoryModule,
        // data sources
        databaseModule(BuildConfig.DEBUG),
        // mocked modules
        mockPharmacyRepositoryModule,
        mockTaskRepositoryModule,
        mockProfileRepositoryModule,
        mockDataSourceModule,
        mockPrescriptionRepositoryModule,
        mockSettingsRepositoryModule,
        allowOverride = true
    )
}
