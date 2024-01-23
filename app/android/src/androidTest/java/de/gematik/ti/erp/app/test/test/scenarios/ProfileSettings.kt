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

package de.gematik.ti.erp.app.test.test.scenarios

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.MediumTest
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.core.sleep
import de.gematik.ti.erp.app.test.test.steps.CardWallScreenSteps
import de.gematik.ti.erp.app.test.test.steps.MainScreenSteps
import de.gematik.ti.erp.app.test.test.steps.OnboardingSteps
import de.gematik.ti.erp.app.test.test.steps.ProfileSettingsSteps
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
class ProfileSettings(fontScale: String) : WithFontScale(fontScale) {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingSteps by lazy { OnboardingSteps(composeRule) }
    private val mainScreenSteps by lazy { MainScreenSteps(composeRule) }
    private val profileSettingsSteps by lazy { ProfileSettingsSteps(composeRule) }
    private val cardWallScreenSteps by lazy { CardWallScreenSteps(composeRule) }

    @Before
    fun skipOnboarding() {
        onboardingSteps.userSkipsOnboarding()
    }

    @Test
    fun no_token_in_profile_if_not_logged_in() {
        profileSettingsSteps.openProfileSettings()
        profileSettingsSteps.checkNoTokenPresent()
        profileSettingsSteps.checkTokenHintPresent()
    }

    @Test
    fun token_in_profile_if_logged_in() {
        cardWallScreenSteps.userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully()
        profileSettingsSteps.openProfileSettings()
        profileSettingsSteps.checkTokenPresent()
        profileSettingsSteps.hintTextNotPresent()
    }

    @Test
    fun no_token_in_profile_after_logged_out() {
        cardWallScreenSteps.userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully()
        profileSettingsSteps.logoutViaProfileSettings()
        profileSettingsSteps.noTokenPresentInProfileSettings()
        profileSettingsSteps.checkTokenHintPresent(
            "Sie erhalten einen Token, wenn Sie am Rezeptdienst angemeldet sind."
        )
    }

    @Test
    fun login_in_profile_settings_opens_cardwall() {
        profileSettingsSteps.openProfileSettings()
        profileSettingsSteps.tapLoginButton()
        profileSettingsSteps.userSeesCardwallWelcomeScreen()
    }

    @Test
    fun no_kvnr_in_profile_settings_if_never_logged_in() {
        profileSettingsSteps.openProfileSettings()
        profileSettingsSteps.checkNoKVNRIsVisible()
    }

    @Test
    fun kvnr_in_profile_settings_is_visible_if_logged_in() {
        cardWallScreenSteps.userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully()
        profileSettingsSteps.openProfileSettings()
        profileSettingsSteps.checkKVNRIsVisible()
    }

    @Test
    fun kvnr_in_profile_settings_is_visible_if_logged_out() {
        cardWallScreenSteps.userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully()
        profileSettingsSteps.logoutViaProfileSettings()
        profileSettingsSteps.openProfileSettings()
        profileSettingsSteps.checkKVNRIsVisible()
    }

    @Test
    fun if_profile_changed_show_no_kvnr() {
        cardWallScreenSteps.userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully()
        mainScreenSteps.createNewProfile("Karoline Karies")
        profileSettingsSteps.openProfileSettingsForCertainProfile(2)
        profileSettingsSteps.checkNoKVNRIsVisible()
    }

    @Test
    fun show_no_access_protocol_screen() {
        profileSettingsSteps.userSeesAuditEventsScreen(1)
        profileSettingsSteps.userSeesEmptyStateForCertainProfile()
    }

    @Test
    fun show_no_access_protocol_screen_when_switching_to_logged_out_profile() {
        cardWallScreenSteps.userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully()
        mainScreenSteps.createNewProfile("Karoline Karies")
        profileSettingsSteps.userSeesAuditEventsScreen(2)
        profileSettingsSteps.userSeesEmptyStateForCertainProfile()
    }

    @Test
    fun show_protocol_after_logout() {
        cardWallScreenSteps.setVirtualEGKWithPrescriptions()
        composeRule.sleep(5_000L) // await audit events
        profileSettingsSteps.userLogsOutOffProfile(1)
        profileSettingsSteps.userSeesAuditEventsScreen(1)
        profileSettingsSteps.userDoesNotSeesEmptyStateForCertainProfile()
    }
}
