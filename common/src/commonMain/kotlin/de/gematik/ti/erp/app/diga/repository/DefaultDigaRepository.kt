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

package de.gematik.ti.erp.app.diga.repository

import de.gematik.ti.erp.app.diga.local.DigaLocalDataSource
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class DefaultDigaRepository(private val localDataSource: DigaLocalDataSource) : DigaRepository {

    override suspend fun updateDigaAsSeen(taskId: String) {
        localDataSource.setDigaIsNotNew(taskId)
    }

    override suspend fun updateDigaStatus(taskId: String, status: DigaStatus, lastModified: Instant?) {
        localDataSource.updateDigaStatus(
            taskId = taskId,
            status = status,
            lastModified = lastModified
        )
    }

    override suspend fun updateDigaCommunicationSent(taskId: String, time: Instant) {
        localDataSource.updateDigaCommunicationSent(taskId, time)
    }

    override fun loadDigaByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> {
        return localDataSource.loadDigaByTaskId(taskId)
    }

    override suspend fun updateArchiveStatus(
        taskId: String,
        lastModified: Instant,
        setArchiveStatus: Boolean
    ) {
        localDataSource.updateArchiveStatus(
            taskId = taskId,
            lastModified = lastModified,
            isArchive = setArchiveStatus
        )
    }

    override suspend fun loadDigasByProfileId(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>> =
        localDataSource.loadDigasByProfileId(profileId = profileId)

    override fun loadArchiveDigasByProfileId(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>> =
        localDataSource.loadArchiveDigasByProfileId(profileId = profileId)
}
