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

package de.gematik.ti.erp.app.settings.repository

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.writeToRealm
import de.gematik.ti.erp.app.database.realm.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.SettingsEntityV1
import de.gematik.ti.erp.app.database.settings.SettingsLocalDataSource
import de.gematik.ti.erp.app.settings.model.SettingsData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSettingsRepository(
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO,
    private val realm: Realm,
    private val settingsLocalDataSource: SettingsLocalDataSource
) : SettingsRepository(
    dispatchers = dispatchers,
    realm = realm
) {
    private val settings: Flow<SettingsEntityV1?>
        get() = realm.query<SettingsEntityV1>().first().asFlow().map { it.obj }

    private val lastRefreshed: Flow<Instant>
        get() = realm.query<SettingsEntityV1>().first().asFlow()
            .mapNotNull { it.obj?.time?.toInstant() }

    override val general: Flow<SettingsData.General>
        get() = realm.query<SettingsEntityV1>().first().asFlow().mapNotNull { query ->
            query.obj
        }.flatMapLatest { settings ->
            combine(
                settingsLocalDataSource.zoomEnabled,
                settingsLocalDataSource.welcomeDrawerShown,
                settingsLocalDataSource.userHasAcceptedInsecureDevice,
                settingsLocalDataSource.userHasAcceptedIntegrityNotOk,
                settingsLocalDataSource.mlKitAccepted,
                settingsLocalDataSource.screenshotsAllowed,
                settingsLocalDataSource.trackingAllowed
            ) { flows ->
                val zoomEnabled = flows[0]
                val welcomeDrawerShown = flows[1]
                val userHasAcceptedInsecureDevice = flows[2]
                val userHasAcceptedIntegrityNotOk = flows[3]
                val mlKitAccepted = flows[4]
                val screenShotsAllowed = flows[5]
                val trackingAllowed = flows[6]

                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(
                        code = settings.latestAppVersionCode,
                        name = settings.latestAppVersionName
                    ),
                    onboardingShownIn = settings.onboardingLatestAppVersionCode
                        .takeIf { it != -1 }
                        ?.let {
                            SettingsData.AppVersion(
                                code = it,
                                name = settings.onboardingLatestAppVersionName
                            )
                        },
                    mainScreenTooltipsShown = settings.mainScreenTooltipsShown,
                    zoomEnabled = zoomEnabled,
                    welcomeDrawerShown = welcomeDrawerShown,
                    userHasAcceptedInsecureDevice = userHasAcceptedInsecureDevice,
                    userHasAcceptedIntegrityNotOk = userHasAcceptedIntegrityNotOk,
                    mlKitAccepted = mlKitAccepted,
                    screenShotsAllowed = screenShotsAllowed,
                    trackingAllowed = trackingAllowed
                )
            }.flowOn(dispatchers)
        }

    @Requirement(
        "A_24525#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Check if analytics is allowed."
    )

    override fun isAnalyticsAllowed(): Flow<Boolean> = settingsLocalDataSource.trackingAllowed

    override val pharmacySearch: Flow<SettingsData.PharmacySearch>
        get() = settings.mapNotNull { settings ->
            settings?.pharmacySearch?.let {
                SettingsData.PharmacySearch(
                    name = it.name,
                    locationEnabled = it.locationEnabled,
                    deliveryService = it.filterDeliveryService,
                    onlineService = it.filterOnlineService,
                    openNow = it.filterOpenNow
                )
            }
        }.flowOn(dispatchers)

    // TODO move to PharmacySearch
    override suspend fun savePharmacySearch(search: SettingsData.PharmacySearch) {
        writeToRealm {
            this.pharmacySearch?.apply {
                this.name = search.name
                this.locationEnabled = search.locationEnabled
                this.filterDeliveryService = search.deliveryService
                this.filterOnlineService = search.onlineService
                this.filterOpenNow = search.openNow
            }
        }
    }

    override suspend fun saveZoomPreference(enabled: Boolean) {
        settingsLocalDataSource.saveZoomEnabled(enabled)
    }

    override val authentication: Flow<SettingsData.Authentication>
        get() = realm.query<SettingsEntityV1>().asFlow().mapNotNull { query ->
            query.list.first().let {
                it.authentication?.let { authentication ->
                    SettingsData.Authentication(
                        password = authentication.password?.let { pw ->
                            SettingsData.Authentication.Password(
                                hash = pw.hash,
                                salt = pw.salt
                            )
                        },
                        deviceSecurity = authentication.deviceSecurity,
                        failedAuthenticationAttempts = authentication.failedAuthenticationAttempts,
                        authenticationTimeOutSystemUptime = authentication.authenticationTimeOutSystemUptime
                    )
                }
            }
        }.flowOn(dispatchers)

    private fun SettingsEntityV1.setAuthentication(authentication: SettingsData.Authentication) {
        this.authentication = AuthenticationEntityV1().apply {
            this.deviceSecurity = authentication.deviceSecurity
            this.failedAuthenticationAttempts = authentication.failedAuthenticationAttempts
            this.password = authentication.password?.let {
                AuthenticationPasswordEntityV1().apply {
                    this.hash = it.hash
                    this.salt = it.salt
                }
            }
        }
    }

    override suspend fun saveOnboardingData(
        authentication: SettingsData.Authentication,
        profileName: String,
        now: Instant
    ) {
        withContext(dispatchers) {
            realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
                copyToRealm(
                    ProfileEntityV1().apply {
                        this.name = profileName
                        this.active = true
                        this.isNewlyCreated = true
                    }
                )
                settings.setAuthentication(authentication)
                settings.setOnboardingAppVersion()
            }
            settingsLocalDataSource.acceptUpdatedDataTerms(now)
        }
    }

    override suspend fun enableDeviceSecurity() {
        writeToRealm {
            this.authentication?.let { it.deviceSecurity = true }
        }
    }

    override suspend fun disableDeviceSecurity() {
        writeToRealm {
            this.authentication?.let { it.deviceSecurity = false }
        }
    }

    override suspend fun setPassword(password: SettingsData.Authentication.Password) {
        writeToRealm {
            this.authentication?.let {
                it.password = password.let {
                    AuthenticationPasswordEntityV1().apply {
                        this.hash = it.hash
                        this.salt = it.salt
                    }
                }
            }
        }
    }

    override suspend fun resetPassword() {
        writeToRealm {
            this.authentication?.let { it.password = null }
        }
    }

    @Requirement(
        "O.Pass_4#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Increments the number of authentication failures when the user fails to authenticate."
    )
    override suspend fun incrementNumberOfAuthenticationFailures() {
        writeToRealm {
            this.authentication?.let { it.failedAuthenticationAttempts += 1 }
        }
    }

    override suspend fun resetNumberOfAuthenticationFailures() {
        writeToRealm {
            this.authentication?.let { it.failedAuthenticationAttempts = 0 }
        }
    }

    override suspend fun setAuthenticationTimeOutSystemUptime(systemUptime: Long) {
        writeToRealm {
            this.authentication?.let { it.authenticationTimeOutSystemUptime = systemUptime }
        }
    }

    override suspend fun resetAuthenticationTimeOutSystemUptime() {
        writeToRealm {
            this.authentication?.let { it.authenticationTimeOutSystemUptime = null }
        }
    }

    override suspend fun saveWelcomeDrawerShown() {
        settingsLocalDataSource.saveWelcomeDrawerShown()
    }

    override suspend fun saveMainScreenTooltipShown() {
        writeToRealm {
            this.mainScreenTooltipsShown = true
        }
    }

    override suspend fun acceptMlKit() {
        settingsLocalDataSource.acceptMlKit()
    }

    override suspend fun saveAllowScreenshots(allow: Boolean) {
        settingsLocalDataSource.saveAllowScreenshots(allow)
    }

    @Requirement(
        "O.Purp_5#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = " save allow/disallow analytics state to settings repository."
    )
    @Requirement(
        "A_24525#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Save the user's decision to allow or disallow tracking."
    )
    override suspend fun saveAllowTracking(allow: Boolean) {
        settingsLocalDataSource.saveAllowTracking(allow)
    }

    override suspend fun acceptInsecureDevice() {
        settingsLocalDataSource.acceptInsecureDevice()
    }

    override suspend fun acceptIntegrityNotOk() {
        settingsLocalDataSource.acceptIntegrityNotOk()
    }

    override suspend fun acceptUpdatedDataTerms(now: Instant) {
        settingsLocalDataSource.acceptUpdatedDataTerms(now)
    }

    override suspend fun updateRefreshTime() {
        writeToRealm {
            this.time = Clock.System.now().toRealmInstant()
        }
    }

    override fun getLastRefreshedTime(): Flow<Instant> = lastRefreshed

    private fun SettingsEntityV1.setOnboardingAppVersion() {
        this.onboardingLatestAppVersionName = this.latestAppVersionName
        this.onboardingLatestAppVersionCode = this.latestAppVersionCode
    }
}
