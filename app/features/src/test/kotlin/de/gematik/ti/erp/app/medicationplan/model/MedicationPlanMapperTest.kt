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

package de.gematik.ti.erp.app.medicationplan.model

import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleEntityV1
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleNotificationDosageEntityV1
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleNotificationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleDurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleIntervalEntityV1
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.MEDICATION_REQUEST
import de.gematik.ti.erp.app.medicationplan.MEDICATION_SCHEDULE
import de.gematik.ti.erp.app.medicationplan.scannedTask
import de.gematik.ti.erp.app.medicationplan.syncedTask
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import io.realm.kotlin.ext.toRealmList
import junit.framework.TestCase.assertNotNull
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class MedicationPlanMapperTest {

    @Test
    fun `test null dosage instruction`() {
        val result = parseInstruction(null)
        assertEquals(MedicationPlanDosageInstruction.Empty, result)
    }

    @Test
    fun `test empty dosage instruction`() {
        val result = parseInstruction("")
        assertEquals(MedicationPlanDosageInstruction.Empty, result)
    }

    @Test
    fun `test empty dosage instruction an space`() {
        val result = parseInstruction(" ")
        assertEquals(MedicationPlanDosageInstruction.Empty, result)
    }

    @Test
    fun `test free text dosage instruction`() {
        val result = parseInstruction("Take one pill in the morning")
        assertEquals(
            MedicationPlanDosageInstruction.FreeText("Take one pill in the morning"),
            result
        )
    }

    @Test
    fun `test once in the morning 3 parts`() {
        val result = parseInstruction("1-0-0")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1-0-0",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 1 in the morning, 1 in the evening 3 parts`() {
        val result = parseInstruction("1-0-1")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1-0-1",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 1 in the morning, 1 2 on noon, 1 in  the evening 3 parts`() {
        val result = parseInstruction("1 - 2- 3")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1 - 2- 3",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.NOON to "2",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "3"
                )
            ),
            result
        )
    }

    @Test
    fun `test 3 float & fraction types`() {
        val result = parseInstruction("1.5-1,5-1½")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1.5-1,5-1½",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1.5",
                    MedicationPlanDosageInstruction.DayTime.NOON to "1,5",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "1½"
                )
            ),
            result
        )
    }

    @Test
    fun `test fraction types with spaces`() {
        val result = parseInstruction("½ - 2 ½ - 3  ½")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "½ - 2 ½ - 3  ½",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "½",
                    MedicationPlanDosageInstruction.DayTime.NOON to "2 ½",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "3  ½"
                )
            ),
            result
        )
    }

    @Test
    fun `test 3 parts with empty middle part`() {
        val result = parseInstruction("1--1")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1--1",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 3 parts with empty middle part and 0 on first`() {
        val result = parseInstruction("0--1")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "0--1",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.EVENING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 3 parts with 1 on middle part`() {
        val result = parseInstruction("-1-")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "-1-",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.NOON to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 3 parts with angle brackets`() {
        val result = parseInstruction("<<1-0-0>>")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "<<1-0-0>>",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 3 parts with angle brackets and spaces`() {
        val result = parseInstruction(" >> << 1-0-0 <<> > ")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = " >> << 1-0-0 <<> > ",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts 1 Morning`() {
        val result = parseInstruction("1-0-0-0")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1-0-0-0",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts 1 Morning, 1 Evening`() {
        val result = parseInstruction("1-0-1-0")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1-0-1-0",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts 1 Noon, 1 Night`() {
        val result = parseInstruction("0-1-0-1")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "0-1-0-1",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.NOON to "1",
                    MedicationPlanDosageInstruction.DayTime.NIGHT to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts with one float and spaces`() {
        val result = parseInstruction("1 - 2- 3-   4.0")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1 - 2- 3-   4.0",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.NOON to "2",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "3",
                    MedicationPlanDosageInstruction.DayTime.NIGHT to "4.0"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts with float, spaces and fraction types`() {
        val result = parseInstruction("1.5  -  1,5  -  2 ½  -  ½")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1.5  -  1,5  -  2 ½  -  ½",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1.5",
                    MedicationPlanDosageInstruction.DayTime.NOON to "1,5",
                    MedicationPlanDosageInstruction.DayTime.EVENING to "2 ½",
                    MedicationPlanDosageInstruction.DayTime.NIGHT to "½"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts 1 morning 1 night `() {
        val result = parseInstruction("1---1")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1---1",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.NIGHT to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts 1 morning 1 night 1 empty `() {
        val result = parseInstruction("1--0-1")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1--0-1",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                    MedicationPlanDosageInstruction.DayTime.NIGHT to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test 4 parts 2 noon `() {
        val result = parseInstruction("-2--0")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "-2--0",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.NOON to "2"
                )
            ),
            result
        )
    }

    @Test
    fun `test dj with angle brackets`() {
        val result = parseInstruction("< << DJ > >>")
        assertEquals(
            MedicationPlanDosageInstruction.External,
            result
        )
    }

    @Test
    fun `test with text`() {
        val result = parseInstruction("1-0-x-0")
        assertEquals(
            MedicationPlanDosageInstruction.Structured(
                text = "1-0-x-0",
                interpretation = mapOf(
                    MedicationPlanDosageInstruction.DayTime.MORNING to "1"
                )
            ),
            result
        )
    }

    @Test
    fun `test with all 0 should be empty`() {
        val result = parseInstruction("0-0-0")
        assertEquals(
            MedicationPlanDosageInstruction.Empty,
            result
        )
    }

    @Test
    fun `test with words`() {
        val result = parseInstruction("2 mal Mörgens")
        assertEquals(
            MedicationPlanDosageInstruction.FreeText(
                text = "2 mal Mörgens"
            ),
            result
        )
    }

    @Test
    fun `test dosage instruction with more than 4 parts should return Freetext`() {
        val result = parseInstruction("1-2-1-1-1-1-2-0")
        assertEquals(
            MedicationPlanDosageInstruction.FreeText(
                text = "1-2-1-1-1-1-2-0"
            ),
            result
        )
    }

    @Test
    fun `MedicationDosageEntityV1 to MedicationDosage`() {
        val dosageEntity = MedicationScheduleNotificationDosageEntityV1().apply {
            form = "tablet"
            ratio = "1"
        }
        val dosage = dosageEntity.toMedicationScheduleNotificationDosage()
        assertEquals("tablet", dosage.form)
        assertEquals("1", dosage.ratio)
    }

    @Test
    fun `RealmList of notificationEntities to list of Notifications`() {
        val now = Instant.parse("2024-01-01T08:00:00Z")
        val notificationEntity = MedicationScheduleNotificationEntityV1().apply {
            time = now.toLocalDateTime(TimeZone.of("Europe/Berlin")).time.toString()
            dosage = MedicationScheduleNotificationDosageEntityV1().apply {
                form = "tablet"
                ratio = "1"
            }
            id = "1234"
        }
        val realmList = listOf(notificationEntity).toRealmList()
        val notifications = realmList.map { it.toMedicationScheduleNotification() }
        assertEquals("09:00", notifications[0].time.toString())
        assertEquals(1, notifications.size)
        assertEquals("1234", notifications[0].id)
        assertEquals("tablet", notifications[0].dosage.form)
        assertEquals("1", notifications[0].dosage.ratio)
    }

    @Test
    fun `MedicationSchedule to MedicationScheduleEntityV1`() {
        val newSchedule = MEDICATION_SCHEDULE
        val scheduleEntity = newSchedule.toMedicationScheduleEntityV1()
        assertEquals("title", scheduleEntity.title)
        assertEquals("body", scheduleEntity.body)
        assertEquals("taskId", scheduleEntity.taskId)
        assertEquals(1, scheduleEntity.notifications.size)
        assertEquals("tablet", scheduleEntity.notifications[0].dosage?.form)
        assertEquals("1", scheduleEntity.notifications[0].dosage?.ratio)
    }

    @Test
    fun `MedicationScheduleEntityV1 to MedicationSchedule`() {
        val scheduleEntity = MedicationScheduleEntityV1().apply {
            val now = Instant.parse("2024-01-01T08:00:00Z")

            duration = MedicationScheduleDurationEntityV1()
            interval = MedicationScheduleIntervalEntityV1()
            isActive = true
            title = "title"
            body = "body"
            taskId = "taskId"
            profileId = "profileId"
            notifications = listOf(
                MedicationScheduleNotificationEntityV1().apply {
                    time = now.toLocalDateTime(TimeZone.of("Europe/Berlin")).time.toString()
                    dosage = MedicationScheduleNotificationDosageEntityV1().apply {
                        form = "tablet"
                        ratio = "1"
                    }
                    id = "1234"
                }
            ).toRealmList()
        }
        val schedule = scheduleEntity.toMedicationSchedule()
        assertEquals("title", schedule.message.title)
        assertEquals("body", schedule.message.body)
        assertEquals("taskId", schedule.taskId)
        assertEquals(1, schedule.notifications.size)
        assertEquals("tablet", schedule.notifications[0].dosage.form)
        assertEquals("1", schedule.notifications[0].dosage.ratio)
    }

    @Test
    fun `ScannedPrescription to MedicationSchedule`() {
        val scannedPrescription = PrescriptionData.Scanned(scannedTask)
        val schedule = scannedPrescription.toMedicationSchedule()
        assertEquals("Scanned Task", schedule.message.title)
        assertEquals("", schedule.message.body)
        assertEquals("active-scanned-task-id-1", schedule.taskId)
        assertEquals(0, schedule.notifications.size)
    }

    @Test
    fun `SyncedPrescription with freetext dosage instruction to MedicationSchedule`() {
        val syncedPrescription = PrescriptionData.Synced(syncedTask)
        val schedule = syncedPrescription.toMedicationSchedule()
        assertEquals("Medication", schedule.message.title)
        assertEquals("", schedule.message.body)
        assertEquals("active-synced-task-id-1", schedule.taskId)
        assertEquals(0, schedule.notifications.size)
    }

    @Test
    fun `SyncedPrescription with structured dosage instruction to MedicationSchedule`() {
        val syncedPrescription = PrescriptionData.Synced(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(
                    dosageInstruction = "1-0-1"
                )
            )
        )
        val schedule = syncedPrescription.toMedicationSchedule()
        assertEquals("Medication", schedule.message.title)
        assertEquals("", schedule.message.body)
        assertEquals("active-synced-task-id-1", schedule.taskId)
        assertEquals(2, schedule.notifications.size)
    }

    @Test
    fun `multiply medicationAmount with float string`() {
        val ratio = Ratio(
            numerator = Quantity(value = "0.5", unit = "mg"),
            denominator = Quantity(value = "1", unit = "")
        )
        val result = multiplyMedicationAmount(ratio, 2)
        assertNotNull(result)
        assertEquals("1", result?.numerator?.value)
    }

    @Test
    fun `multiply medicationAmount with int string`() {
        val ratio = Ratio(
            numerator = Quantity(value = "10", unit = "mg"),
            denominator = Quantity(value = "1", unit = "")
        )
        val result = multiplyMedicationAmount(ratio, 2)
        assertNotNull(result)
        assertEquals("20", result?.numerator?.value)
    }

    @Test
    fun `multiply medicationAmount with comma string`() {
        val ratio = Ratio(
            numerator = Quantity(value = "0,7", unit = "mg"),
            denominator = Quantity(value = "1", unit = "")
        )
        val result = multiplyMedicationAmount(ratio, 2)
        assertEquals("1.4", result?.numerator?.value)
    }

    @Test
    fun `multiply medicationAmount with null value`() {
        val result = multiplyMedicationAmount(null, 2)
        assert(result == null)
    }

    @Test
    fun `multiply medicationAmount with int Result`() {
        val ratio = Ratio(
            numerator = Quantity(value = "1.0", unit = "mg"),
            denominator = Quantity(value = "1", unit = "")
        )
        val result = multiplyMedicationAmount(ratio, 2)
        assertEquals("2", result?.numerator?.value)
    }

    @Test
    fun `get calculated end date with empty dosage instructions`() {
        val start = Instant.parse("2024-01-31T08:00:00Z")
        val schedule = MEDICATION_SCHEDULE.copy(
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = emptyList()
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(start.toLocalDate(), endDate)
    }

    @Test
    fun `get calculated end date with 2 Notifications`() {
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val notification1 = Instant.parse("2024-01-01T08:00:00Z")
        val notification2 = Instant.parse("2024-01-01T18:00:00Z")
        val expectedEndDate = start.plus(14.days)
        val schedule = MEDICATION_SCHEDULE.copy(
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = listOf(
                MedicationScheduleNotification(
                    time = notification1.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                ),
                MedicationScheduleNotification(
                    time = notification2.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                )
            )
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(expectedEndDate.toLocalDate(), endDate)
    }

    @Test
    fun `get calculated end date with Notifications and reminders every two days`() {
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val notification1 = Instant.parse("2024-01-01T08:00:00Z")
        val notification2 = Instant.parse("2024-01-01T18:00:00Z")
        val expectedEndDate = start.plus(28.days)
        val schedule = MEDICATION_SCHEDULE.copy(
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = listOf(
                MedicationScheduleNotification(
                    time = notification1.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                ),
                MedicationScheduleNotification(
                    time = notification2.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                )
            ),
            interval = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval.EveryTwoDays
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(expectedEndDate.toLocalDate(), endDate)
    }

    @Test
    fun `get calculated end date with two Notifications but half ratios`() {
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val notification1 = Instant.parse("2024-01-01T08:00:00Z")
        val notification2 = Instant.parse("2024-01-01T18:00:00Z")
        val expectedEndDate = start.plus(29.days)
        val schedule = MEDICATION_SCHEDULE.copy(
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = listOf(
                MedicationScheduleNotification(
                    time = notification1.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "0.5")
                ),
                MedicationScheduleNotification(
                    time = notification2.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "0.5")
                )
            )
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(expectedEndDate.toLocalDate(), endDate)
    }

    @Test
    fun `get calculated end date with two Notifications but start in the evening`() {
        val start = Instant.parse("2024-01-01T16:00:00Z")
        val notification1 = Instant.parse("2024-01-01T08:00:00Z")
        val notification2 = Instant.parse("2024-01-01T18:00:00Z")
        val expectedEndDate = start.plus(15.days)
        val schedule = MEDICATION_SCHEDULE.copy(
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = listOf(
                MedicationScheduleNotification(
                    time = notification1.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                ),
                MedicationScheduleNotification(
                    time = notification2.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                )
            )
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(expectedEndDate.toLocalDate(), endDate)
    }

    @Test
    fun `get calculated end date with two Notifications but only Mondays`() {
        val start = Instant.parse("2025-01-01T00:00:00Z")
        val notification1 = Instant.parse("2025-01-01T08:00:00Z")
        val notification2 = Instant.parse("2025-01-01T18:00:00Z")
        val expectedEndDate = Instant.parse("2025-04-14T00:00:00Z")
        val schedule = MEDICATION_SCHEDULE.copy(
            duration = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration.Personalized(
                startDate = start.toLocalDate(),
                endDate = start.toLocalDate()
            ),
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = listOf(
                MedicationScheduleNotification(
                    time = notification1.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                ),
                MedicationScheduleNotification(
                    time = notification2.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                )
            ),
            interval = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval.Personalized(selectedDays = setOf(DayOfWeek.MONDAY))
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(expectedEndDate.toLocalDate(), endDate)
    }

    @Test
    fun `get calculated end date with one Notification but only Tuesday and Thursday`() {
        val start = Instant.parse("2025-01-01T00:00:00Z")
        val notification1 = Instant.parse("2025-01-01T08:00:00Z")
        val expectedEndDate = Instant.parse("2025-04-15T00:00:00Z")
        val schedule = MEDICATION_SCHEDULE.copy(
            duration = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration.Personalized(
                startDate = start.toLocalDate(),
                endDate = start.toLocalDate()
            ),
            amount = Ratio(
                numerator = Quantity(value = "30", unit = "TAB"),
                denominator = Quantity(value = "1", unit = "TAB")
            ),
            notifications = listOf(
                MedicationScheduleNotification(
                    time = notification1.toLocalDateTime(TimeZone.currentSystemDefault()).time,
                    dosage = MedicationScheduleNotificationDosage("TAB", ratio = "1")
                )
            ),
            interval = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval.Personalized(
                selectedDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            )
        )
        val endDate = schedule.calculateEndOfPack(
            currentDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertEquals(expectedEndDate.toLocalDate(), endDate)
    }
}
