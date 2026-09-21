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

package de.gematik.ti.erp.app.database.bridge.settings

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow

class SettingsLocalDataSourceBridge(
    private val settingsLocalDataSourceV1: SettingsLocalDataSource,
    private val settingsLocalDataSourceV2: SettingsLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : SettingsLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun loadSettings(): Flow<SettingsErpModel> {
        return when {
            useRoom -> settingsLocalDataSourceV2.loadSettings()
            else -> settingsLocalDataSourceV1.loadSettings()
        }
    }

    override fun isAnalyticsAllowed(): Flow<Boolean> {
        return when {
            useRoom -> settingsLocalDataSourceV2.isAnalyticsAllowed()
            else -> settingsLocalDataSourceV1.isAnalyticsAllowed()
        }
    }

    override suspend fun saveLatestAppVersion(appVersion: AppVersionErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveLatestAppVersion(appVersion)
            else -> settingsLocalDataSourceV1.saveLatestAppVersion(appVersion)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveOnboardingShownIn(appVersion: AppVersionErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveOnboardingShownIn(appVersion)
            else -> settingsLocalDataSourceV1.saveOnboardingShownIn(appVersion)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveTheme(theme: ThemeMode) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveTheme(theme)
            else -> settingsLocalDataSourceV1.saveTheme(theme)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveZoomEnabled(enabled: Boolean) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveZoomEnabled(enabled)
            else -> settingsLocalDataSourceV1.saveZoomEnabled(enabled)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun acceptInsecureDevice() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.acceptInsecureDevice()
            else -> settingsLocalDataSourceV1.acceptInsecureDevice()
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveWelcomeDrawerShown() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveWelcomeDrawerShown()
            else -> settingsLocalDataSourceV1.saveWelcomeDrawerShown()
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveAllowScreenshots(allow: Boolean) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveAllowScreenshots(allow)
            else -> settingsLocalDataSourceV1.saveAllowScreenshots(allow)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun saveAllowTracking(allow: Boolean) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.saveAllowTracking(allow)
            else -> settingsLocalDataSourceV1.saveAllowTracking(allow)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun acceptIntegrityNotOk() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.acceptIntegrityNotOk()
            else -> settingsLocalDataSourceV1.acceptIntegrityNotOk()
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun resetOnboardingShownIn() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> settingsLocalDataSourceV2.resetOnboardingShownIn()
            else -> settingsLocalDataSourceV1.resetOnboardingShownIn()
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override fun isDataPortedToRoom(): Flow<Boolean> = settingsLocalDataSourceV2.isDataPortedToRoom()

    override suspend fun markDataAsPortedToRoom() {
        settingsLocalDataSourceV2.markDataAsPortedToRoom()
    }
}
