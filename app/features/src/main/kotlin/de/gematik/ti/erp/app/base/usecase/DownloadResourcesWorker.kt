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

package de.gematik.ti.erp.app.base.usecase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.gematik.ti.erp.app.base.model.DownloadResourcesState
import de.gematik.ti.erp.app.prescription.repository.DownloadResourcesStateRepository
import org.kodein.di.DIAware
import org.kodein.di.instance

class DownloadResourcesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val kodein = (appContext.applicationContext as DIAware).di
    private val downloadAllResourcesUseCase: DownloadAllResourcesUseCase by kodein.instance()
    private val stateRepository: DownloadResourcesStateRepository by kodein.instance()

    override suspend fun doWork(): Result {
        val profileId = inputData.getString("profileId") ?: return Result.failure()

        return try {
            stateRepository.updateSnapshotState(DownloadResourcesState.InProgress)
            val result = downloadAllResourcesUseCase.invoke(profileId)
            stateRepository.updateSnapshotState(DownloadResourcesState.Finished)
            if (result.isSuccess) Result.success() else Result.failure()
        } catch (e: Exception) {
            stateRepository.updateSnapshotState(DownloadResourcesState.Finished)
            Result.failure()
        }
    }
}
