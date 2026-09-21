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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class MedicationPlanNotificationScheduler(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMedicationSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {
        val localDateNow = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (medicationScheduleErpModel.shouldBeScheduled(localDateNow)) {
            scheduleNotification(medicationScheduleErpModel)
        } else {
            cancelNotification(medicationScheduleErpModel.taskId)
        }
    }

    private fun scheduleNotification(medicationScheduleErpModel: MedicationScheduleErpModel) {
        val nextNotificationTime = medicationScheduleErpModel.calculateNextNotificationTime()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextNotificationTime,
                    createSchedulingPendingIntent(medicationScheduleErpModel)
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextNotificationTime,
                    createSchedulingPendingIntent(medicationScheduleErpModel)
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextNotificationTime,
                createSchedulingPendingIntent(medicationScheduleErpModel)
            )
        }
    }

    fun cancelNotification(taskId: String) {
        alarmManager.cancel(
            createCancelingPendingIntent(taskId)
        )
    }

    private fun createSchedulingPendingIntent(medicationScheduleErpModel: MedicationScheduleErpModel): PendingIntent {
        val intent = Intent(context, MedicationPlanNotificationReceiver::class.java)
        val medicationScheduleJson = Json.encodeToString(medicationScheduleErpModel)
        intent.putExtra("medicationScheduleJson", medicationScheduleJson)
        return PendingIntent.getBroadcast(
            context,
            medicationScheduleErpModel.taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createCancelingPendingIntent(taskId: String): PendingIntent {
        val intent = Intent(context, MedicationPlanNotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
