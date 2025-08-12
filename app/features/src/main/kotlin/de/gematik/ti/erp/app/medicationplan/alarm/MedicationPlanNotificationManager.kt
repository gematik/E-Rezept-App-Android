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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getString
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.presentation.checkNotificationPermission

private const val REMINDER_NOTIFICATION_CHANNEL = "MedicationPlanNotificationChannel"
const val REMINDER_NOTIFICATION_INTENT_ACTION = "de.gematik.erp.app.ReminderNotificationIntentAction"
private val ReminderNotificationId = "ReminderNotificationId".hashCode()

class MedicationPlanNotificationManager(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification() {
        val channel = createNotificationChannel()
        notificationManager.createNotificationChannel(channel)
        val notification = buildNotification()
        @Requirement(
            "O.Plat_5#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Check for notification permission and notify if granted.",
            codeLines = 50
        )
        context.checkNotificationPermission(
            onGranted = {
                NotificationManagerCompat.from(context).notify(ReminderNotificationId, notification)
            },
            onDenied = {}
        )
    }

    private fun createNotificationChannel(): NotificationChannel =
        NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL,
            context.getString(R.string.notification_header),
            NotificationManager.IMPORTANCE_HIGH
        )

    @Requirement(
        "O.Plat_4#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Reminder notifications do not include any sensitive data, only static text.",
        codeLines = 50
    )
    private fun buildNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.action = REMINDER_NOTIFICATION_INTENT_ACTION
        }
        val openAppIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo_outlined)
            .setContentTitle(context.getString(R.string.notification_header))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_logo_outlined, context.getString(R.string.notification_action_open_app), openAppIntent)
            .build()
    }
}
