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
import de.gematik.ti.erp.app.test.test.screens.MainScreen

class MainScreenSteps(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    private val mainScreen by lazy { MainScreen(composeRule) }

    fun userTapsSettingsMenuButton() {
        mainScreen.tapSettingsButton()
    }

    fun createNewProfile(profileName: String) {
        userTapsAddProfileButton()
        mainScreen.enterProfileName(profileName)
        if ("" != profileName) {
            mainScreen.tapNewProfileConfirmButton()
            mainScreen.userSeesMainScreen()
        }
    }

    fun userTapsAddProfileButton() {
        mainScreen.tapAddProfileButton()
        mainScreen.userSeesBottomSheet()
    }

    fun userTapsAbort() {
        mainScreen.userSeesBottomSheet()
        mainScreen.tapCancelAddProfileButton()
        mainScreen.userClicksBottomBarPrescriptions()
    }

    fun userCantConfirmCreation() {
        mainScreen.userSeesBottomSheet()
        mainScreen.assertConfirmationCanNotBeClicked()
    }

    fun userTapsConnect() {
        mainScreen.tapLoginButton()
    }
}
