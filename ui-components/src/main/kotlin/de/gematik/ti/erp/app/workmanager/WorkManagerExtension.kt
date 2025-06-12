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

package de.gematik.ti.erp.app.workmanager

import androidx.lifecycle.LifecycleOwner
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import io.github.aakira.napier.Napier

fun WorkManager.listenToWorkManagerState(
    workRequest: WorkRequest,
    lifecycleOwner: LifecycleOwner,
    onObserveWorkState: (WorkInfo.State?) -> Unit = {},
    onWorkInfoSucceeded: () -> Unit = {}
) {
    this.getWorkInfoByIdLiveData(workRequest.id).observe(lifecycleOwner) { workInfo ->
        onObserveWorkState(workInfo?.state)
        when (workInfo?.state) {
            WorkInfo.State.SUCCEEDED -> {
                Napier.i(tag = "DownloadWorker", message = "✅ Work succeeded")
                onWorkInfoSucceeded()
            }

            WorkInfo.State.FAILED -> Napier.e(tag = "DownloadWorker", message = "❌ Work failed")
            WorkInfo.State.RUNNING -> Napier.d(tag = "DownloadWorker", message = "🔄 Work is running")
            else -> Napier.d(tag = "DownloadWorker", message = "ℹ️ Work state: ${workInfo?.state}")
        }
    }
}
