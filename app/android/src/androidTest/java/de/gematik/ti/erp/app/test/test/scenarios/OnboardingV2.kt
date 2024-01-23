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
import androidx.test.filters.SmallTest
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.OnboardingScreen
import org.junit.Rule
import org.junit.Test

@SmallTest
class OnboardingV2(fontScale: String) : WithFontScale(fontScale) {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingScreen by lazy { OnboardingScreen(composeRule) }
    private val mainScreen by lazy { MainScreen(composeRule) }

    private fun restartApp() {
        composeRule.activityRule.scenario.recreate()
    }

    @Test
    fun first_time() {
        // Onboarding wird beim 1. App-Start angezeigt

        onboardingScreen.checkWelcomePageIsPresent()
    }

    @Test
    fun shown_again_if_not_finished() {
        // Onboarding wird nach App-Neustart weiterhin angezeigt, solange es noch nicht beendet wurde

        onboardingScreen.checkWelcomePageIsPresent()
        restartApp()
        onboardingScreen.checkWelcomePageIsPresent()
    }

    @Test
    fun navigate_through() {
        // welcome screen
        onboardingScreen.checkWelcomePageIsPresent()
        onboardingScreen.waitForSecondOnboardingPage()

        // data terms screen
        onboardingScreen.checkDataTermsPageIsPresent()

        onboardingScreen.checkDataTermsSwitchDeactivated()
        onboardingScreen.checkContinueTutorialButtonIsDeactivated()

        onboardingScreen.tapDataTermsSwitch()
        onboardingScreen.checkContinueTutorialButtonIsEnabled()

        onboardingScreen.tapContinueButton()

        // credentials screen
        onboardingScreen.checkCredentialsPageIsPresent()

        onboardingScreen.switchToPasswordMode()
        onboardingScreen.enterPasswordA(TestConfig.StrongPassword)
        onboardingScreen.enterPasswordB(TestConfig.StrongPassword)
        onboardingScreen.tapContinueButton()

        // analytics screen
        onboardingScreen.checkAnalyticsPageIsPresent()

        onboardingScreen.checkContinueTutorialButtonIsEnabled()

        onboardingScreen.checkAnalyticsSwitchIsDeactivated()

        onboardingScreen.toggleAnalyticsSwitch()
        onboardingScreen.waitForAnalyticsPage()
        onboardingScreen.tapAcceptAnalyticsButton()

        onboardingScreen.checkAnalyticsSwitchIsActivated()

        onboardingScreen.checkContinueTutorialButtonIsEnabled()

        onboardingScreen.tapContinueButton()

        // main screen
        mainScreen.userSeesMainScreen()
    }

    @Test
    fun navigate_through_analytics_optional() {
        // welcome screen
        onboardingScreen.checkWelcomePageIsPresent()
        onboardingScreen.waitForSecondOnboardingPage()

        // data terms screen
        onboardingScreen.checkDataTermsPageIsPresent()

        onboardingScreen.checkDataTermsSwitchDeactivated()
        onboardingScreen.checkContinueTutorialButtonIsDeactivated()

        onboardingScreen.tapDataTermsSwitch()
        onboardingScreen.checkContinueTutorialButtonIsEnabled()

        onboardingScreen.tapContinueButton()

        // credentials screen
        onboardingScreen.checkCredentialsPageIsPresent()

        onboardingScreen.switchToPasswordMode()
        onboardingScreen.enterPasswordA(TestConfig.StrongPassword)
        onboardingScreen.enterPasswordB(TestConfig.StrongPassword)
        onboardingScreen.checkContinueTutorialButtonIsEnabled()

        onboardingScreen.tapContinueButton()

        // analytics screen
        onboardingScreen.checkAnalyticsPageIsPresent()

        onboardingScreen.checkContinueTutorialButtonIsEnabled()
        onboardingScreen.checkAnalyticsSwitchIsDeactivated()

        onboardingScreen.tapContinueButton()

        // main screen
        mainScreen.userSeesMainScreen()
    }

    private enum class Page {
        DataTerms, Credentials, Analytics, MainScreen
    }

    @Suppress("ReturnCount")
    private fun walkThroughOnboardingWithoutChecks(until: Page? = null) {
        // welcome screen
        onboardingScreen.checkWelcomePageIsPresent()
        onboardingScreen.waitForSecondOnboardingPage()

        if (until == Page.DataTerms) return

        // data terms screen
        onboardingScreen.checkDataTermsPageIsPresent()

        onboardingScreen.tapDataTermsSwitch()

        onboardingScreen.tapContinueButton()

        if (until == Page.Credentials) return

        // credentials screen
        onboardingScreen.checkCredentialsPageIsPresent()

        onboardingScreen.switchToPasswordMode()
        onboardingScreen.enterPasswordA(TestConfig.StrongPassword)
        onboardingScreen.enterPasswordB(TestConfig.StrongPassword)
        onboardingScreen.checkContinueTutorialButtonIsEnabled()

        onboardingScreen.tapContinueButton()

        if (until == Page.Analytics) return

        // analytics screen
        onboardingScreen.checkAnalyticsPageIsPresent()

        onboardingScreen.tapContinueButton()

        if (until == Page.MainScreen) return

        // main screen
        mainScreen.userSeesMainScreen()
    }

    @Test
    fun navigate_through_and_restart_app() {
        walkThroughOnboardingWithoutChecks()

        restartApp()

        mainScreen.userSeesMainScreen()
    }

    @Test
    fun weak_password_blocks_next() {
        // Onboarding Screen 3 >>> Zu schwaches Passwort = Weiter-Button inaktiv + Fehlermeldung

        walkThroughOnboardingWithoutChecks(until = Page.Credentials)

        // credentials screen
        onboardingScreen.checkCredentialsPageIsPresent()

        onboardingScreen.switchToPasswordMode()
        onboardingScreen.enterPasswordA(TestConfig.WeakPassword)
        onboardingScreen.enterPasswordB(TestConfig.WeakPassword)

        onboardingScreen.checkContinueTutorialButtonIsDisabled()
        onboardingScreen.checkPasswordErrorMessagePresent()
    }

    @Test
    fun data_protection_can_be_read() {
        // Datenschutzbestimmungen werden angezeigt und können geschlossen werden

        walkThroughOnboardingWithoutChecks(until = Page.DataTerms)

        onboardingScreen.checkDataTermsPageIsPresent()
        onboardingScreen.checkDataProtectionAreNotDisplayed()
        onboardingScreen.openDataProtection()
        onboardingScreen.checkDataProtectionIsDisplayed()
        onboardingScreen.closeDataProtection()
        onboardingScreen.checkDataProtectionAreNotDisplayed()
    }

    @Test
    fun terms_of_use_can_be_read() {
        // Nutzungsbedingungen werden angezeigt und können geschlossen werden

        walkThroughOnboardingWithoutChecks(until = Page.DataTerms)

        onboardingScreen.checkDataTermsPageIsPresent()
        onboardingScreen.checkTermsOfUseAreNotDisplayed()
        onboardingScreen.openTermsOfUse()
        onboardingScreen.checkTermsOfUseAreDisplayed()
        onboardingScreen.closeTermsOfUse()
        onboardingScreen.checkTermsOfUseAreNotDisplayed()
    }
}
