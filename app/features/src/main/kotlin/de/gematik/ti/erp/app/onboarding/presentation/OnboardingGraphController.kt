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

package de.gematik.ti.erp.app.onboarding.presentation

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.analytics.usecase.StartTrackerUseCase
import de.gematik.ti.erp.app.analytics.usecase.StopTrackerUseCase
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthScenario
import de.gematik.ti.erp.app.onboarding.usecase.DetermineAuthScenarioUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveOnboardingDataUseCase
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

private const val PASSWORD_SCORE = 9
private const val ONBOARDING_FIRST_STEP = 1
private const val ONBOARDING_LAST_STEP = 3

@Requirement(
    "O.Data_1#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Viewmodel that is shared during the onboarding process to provide the user permissions.",
    codeLines = 10
)
class OnboardingGraphController(
    private val allowScreenshotsUseCase: AllowScreenshotsUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val saveOnboardingDataUseCase: SaveOnboardingDataUseCase,
    private val startTrackerUseCase: StartTrackerUseCase,
    private val stopTrackerUseCase: StopTrackerUseCase,
    private val determineAuthScenarioUseCase: DetermineAuthScenarioUseCase,
    private val resources: Resources
) : Controller() {
    private val skipOnboardingWithAuthenticationPassword = SettingsData.Authentication(
        deviceSecurity = false,
        failedAuthenticationAttempts = 0,
        password = SettingsData.Authentication.Password("password"),
        authenticationTimeOutSystemUptime = null
    )

    private val authentication = MutableStateFlow<SettingsData.Authentication?>(null)

    private val profileName = MutableStateFlow(resources.getString(R.string.onboarding_default_profile_name))

    private val _authScenario = mutableStateOf(determineAuthScenarioUseCase())

    private val _currentStep = MutableStateFlow(ONBOARDING_FIRST_STEP)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    fun nextStep() {
        _currentStep.update { minOf(ONBOARDING_LAST_STEP, _currentStep.value + 1) }
    }

    private val isAnalyticsAllowed by lazy {
        isAnalyticsAllowedUseCase.invoke()
    }

    val authScenario: OnboardingAuthScenario
        get() = _authScenario.value

    fun refreshAuthScenario() {
        _authScenario.value = determineAuthScenarioUseCase()
    }

    val isAnalyticsAllowedState
        @Composable
        get() = isAnalyticsAllowed.collectAsStateWithLifecycle(
            initialValue = false,
            minActiveState = Lifecycle.State.RESUMED
        )

    fun onChooseAuthentication(authentication: SettingsData.Authentication) {
        this.authentication.update { authentication }
    }

    fun createProfile() = controllerScope.launch {
        letNotNull(
            authentication.value,
            profileName.value
        ) { auth, name ->
            saveOnboardingDataUseCase(
                authentication = auth,
                profileName = name
            )
        }
    }

    @Requirement(
        "O.Data_1#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Permissions are asked and obtained during the onboarding process.",
        codeLines = 5
    )
    fun allowScreenshots(allow: Boolean) {
        controllerScope.launch {
            allowScreenshotsUseCase(allow)
        }
    }

    @Requirement(
        "O.Data_1#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Permissions are asked and obtained during the onboarding process.",
        codeLines = 5
    )
    fun changeAnalyticsState(state: Boolean) {
        controllerScope.launch {
            changeAnalyticsStateUseCase.invoke(state)
            when {
                state -> startTrackerUseCase()
                else -> stopTrackerUseCase()
            }
        }
    }

    fun createProfileOnSkipOnboarding() {
        changeAnalyticsState(false)
        this.authentication.value = skipOnboardingWithAuthenticationPassword
        this.profileName.value = resources.getString(R.string.onboarding_default_profile_name)
        controllerScope.launch {
            val profileList = getProfilesUseCase.invoke().firstOrNull()
            // avoid creating new profiles when it is already existing
            if (profileList?.isEmpty() == true) {
                createProfile()
            }
        }
    }
}

@Composable
fun rememberOnboardingController(): OnboardingGraphController {
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val saveOnboardingSucceededUseCase by rememberInstance<SaveOnboardingDataUseCase>()
    val allowScreenshotsUseCase by rememberInstance<AllowScreenshotsUseCase>()
    val isAnalyticsAllowedUseCase by rememberInstance<IsAnalyticsAllowedUseCase>()
    val changeAnalyticsStateUseCase by rememberInstance<ChangeAnalyticsStateUseCase>()
    val startTrackerUseCase by rememberInstance<StartTrackerUseCase>()
    val stopTrackerUseCase by rememberInstance<StopTrackerUseCase>()
    val determineAuthScenarioUseCase by rememberInstance<DetermineAuthScenarioUseCase>()
    val resources by rememberInstance<Resources>()

    return remember {
        OnboardingGraphController(
            getProfilesUseCase = getProfilesUseCase,
            saveOnboardingDataUseCase = saveOnboardingSucceededUseCase,
            allowScreenshotsUseCase = allowScreenshotsUseCase,
            isAnalyticsAllowedUseCase = isAnalyticsAllowedUseCase,
            changeAnalyticsStateUseCase = changeAnalyticsStateUseCase,
            startTrackerUseCase = startTrackerUseCase,
            stopTrackerUseCase = stopTrackerUseCase,
            determineAuthScenarioUseCase = determineAuthScenarioUseCase,
            resources = resources
        )
    }
}
