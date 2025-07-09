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

package de.gematik.ti.erp.app.digas.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.gematik.ti.erp.app.base.usecase.TriggerNavigationUseCase
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * A [CoroutineWorker] responsible for triggering a navigation-related event for a specific profile.
 *
 * This worker is typically scheduled with a profile ID as input and is used to trigger
 * one-time events such as feedback prompts or in-app guidance flows after a delay.
 *
 * Dependencies are injected via Kodein using the application context.
 *
 * Input:
 * - `profileId`: A string passed via `workDataOf("profileId" to ...)` identifying the target profile.
 *
 * Output:
 * - [Result.success] if the trigger was applied successfully.
 * - [Result.failure] if the profileId is missing or the use case throws an exception.
 *
 * @param appContext The application context (used to retrieve DI container).
 * @param workerParams The worker parameters provided by WorkManager.
 */
class FeedbackNavigationTriggerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val kodein = (appContext.applicationContext as DIAware).di
    private val trigger: TriggerNavigationUseCase by kodein.instance()

    override suspend fun doWork(): Result {
        val profileId = inputData.getString("profileId") ?: return Result.failure()

        return try {
            trigger.invoke(profileId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
