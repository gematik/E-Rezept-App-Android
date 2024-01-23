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

package de.gematik.ti.erp.app.test.test.steps

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.ComposeTestRule
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.sleep
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.OnboardingScreen

class OnboardingSteps(
    private val composeRule: ComposeTestRule
) : SemanticsNodeInteractionsProvider by composeRule {

    private val onboardingScreen by lazy { OnboardingScreen(composeRule) }
    private val mainScreen by lazy { MainScreen(composeRule) }

    enum class Page {
        DataTerms, Credentials, Analytics, MainScreen
    }

    fun userSeesMainScreen() {
        mainScreen.userSeesMainScreen()
    }

    fun userIsNotSeeingTheOnboarding() {
        onboardingScreen.checkTutorialIsNotPresent()
    }

    fun userIsFinishingTheOnboardingWithoutAnalytics() {
        userNavigatesToOnboardingScreenName(Page.MainScreen)
    }

    fun userIsFinishingTheOnboardingWithAnalytics() {
        userNavigatesToOnboardingScreenName(Page.Analytics)
        onboardingScreen.toggleAnalyticsSwitch()
        onboardingScreen.waitForAnalyticsPage()
        onboardingScreen.tapAcceptAnalyticsButton()
        onboardingScreen.checkAnalyticsSwitchIsActivated()
        onboardingScreen.checkContinueTutorialButtonIsEnabled()
        onboardingScreen.tapContinueButton()
        mainScreen.userSeesMainScreen()
    }

    fun userSkipsOnboarding() {
        onboardingScreen.tapSkipOnboardingButton()
        mainScreen.tapConnectLater()
        mainScreen.userSeesMainScreen()
        tapToGetRidOfTour()
    }

    fun tapToGetRidOfTour() {
        mainScreen.userClicksBottomBarPrescriptions()
        mainScreen.userClicksBottomBarPrescriptions()
        mainScreen.userClicksBottomBarPrescriptions()
        mainScreen.userClicksBottomBarPrescriptions()
        mainScreen.userClicksBottomBarPrescriptions()
        composeRule.sleep(2000L)
    }

    fun userSeesWelcomeScreen() {
        onboardingScreen.checkWelcomePageIsPresent()
    }

    fun userNavigatesToOnboardingScreenName(page: Page) {
        when (page) {
            // go to Data Terms Screen
            Page.DataTerms -> {
                onboardingScreen.waitForSecondOnboardingPage()
                onboardingScreen.checkDataTermsPageIsPresent()
            }
            Page.Credentials -> { // go to Password Screen
                onboardingScreen.waitForSecondOnboardingPage()
                onboardingScreen.tapDataTermsSwitch()
                onboardingScreen.tapContinueButton()
                onboardingScreen.checkCredentialsPageIsPresent()
            }
            Page.Analytics -> { // go to Analytics Screen
                userNavigatesToOnboardingScreenName(Page.Credentials)
                onboardingScreen.switchToPasswordMode()
                userEntersStrongEnoughPasswordTwice()
                onboardingScreen.tapContinueButton()
                userSeesAnalyticsScreen()
            }
            Page.MainScreen -> {
                userNavigatesToOnboardingScreenName(Page.Analytics)
                onboardingScreen.tapContinueButton()
                mainScreen.userSeesMainScreen()
            }
        }
    }

    private fun userSeesAnalyticsScreen() {
        onboardingScreen.checkAnalyticsPageIsPresent()
        onboardingScreen.checkContinueTutorialButtonIsEnabled()
        onboardingScreen.checkAnalyticsSwitchIsDeactivated()
    }

    fun userSeesCredentialScreen() {
        onboardingScreen.checkCredentialsPageIsPresent()
    }

    fun dataTermsSwitchDeactivated() {
        onboardingScreen.checkDataTermsSwitchDeactivated()
    }

    fun confirmContinueButtonIsDeactivated() {
        onboardingScreen.checkContinueTutorialButtonIsDeactivated()
    }

    fun toggleDataTermsSwitch() {
        onboardingScreen.tapDataTermsSwitch()
    }

    fun userEntersAWeakPasswordTwice() {
        onboardingScreen.switchToPasswordMode()
        onboardingScreen.enterPasswordA(TestConfig.WeakPassword)
        onboardingScreen.enterPasswordB(TestConfig.WeakPassword)
    }

    fun userEntersStrongEnoughPasswordTwice() {
        onboardingScreen.switchToPasswordMode()
        onboardingScreen.enterPasswordA(TestConfig.StrongPassword)
        onboardingScreen.enterPasswordB(TestConfig.StrongPassword)
        onboardingScreen.checkNoPasswordErrorMessagePresent()
    }

    fun userSwitchesToPasswordMode() {
        onboardingScreen.switchToPasswordMode()
    }

    fun userDoesNotSeeContinueButton() {
        onboardingScreen.checkContinueTutorialButtonIsDisabled()
    }

    fun userSeesActivatedContinueButton() {
        onboardingScreen.checkContinueTutorialButtonIsEnabled()
    }

    fun userSeesTermsOfUse() {
        onboardingScreen.checkTermsOfUseAreDisplayed()
    }

    fun userSeesNoTermsOfUse() {
        onboardingScreen.checkTermsOfUseAreNotDisplayed()
    }

    fun userOpensTermsOfUse() {
        onboardingScreen.openTermsOfUse()
    }

    fun userClosesTermsOfUse() {
        onboardingScreen.closeTermsOfUse()
    }

    fun userDoesntSeeDataProtection() {
        onboardingScreen.checkDataProtectionAreNotDisplayed()
    }

    fun userOpensDataProtection() {
        onboardingScreen.openDataProtection()
    }

    fun userSeesDataProtection() {
        onboardingScreen.checkDataProtectionIsDisplayed()
    }

    fun userClosesDataProtection() {
        onboardingScreen.closeDataProtection()
    }

    fun userSeesErrorMessageForPasswordStrength() {
        onboardingScreen.checkPasswordErrorMessagePresent()
    }
}
