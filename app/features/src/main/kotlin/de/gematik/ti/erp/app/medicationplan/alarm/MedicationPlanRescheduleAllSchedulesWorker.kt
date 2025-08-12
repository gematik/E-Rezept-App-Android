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

package de.gematik.ti.erp.app.medicationplan.alarm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.gematik.ti.erp.app.medicationplan.usecase.GetAllMedicationSchedulesUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleMedicationScheduleUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.firstOrNull
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.subDI
import org.kodein.di.instance

class MedicationPlanRescheduleAllSchedulesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), DIAware {
    override val di by context.subDI(closestDI()) {}
    private val loadAllMedicationSchedulesUseCase by instance<GetAllMedicationSchedulesUseCase>()
    private val scheduleMedicationScheduleUseCase by instance<ScheduleMedicationScheduleUseCase>()

    override suspend fun doWork(): Result = runCatching {
        val schedules = loadAllMedicationSchedulesUseCase.invoke().firstOrNull()
        schedules?.let {
                scheduleList ->
            scheduleList.forEach { schedule ->
                scheduleMedicationScheduleUseCase(schedule)
            }
        }
    }.fold(
        onSuccess = {
            Result.success()
        },
        onFailure = {
            Napier.e(it) { "Rescheduling failed" }
            Result.failure()
        }
    )
}
