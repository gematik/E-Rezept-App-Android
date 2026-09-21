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

package de.gematik.ti.erp.app.database.realm.v1.medicationplan

import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.task.entity.toRatioEntity
import de.gematik.ti.erp.app.database.realm.v1.task.entity.toRatioErpModel
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleDurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleDurationTypeV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleIntervalEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleIntervalTypeV1
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotificationMessageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.ext.toRealmSet
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

val MORNING_HOUR = LocalTime.parse("08:00")
val NOON_HOUR = LocalTime.parse("12:00")
val EVENING_HOUR = LocalTime.parse("18:00")
val NIGHT_HOUR = LocalTime.parse("20:00")

// Data -> Entity
fun MedicationScheduleErpModel.toMedicationScheduleEntityV1(): MedicationScheduleEntityV1 =
    MedicationScheduleEntityV1().apply {
        taskId = this@toMedicationScheduleEntityV1.taskId
        amount = this@toMedicationScheduleEntityV1.amount?.toRatioEntity()
        isActive = this@toMedicationScheduleEntityV1.isActive
        profileId = this@toMedicationScheduleEntityV1.profileId
        duration = this@toMedicationScheduleEntityV1.duration.toMedicationScheduleDurationEntityV1()
        interval = this@toMedicationScheduleEntityV1.interval.toMedicationScheduleIntervalEntityV1()
        title = this@toMedicationScheduleEntityV1.message.title
        body = this@toMedicationScheduleEntityV1.message.body
        notifications = this@toMedicationScheduleEntityV1.notifications.map { it.toMedicationScheduleNotificationEntityV1() }.toRealmList()
    }
fun MedicationScheduleDurationErpModel.toMedicationScheduleDurationEntityV1(): MedicationScheduleDurationEntityV1 =
    MedicationScheduleDurationEntityV1().apply {
        startDate = this@toMedicationScheduleDurationEntityV1.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toRealmInstant()
        endDate = this@toMedicationScheduleDurationEntityV1.endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toRealmInstant()
        type = when (this@toMedicationScheduleDurationEntityV1) {
            is MedicationScheduleDurationErpModel.Endless -> MedicationScheduleDurationTypeV1.ENDLESS
            is MedicationScheduleDurationErpModel.EndOfPack -> MedicationScheduleDurationTypeV1.END_OF_PACK
            is MedicationScheduleDurationErpModel.Personalized -> MedicationScheduleDurationTypeV1.PERSONALIZED
        }
    }

fun MedicationScheduleIntervalErpModel.toMedicationScheduleIntervalEntityV1(): MedicationScheduleIntervalEntityV1 =
    MedicationScheduleIntervalEntityV1().apply {
        when (this@toMedicationScheduleIntervalEntityV1) {
            is MedicationScheduleIntervalErpModel.Daily -> {
                this.type = MedicationScheduleIntervalTypeV1.DAILY
                this.selectedDaysStrings = realmSetOf()
            }
            is MedicationScheduleIntervalErpModel.EveryTwoDays -> {
                this.type = MedicationScheduleIntervalTypeV1.EVERY_TWO_DAYS
                this.selectedDaysStrings = realmSetOf()
            }
            is MedicationScheduleIntervalErpModel.Personalized -> {
                this.type = MedicationScheduleIntervalTypeV1.PERSONALIZED
                this.selectedDaysStrings = this@toMedicationScheduleIntervalEntityV1.selectedDays.map { it.value }.toRealmSet()
            }
        }
    }

fun MedicationScheduleNotificationErpModel.toMedicationScheduleNotificationEntityV1(): MedicationScheduleNotificationEntityV1 =
    MedicationScheduleNotificationEntityV1().apply {
        id = this@toMedicationScheduleNotificationEntityV1.id
        time = this@toMedicationScheduleNotificationEntityV1.time.toString()
        dosage = this@toMedicationScheduleNotificationEntityV1.dosage.toMedicationScheduleNotificationDosageEntityV1()
    }

fun MedicationScheduleNotificationDosageErpModel.toMedicationScheduleNotificationDosageEntityV1(): MedicationScheduleNotificationDosageEntityV1 =
    MedicationScheduleNotificationDosageEntityV1().apply {
        this.form = this@toMedicationScheduleNotificationDosageEntityV1.form
        this.ratio = this@toMedicationScheduleNotificationDosageEntityV1.ratio
    }

// Entity -> Data
fun MedicationScheduleEntityV1.toMedicationSchedule() =
    MedicationScheduleErpModel(
        isActive = this.isActive,
        profileId = this.profileId,
        taskId = this.taskId,
        amount = this.amount.toRatioErpModel() ?: RatioErpModel(QuantityErpModel("", ""), QuantityErpModel("", "")),
        interval = this.interval?.toMedicationScheduleInterval() ?: MedicationScheduleIntervalErpModel.Daily,
        duration = this.duration?.toMedicationScheduleDuration() ?: MedicationScheduleDurationErpModel.Endless(),
        message = MedicationNotificationMessageErpModel(
            title = this.title,
            body = this.body
        ),
        notifications = this.notifications.map { it.toMedicationScheduleNotification() }.sortedBy { it.time }
    )

fun MedicationScheduleDurationEntityV1.toMedicationScheduleDuration(): MedicationScheduleDurationErpModel {
    val parsedStartDate = this.startDate.toInstant().toLocalDate()
    val parsedEndDate = this.endDate.toInstant().toLocalDate()
    return when (this.type) {
        MedicationScheduleDurationTypeV1.ENDLESS -> MedicationScheduleDurationErpModel.Endless(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
        MedicationScheduleDurationTypeV1.END_OF_PACK -> MedicationScheduleDurationErpModel.EndOfPack(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
        MedicationScheduleDurationTypeV1.PERSONALIZED -> MedicationScheduleDurationErpModel.Personalized(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
    }
}

fun MedicationScheduleIntervalEntityV1.toMedicationScheduleInterval(): MedicationScheduleIntervalErpModel {
    return when (this.type) {
        MedicationScheduleIntervalTypeV1.DAILY -> MedicationScheduleIntervalErpModel.Daily
        MedicationScheduleIntervalTypeV1.EVERY_TWO_DAYS -> MedicationScheduleIntervalErpModel.EveryTwoDays
        MedicationScheduleIntervalTypeV1.PERSONALIZED -> MedicationScheduleIntervalErpModel.Personalized(
            selectedDays = this.selectedDaysStrings.map { DayOfWeek(it) }.toSet()
        )
    }
}

fun MedicationScheduleNotificationEntityV1.toMedicationScheduleNotification(): MedicationScheduleNotificationErpModel =
    MedicationScheduleNotificationErpModel(
        time = LocalTime.parse(this.time),
        dosage = this.dosage?.toMedicationScheduleNotificationDosage() ?: MedicationScheduleNotificationDosageErpModel("", ""),
        id = this.id
    )

fun MedicationScheduleNotificationDosageEntityV1.toMedicationScheduleNotificationDosage(): MedicationScheduleNotificationDosageErpModel =
    MedicationScheduleNotificationDosageErpModel(
        form = this.form,
        ratio = this.ratio
    )
