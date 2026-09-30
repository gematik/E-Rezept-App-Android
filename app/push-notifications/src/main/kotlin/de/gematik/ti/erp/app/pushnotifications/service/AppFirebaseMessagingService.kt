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

package de.gematik.ti.erp.app.pushnotifications.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoError
import de.gematik.ti.erp.app.pushnotifications.domain.model.IncomingPushNotification
import de.gematik.ti.erp.app.pushnotifications.domain.model.IncomingPushNotificationMapper
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationContent
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DecryptPushNotificationUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.InitializeDebugPushKeyChainUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.UpdateFcmTokenUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.concurrent.atomic.AtomicInteger

class AppFirebaseMessagingService : FirebaseMessagingService(), DIAware {

    override val di by closestDI()

    private val decryptPushNotificationUseCase: DecryptPushNotificationUseCase by instance()
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase by instance()
    private val initializeDebugPushKeyChainUseCase: InitializeDebugPushKeyChainUseCase by instance()
    private val incomingPushNotificationMapper: IncomingPushNotificationMapper by instance()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = checkNotNull(getSystemService()) { "NotificationManager not available" }
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.push_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.push_notification_channel_description)
                enableVibration(true)
                enableLights(true)
            }
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                DEBUG_CHANNEL_ID,
                "Raw Push Debug",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Shows raw JSON payloads of incoming FCM pushes"
                enableVibration(true)
                enableLights(true)
            }
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            updateFcmTokenUseCase(token)
                .onFailure { error -> Napier.e("Failed to update rotated FCM token", error) }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val incoming = incomingPushNotificationMapper.parse(message.data)

        val typeLabel = when (incoming) {
            is IncomingPushNotification.Encrypted -> "Encrypted"
            is IncomingPushNotification.Rejected -> if (message.data.isNotEmpty()) "Plain/Other" else "Rejected"
            else -> "Unknown"
        }

        Napier.d(
            message = "Incoming push received (Type: $typeLabel) — message: ${message.data}",
            tag = "Push"
        )

        showDebugNotificationIfEnabled(typeLabel, message.data.toString())

        when (incoming) {
            is IncomingPushNotification.Encrypted -> handleEncryptedNotification(incoming)
            is IncomingPushNotification.Rejected -> Napier.w(incoming.reason, tag = "Push")
        }
    }

    private fun showDebugNotificationIfEnabled(typeLabel: String, rawData: String) {
        val prefs = applicationContext.getSharedPreferences(
            "debug_push_notifications_prefs_sp",
            MODE_PRIVATE
        )
        val showRaw = prefs.getBoolean("show_raw_push_notification", false)
        Napier.d("Debug notification flag is set to: $showRaw", tag = "Push")
        if (!showRaw) return

        val notificationId = notificationIdCounter.incrementAndGet()

        // 1. Copy Action (BroadcastReceiver)
        val copyIntent = Intent(applicationContext, DebugPushClipboardReceiver::class.java).apply {
            putExtra("clip_text", rawData)
        }
        val copyPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Main Content Intent (Activity)
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            launchIntent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, DEBUG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("RAW Push ($typeLabel)")
            .setContentText("Payload: $rawData")
            .setStyle(NotificationCompat.BigTextStyle().bigText(rawData))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Copy Payload", copyPendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun handleEncryptedNotification(message: IncomingPushNotification.Encrypted) {
        runBlocking(Dispatchers.IO) {
            try {
                initializeDebugPushKeyChainUseCase(requiredKeyIdentifier = message.keyIdentifier)
                val payload = decryptPushNotificationUseCase(
                    message.ciphertext,
                    message.timeMessageEncrypted,
                    message.keyIdentifier
                ).getOrThrow()

                Napier.d("Decrypted push payload: channelId=${payload.channelId}, identifier=${payload.identifier}, identifierType=${payload.identifierType}")

                val content = incomingPushNotificationMapper.mapDecrypted(payload)
                if (content != null) {
                    showNotification(content)
                } else {
                    Napier.d("Push notification ignored for no-op channel: ${payload.channelId}")
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: PushNotificationCryptoError.UnknownKeyIdentifier) {
                // Key material is unknown or was cleared (e.g. profile was deleted locally while remote deregistration failed).
                // deregistration failed). Drop the push silently instead of showing a fallback for a deleted profile.
                Napier.w("Dropping push notification for unknown key_identifier: ${error.message}")
            } catch (error: PushNotificationCryptoError) {
                Napier.w("Failed to decrypt push notification: ${error.message}")
                // Show a generic notification so the user knows something arrived.
                showNotification(incomingPushNotificationMapper.fallback())
            } catch (error: Exception) {
                Napier.e("Unexpected error in push notification handler — dropping message.", error)
                showNotification(incomingPushNotificationMapper.fallback())
            }
        }
    }

    private fun showNotification(content: PushNotificationContent) {
        val notificationId = notificationIdCounter.incrementAndGet()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            launchIntent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(getString(content.title))
            .setContentText(getString(content.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        content.groupKey?.let { groupKey ->
            notificationBuilder.setGroup(groupKey)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        const val CHANNEL_ID = "fcm_push_channel"
        const val DEBUG_CHANNEL_ID = "raw_push_message_channel"

        private val notificationIdCounter = AtomicInteger(0)
    }
}
