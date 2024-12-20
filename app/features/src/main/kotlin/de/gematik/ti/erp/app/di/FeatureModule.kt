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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.analytics.di.analyticsModule
import de.gematik.ti.erp.app.appsecurity.appSecurityModule
import de.gematik.ti.erp.app.appupdate.di.appUpdateModule
import de.gematik.ti.erp.app.authentication.di.authenticationModule
import de.gematik.ti.erp.app.cardunlock.di.cardUnlockModule
import de.gematik.ti.erp.app.cardwall.cardWallModule
import de.gematik.ti.erp.app.debugsettings.di.debugSettingsModule
import de.gematik.ti.erp.app.featuretoggle.di.newFeaturesSharedPrefsModule
import de.gematik.ti.erp.app.idp.idpModule
import de.gematik.ti.erp.app.idp.idpUseCaseModule
import de.gematik.ti.erp.app.logger.di.loggerModule
import de.gematik.ti.erp.app.messages.di.messageRepositoryModule
import de.gematik.ti.erp.app.messages.di.messagesModule
import de.gematik.ti.erp.app.medicationplan.di.medicationPlanModule
import de.gematik.ti.erp.app.mlkit.mlKitModule
import de.gematik.ti.erp.app.onboarding.di.onboardingModule
import de.gematik.ti.erp.app.orderhealthcard.di.orderHealthCardModule
import de.gematik.ti.erp.app.pharmacy.di.pharmacyModule
import de.gematik.ti.erp.app.pharmacy.di.pharmacyRepositoryModule
import de.gematik.ti.erp.app.pkv.consentRepositoryModule
import de.gematik.ti.erp.app.pkv.pkvModule
import de.gematik.ti.erp.app.prescription.prescriptionModule
import de.gematik.ti.erp.app.prescription.prescriptionRepositoryModule
import de.gematik.ti.erp.app.prescription.taskModule
import de.gematik.ti.erp.app.prescription.taskRepositoryModule
import de.gematik.ti.erp.app.profiles.profileRepositoryModule
import de.gematik.ti.erp.app.profiles.profilesModule
import de.gematik.ti.erp.app.protocol.protocolModule
import de.gematik.ti.erp.app.protocol.protocolRepositoryModule
import de.gematik.ti.erp.app.redeem.redeemModule
import de.gematik.ti.erp.app.settings.settingsModule
import de.gematik.ti.erp.app.settings.settingsRepositoryModule
import de.gematik.ti.erp.app.timeouts.di.timeoutsSharedPrefsModule
import de.gematik.ti.erp.app.vau.vauModule
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI
import org.kodein.di.bindProvider

/**
 * Use this only in the android-app module
 */
val featureModule = DI.Module("featureModule", allowSilentOverride = true) {
    importAll(
        applicationControllerModule,
        onboardingModule,
        dispatchersModule,
        cardWallModule,
        appSecurityModule,
        networkModule,
        loggerModule,
        realmModule,
        idpModule,
        idpUseCaseModule,
        messagesModule,
        orderHealthCardModule,
        pharmacyModule,
        redeemModule,
        prescriptionModule,
        profilesModule,
        protocolModule,
        taskModule,
        settingsModule,
        vauModule,
        cardUnlockModule,
        pkvModule,
        authenticationModule,
        // shared-prefs modules
        timeoutsSharedPrefsModule,
        newFeaturesSharedPrefsModule,
        // other modules
        analyticsModule,
        debugSettingsModule,
        mlKitModule,
        appUpdateModule,
        // repositories
        settingsRepositoryModule,
        profileRepositoryModule,
        prescriptionRepositoryModule,
        consentRepositoryModule,
        protocolRepositoryModule,
        pharmacyRepositoryModule,
        messageRepositoryModule,
        taskRepositoryModule,
        medicationPlanModule,
        allowOverride = true
    )
}

val dispatchersModule = DI.Module("dispatchersModule") {
    bindProvider { Dispatchers.IO }
}
