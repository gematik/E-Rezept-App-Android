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

package de.gematik.ti.erp.app.migration.usecase

import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.datastore.featuretoggle.ROOM_DB
import de.gematik.ti.erp.app.datastore.featuretoggle.FeatureToggleRepository
import de.gematik.ti.erp.app.logger.SessionLogHolder
import de.gematik.ti.erp.app.logger.model.ContentLog
import de.gematik.ti.erp.app.logger.model.LogEntry
import de.gematik.ti.erp.app.logger.model.RequestLog
import de.gematik.ti.erp.app.logger.model.ResponseLog
import de.gematik.ti.erp.app.logger.model.TimingsLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class CompleteMigrationUseCase(
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val featureToggleRepository: FeatureToggleRepository,
    private val sessionLogHolder: SessionLogHolder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke() = withContext(dispatcher) {
        val isRoomDbEnabled = featureToggleRepository
            .getFeatures()
            .firstOrNull()
            ?.find { it.name == ROOM_DB.name }
            ?.isActive == true

        if (!isRoomDbEnabled) {
            featureToggleRepository.toggleFeature(ROOM_DB)
        }
        settingsLocalDataSource.markDataAsPortedToRoom()
        val isPortingDone = settingsLocalDataSource.isDataPortedToRoom().firstOrNull() == true
        sessionLogHolder.addLog(
            LogEntry(
                timestamp = Clock.System.now().toString(),
                request = RequestLog(method = "INTERNAL", url = "internal://db-switch", headers = emptyList()),
                response = ResponseLog(
                    status = 200,
                    statusText = "OK",
                    headers = emptyList(),
                    content = ContentLog(
                        mimeType = "text/plain",
                        text = "db_backend_selected=ROOM migration_completed=true is_porting_done=$isPortingDone"
                    )
                ),
                timings = TimingsLog(send = 0, wait = 0, receive = 0)
            )
        )
    }
}
