/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.settings.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsAuthenticationMethodV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.writeToRealm
import de.gematik.ti.erp.app.settings.GeneralSettings
import de.gematik.ti.erp.app.settings.PharmacySettings
import de.gematik.ti.erp.app.settings.model.SettingsData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

class SettingsRepository constructor(
    private val dispatchers: DispatchProvider,
    private val realm: Realm
) : GeneralSettings, PharmacySettings {
    private val settings: Flow<SettingsEntityV1?>
        get() = realm.query<SettingsEntityV1>().first().asFlow().map { it.obj }

    override val general: Flow<SettingsData.General>
        get() = realm.query<SettingsEntityV1>().first().asFlow().mapNotNull { query ->
            query.obj?.let {
                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(
                        code = it.latestAppVersionCode,
                        name = it.latestAppVersionName
                    ),
                    onboardingShownIn = if (it.onboardingLatestAppVersionCode != -1) {
                        SettingsData.AppVersion(
                            code = it.onboardingLatestAppVersionCode,
                            name = it.onboardingLatestAppVersionName
                        )
                    } else {
                        null
                    },
                    welcomeDrawerShown = it.welcomeDrawerShown,
                    dataProtectionVersionAcceptedOn = it.dataProtectionVersionAccepted.toInstant(),
                    zoomEnabled = it.zoomEnabled,
                    userHasAcceptedInsecureDevice = it.userHasAcceptedInsecureDevice,
                    authenticationFails = it.authenticationFails
                )
            }
        }.flowOn(dispatchers.IO)

    override val authenticationMode: Flow<SettingsData.AuthenticationMode>
        get() = realm.query<SettingsEntityV1>().first().asFlow().mapNotNull { query ->
            query.obj?.let {
                when (it.authenticationMethod) {
                    SettingsAuthenticationMethodV1.DeviceSecurity -> SettingsData.AuthenticationMode.DeviceSecurity
                    SettingsAuthenticationMethodV1.Password -> {
                        it.password?.let { pw ->
                            SettingsData.AuthenticationMode.Password(
                                hash = pw.hash,
                                salt = pw.salt
                            )
                        }
                    }

                    SettingsAuthenticationMethodV1.Biometrics -> SettingsData.AuthenticationMode.Biometrics
                    SettingsAuthenticationMethodV1.DeviceCredentials -> SettingsData.AuthenticationMode.DeviceCredentials
                    SettingsAuthenticationMethodV1.None -> SettingsData.AuthenticationMode.None
                    else -> SettingsData.AuthenticationMode.Unspecified
                }
            }
        }.flowOn(dispatchers.IO)

    override val pharmacySearch: Flow<SettingsData.PharmacySearch>
        get() = settings.mapNotNull { settings ->
            settings?.pharmacySearch?.let {
                SettingsData.PharmacySearch(
                    name = it.name,
                    locationEnabled = it.locationEnabled,
                    ready = it.filterReady,
                    deliveryService = it.filterDeliveryService,
                    onlineService = it.filterOnlineService,
                    openNow = it.filterOpenNow
                )
            }
        }.flowOn(dispatchers.IO)

    override suspend fun savePharmacySearch(search: SettingsData.PharmacySearch) {
        writeToRealm {
            this.pharmacySearch?.apply {
                this.name = search.name
                this.locationEnabled = search.locationEnabled
                this.filterReady = search.ready
                this.filterDeliveryService = search.deliveryService
                this.filterOnlineService = search.onlineService
                this.filterOpenNow = search.openNow
            }
        }
    }

    override suspend fun saveZoomPreference(enabled: Boolean) {
        writeToRealm {
            this.zoomEnabled = enabled
        }
    }

    override suspend fun saveAuthenticationMode(mode: SettingsData.AuthenticationMode) {
        writeToRealm {
            this.setAuthenticationMode(mode)
        }
    }

    private fun SettingsEntityV1.setAuthenticationMode(mode: SettingsData.AuthenticationMode) {
        this.authenticationMethod = when (mode) {
            SettingsData.AuthenticationMode.DeviceSecurity -> SettingsAuthenticationMethodV1.DeviceSecurity
            is SettingsData.AuthenticationMode.Password -> SettingsAuthenticationMethodV1.Password
            else -> SettingsAuthenticationMethodV1.Unspecified
        }
        if (mode is SettingsData.AuthenticationMode.Password) {
            this.authenticationMethod = SettingsAuthenticationMethodV1.Password
            this.password?.apply {
                this.hash = mode.hash
                this.salt = mode.salt
            }
        } else {
            this.password?.reset()
        }
    }

    override suspend fun saveOnboardingSucceededData(
        authenticationMode: SettingsData.AuthenticationMode,
        profileName: String,
        now: Instant
    ) {
        withContext(dispatchers.IO) {
            realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
                copyToRealm(
                    ProfileEntityV1().apply {
                        this.name = profileName
                        this.active = true
                    }
                )
                settings.setAuthenticationMode(authenticationMode)
                settings.setAcceptedUpdatedDataTerms(now)
                settings.setOnboardingAppVersion()
            }
        }
    }

    override suspend fun incrementNumberOfAuthenticationFailures() {
        writeToRealm {
            this.authenticationFails += 1
        }
    }

    override suspend fun resetNumberOfAuthenticationFailures() {
        writeToRealm {
            this.authenticationFails = 0
        }
    }

    override suspend fun saveWelcomeDrawerShown() {
        writeToRealm {
            this.welcomeDrawerShown = true
        }
    }

    override suspend fun acceptInsecureDevice() {
        writeToRealm {
            this.userHasAcceptedInsecureDevice = true
        }
    }

    override suspend fun acceptUpdatedDataTerms(now: Instant) {
        writeToRealm {
            this.setAcceptedUpdatedDataTerms(now)
        }
    }

    private fun SettingsEntityV1.setAcceptedUpdatedDataTerms(now: Instant) {
        this.dataProtectionVersionAccepted = now.toRealmInstant()
    }

    private fun SettingsEntityV1.setOnboardingAppVersion() {
        this.onboardingLatestAppVersionName = this.latestAppVersionName
        this.onboardingLatestAppVersionCode = this.latestAppVersionCode
    }

    private suspend fun writeToRealm(block: SettingsEntityV1.() -> Unit) {
        withContext(dispatchers.IO) {
            realm.writeToRealm<SettingsEntityV1, Unit> {
                it.block()
            }
        }
    }
}
