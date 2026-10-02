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
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainAdvancer
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetPusherChannelsUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetPushersUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.InitializeDebugPushKeyChainUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.RegisterPushNotificationsForProfileUseCase
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.provider.FirebaseProjectIdProvider
import de.gematik.ti.erp.app.pushnotifications.provider.PushApplicationIdProvider
import de.gematik.ti.erp.app.pushnotifications.provider.PushGatewayUrlProvider
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kodein.di.compose.rememberInstance
import kotlin.coroutines.cancellation.CancellationException

class DebugPushNotificationsViewModel(
    private val fcmTokenProvider: FcmTokenProvider,
    firebaseProjectIdProvider: FirebaseProjectIdProvider,
    pushApplicationIdProvider: PushApplicationIdProvider,
    private val cryptoService: PushNotificationCryptoService,
    private val keyChainAdvancer: PushKeyChainAdvancer,
    private val localDataSource: DebugPushNotificationsLocalDataSource,
    private val initializeDebugPushKeyChainUseCase: InitializeDebugPushKeyChainUseCase,
    private val encryptDebugPushNotificationPayloadUseCase: EncryptDebugPushNotificationPayloadUseCase,
    private val sendFcmMessageUseCase: SendFcmMessageUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getPusherChannelsUseCase: GetPusherChannelsUseCase,
    private val getPushersUseCase: GetPushersUseCase,
    private val registerPushNotificationsForProfileUseCase: RegisterPushNotificationsForProfileUseCase,
    private val pushGatewayUrlProvider: PushGatewayUrlProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val firebaseProjectId: String = firebaseProjectIdProvider.getProjectId()
    private val pushApplicationId: String = pushApplicationIdProvider.getPushApplicationId()
    private val json = Json { encodeDefaults = true }

    private val _latestKeyGeneration = MutableStateFlow<PushNotificationKeyGeneration?>(null)
    val latestKeyGeneration: StateFlow<PushNotificationKeyGeneration?> = _latestKeyGeneration.asStateFlow()

    private val _oauthToken = MutableStateFlow("")
    val oauthToken: StateFlow<String> = _oauthToken.asStateFlow()

    private val _failPushGatewayTest = MutableStateFlow(false)
    val failPushGatewayTest: StateFlow<Boolean> = _failPushGatewayTest.asStateFlow()

    private val _showRawPushNotification = MutableStateFlow(false)
    val showRawPushNotification: StateFlow<Boolean> = _showRawPushNotification.asStateFlow()

    private val _selectedGatewayUrl = MutableStateFlow(pushGatewayUrlProvider.getPushGatewayUrl())
    val selectedGatewayUrl: StateFlow<String> = _selectedGatewayUrl.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            initializeDebugPushKeyChainUseCase()
            updateLatestKeyGeneration()
        }
        viewModelScope.launch {
            localDataSource.oauthToken().collect { token ->
                _oauthToken.value = token
            }
        }
        viewModelScope.launch {
            localDataSource.isFailPushGatewayTest().collect { fail ->
                _failPushGatewayTest.value = fail
            }
        }
        viewModelScope.launch {
            localDataSource.isShowRawPushNotification().collect { show ->
                _showRawPushNotification.value = show
            }
        }
        viewModelScope.launch {
            localDataSource.pushGatewayUrl().collect { url ->
                val activeUrl = url.ifBlank { pushGatewayUrlProvider.getPushGatewayUrl() }
                _selectedGatewayUrl.value = activeUrl
                updateEncryptedCurlCommands(_fcmToken.value.orEmpty())
            }
        }
    }

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()
    val pushErrorEvent = ComposableEvent<String>()
    val pushSuccessEvent = ComposableEvent<String>()

    private val _channels = MutableStateFlow<UiState<List<PushChannel>>>(UiState.Empty())
    val channels: StateFlow<UiState<List<PushChannel>>> = _channels.asStateFlow()

    private val _pushers = MutableStateFlow<UiState<List<Pusher>>>(UiState.Empty())
    val pushers: StateFlow<UiState<List<Pusher>>> = _pushers.asStateFlow()

    private val _pusherRegistration = MutableStateFlow<UiState<Unit>>(UiState.Empty())
    val pusherRegistration: StateFlow<UiState<Unit>> = _pusherRegistration.asStateFlow()

    private var fetchFcmTokenJob: Job? = null

    fun fetchFcmToken() {
        if (_fcmToken.value != null) return
        if (fetchFcmTokenJob?.isActive == true) return
        fetchFcmTokenJob = viewModelScope.launch {
            runCatching { fcmTokenProvider.getToken() }
                .onSuccess { _fcmToken.value = it }
                .onFailure {
                    Napier.e("Failed to fetch FCM token", it)
                    pushErrorEvent.trigger("Failed to fetch FCM token: ${it.message}")
                }
        }
    }

    fun refreshChannels() {
        launchUiStateRequest(
            state = _channels,
            errorMessage = "Failed to load push channel status"
        ) {
            val activeProfile = getActiveProfileUseCase().first()
            getPusherChannelsUseCase(activeProfile.id).getOrThrow()
        }
    }

    fun refreshPushers() {
        launchUiStateRequest(
            state = _pushers,
            errorMessage = "Failed to load pushers"
        ) {
            val activeProfile = getActiveProfileUseCase().first()
            getPushersUseCase(activeProfile.id).getOrThrow()
        }
    }

    fun refreshStatus() {
        refreshPushers()
        refreshChannels()
    }

    fun registerPusher() {
        launchUiStateRequest(
            state = _pusherRegistration,
            errorMessage = "Failed to register pusher",
            onSuccess = ::refreshStatus
        ) {
            val activeProfile = getActiveProfileUseCase().first()
            registerPushNotificationsForProfileUseCase(
                activeProfile.id,
                ProfilePushNotificationSettings()
            ).getOrThrow()
        }
    }

    private fun <T> launchUiStateRequest(
        state: MutableStateFlow<UiState<T>>,
        errorMessage: String,
        onSuccess: () -> Unit = {},
        request: suspend () -> T
    ) {
        if (state.value.isLoading) return

        state.value = UiState.Loading()
        viewModelScope.launch(dispatcher) {
            try {
                state.value = UiState.Data(request())
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Napier.e(errorMessage, error)
                state.value = UiState.Error(error)
            }
        }
    }

    fun updateOauthToken(token: String) {
        _oauthToken.value = token
        viewModelScope.launch { localDataSource.saveOauthToken(token) }
    }

    fun updateFailPushGatewayTest(fail: Boolean) {
        _failPushGatewayTest.value = fail
        viewModelScope.launch { localDataSource.saveFailPushGatewayTest(fail) }
    }

    // Encrypted push state
    private val _cipherOutput = MutableStateFlow("")
    val cipherOutput: StateFlow<String> = _cipherOutput.asStateFlow()

    private val _decryptedOutput = MutableStateFlow("")
    val decryptedOutput: StateFlow<String> = _decryptedOutput.asStateFlow()

    private val _encryptedCurlCommand = MutableStateFlow("")
    val encryptedCurlCommand: StateFlow<String> = _encryptedCurlCommand.asStateFlow()

    private val _encryptedPushGatewayCurlCommand = MutableStateFlow("")
    val encryptedPushGatewayCurlCommand: StateFlow<String> = _encryptedPushGatewayCurlCommand.asStateFlow()

    private val _isEncryptedSending = MutableStateFlow(false)
    val isEncryptedSending: StateFlow<Boolean> = _isEncryptedSending.asStateFlow()

    fun encryptPayload(plaintext: String, targetToken: String) {
        viewModelScope.launch {
            withContext(dispatcher) { initializeDebugPushKeyChainUseCase() }
            val encryptedPayload = encryptDebugPushNotificationPayloadUseCase(plaintext.toByteArray(Charsets.UTF_8))
            val cipher = Base64.encodeToString(encryptedPayload.ciphertext, Base64.NO_WRAP)
            val gen = encryptedPayload.keyGeneration

            _cipherOutput.value = cipher
            _decryptedOutput.value = ""
            _latestKeyGeneration.value = gen

            updateEncryptedCurlCommands(targetToken)
        }
    }

    fun updateEncryptedCurlCommands(targetToken: String) {
        val cipher = _cipherOutput.value.takeIf(String::isNotBlank) ?: return
        val generation = _latestKeyGeneration.value ?: return
        val fcmToken = targetToken.ifBlank { "YOUR_FCM_TOKEN" }

        _encryptedCurlCommand.value = DebugPushNotificationCurlCommands.buildEncryptedCurlCommand(
            projectId = firebaseProjectId,
            oauthToken = _oauthToken.value.ifBlank { "YOUR_OAUTH2_TOKEN" },
            fcmToken = fcmToken,
            ciphertext = cipher,
            month = generation.month,
            keyIdentifier = generation.keyIdentifier
        )
        _encryptedPushGatewayCurlCommand.value = DebugPushNotificationCurlCommands.buildEncryptedPushGatewayCurlCommand(
            applicationId = pushApplicationId,
            fcmToken = fcmToken,
            ciphertext = cipher,
            month = generation.month,
            keyIdentifier = generation.keyIdentifier,
            gatewayUrl = _selectedGatewayUrl.value
        )
    }

    fun updatePushGatewayUrl(url: String) {
        _selectedGatewayUrl.value = url
        viewModelScope.launch {
            localDataSource.savePushGatewayUrl(url)
            updateEncryptedCurlCommands(_fcmToken.value.orEmpty())
        }
    }

    fun setFailPushGatewayTest(fail: Boolean) {
        _failPushGatewayTest.value = fail
        viewModelScope.launch {
            localDataSource.saveFailPushGatewayTest(fail)
        }
    }

    fun setShowRawPushNotification(show: Boolean) {
        _showRawPushNotification.value = show
        viewModelScope.launch {
            localDataSource.saveShowRawPushNotification(show)
        }
    }

    fun decryptPayload() {
        val cipher = _cipherOutput.value
        viewModelScope.launch {
            try {
                val bytes = Base64.decode(cipher, Base64.NO_WRAP)
                val generation = withContext(dispatcher) { latestGeneration() }
                if (generation == null) {
                    _decryptedOutput.value = "Error: No debug key chain available"
                    return@launch
                }
                _decryptedOutput.value = withContext(dispatcher) {
                    String(
                        cryptoService.decryptForMonth(bytes, generation.month, generation.keyIdentifier),
                        Charsets.UTF_8
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _decryptedOutput.value = "Error: ${error.message}"
            }
        }
    }

    fun sendEncryptedPush(targetToken: String) {
        val cipher = _cipherOutput.value
        val oauth = _oauthToken.value
        viewModelScope.launch {
            withContext(dispatcher) { initializeDebugPushKeyChainUseCase() }
            val generation = withContext(dispatcher) { latestGeneration() } ?: return@launch
            _isEncryptedSending.value = true
            try {
                sendFcmMessageUseCase.invoke(
                    projectId = firebaseProjectId,
                    oauthToken = oauth,
                    json = buildEncryptedPayloadJson(
                        fcmToken = targetToken,
                        ciphertext = cipher,
                        timeMessageEncrypted = generation.month,
                        keyIdentifier = generation.keyIdentifier
                    )
                )
                pushSuccessEvent.trigger("Encrypted push sent to FCM.")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                pushErrorEvent.trigger("Failed: ${error.message}")
            } finally {
                _isEncryptedSending.value = false
            }
        }
    }

    private suspend fun updateLatestKeyGeneration() {
        _latestKeyGeneration.value = latestGeneration()
    }

    private suspend fun latestGeneration(): PushNotificationKeyGeneration? {
        val keyId = keyChainAdvancer.knownKeyIdentifiers().firstOrNull() ?: return null
        return keyChainAdvancer.getLatestGeneration(keyId)
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

@Composable
fun debugPushNotificationsViewModel(): DebugPushNotificationsViewModel {
    val fcmTokenProvider by rememberInstance<FcmTokenProvider>()
    val firebaseProjectIdProvider by rememberInstance<FirebaseProjectIdProvider>()
    val pushApplicationIdProvider by rememberInstance<PushApplicationIdProvider>()
    val cryptoService by rememberInstance<PushNotificationCryptoService>()
    val keyChainAdvancer by rememberInstance<PushKeyChainAdvancer>()
    val localDataSource by rememberInstance<DebugPushNotificationsLocalDataSource>()
    val initializeDebugPushKeyChainUseCase by rememberInstance<InitializeDebugPushKeyChainUseCase>()
    val encryptDebugPushNotificationPayloadUseCase by rememberInstance<EncryptDebugPushNotificationPayloadUseCase>()
    val sendFcmMessageUseCase by rememberInstance<SendFcmMessageUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getPusherChannelsUseCase by rememberInstance<GetPusherChannelsUseCase>()
    val getPushersUseCase by rememberInstance<GetPushersUseCase>()
    val registerPushNotificationsForProfileUseCase by rememberInstance<RegisterPushNotificationsForProfileUseCase>()
    val pushGatewayUrlProvider by rememberInstance<PushGatewayUrlProvider>()
    val dispatcher by rememberInstance<CoroutineDispatcher>()

    return remember {
        DebugPushNotificationsViewModel(
            fcmTokenProvider = fcmTokenProvider,
            firebaseProjectIdProvider = firebaseProjectIdProvider,
            pushApplicationIdProvider = pushApplicationIdProvider,
            cryptoService = cryptoService,
            keyChainAdvancer = keyChainAdvancer,
            localDataSource = localDataSource,
            initializeDebugPushKeyChainUseCase = initializeDebugPushKeyChainUseCase,
            encryptDebugPushNotificationPayloadUseCase = encryptDebugPushNotificationPayloadUseCase,
            sendFcmMessageUseCase = sendFcmMessageUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            getPusherChannelsUseCase = getPusherChannelsUseCase,
            getPushersUseCase = getPushersUseCase,
            registerPushNotificationsForProfileUseCase = registerPushNotificationsForProfileUseCase,
            pushGatewayUrlProvider = pushGatewayUrlProvider,
            dispatcher = dispatcher
        )
    }
}
