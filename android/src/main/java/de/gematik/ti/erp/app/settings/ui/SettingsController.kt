/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.content.edit
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import androidx.paging.PagingData
import de.gematik.ti.erp.app.ScreenshotsAllowed
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.di.ApplicationPreferencesTag
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesWithPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.kodein.di.compose.rememberInstance

@Suppress("TooManyFunctions")
class SettingsController(
    private val settingsUseCase: SettingsUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val profilesWithPairedDevicesUseCase: ProfilesWithPairedDevicesUseCase,
    private val analytics: Analytics,
    private val appPrefs: SharedPreferences
) {

    private var screenshotsAllowed =
        MutableStateFlow(appPrefs.getBoolean(ScreenshotsAllowed, false))

    private var screenShotFlow = screenshotsAllowed.map { SettingStatesData.ScreenshotState(it) }

    val screenShotState
        @Composable
        get() = screenShotFlow.collectAsState(SettingStatesData.defaultScreenshotState)

    private val analyticsFlow = analytics.analyticsAllowed.map { SettingStatesData.AnalyticsState(it) }

    val analyticsState
        @Composable
        get() = analyticsFlow.collectAsState(SettingStatesData.defaultAnalyticsState)

    private val authenticationModeFlow = settingsUseCase.authenticationMode.map {
        SettingStatesData.AuthenticationModeState(
            it
        )
    }

    val authenticationModeState
        @Composable
        get() = authenticationModeFlow.collectAsState(SettingStatesData.defaultAuthenticationState)

    private val zoomFlow = settingsUseCase.general.map { SettingStatesData.ZoomState(it.zoomEnabled) }

    val zoomState
        @Composable
        get() = zoomFlow.collectAsState(SettingStatesData.defaultZoomState)

    private val profilesFlow = profilesUseCase.profiles.map { SettingStatesData.ProfilesState(it) }

    val profilesState
        @Composable
        get() = profilesFlow.collectAsState(SettingStatesData.defaultProfilesState)

    fun pairedDevices(profileId: ProfileIdentifier) =
        profilesWithPairedDevicesUseCase.pairedDevices(profileId)

    // tag::DeletePairedDevicesViewModel[]
    suspend fun deletePairedDevice(profileId: ProfileIdentifier, device: ProfilesUseCaseData.PairedDevice) =
        profilesWithPairedDevicesUseCase.deletePairedDevices(profileId, device)

    // end::DeletePairedDevicesViewModel[]
    fun decryptedAccessToken(profile: ProfilesUseCaseData.Profile) =
        profilesUseCase.decryptedAccessToken(profile.id)

    suspend fun onSelectDeviceSecurityAuthenticationMode() {
        settingsUseCase.saveAuthenticationMode(
            SettingsData.AuthenticationMode.DeviceSecurity
        )
    }

    suspend fun onSelectPasswordAsAuthenticationMode(password: String) {
        settingsUseCase.saveAuthenticationMode(SettingsData.AuthenticationMode.Password(password = password))
    }

    fun onSwitchAllowScreenshots(allowScreenshots: Boolean) {
        appPrefs.edit {
            putBoolean(ScreenshotsAllowed, allowScreenshots)
        }
        screenshotsAllowed.value = allowScreenshots
    }

    suspend fun onEnableZoom() {
        settingsUseCase.saveZoomPreference(true)
    }

    suspend fun onDisableZoom() {
        settingsUseCase.saveZoomPreference(false)
    }

    fun onTrackingAllowed() {
        analytics.allowTracking()
    }

    fun onTrackingDisallowed() {
        analytics.disallowTracking()
    }

    suspend fun logout(profile: ProfilesUseCaseData.Profile) {
        profilesUseCase.logout(profile)
    }

    suspend fun addProfile(profileName: String) {
        profilesUseCase.addProfile(profileName, activate = true)
    }

    suspend fun removeProfile(profile: ProfilesUseCaseData.Profile, newProfileName: String?) {
        if (newProfileName != null) {
            profilesUseCase.removeAndSaveProfile(profile, newProfileName)
        } else {
            profilesUseCase.removeProfile(profile)
        }
    }

    suspend fun switchProfile(profile: ProfilesUseCaseData.Profile) {
        profilesUseCase.switchActiveProfile(profile)
    }

    fun loadAuditEventsForProfile(profileId: ProfileIdentifier): Flow<PagingData<AuditEventData.AuditEvent>> =
        profilesUseCase.auditEvents(profileId)

    suspend fun onboardingSucceeded(
        authenticationMode: SettingsData.AuthenticationMode,
        defaultProfileName: String,
        allowTracking: Boolean
    ) {
        settingsUseCase.onboardingSucceeded(
            authenticationMode = authenticationMode,
            defaultProfileName = defaultProfileName
        )
        if (allowTracking) {
            onTrackingAllowed()
        } else {
            onTrackingDisallowed()
        }
    }

    val authenticationMethod = settingsUseCase.authenticationMode
    var showOnboarding = runBlocking { settingsUseCase.showOnboarding.first() }
    var showWelcomeDrawer = runBlocking { settingsUseCase.showWelcomeDrawer }

    private var insecureDevicePromptShown = false
    val showInsecureDevicePrompt = settingsUseCase
        .showInsecureDevicePrompt
        .map {
            if (showOnboarding) {
                false
            } else if (!insecureDevicePromptShown) {
                insecureDevicePromptShown = true
                it
            } else {
                false
            }
        }

    suspend fun onAcceptInsecureDevice() {
        settingsUseCase.acceptInsecureDevice()
    }

    suspend fun acceptMlKit() {
        settingsUseCase.acceptMlKit()
    }

    suspend fun acceptUpdatedDataTerms() {
        settingsUseCase.acceptUpdatedDataTerms()
    }

    suspend fun welcomeDrawerShown() {
        settingsUseCase.welcomeDrawerShown()
    }

    suspend fun mainScreenTooltipsShown() {
        settingsUseCase.mainScreenTooltipsShown()
    }

    fun showMainScreenToolTips(): Flow<Boolean> = settingsUseCase.general
        .map { !it.mainScreenTooltipsShown && it.welcomeDrawerShown }

    fun mlKitNotAccepted() =
        settingsUseCase.general.map { !it.mlKitAccepted }

    fun talkbackEnabled(context: Context): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager

        return accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
            .isNotEmpty()
    }
}

@Composable
fun rememberSettingsController(): SettingsController {
    val settingsUseCase by rememberInstance<SettingsUseCase>()
    val profilesUseCase by rememberInstance<ProfilesUseCase>()
    val profilesWithPairedDevicesUseCase by rememberInstance<ProfilesWithPairedDevicesUseCase>()
    val analytics by rememberInstance<Analytics>()
    val appPrefs by rememberInstance<SharedPreferences>(ApplicationPreferencesTag)

    return remember {
        SettingsController(
            settingsUseCase,
            profilesUseCase,
            profilesWithPairedDevicesUseCase,
            analytics,
            appPrefs
        )
    }
}

object SettingStatesData {

    @Immutable
    data class AnalyticsState(
        val analyticsAllowed: Boolean
    )

    val defaultAnalyticsState = AnalyticsState(analyticsAllowed = false)

    @Immutable
    data class AuthenticationModeState(
        val authenticationMode: SettingsData.AuthenticationMode
    )

    val defaultAuthenticationState = AuthenticationModeState(SettingsData.AuthenticationMode.Unspecified)

    @Immutable
    data class ZoomState(
        val zoomEnabled: Boolean
    )

    val defaultZoomState = ZoomState(zoomEnabled = false)

    @Immutable
    data class ScreenshotState(
        val screenshotsAllowed: Boolean
    )

    // `gemSpec_eRp_FdV A_20203` default settings does not allow screenshots
    val defaultScreenshotState = ScreenshotState(screenshotsAllowed = false)

    @Immutable
    data class ProfilesState(
        val profiles: List<ProfilesUseCaseData.Profile>
    ) {
        fun activeProfile() = profiles.find { it.active }!!
        fun profileById(profileId: String) = profiles.find { it.id == profileId }
        fun containsProfileWithName(name: String) = profiles.any {
            it.name.equals(name.trim(), true)
        }
    }

    val defaultProfilesState = ProfilesState(
        profiles = listOf()
    )
}
