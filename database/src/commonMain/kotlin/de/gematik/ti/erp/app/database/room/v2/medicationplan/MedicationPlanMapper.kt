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

package de.gematik.ti.erp.app.database.room.v2.medicationplan

import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpQuantityEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpRatioEmbeddable
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotificationMessageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

// Ratio mappings
fun RatioErpModel.toRatioEmbeddable(): ErpRatioEmbeddable = ErpRatioEmbeddable(
    numerator = ErpQuantityEmbeddable(numerator?.value, numerator?.unit),
    denominator = ErpQuantityEmbeddable(denominator?.value, denominator?.unit)
)

fun ErpRatioEmbeddable?.toRatioErpModel(): RatioErpModel? = this?.let {
    RatioErpModel(
        numerator = QuantityErpModel(it.numerator.value ?: "", it.numerator.unit ?: ""),
        denominator = QuantityErpModel(it.denominator.value ?: "", it.denominator.unit ?: "")
    )
}

// Data -> Entity
fun MedicationScheduleErpModel.toMedicationScheduleEntity(): MedicationScheduleEntity =
    MedicationScheduleEntity(
        medicationPlanId = taskId,
        taskId = taskId,
        amount = amount?.toRatioEmbeddable(),
        isActive = isActive,
        profileId = profileId,
        duration = duration.toMedicationScheduleDurationEntity(),
        interval = interval.toMedicationScheduleIntervalEntity(),
        title = message.title,
        body = message.body
    )

fun MedicationScheduleDurationErpModel.toMedicationScheduleDurationEntity(): MedicationScheduleDurationEntity =
    MedicationScheduleDurationEntity(
        startDate = this.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
        endDate = this.endDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
        type = when (this) {
            is MedicationScheduleDurationErpModel.Endless -> MedicationScheduleDurationType.ENDLESS
            is MedicationScheduleDurationErpModel.EndOfPack -> MedicationScheduleDurationType.END_OF_PACK
            is MedicationScheduleDurationErpModel.Personalized -> MedicationScheduleDurationType.PERSONALIZED
        }
    )

fun MedicationScheduleIntervalErpModel.toMedicationScheduleIntervalEntity(): MedicationScheduleIntervalEntity =
    when (this) {
        is MedicationScheduleIntervalErpModel.Daily -> MedicationScheduleIntervalEntity(
            type = MedicationScheduleIntervalType.DAILY,
            selectedDaysStrings = emptySet()
        )
        is MedicationScheduleIntervalErpModel.EveryTwoDays -> MedicationScheduleIntervalEntity(
            type = MedicationScheduleIntervalType.EVERY_TWO_DAYS,
            selectedDaysStrings = emptySet()
        )
        is MedicationScheduleIntervalErpModel.Personalized -> MedicationScheduleIntervalEntity(
            type = MedicationScheduleIntervalType.PERSONALIZED,
            selectedDaysStrings = this.selectedDays.map { it.value }.toSet()
        )
    }

fun MedicationScheduleNotificationErpModel.toMedicationScheduleNotificationEntity(taskId: String): MedicationScheduleNotificationEntity =
    MedicationScheduleNotificationEntity(
        id = this.id,
        taskId = taskId,
        time = this.time,
        dosage = this.dosage.toMedicationScheduleNotificationDosageEntity()
    )

fun MedicationScheduleNotificationDosageErpModel.toMedicationScheduleNotificationDosageEntity(): MedicationScheduleNotificationDosageEntity =
    MedicationScheduleNotificationDosageEntity(
        form = this.form,
        ratio = this.ratio
    )

// Entity -> Data
fun MedicationScheduleWithNotifications.toErpModel(): MedicationScheduleErpModel =
    MedicationScheduleErpModel(
        isActive = schedule.isActive,
        profileId = schedule.profileId,
        taskId = schedule.taskId,
        amount = schedule.amount.toRatioErpModel() ?: RatioErpModel(QuantityErpModel("", ""), QuantityErpModel("", "")),
        interval = schedule.interval?.toMedicationScheduleInterval() ?: MedicationScheduleIntervalErpModel.Daily,
        duration = schedule.duration?.toMedicationScheduleDuration() ?: MedicationScheduleDurationErpModel.Endless(),
        message = MedicationNotificationMessageErpModel(
            title = schedule.title,
            body = schedule.body
        ),
        notifications = notifications.map { it.toErpModel() }.sortedBy { it.time }
    )

fun MedicationScheduleDurationEntity.toMedicationScheduleDuration(): MedicationScheduleDurationErpModel {
    val parsedStartDate = this.startDate.toLocalDate()
    val parsedEndDate = this.endDate.toLocalDate()
    return when (this.type) {
        MedicationScheduleDurationType.ENDLESS -> MedicationScheduleDurationErpModel.Endless(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
        MedicationScheduleDurationType.END_OF_PACK -> MedicationScheduleDurationErpModel.EndOfPack(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
        MedicationScheduleDurationType.PERSONALIZED -> MedicationScheduleDurationErpModel.Personalized(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
    }
}

fun MedicationScheduleIntervalEntity.toMedicationScheduleInterval(): MedicationScheduleIntervalErpModel =
    when (this.type) {
        MedicationScheduleIntervalType.DAILY -> MedicationScheduleIntervalErpModel.Daily
        MedicationScheduleIntervalType.EVERY_TWO_DAYS -> MedicationScheduleIntervalErpModel.EveryTwoDays
        MedicationScheduleIntervalType.PERSONALIZED -> MedicationScheduleIntervalErpModel.Personalized(
            selectedDays = this.selectedDaysStrings.map { DayOfWeek(it) }.toSet()
        )
    }

fun MedicationScheduleNotificationEntity.toErpModel(): MedicationScheduleNotificationErpModel =
    MedicationScheduleNotificationErpModel(
        id = this.id,
        time = this.time,
        dosage = this.dosage?.toErpModel() ?: MedicationScheduleNotificationDosageErpModel("", "")
    )

fun MedicationScheduleNotificationDosageEntity.toErpModel(): MedicationScheduleNotificationDosageErpModel =
    MedicationScheduleNotificationDosageErpModel(
        form = this.form,
        ratio = this.ratio
    )
