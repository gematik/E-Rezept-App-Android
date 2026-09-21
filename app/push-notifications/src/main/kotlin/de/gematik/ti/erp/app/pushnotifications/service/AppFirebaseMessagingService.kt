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
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.pushnotifications.BuildConfig
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoError
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DecryptPushNotificationUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.concurrent.atomic.AtomicInteger

class AppFirebaseMessagingService : FirebaseMessagingService(), DIAware {

    override val di by closestDI()

    private val decryptPushNotificationUseCase: DecryptPushNotificationUseCase by instance()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = checkNotNull(getSystemService()) { "NotificationManager not available" }
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "E-Rezept Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Push notifications for E-Rezept"
                enableVibration(true)
                enableLights(true)
            }
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Handle FCM token rotation by updating the stored registration and updating the Fachdienst with the new FCM token when push backend is ready.
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Napier.d("Message received — notification: ${message.notification?.title}, data keys: ${message.data.keys}")

        val data = message.data
        val ciphertext = data[FCM_KEY_CIPHERTEXT]
        val timeEncrypted = data[FCM_KEY_TIME_ENCRYPTED]
        val keyIdentifier = data[FCM_KEY_IDENTIFIER]

        if (ciphertext != null && timeEncrypted != null) {
            if (keyIdentifier == null) {
                Napier.w("Encrypted push missing $FCM_KEY_IDENTIFIER — rejecting message.")
                return
            }
            handleEncryptedNotification(ciphertext, timeEncrypted, keyIdentifier)
        } else {
            val title = message.notification?.title ?: data[FCM_KEY_TITLE] ?: "E-Rezept"
            val body = message.notification?.body ?: data[FCM_KEY_BODY] ?: ""
            if (title.isNotBlank() || body.isNotBlank()) showNotification(title, body)
        }
    }

    private fun handleEncryptedNotification(
        ciphertext: String,
        timeMessageEncrypted: String,
        keyIdentifier: String
    ) {
        serviceScope.launch {
            runCatching {
                val payload = decryptPushNotificationUseCase(ciphertext, timeMessageEncrypted, keyIdentifier)
                    .getOrThrow()

                if (BuildKonfig.INTERNAL && BuildConfig.DEBUG) {
                    Napier.d("Decrypted push payload: ${payload.rawPayload}")
                }

                // TODO: Map all ChannelIds for navigation when backend is ready.
                val title = when (payload.channelId) {
                    CHANNEL_ID_NEW_PRESCRIPTION -> "Neues Rezept"
                    else -> "E-Rezept"
                }
                val body = if (payload.identifier != null) {
                    "${payload.channelId}: ${payload.identifier}: ${payload.identifierType}"
                } else {
                    payload.channelId ?: payload.rawPayload
                }

                showNotification(title, body)
            }.onFailure { e ->
                when (e) {
                    is PushNotificationCryptoError -> Napier.w("Failed to decrypt push notification: ${e.message}")
                    else ->
                        Napier.e("Unexpected error in push notification handler — dropping message.", e)
                }
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        val notificationId = notificationIdCounter.incrementAndGet()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            launchIntent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "fcm_push_channel"

        // FCM data payload keys
        const val FCM_KEY_CIPHERTEXT = "ciphertext"
        const val FCM_KEY_TIME_ENCRYPTED = "time_message_encrypted"
        const val FCM_KEY_IDENTIFIER = "key_identifier"
        const val FCM_KEY_TITLE = "title"
        const val FCM_KEY_BODY = "body"

        // Known ChannelId values from the decrypted payload
        const val CHANNEL_ID_NEW_PRESCRIPTION = "erp.task.activate"

        private val notificationIdCounter = AtomicInteger(0)
    }
}
