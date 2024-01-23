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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.analytics.usecase.ChangeAnalyticsStateUseCase
import de.gematik.ti.erp.app.analytics.usecase.IsAnalyticsAllowedUseCase
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveOnboardingSuccededUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class OnboardingController(
    private val saveOnboardingSuccededUseCase: SaveOnboardingSuccededUseCase,
    private val allowScreenshotsUseCase: AllowScreenshotsUseCase,
    private val isAnalyticsAllowedUseCase: IsAnalyticsAllowedUseCase,
    private val changeAnalyticsStateUseCase: ChangeAnalyticsStateUseCase,
    private val analytics: Analytics,
    private val scope: CoroutineScope
) {

    private val isAnalyticsAllowed by lazy {
        isAnalyticsAllowedUseCase.invoke()
    }

    val isAnalyticsAllowedState
        @Composable
        get() = isAnalyticsAllowed.collectAsStateWithLifecycle(false)

    fun onboardingSucceeded(
        authenticationMode: SettingsData.AuthenticationMode,
        profileName: String,
        allowAnalytics: Boolean
    ) = scope.launch {
        analytics.setAnalyticsPreference(allowAnalytics)
        saveOnboardingSuccededUseCase(
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
}

@Composable
fun rememberOnboardingController(): OnboardingController {
    val saveOnboardingSuccededUseCase by rememberInstance<SaveOnboardingSuccededUseCase>()
    val allowScreenshotsUseCase by rememberInstance<AllowScreenshotsUseCase>()
    val isAnalyticsAllowedUseCase by rememberInstance<IsAnalyticsAllowedUseCase>()
    val changeAnalyticsStateUseCase by rememberInstance<ChangeAnalyticsStateUseCase>()
    val scope = rememberCoroutineScope()
    val analytics by rememberInstance<Analytics>()

    return remember {
        OnboardingController(
            saveOnboardingSuccededUseCase = saveOnboardingSuccededUseCase,
            allowScreenshotsUseCase = allowScreenshotsUseCase,
            isAnalyticsAllowedUseCase = isAnalyticsAllowedUseCase,
            changeAnalyticsStateUseCase = changeAnalyticsStateUseCase,
            analytics = analytics,
            scope = scope
        )
    }
}
