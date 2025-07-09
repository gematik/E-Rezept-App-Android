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

package de.gematik.ti.erp.app.medicationplan.repository

import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationDosageEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationNotificationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleEntityV1
import de.gematik.ti.erp.app.db.writeOrCopyToRealm
import de.gematik.ti.erp.app.medicationplan.model.MedicationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotificationMessage
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.prescription.repository.toRatio
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

private const val QUERY_TASK_ID = "taskId = $0"

class MedicationPlanLocalDataSource(
    private val realm: Realm
) {
    fun loadMedicationSchedule(taskId: String): Flow<MedicationSchedule?> =
        realm.query<MedicationScheduleEntityV1>(
            QUERY_TASK_ID,
            taskId
        ).asFlow().map {
            it.list.firstOrNull()?.toMedicationSchedule()
        }

    suspend fun updateMedicationSchedule(medicationSchedule: MedicationSchedule) {
        realm.writeOrCopyToRealm(
            ::MedicationScheduleEntityV1,
            QUERY_TASK_ID,
            medicationSchedule.taskId
        ) { entity ->
            entity.profileId = medicationSchedule.profileId
            entity.start = medicationSchedule.start.toString()
            entity.isActive = medicationSchedule.isActive
            entity.end = medicationSchedule.end.toString()
            entity.title = medicationSchedule.message.title
            entity.body = medicationSchedule.message.body
            entity.taskId = medicationSchedule.taskId
            entity.amount = medicationSchedule.amount?.toRatioEntity()
            entity.notifications = medicationSchedule.notifications.toNotificationsEntity()
        }
    }

    fun loadAllMedicationSchedules(): Flow<List<MedicationSchedule>> =
        realm.query<MedicationScheduleEntityV1>().asFlow().map {
            it.list.map { schedule ->
                schedule.toMedicationSchedule()
            }
        }
}

private fun MedicationScheduleEntityV1.toMedicationSchedule() =
    MedicationSchedule(
        taskId = taskId,
        profileId = profileId,
        start = LocalDate.parse(start),
        end = LocalDate.parse(end),
        amount = amount.toRatio(),
        isActive = isActive,
        message = MedicationNotificationMessage(
            title = title,
            body = body
        ),
        notifications = notifications.toNotifications()
    )

private fun List<MedicationNotification>.toNotificationsEntity(): RealmList<MedicationNotificationEntityV1> =
    this.sortedBy { it.time }.map { notification ->
        MedicationNotificationEntityV1().apply {
            this.id = notification.id
            this.time = notification.time.toString()
            this.dosage = MedicationDosageEntityV1().apply {
                this.form = notification.dosage.form
                this.ratio = notification.dosage.ratio
            }
        }
    }.toRealmList()

private fun RealmList<MedicationNotificationEntityV1>.toNotifications(): List<MedicationNotification> =
    this.map { notification ->
        MedicationNotification(
            id = notification.id,
            time = LocalTime.parse(notification.time),
            dosage = MedicationDosage(
                form = notification.dosage?.form ?: "",
                ratio = notification.dosage?.ratio ?: ""
            )
        )
    }
