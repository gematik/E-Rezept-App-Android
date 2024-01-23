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
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.awaitDisplay
import de.gematik.ti.erp.app.test.test.screens.OrderEgkScreen
import de.gematik.ti.erp.app.test.test.screens.SettingsScreen

class SettingScreenSteps(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    private val settingsScreen by lazy { SettingsScreen(composeRule) }
    private val orderEgkScreen by lazy { OrderEgkScreen(composeRule) }

    fun userWantsToOrderNewCard() {
        settingsScreen.tapOrderEgk()
        orderEgkScreen.userSeesOrderEgkScreenScreen()
    }

    fun userUsesLinkAboutNFC() {
        orderEgkScreen.tapNFCExplanationPageLink()
    }

    fun userIsNotInERezeptAppAnymore() {
        // TODO Wir sind im browser und wollen den URL prüfen. URL = "das-e-rezept-fuer-deutschland.de"
    }

    fun userSeesAListOfInsurances() {
        orderEgkScreen.userSeesOrderEgkScreenScreen()
        orderEgkScreen.checkIfAtLeastFourInsurerIsVisible()
    }

    fun userChoosesInsurance(insurance: String) {
        orderEgkScreen.chooseInsurance(insurance)
    }

    fun userSeesOrderOptionScreen() {
        composeRule.awaitDisplay(TestConfig.ScreenChangeTimeout, TestTag.Settings.OrderEgk.SelectOrderOptionScreen)
    }

    fun userSeesHealthCardOrderContactScreen() {
        composeRule.awaitDisplay(TestConfig.ScreenChangeTimeout, TestTag.Settings.OrderEgk.HealthCardOrderContactScreen)
    }

    fun userSeesPossibilitiesWhatCanBeOrdered(orderPossibility: String) {
        orderEgkScreen.checkOrderPossibilities(orderPossibility)
    }

    fun userSeesPossibilitiesHowCanBeOrdered(contactPossibility: String) {
        orderEgkScreen.checkContactPossibilities(contactPossibility)
    }

    fun userAbortsOrderingOfNewCard() {
        orderEgkScreen.tapOrderCardAbort()
    }

    fun userSeesSettingsScreen() {
        settingsScreen.userSeesSettingsScreen()
    }
}
