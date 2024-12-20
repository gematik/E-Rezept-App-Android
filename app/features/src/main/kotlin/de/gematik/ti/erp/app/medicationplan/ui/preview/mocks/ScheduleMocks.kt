/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.medicationplan.ui.preview.mocks

import de.gematik.ti.erp.app.medicationplan.presentation.PrescriptionSchedule
import de.gematik.ti.erp.app.medicationplan.ui.preview.medicationPlanPreviewCurrentTime
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.utils.maxLocalDate

val SCANNED_TASK_SCHEDULE = PrescriptionData.Scanned(
    SCANNED_TASK
).toMedicationSchedule(medicationPlanPreviewCurrentTime)

val SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE = PrescriptionSchedule(
    prescription = PrescriptionData.Scanned(SCANNED_TASK),
    dosageInstruction = MedicationPlanDosageInstruction.Empty,
    medicationSchedule = SCANNED_TASK_SCHEDULE
)

val SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE = PrescriptionSchedule(
    prescription = PrescriptionData.Scanned(SCANNED_TASK),
    dosageInstruction = MedicationPlanDosageInstruction.Empty,
    medicationSchedule = SCANNED_TASK_SCHEDULE.copy(isActive = true)
)

val SYNCED_PRESCRIPTION_SCHEDULE = PrescriptionData.Synced(SYNCED_TASK).toMedicationSchedule(medicationPlanPreviewCurrentTime)

val SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE = PrescriptionData.Synced(
    SYNCED_TASK_STRUCTURED_DOSAGE
).toMedicationSchedule(medicationPlanPreviewCurrentTime)

val ACTIVE_SYNCED_PRESCRIPTION_SCHEDULE = PrescriptionSchedule(
    prescription = PrescriptionData.Synced(SYNCED_TASK),
    dosageInstruction = MedicationPlanDosageInstruction.FreeText("Dosage"),
    medicationSchedule = SYNCED_PRESCRIPTION_SCHEDULE.copy(isActive = true)
)

val ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE = PrescriptionSchedule(
    prescription = PrescriptionData.Synced(SYNCED_TASK),
    dosageInstruction = MedicationPlanDosageInstruction.Structured(
        "1-0-1-0",
        mapOf(
            MedicationPlanDosageInstruction.DayTime.MORNING to "1",
            MedicationPlanDosageInstruction.DayTime.EVENING to "1"
        )
    ),
    medicationSchedule = SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE.copy(isActive = true)
)

val ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ENDLESS = ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE.copy(
    medicationSchedule = SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE.copy(
        isActive = true,
        end = maxLocalDate()
    )
)
