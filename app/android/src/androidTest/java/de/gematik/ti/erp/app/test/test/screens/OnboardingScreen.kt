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

package de.gematik.ti.erp.app.test.test.screens

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class OnboardingScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun checkTutorialIsNotPresent() {
        onNodeWithTag(TestTag.Onboarding.Pager)
            .assertDoesNotExist()
    }

    fun waitForSecondOnboardingPage() {
        composeRule.awaitDisplay(5000L, TestTag.Onboarding.DataTermsScreen)
    }

    fun waitForAnalyticsPage() {
        composeRule.awaitDisplay(5000L, TestTag.Onboarding.Analytics.ScreenContent)
    }

    fun tapContinueButton() {
        onNodeWithTag(TestTag.Onboarding.NextButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun switchToPasswordMode() {
        onNodeWithTag(TestTag.Onboarding.Credentials.PasswordTab)
            .assertIsDisplayed()
            .performClick()
    }

    fun enterPasswordA(password: String) {
        onNodeWithTag(TestTag.Onboarding.Credentials.PasswordFieldA)
            .assertIsDisplayed()
            .performClick()
            .assertIsFocused()
            .performTextInput(password)
    }

    fun enterPasswordB(password: String) {
        onNodeWithTag(TestTag.Onboarding.ScreenContent)
            .performTouchInput {
                swipeUp()
            }

        onNodeWithTag(TestTag.Onboarding.Credentials.PasswordFieldB)
            .assertIsDisplayed()
            .performClick()
            .assertIsFocused()
            .performTextInput(password)
    }

    fun tapDataTermsSwitch() {
        onNodeWithTag(TestTag.Onboarding.ScreenContent)
            .performTouchInput {
                swipeUp()
            }

        onNodeWithTag(TestTag.Onboarding.DataTerms.AcceptDataTermsSwitch)
            .assertIsDisplayed()
            .assertIsToggleable()
            .performClick()
    }

    fun closeDataProtection() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun checkDataProtectionIsDisplayed() {
        onNodeWithTag(TestTag.Onboarding.DataProtectionScreen)
            .assertIsDisplayed()
    }

    fun openDataProtection() {
        onNodeWithTag(TestTag.Onboarding.DataTerms.OpenDataProtectionButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun checkDataProtectionAreNotDisplayed() {
        onNodeWithTag(TestTag.Onboarding.DataProtectionScreen)
            .assertDoesNotExist()
    }

    fun closeTermsOfUse() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun openTermsOfUse() {
        onNodeWithTag(TestTag.Onboarding.DataTerms.OpenTermsOfUseButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun checkTermsOfUseAreDisplayed() {
        onNodeWithTag(TestTag.Onboarding.TermsOfUseScreen)
            .assertIsDisplayed()
    }

    fun checkTermsOfUseAreNotDisplayed() {
        onNodeWithTag(TestTag.Onboarding.TermsOfUseScreen)
            .assertDoesNotExist()
    }

    fun checkNoPasswordErrorMessagePresent() {
        onNodeWithTag(TestTag.Onboarding.Credentials.PasswordStrengthCheck)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "sufficient"))
    }

    fun checkPasswordErrorMessagePresent() {
        onNodeWithTag(TestTag.Onboarding.Credentials.PasswordStrengthCheck)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "insufficient"))
    }

    fun checkContinueTutorialButtonIsEnabled() {
        onNodeWithTag(TestTag.Onboarding.NextButton)
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    fun checkContinueTutorialButtonIsDeactivated() {
        onNodeWithTag(TestTag.Onboarding.NextButton)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    fun checkDataTermsSwitchDeactivated() {
        onNodeWithTag(TestTag.Onboarding.DataTerms.AcceptDataTermsSwitch)
            .assertIsDisplayed()
            .assertIsToggleable()
            .assertIsOff()
    }

    fun checkWelcomePageIsPresent() {
        onNodeWithTag(TestTag.Onboarding.WelcomeScreen)
            .assertExists()
    }

    fun checkCredentialsPageIsPresent() {
        onNodeWithTag(TestTag.Onboarding.CredentialsScreen)
            .assertExists()
    }

    fun checkAnalyticsPageIsPresent() {
        onNodeWithTag(TestTag.Onboarding.AnalyticsScreen)
            .assertExists()
    }

    fun checkDataTermsPageIsPresent() {
        onNodeWithTag(TestTag.Onboarding.DataTermsScreen)
            .assertExists()
    }

    fun swipeToNextTutorialStep() {
        onNodeWithTag(TestTag.Onboarding.Pager)
            .assertIsDisplayed()
            .performTouchInput { swipeLeft() }
    }

    fun swipeToPreviousTutorialStep() {
        onNodeWithTag(TestTag.Onboarding.Pager)
            .assertIsDisplayed()
            .performTouchInput { swipeRight() }
    }

    fun checkContinueTutorialButtonIsDisabled() {
        onNodeWithTag(TestTag.Onboarding.NextButton)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    fun tapSkipOnboardingButton() {
        onNodeWithTag(TestTag.Onboarding.SkipOnboardingButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun checkAnalyticsSwitchIsDeactivated() {
        onNodeWithTag(TestTag.Onboarding.AnalyticsSwitch)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsToggleable()
            .assertIsOff()
    }

    fun checkAnalyticsSwitchIsActivated() {
        onNodeWithTag(TestTag.Onboarding.AnalyticsSwitch)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsToggleable()
            .assertIsOn()
    }

    fun toggleAnalyticsSwitch() {
        onNodeWithTag(TestTag.Onboarding.ScreenContent)
            .performTouchInput {
                swipeUp()
            }

        onNodeWithTag(TestTag.Onboarding.AnalyticsSwitch)
            .assertIsDisplayed()
            .assertIsToggleable()
            .performClick()
    }

    fun tapAcceptAnalyticsButton() {
        onNodeWithTag(TestTag.Onboarding.Analytics.AcceptAnalyticsButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
}
