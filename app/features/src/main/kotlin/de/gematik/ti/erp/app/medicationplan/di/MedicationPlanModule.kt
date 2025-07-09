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

package de.gematik.ti.erp.app.medicationplan.di

import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.GetDosageInstructionByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.LoadAllMedicationSchedulesUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.LoadMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.LoadProfilesWithSchedulesUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.PlanMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleReminderWorker
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val medicationPlanModule = DI.Module("medicationPlanModule") {
    bindProvider { MedicationPlanLocalDataSource(instance()) }
    bindProvider { MedicationPlanRepository(instance()) }
    bindProvider { LoadAllMedicationSchedulesUseCase(instance()) }
    bindProvider { LoadMedicationScheduleByTaskIdUseCase(instance()) }
    bindProvider { GetDosageInstructionByTaskIdUseCase(instance()) }
    bindProvider { LoadProfilesWithSchedulesUseCase(instance(), instance()) }
    bindProvider { ScheduleReminderWorker(instance()) }
    bindProvider { PlanMedicationScheduleUseCase(instance(), instance()) }
}
