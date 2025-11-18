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

package de.gematik.ti.erp.app.eurezept.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.eurezept.domain.model.CountrySpecificLabels
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetCountryLocaleRedemptionCodeUseCase
import de.gematik.ti.erp.app.eurezept.util.TextToSpeechManager
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.compose.rememberInstance
import java.util.Locale

const val TIME_TO_RE_EMIT = 5L
private const val TTS_NORMAL_SPEECH_RATE = 1.0f

internal class EuRedemptionCodeController(
    private val textToSpeechManager: TextToSpeechManager,
    selectedCountryCode: String,
    getCountryLocaleRedemptionCodeUseCase: GetCountryLocaleRedemptionCodeUseCase,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {

    val onBiometricAuthenticationSuccessForSubmitEvent = ComposableEvent<Unit>()

    private val _isQrCodeVisible = MutableStateFlow(false)
    val isQrCodeVisible: StateFlow<Boolean> = _isQrCodeVisible.asStateFlow()

    private val _countrySpecificLabels = MutableStateFlow(
        getCountryLocaleRedemptionCodeUseCase(selectedCountryCode)
    )
    val countrySpecificLabels: StateFlow<CountrySpecificLabels> = _countrySpecificLabels.asStateFlow()

    private val ttsLocale: Locale = getCountryLocaleRedemptionCodeUseCase.getLocaleForTTS(selectedCountryCode)

    init {
        textToSpeechManager.initialize()

        biometricAuthenticationSuccessEvent.listen(controllerScope) { scope ->
            if (scope == AuthReason.SUBMIT) {
                onBiometricAuthenticationSuccessForSubmitEvent.trigger()
            }
        }
    }

    fun onPlayInsuranceNumberAudio(codeData: EuRedemptionDetails?) {
        codeData?.let {
            val label = _countrySpecificLabels.value.insuranceNumberLabel

            // Speak label in country-specific language
            textToSpeechManager.speakWithLocale(
                text = label,
                locale = ttsLocale,
                speechRate = 1.0f,
                utteranceId = "insurance_label",
                addPauses = false,
                spellOutCharacters = false
            )

            // Speak insurance number in country-specific language
            textToSpeechManager.speakQueuedWithLocale(
                text = it.insuranceNumber,
                locale = ttsLocale,
                speechRate = TTS_NORMAL_SPEECH_RATE,
                utteranceId = "insurance_number",
                addPauses = true,
                spellOutCharacters = true
            )
        }
    }

    fun onPlayCodeAudio(codeData: EuRedemptionDetails?) {
        codeData?.let {
            val label = _countrySpecificLabels.value.codeLabel

            // Speak label in country-specific language
            textToSpeechManager.speakWithLocale(
                text = label,
                locale = ttsLocale,
                speechRate = 1.0f,
                utteranceId = "code_label",
                addPauses = false,
                spellOutCharacters = false
            )

            // Speak access code in country-specific language
            textToSpeechManager.speakQueuedWithLocale(
                text = it.euAccessCode.accessCode,
                locale = ttsLocale,
                speechRate = TTS_NORMAL_SPEECH_RATE,
                utteranceId = "redemption_code",
                addPauses = true,
                spellOutCharacters = true
            )
        }
    }

    fun toggleQrCodeView() {
        _isQrCodeVisible.update { !it }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechManager.cleanup()
    }
}

@Composable
internal fun rememberEuRedemptionCodeController(selectedCountryCode: String): EuRedemptionCodeController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val context = LocalContext.current

    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getCountryLocaleRedemptionCodeUseCase by rememberInstance<GetCountryLocaleRedemptionCodeUseCase>()

    return remember {
        EuRedemptionCodeController(
            selectedCountryCode = selectedCountryCode,
            textToSpeechManager = TextToSpeechManager(context),
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator,
            getCountryLocaleRedemptionCodeUseCase = getCountryLocaleRedemptionCodeUseCase
        )
    }
}
