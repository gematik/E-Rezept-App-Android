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

package de.gematik.ti.erp.app.medicationplan.ui.preview.mocks

import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import java.time.DayOfWeek

val medicationPlanPreviewCurrentTime = Instant.parse("2023-01-01T16:20:00Z")

val SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE = PrescriptionData.Scanned(
    SCANNED_TASK
).toMedicationSchedule(medicationPlanPreviewCurrentTime)

val SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS = SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE.copy(isActive = true)

val SYNCED_PRESCRIPTION_SCHEDULE_INACTIVE = PrescriptionData.Synced(SYNCED_TASK).toMedicationSchedule(medicationPlanPreviewCurrentTime)

val SYNCED_PRESCRIPTION_SCHEDULE_ACTIVE_EVERY_TWO_DAYS = SYNCED_PRESCRIPTION_SCHEDULE_INACTIVE.copy(
    isActive = true,
    interval = MedicationScheduleInterval.EveryTwoDays
)

val SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_INACTIVE = PrescriptionData.Synced(
    SYNCED_TASK_STRUCTURED_DOSAGE
).toMedicationSchedule(medicationPlanPreviewCurrentTime)

val SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS = SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_INACTIVE.copy(isActive = true)

val SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED = SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_INACTIVE.copy(
    isActive = true,
    interval = MedicationScheduleInterval.Personalized(
        selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
    ),
    duration = MedicationScheduleDuration.Personalized(
        startDate = medicationPlanPreviewCurrentTime.toLocalDate(),
        endDate = medicationPlanPreviewCurrentTime.toLocalDate().plus(period = DatePeriod(days = 14))
    )
)
