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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.analytics.di.analyticsModule
import de.gematik.ti.erp.app.appupdate.di.appUpdateModule
import de.gematik.ti.erp.app.appsecurity.appSecurityModule
import de.gematik.ti.erp.app.authentication.di.authenticationModule
import de.gematik.ti.erp.app.cardunlock.di.cardUnlockModule
import de.gematik.ti.erp.app.cardwall.cardWallModule
import de.gematik.ti.erp.app.idp.idpModule
import de.gematik.ti.erp.app.idp.idpUseCaseModule
import de.gematik.ti.erp.app.mlkit.mlKitModule
import de.gematik.ti.erp.app.orderhealthcard.orderHealthCardModule
import de.gematik.ti.erp.app.orders.messageRepositoryModule
import de.gematik.ti.erp.app.orders.messagesModule
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
import de.gematik.ti.erp.app.timeouts.di.timeoutsSharedPrefsModule
import de.gematik.ti.erp.app.vau.vauModule
import org.kodein.di.DI

/**
 * Use this only in the android-app module
 */
val featureModule = DI.Module("featureModule", allowSilentOverride = true) {
    importAll(
        cardWallModule,
        appSecurityModule,
        networkModule,
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
        profileRepositoryModule,
        prescriptionRepositoryModule,
        consentRepositoryModule,
        protocolRepositoryModule,
        pharmacyRepositoryModule,
        messageRepositoryModule,
        taskRepositoryModule,
        timeoutsSharedPrefsModule,
        analyticsModule,
        mlKitModule,
        appUpdateModule,
        allowOverride = true
    )
}
