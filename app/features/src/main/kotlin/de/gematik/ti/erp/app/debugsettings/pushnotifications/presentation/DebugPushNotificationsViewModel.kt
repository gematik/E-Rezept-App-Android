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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.debugsettings.pushnotifications.presentation

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.debugsettings.pushnotifications.datasource.DebugPushNotificationsLocalDataSource
import de.gematik.ti.erp.app.debugsettings.pushnotifications.usecase.EncryptDebugPushNotificationPayloadUseCase
import de.gematik.ti.erp.app.debugsettings.pushnotifications.usecase.SendFcmMessageUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyRotationService
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetFcmTokenUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetFirebaseProjectIdUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kodein.di.compose.rememberInstance
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DebugPushNotificationsViewModel(
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    getFirebaseProjectIdUseCase: GetFirebaseProjectIdUseCase,
    private val cryptoService: PushNotificationCryptoService,
    private val keyRotationService: PushNotificationKeyRotationService,
    private val localDataSource: DebugPushNotificationsLocalDataSource,
    private val encryptDebugPushNotificationPayloadUseCase: EncryptDebugPushNotificationPayloadUseCase,
    private val sendFcmMessageUseCase: SendFcmMessageUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val fcmProjectId: String = getFirebaseProjectIdUseCase()
    private val json = Json { encodeDefaults = true }

    private val currentMonth: String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    private val _latestKeyGeneration = MutableStateFlow<PushNotificationKeyGeneration?>(null)
    val latestKeyGeneration: StateFlow<PushNotificationKeyGeneration?> = _latestKeyGeneration.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            keyRotationService.advanceToMonth(currentMonth)
            updateLatestKeyGeneration()
        }
        viewModelScope.launch {
            localDataSource.oauthToken().collect { token ->
                _oauthToken.value = token
            }
        }
    }

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private var fetchFcmTokenJob: Job? = null

    fun fetchFcmToken() {
        if (_fcmToken.value != null) return
        if (fetchFcmTokenJob?.isActive == true) return
        fetchFcmTokenJob = viewModelScope.launch {
            runCatching { getFcmTokenUseCase() }
                .onSuccess { _fcmToken.value = it }
                .onFailure {
                    Napier.e("Failed to fetch FCM token", it)
                    fcmErrorEvent.trigger("Failed to fetch FCM token: ${it.message}")
                }
        }
    }

    private val _oauthToken = MutableStateFlow("")
    val oauthToken: StateFlow<String> = _oauthToken.asStateFlow()

    fun updateOauthToken(token: String) {
        _oauthToken.value = token
        viewModelScope.launch { localDataSource.saveOauthToken(token) }
    }

    // Encrypted push state
    private val _cipherOutput = MutableStateFlow("")
    val cipherOutput: StateFlow<String> = _cipherOutput.asStateFlow()

    private val _decryptedOutput = MutableStateFlow("")
    val decryptedOutput: StateFlow<String> = _decryptedOutput.asStateFlow()

    private val _encryptedCurlCommand = MutableStateFlow("")
    val encryptedCurlCommand: StateFlow<String> = _encryptedCurlCommand.asStateFlow()

    private val _isEncryptedSending = MutableStateFlow(false)
    val isEncryptedSending: StateFlow<Boolean> = _isEncryptedSending.asStateFlow()

    val encryptedErrorEvent = ComposableEvent<String>()

    fun encryptPayload(plaintext: String, targetToken: String) {
        viewModelScope.launch {
            val encryptedPayload = encryptDebugPushNotificationPayloadUseCase(plaintext.toByteArray(Charsets.UTF_8))
            val cipher = Base64.encodeToString(encryptedPayload.ciphertext, Base64.NO_WRAP)
            val gen = encryptedPayload.keyGeneration

            _cipherOutput.value = cipher
            _decryptedOutput.value = ""
            _latestKeyGeneration.value = gen

            val oauth = _oauthToken.value
            _encryptedCurlCommand.value = buildEncryptedCurlCommand(
                projectId = fcmProjectId,
                oauthToken = oauth.ifBlank { "YOUR_OAUTH2_TOKEN" },
                fcmToken = targetToken.ifBlank { "YOUR_FCM_TOKEN" },
                ciphertext = cipher,
                month = gen.month,
                keyIdentifier = keyRotationService.keyIdentifier
            )
        }
    }

    fun decryptPayload() {
        val cipher = _cipherOutput.value
        viewModelScope.launch {
            _decryptedOutput.value = try {
                val bytes = Base64.decode(cipher, Base64.NO_WRAP)
                withContext(dispatcher) {
                    String(cryptoService.decrypt(bytes), Charsets.UTF_8)
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    fun sendEncryptedPush(targetToken: String) {
        val cipher = _cipherOutput.value
        val oauth = _oauthToken.value
        viewModelScope.launch {
            val month = withContext(dispatcher) { keyRotationService.getLatestGeneration()?.month } ?: return@launch
            _isEncryptedSending.value = true
            try {
                runCatching {
                    sendFcmMessageUseCase.invoke(
                        projectId = fcmProjectId,
                        oauthToken = oauth,
                        json = buildEncryptedPayloadJson(
                            fcmToken = targetToken,
                            ciphertext = cipher,
                            timeMessageEncrypted = month,
                            keyIdentifier = keyRotationService.keyIdentifier
                        )
                    )
                }.onFailure { encryptedErrorEvent.trigger("Failed: ${it.message}") }
            } finally {
                _isEncryptedSending.value = false
            }
        }
    }

    // Plain FCM push state
    private val _isFcmSending = MutableStateFlow(false)
    val isFcmSending: StateFlow<Boolean> = _isFcmSending.asStateFlow()

    val fcmErrorEvent = ComposableEvent<String>()

    fun sendFcmPush(targetToken: String, title: String, body: String) {
        val oauth = _oauthToken.value
        _isFcmSending.value = true
        viewModelScope.launch {
            try {
                runCatching {
                    sendFcmMessageUseCase.invoke(
                        projectId = fcmProjectId,
                        oauthToken = oauth,
                        json = buildPlainPayloadJson(
                            fcmToken = targetToken,
                            title = title,
                            body = body
                        )
                    )
                }.onFailure {
                    fcmErrorEvent.trigger("Failed: ${it.message}")
                    Napier.e { "Failed: ${it.message}" }
                }
            } finally {
                _isFcmSending.value = false
            }
        }
    }

    private suspend fun updateLatestKeyGeneration() {
        _latestKeyGeneration.value = keyRotationService.getLatestGeneration()
    }

    private fun buildEncryptedPayloadJson(
        fcmToken: String,
        ciphertext: String,
        timeMessageEncrypted: String,
        keyIdentifier: String
    ): String = json.encodeToString(
        FcmMessagePayload(
            message = EncryptedFcmMessage(
                token = fcmToken,
                data = EncryptedData(
                    ciphertext = ciphertext,
                    timeMessageEncrypted = timeMessageEncrypted,
                    keyIdentifier = keyIdentifier
                )
            )
        )
    )

    private fun buildPlainPayloadJson(
        fcmToken: String,
        title: String,
        body: String
    ): String = json.encodeToString(
        FcmMessagePayload(
            message = PlainFcmMessage(
                token = fcmToken,
                notification = FcmNotification(title = title, body = body)
            )
        )
    )

    private fun buildEncryptedCurlCommand(
        projectId: String,
        oauthToken: String,
        fcmToken: String,
        ciphertext: String,
        month: String,
        keyIdentifier: String
    ) = """
curl -X POST '${SendFcmMessageUseCase.FCM_BASE_URL}v1/projects/$projectId/messages:send' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer $oauthToken' \
  -d '{
  "message": {
    "token": "$fcmToken",
    "android": {
      "priority": "HIGH"
    },
    "data": {
      "ciphertext": "$ciphertext",
      "time_message_encrypted": "$month",
      "key_identifier": "$keyIdentifier"
    }
  }
}'
    """.trimIndent()
}

@Serializable
private data class FcmMessagePayload<T>(val message: T)

@Serializable
private data class EncryptedFcmMessage(
    val token: String,
    val android: FcmAndroidConfig = FcmAndroidConfig(),
    val data: EncryptedData
)

@Serializable
private data class FcmAndroidConfig(val priority: String = "HIGH")

@Serializable
private data class EncryptedData(
    val ciphertext: String,
    @SerialName("time_message_encrypted") val timeMessageEncrypted: String,
    @SerialName("key_identifier") val keyIdentifier: String
)

@Serializable
private data class PlainFcmMessage(
    val token: String,
    val notification: FcmNotification
)

@Serializable
private data class FcmNotification(val title: String, val body: String)

@Composable
fun debugPushNotificationsViewModel(): DebugPushNotificationsViewModel {
    val getFcmTokenUseCase by rememberInstance<GetFcmTokenUseCase>()
    val getFirebaseProjectIdUseCase by rememberInstance<GetFirebaseProjectIdUseCase>()
    val cryptoService by rememberInstance<PushNotificationCryptoService>()
    val keyRotationService by rememberInstance<PushNotificationKeyRotationService>()
    val localDataSource by rememberInstance<DebugPushNotificationsLocalDataSource>()
    val encryptDebugPushNotificationPayloadUseCase by rememberInstance<EncryptDebugPushNotificationPayloadUseCase>()
    val sendFcmMessageUseCase by rememberInstance<SendFcmMessageUseCase>()
    val dispatcher by rememberInstance<CoroutineDispatcher>()

    return remember {
        DebugPushNotificationsViewModel(
            getFcmTokenUseCase = getFcmTokenUseCase,
            getFirebaseProjectIdUseCase = getFirebaseProjectIdUseCase,
            cryptoService = cryptoService,
            keyRotationService = keyRotationService,
            localDataSource = localDataSource,
            encryptDebugPushNotificationPayloadUseCase = encryptDebugPushNotificationPayloadUseCase,
            sendFcmMessageUseCase = sendFcmMessageUseCase,
            dispatcher = dispatcher
        )
    }
}
