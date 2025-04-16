/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.medicationplan.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Stable
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.presentation.checkNotificationPermission
import de.gematik.ti.erp.app.medicationplan.usecase.LoadAllMedicationSchedulesUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.subDI
import org.kodein.di.instance
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private val LookaheadDuration = 12.hours
private val NotificationsInRangeDuration = 5.minutes
private const val REMINDER_NOTIFICATION_GROUP = "ReminderNotificationGroup"
private const val REMINDER_NOTIFICATION = "ShareNotificationAcceptRequest"
private val ReminderNotificationId = "ReminderNotificationId".hashCode()
const val REMINDER_NOTIFICATION_INTENT_ACTION = "de.gematik.erp.app.ReminderNotificationIntentAction"

/**
 * A CoroutineWorker that handles scheduling and showing medication reminders.
 *
 * @property di Dependency injection property.
 * @property loadAllMedicationSchedulesUseCase Use case to load all medication schedules.
 */
class PlanMedicationScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), DIAware {
    override val di by context.subDI(closestDI()) {}
    private val loadAllMedicationSchedulesUseCase by instance<LoadAllMedicationSchedulesUseCase>()

    /**
     * Performs the work to schedule and show medication reminders.
     * It loads all medication schedules and checks for notifications in the range of NotificationsInRangeDuration.
     * If there are notifications, it cancels the previous reminder notification and shows the new one.
     * If there are no notifications, it schedules the next reminder worker with LookaheadDuration.
     *
     * @return Listenable Result of the work.
     */

    override suspend fun doWork(): Result =
        runCatching {
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val schedules = loadAllMedicationSchedulesUseCase().first()

            val currentNotifications = schedules.nextNotificationsInRange(
                (now - NotificationsInRangeDuration).toLocalDateTime(timeZone)..(now + NotificationsInRangeDuration).toLocalDateTime(timeZone)
            )

            val nextNotifications = schedules.nextNotificationsInRange(
                (now + NotificationsInRangeDuration).toLocalDateTime(timeZone)..(now + LookaheadDuration).toLocalDateTime(timeZone)
            )

            if (nextNotifications.isEmpty()) {
                applicationContext.scheduleReminderWorker(LookaheadDuration)
            } else {
                applicationContext.scheduleReminderWorker(
                    nextNotifications.first().notificationDateTime.toInstant(
                        TimeZone.currentSystemDefault()
                    ) - now
                )
            }

            if (currentNotifications.isNotEmpty()) {
                applicationContext.cancelReminderNotification()
                applicationContext.showReminderNotification()
            }
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Napier.e(it) { "Reminder worker failed" }
                Result.failure()
            }
        )

    companion object {
        const val TAG = "PlanMedicationScheduleWorker"
    }
}

/**
 * Data class representing a notification with its schedule and date-time.
 *
 * @property schedule The medication schedule.
 * @property notification The medication notification.
 * @property notificationDateTime The date and time of the notification.
 */
@Stable
data class DateTimeNotification(
    val schedule: MedicationSchedule,
    val notification: MedicationNotification,
    val notificationDateTime: LocalDateTime
)

/**
 * Extension function to get the next notifications from a list of scheduled medications in a given range.
 *
 * @param range The range of LocalDateTime to check for notifications.
 * @return List of DateTimeNotification within the range.
 */
fun List<MedicationSchedule>.nextNotificationsInRange(range: ClosedRange<LocalDateTime>): List<DateTimeNotification> =
    asSequence()
        .filter { it.isActive && it.notifications.isNotEmpty() }
        .notificationsInRange(range)
        .sortedBy { (_, _, dateTime) -> dateTime }
        .toList()

/**
 * Extension function to get notifications in a given range from a sequence of MedicationSchedule.
 *
 * @param range The range of LocalDateTime to check for notifications.
 * @return Sequence of DateTimeNotification within the range.
 */
fun Sequence<MedicationSchedule>.notificationsInRange(
    range: ClosedRange<LocalDateTime>
): Sequence<DateTimeNotification> =
    flatMap { schedule -> schedule.notificationsInRange(range) }

/**
 * Extension function to get notifications in a given range from a MedicationSchedule.
 *
 * @param range The range of LocalDateTime to check for notifications.
 * @return Sequence of DateTimeNotification within the range.
 */
fun MedicationSchedule.notificationsInRange(
    range: ClosedRange<LocalDateTime>
): Sequence<DateTimeNotification> {
    val startDate = range.start.date
    val endDate = range.endInclusive.date

    return sequence {
        var date = startDate
        do {
            this@notificationsInRange.notifications.forEach {
                yield(DateTimeNotification(this@notificationsInRange, it, it.time.atDate(date)))
            }
            date = date.plus(1, DateTimeUnit.DAY)
        } while (date <= endDate)
    }.filter { (_, _, dateTime) -> dateTime in range }
}

/**
 * Extension function to cancel the reminder notification.
 */
fun Context.cancelReminderNotification() {
    NotificationManagerCompat.from(this).cancel(ReminderNotificationId)
}

/**
 * Extension function to schedule a reminder worker with a given duration.
 *
 * @param duration The duration to delay the worker.
 */
fun Context.scheduleReminderWorker(
    duration: Duration
) {
    try {
        val request =
            OneTimeWorkRequest
                .Builder(PlanMedicationScheduleWorker::class.java)
                .setInitialDelay(duration.toJavaDuration())
                .addTag(PlanMedicationScheduleWorker.TAG)
                .build()

        WorkManager
            .getInstance(this)
            .apply {
                cancelAllWorkByTag(PlanMedicationScheduleWorker.TAG)
                enqueue(request)
            }
    } catch (e: Throwable) {
        // silent fail, ignore
    }
}

/**
 * Extension function to create a notification reminder group.
 */
fun Context.createNotificationReminderGroup() {
    val group =
        NotificationChannelGroupCompat
            .Builder(REMINDER_NOTIFICATION_GROUP)
            .setName(R.string.notification_header.toString())
            .build()

    NotificationManagerCompat.from(this).createNotificationChannelGroup(group)
}

/**
 * Extension function to create a notification reminder channel.
 */
fun Context.createNotificationReminderChannel() {
    val channel =
        NotificationChannelCompat.Builder(
            REMINDER_NOTIFICATION,
            NotificationManagerCompat.IMPORTANCE_MAX
        )
            .setName(R.string.notification_header.toString())
            .setGroup(REMINDER_NOTIFICATION_GROUP)
            .build()

    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}

/**
 * Extension function to show a reminder notification with a list of notifications.
 * (shows the notifications which was planned in NotificationsInRangeDuration)
 *
 * @param notifications The list of DateTimeNotification to show.
 */
@Requirement(
    "O.Plat_4#6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Reminder notifications do not include any sensitive data, only static text.",
    codeLines = 50
)
fun Context.showReminderNotification() {
    val openAppIntent =
        openAppIntent(
            action = REMINDER_NOTIFICATION_INTENT_ACTION,
            extras = Bundle.EMPTY
        )

    val notification =
        NotificationCompat.Builder(this, REMINDER_NOTIFICATION)
            .setSmallIcon(R.drawable.ic_logo_outlined)
            .setContentTitle(getString(R.string.notification_header))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_logo_outlined, getString(R.string.notification_action_open_app), openAppIntent)
            .build()
    @Requirement(
        "O.Plat_5#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Check for notification permission and notify if granted.",
        codeLines = 50
    )
    checkNotificationPermission(
        onGranted = {
            NotificationManagerCompat.from(this).notify(ReminderNotificationId, notification)
        },
        onDenied = {}
    )
}

/**
 * Extension function to create an intent to open the app.
 *
 * @param action The action for the intent.
 * @param extras The extras to add to the intent.
 * @return PendingIntent to open the app.
 */
fun Context.openAppIntent(
    action: String,
    extras: Bundle
): PendingIntent {
    val intent =
        Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            this.action = action
            this.putExtras(extras)
        }
    return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}
