/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.repository

import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.writeToRealm
import de.gematik.ti.erp.app.settings.datasource.SettingsDataSource
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.realm.kotlin.Realm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class MockSettingsRepository(
    private val settingsDataSource: SettingsDataSource,
    // keep realm till the profile mock is implemented
    private val realm: Realm,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SettingsRepository(
    dispatchers = dispatcher,
    realm = realm
) {
    override val general: Flow<SettingsData.General>
        get() = settingsDataSource.generalData

    override suspend fun acceptUpdatedDataTerms(now: Instant) {
        // no-op
    }

    override suspend fun saveOnboardingData(
        authentication: SettingsData.Authentication,
        profileName: String,
        now: Instant
    ) {
        withContext(dispatcher) {
            if (authentication.methodIsPassword) {
                settingsDataSource.authentication.value = authentication
            }
        }
        withContext(dispatcher) {
            realm.writeToRealm<SettingsEntityV1, Unit> { profile ->
                copyToRealm(
                    ProfileEntityV1().apply {
                        this.name = profileName
                        this.active = true
                    }
                )
            }
        }
    }

    override suspend fun enableDeviceSecurity() {
        // no-op since we only support password authentication for mock
    }

    override suspend fun disableDeviceSecurity() {
        // no-op since we only support password authentication for mock
    }

    override suspend fun setPassword(password: SettingsData.Authentication.Password) {
        settingsDataSource.authentication.value = SettingsData.Authentication(
            password = password,
            deviceSecurity = false,
            failedAuthenticationAttempts = 0
        )
    }

    override suspend fun resetPassword() {
        // no-op since we only support password authentication for mock
    }

    override val authentication: Flow<SettingsData.Authentication>
        get() = settingsDataSource.authentication

    override suspend fun saveZoomPreference(enabled: Boolean) {
        settingsDataSource.generalData.update {
            it.copy(
                zoomEnabled = enabled
            )
        }
    }

    override suspend fun acceptInsecureDevice() {
        settingsDataSource.generalData.update {
            it.copy(
                userHasAcceptedInsecureDevice = true
            )
        }
    }

    override suspend fun incrementNumberOfAuthenticationFailures() {
        settingsDataSource.authentication.update {
            it.copy(
                failedAuthenticationAttempts = it.failedAuthenticationAttempts + 1
            )
        }
    }

    override suspend fun resetNumberOfAuthenticationFailures() {
        settingsDataSource.authentication.update {
            it.copy(
                failedAuthenticationAttempts = 0
            )
        }
    }

    override suspend fun saveWelcomeDrawerShown() {
        settingsDataSource.generalData.update {
            it.copy(
                welcomeDrawerShown = true
            )
        }
    }

    override suspend fun saveMainScreenTooltipShown() {
        settingsDataSource.generalData.update {
            it.copy(
                mainScreenTooltipsShown = true
            )
        }
    }

    override suspend fun acceptMlKit() {
        settingsDataSource.generalData.update {
            it.copy(
                mlKitAccepted = true
            )
        }
    }

    override suspend fun saveAllowScreenshots(allow: Boolean) {
        settingsDataSource.generalData.update {
            it.copy(
                screenShotsAllowed = allow
            )
        }
    }

    override suspend fun saveAllowTracking(allow: Boolean) {
        settingsDataSource.generalData.update {
            it.copy(
                trackingAllowed = allow
            )
        }
    }

    override suspend fun acceptIntegrityNotOk() {
        settingsDataSource.generalData.update {
            it.copy(
                userHasAcceptedIntegrityNotOk = true
            )
        }
    }

    override suspend fun savePharmacySearch(search: SettingsData.PharmacySearch) {
        settingsDataSource.pharmacySearch.update {
            it.copy(
                name = search.name,
                locationEnabled = search.locationEnabled,
                deliveryService = search.deliveryService,
                onlineService = search.onlineService,
                openNow = search.openNow
            )
        }
    }

    override val pharmacySearch: Flow<SettingsData.PharmacySearch>
        get() = settingsDataSource.pharmacySearch

    override fun isAnalyticsAllowed(): Flow<Boolean> {
        return settingsDataSource.generalData.map { it.trackingAllowed }
    }
}
