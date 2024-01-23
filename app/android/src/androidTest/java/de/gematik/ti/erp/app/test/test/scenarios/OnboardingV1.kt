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
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.steps.OnboardingSteps
import org.junit.Rule
import org.junit.Test

@SmallTest
class OnboardingV1(fontScale: String) : WithFontScale(fontScale) {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingSteps by lazy { OnboardingSteps(composeRule) }
    private fun restartApp() {
        composeRule.activityRule.scenario.recreate()
    }

    @Test
    fun first_time() {
        // Onboarding wird beim 1. App-Start angezeigt
        onboardingSteps.userSeesWelcomeScreen()
    }

    @Test
    fun shown_again_if_not_finished() {
        // Onboarding wird nach App-Neustart weiterhin angezeigt, solange es noch nicht beendet wurde

        onboardingSteps.userSeesWelcomeScreen()
        restartApp()
        onboardingSteps.userSeesWelcomeScreen()
    }

    @Test
    fun navigate_through_onboarding_without_analytics() {
        // welcome screen
        onboardingSteps.userSeesWelcomeScreen()
        onboardingSteps.userIsFinishingTheOnboardingWithoutAnalytics()
    }

    @Test
    fun navigate_through_analytics_optional() {
        // welcome screen
        onboardingSteps.userSeesWelcomeScreen()
        onboardingSteps.userIsFinishingTheOnboardingWithAnalytics()
    }

    @Test
    fun navigate_through_and_restart_app() {
        onboardingSteps.userNavigatesToOnboardingScreenName(OnboardingSteps.Page.MainScreen)

        restartApp()

        onboardingSteps.userIsNotSeeingTheOnboarding()
        onboardingSteps.userSeesMainScreen()
    }

    @Test
    fun weak_password_blocks_next() {
        // Onboarding Screen 3 >>> Zu schwaches Passwort = Weiter-Button inaktiv + Fehlermeldung

        onboardingSteps.userNavigatesToOnboardingScreenName(OnboardingSteps.Page.Credentials)
        // credentials screen
        onboardingSteps.userSeesCredentialScreen()

        onboardingSteps.userEntersAWeakPasswordTwice()

        onboardingSteps.userDoesNotSeeContinueButton()
        onboardingSteps.userSeesErrorMessageForPasswordStrength()
    }

    @Test
    fun no_password_blocks_next() {
        // Onboarding Screen 3 >>> kein Passwort = Weiter-Button inaktiv + Fehlermeldung

        onboardingSteps.userNavigatesToOnboardingScreenName(OnboardingSteps.Page.Credentials)
        // credentials screen
        onboardingSteps.userSeesCredentialScreen()

        onboardingSteps.userSwitchesToPasswordMode()

        onboardingSteps.userDoesNotSeeContinueButton()
        onboardingSteps.userSeesErrorMessageForPasswordStrength()
    }

    @Test
    fun not_confirming_data_protection_blocks_next() {
        onboardingSteps.userNavigatesToOnboardingScreenName(OnboardingSteps.Page.DataTerms)
        onboardingSteps.dataTermsSwitchDeactivated()
        onboardingSteps.confirmContinueButtonIsDeactivated()
        onboardingSteps.toggleDataTermsSwitch()
        onboardingSteps.userSeesActivatedContinueButton()
        onboardingSteps.toggleDataTermsSwitch()
        onboardingSteps.confirmContinueButtonIsDeactivated()
    }

    @Test
    fun data_protection_can_be_read() {
        // Datenschutzbestimmungen werden angezeigt und können geschlossen werden

        onboardingSteps.userNavigatesToOnboardingScreenName(OnboardingSteps.Page.DataTerms)

        onboardingSteps.userDoesntSeeDataProtection()
        onboardingSteps.userOpensDataProtection()
        onboardingSteps.userSeesDataProtection()
        onboardingSteps.userClosesDataProtection()
        onboardingSteps.userDoesntSeeDataProtection()
    }

    @Test
    fun terms_of_use_can_be_read() {
        // Nutzungsbedingungen werden angezeigt und können geschlossen werden

        onboardingSteps.userNavigatesToOnboardingScreenName(OnboardingSteps.Page.DataTerms)

        onboardingSteps.userSeesNoTermsOfUse()
        onboardingSteps.userOpensTermsOfUse()
        onboardingSteps.userSeesTermsOfUse()
        onboardingSteps.userClosesTermsOfUse()
        onboardingSteps.userSeesNoTermsOfUse()
    }
}
