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
import androidx.test.filters.LargeTest
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.steps.CardWallScreenSteps
import de.gematik.ti.erp.app.test.test.steps.MainScreenSteps
import de.gematik.ti.erp.app.test.test.steps.OnboardingSteps
import de.gematik.ti.erp.app.test.test.steps.SettingScreenSteps
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class HealthCardOrder(fontScale: String) : WithFontScale(fontScale) {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingSteps by lazy { OnboardingSteps(composeRule) }
    private val mainScreenSteps by lazy { MainScreenSteps(composeRule) }
    private val cardWallScreenSteps by lazy { CardWallScreenSteps(composeRule) }

    private val settingScreenSteps by lazy { SettingScreenSteps(composeRule) }

    @Before
    fun skipsOnboarding() {
        onboardingSteps.userSkipsOnboarding()
    }

    @After
    fun destroyActivity() {
        composeRule.activity.finish()
    }

    @Test
    fun list_health_insurance_company() {
        mainScreenSteps.userTapsSettingsMenuButton()
        settingScreenSteps.userWantsToOrderNewCard()
        settingScreenSteps.userSeesAListOfInsurances()
    }

    @Test
    fun ordering_new_EGKFromSettings() {
        mainScreenSteps.userTapsSettingsMenuButton()
        settingScreenSteps.userWantsToOrderNewCard()
        settingScreenSteps.userSeesAListOfInsurances()
        settingScreenSteps.userChoosesInsurance("AOK - Die Gesundheitskasse Hessen")
        settingScreenSteps.userSeesOrderOptionScreen()
        settingScreenSteps.userSeesPossibilitiesWhatCanBeOrdered("Karten & PIN, Nur PIN")
        settingScreenSteps.userSeesHealthCardOrderContactScreen()
        settingScreenSteps.userSeesPossibilitiesHowCanBeOrdered("Webseite")
    }

    @Test
    fun ordering_new_EGKFromIntroScreen() {
        cardWallScreenSteps.fakeNFCCapabilities()
        mainScreenSteps.userTapsConnect()
        cardWallScreenSteps.userClicksOrderHealthCardFromCardWallIntroScreen()
        settingScreenSteps.userSeesAListOfInsurances()
        settingScreenSteps.userChoosesInsurance("AOK - Die Gesundheitskasse Hessen")
        settingScreenSteps.userSeesOrderOptionScreen()
        settingScreenSteps.userSeesPossibilitiesWhatCanBeOrdered("Karten & PIN, Nur PIN")
        settingScreenSteps.userSeesHealthCardOrderContactScreen()
        settingScreenSteps.userSeesPossibilitiesHowCanBeOrdered("Webseite")
    }

    @Test
    fun ordering_new_EGKFromCANScreen() {
        cardWallScreenSteps.fakeNFCCapabilities()
        mainScreenSteps.userTapsConnect()
        cardWallScreenSteps.userClicksOrderHealthCardFromCardWallCANScreen()
        settingScreenSteps.userSeesAListOfInsurances()
        settingScreenSteps.userChoosesInsurance("AOK - Die Gesundheitskasse Hessen")
        settingScreenSteps.userSeesOrderOptionScreen()
        settingScreenSteps.userSeesPossibilitiesWhatCanBeOrdered("Karten & PIN, Nur PIN")
        settingScreenSteps.userSeesHealthCardOrderContactScreen()
        settingScreenSteps.userSeesPossibilitiesHowCanBeOrdered("Webseite")
    }

    @Test
    fun ordering_new_EGKFromPINScreen() {
        cardWallScreenSteps.fakeNFCCapabilities()
        mainScreenSteps.userTapsConnect()
        cardWallScreenSteps.userClicksOrderHealthCardFromCardWallPinScreen()
        settingScreenSteps.userSeesAListOfInsurances()
        settingScreenSteps.userChoosesInsurance("AOK - Die Gesundheitskasse Hessen")
        settingScreenSteps.userSeesOrderOptionScreen()
        settingScreenSteps.userSeesPossibilitiesWhatCanBeOrdered("Karten & PIN, Nur PIN")
        settingScreenSteps.userSeesHealthCardOrderContactScreen()
        settingScreenSteps.userSeesPossibilitiesHowCanBeOrdered("Webseite")
    }

    @Test
    fun cancel_ordering_new_EGK() {
        mainScreenSteps.userTapsSettingsMenuButton()
        settingScreenSteps.userWantsToOrderNewCard()
        settingScreenSteps.userSeesAListOfInsurances()
        settingScreenSteps.userAbortsOrderingOfNewCard()
        settingScreenSteps.userSeesSettingsScreen()
    }
}
