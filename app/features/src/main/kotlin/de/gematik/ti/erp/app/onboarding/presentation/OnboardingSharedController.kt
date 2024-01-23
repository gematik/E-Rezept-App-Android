/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod.Companion.toAuthenticationMode
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod.None
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveOnboardingDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

private const val PASSWORD_SCORE = 9

class OnboardingSharedController(
    private val allowScreenshotsUseCase: AllowScreenshotsUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val saveOnboardingDataUseCase: SaveOnboardingDataUseCase,
    private val defaultProfileName: String,
    private val scope: CoroutineScope
) {
    private val mutableSecureAppMethod = MutableStateFlow<OnboardingSecureAppMethod>(None)

    private val skipPasswordAppMethod = OnboardingSecureAppMethod.Password("a", "a", PASSWORD_SCORE)

    private val isAnalyticsAllowed by lazy {
        isAnalyticsAllowedUseCase.invoke()
    }

    val isAnalyticsAllowedState
        @Composable
        get() = isAnalyticsAllowed.collectAsStateWithLifecycle(
            initialValue = false,
            minActiveState = Lifecycle.State.RESUMED
        )

    val secureAppMethod
        @Composable
        get() = mutableSecureAppMethod.collectAsStateWithLifecycle()

    fun updateAuthenticationMode(mode: OnboardingSecureAppMethod) {
        mutableSecureAppMethod.value = mode
    }

    fun onSaveOnboardingData(
        authenticationMode: SettingsData.AuthenticationMode,
        profileName: String
    ) = scope.launch {
        saveOnboardingDataUseCase(
            authenticationMode = authenticationMode,
            profileName = profileName
        )
    }

    fun allowScreenshots(allow: Boolean) {
        scope.launch {
            allowScreenshotsUseCase(allow)
        }
    }

    fun changeAnalyticsState(state: Boolean) {
        scope.launch {
            changeAnalyticsStateUseCase.invoke(state)
        }
    }

    fun createProfileOnSkipOnboarding() {
        changeAnalyticsState(false)
        updateAuthenticationMode(skipPasswordAppMethod)
        scope.launch {
            val profileList = getProfilesUseCase.invoke().first()
            if (profileList.isEmpty()) {
                // avoid creating new profiles when it is already existing
                onSaveOnboardingData(
                    authenticationMode = skipPasswordAppMethod.toAuthenticationMode(),
                    profileName = defaultProfileName
                )
            }
        }
    }
}

@Composable
fun rememberOnboardingController(): OnboardingSharedController {
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val saveOnboardingSucceededUseCase by rememberInstance<SaveOnboardingDataUseCase>()
    val allowScreenshotsUseCase by rememberInstance<AllowScreenshotsUseCase>()
    val isAnalyticsAllowedUseCase by rememberInstance<IsAnalyticsAllowedUseCase>()
    val changeAnalyticsStateUseCase by rememberInstance<ChangeAnalyticsStateUseCase>()
    val scope = rememberCoroutineScope()

    val defaultProfileName = stringResource(R.string.onboarding_default_profile_name)

    return remember {
        OnboardingSharedController(
            getProfilesUseCase = getProfilesUseCase,
            saveOnboardingDataUseCase = saveOnboardingSucceededUseCase,
            allowScreenshotsUseCase = allowScreenshotsUseCase,
            isAnalyticsAllowedUseCase = isAnalyticsAllowedUseCase,
            changeAnalyticsStateUseCase = changeAnalyticsStateUseCase,
            defaultProfileName = defaultProfileName,
            scope = scope
        )
    }
}
