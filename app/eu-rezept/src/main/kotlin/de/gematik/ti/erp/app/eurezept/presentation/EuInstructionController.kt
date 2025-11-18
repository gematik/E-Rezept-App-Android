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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.TriggerNavigationUseCase
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.model.CountryPhrases
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetPrescriptionPhrasesUseCase
import de.gematik.ti.erp.app.eurezept.ui.model.EuRedeemSelector.WAS_EU_REDEEM_INSTRUCTION_VIEWED
import de.gematik.ti.erp.app.localization.CountryCode
import de.gematik.ti.erp.app.localization.GetSupportedCountriesFromXmlUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

private const val INSTRUCTIONS_WEBSITE_URL =
    "https://www.eu-patienten.de/de/behandlung_ausland/verordnungen_einloesen_eu/verordnungen_einloesen_eu.jsp"

internal class EuInstructionController(
    private val getSupportedCountriesFromXmlUseCase: GetSupportedCountriesFromXmlUseCase,
    private val getPrescriptionPhrasesUseCase: GetPrescriptionPhrasesUseCase,
    private val triggerNavigationUseCase: TriggerNavigationUseCase,
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

    private val _supportedCountries by lazy {
        getSupportedCountriesFromXmlUseCase()
    }

    val onBiometricAuthenticationSuccessForSubmitEvent = ComposableEvent<Unit>()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { reason ->
            if (reason == AuthReason.SUBMIT) {
                onBiometricAuthenticationSuccessForSubmitEvent.trigger()
            }
        }
    }

    /**
     * Marks the EU redemption instructions as viewed.
     *
     * Use case must be called to ensure proper one-time navigation behavior:
     * 1. [triggerNavigationUseCase] - Sets the trigger flag to indicate instructions were viewed
     *
     */
    fun markInstructionsViewed() {
        controllerScope.launch {
            triggerNavigationUseCase.invoke(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        }
    }

    fun findSupportedCountryByCode(code: String?): CountryCode? {
        return if (!code.isNullOrBlank()) {
            _supportedCountries.firstOrNull {
                it.code.equals(code.trim(), ignoreCase = true)
            }
        } else {
            null
        }
    }

    fun getPhrasesForCountry(country: Country?): CountryPhrases {
        val countryCode = country?.code
        val supportedCountryCode = findSupportedCountryByCode(countryCode)

        return getPrescriptionPhrasesUseCase(supportedCountryCode ?: CountryCode.UK)
    }

    fun openInstructionsWebsite(context: Context) {
        runCatching {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                INSTRUCTIONS_WEBSITE_URL.toUri()
            )
            context.startActivity(intent)
        }.onFailure { e ->
            Napier.e("Error opening instructions website", e)
        }
    }
}

@Composable
internal fun rememberEuInstructionController(): EuInstructionController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val triggerNavigationUseCase by rememberInstance<TriggerNavigationUseCase>()

    val getSupportedCountriesFromXmlUseCase by rememberInstance<GetSupportedCountriesFromXmlUseCase>()
    val getPrescriptionPhrasesUseCase by rememberInstance<GetPrescriptionPhrasesUseCase>()

    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()

    return remember {
        EuInstructionController(
            getSupportedCountriesFromXmlUseCase = getSupportedCountriesFromXmlUseCase,
            getPrescriptionPhrasesUseCase = getPrescriptionPhrasesUseCase,
            triggerNavigationUseCase = triggerNavigationUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator
        )
    }
}
