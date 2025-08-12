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

import de.gematik.ti.erp.app.medicationplan.alarm.MedicationPlanNotificationScheduler
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.medicationplan.repository.DefaultMedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.DeactivateMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.DeleteMedicationScheduleNotificationUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.DeleteMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetDosageInstructionByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetAllMedicationSchedulesUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetActiveProfileWithSchedulesUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetAllProfileWithSchedulesUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleDurationUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleIntervalUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleNotificationDosageUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleNotificationTimeUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetOrCreateActiveMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetOrCreateMedicationScheduleNotificationUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val medicationPlanModule = DI.Module("medicationPlanModule") {
    bindProvider { MedicationPlanLocalDataSource(instance()) }
    bindProvider<MedicationPlanRepository> { DefaultMedicationPlanRepository(instance(), instance()) }
    bindProvider { GetAllMedicationSchedulesUseCase(instance()) }
    bindProvider { GetMedicationScheduleByTaskIdUseCase(instance()) }
    bindProvider { SetMedicationScheduleDurationUseCase(instance()) }
    bindProvider { SetMedicationScheduleIntervalUseCase(instance()) }
    bindProvider { SetMedicationScheduleNotificationTimeUseCase(instance()) }
    bindProvider { SetMedicationScheduleNotificationDosageUseCase(instance()) }
    bindProvider { DeleteMedicationScheduleNotificationUseCase(instance()) }
    bindProvider { DeactivateMedicationScheduleUseCase(instance()) }
    bindProvider { SetOrCreateActiveMedicationScheduleUseCase(instance()) }
    bindProvider { SetOrCreateMedicationScheduleNotificationUseCase(instance()) }
    bindProvider { GetDosageInstructionByTaskIdUseCase(instance()) }
    bindProvider { GetActiveProfileWithSchedulesUseCase(instance(), instance()) }
    bindProvider { GetAllProfileWithSchedulesUseCase(instance(), instance()) }
    bindProvider { MedicationPlanNotificationScheduler(instance()) }
    bindProvider { ScheduleMedicationScheduleUseCase(instance()) }
    bindProvider { DeleteMedicationScheduleUseCase(instance()) }
}
