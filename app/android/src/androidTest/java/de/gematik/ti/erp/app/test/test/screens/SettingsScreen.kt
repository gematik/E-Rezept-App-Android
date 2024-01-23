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

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.awaitDisplay
import junit.framework.TestCase.assertTrue

class SettingsScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesSettingsScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Settings.SettingsScreen)
    }
    fun userSeesCreateNewProfileAlert(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Settings.AddProfileDialog.Modal)
    }

    private val profileSettingsScreen by lazy { ProfileSettingsScreen(composeRule) }

    fun goToFirstProfileDetails() {
        goToProfileDetails(0)
    }

    fun goToProfileDetails(index: Int) {
        tapProfileDetailsButton(index)
        profileSettingsScreen.userSeesProfileSettingsScreen()
    }

    fun assertAmountOfProfiles(numberOfProfiles: Int) {
        assertTrue(
            onAllNodesWithTag(TestTag.Settings.ProfileButton)
                .fetchSemanticsNodes().size == numberOfProfiles
        )
    }

    fun tapDebugMenuButton() {
        onNodeWithTag(TestTag.Settings.DebugMenuButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun enterProfileName(name: String) {
        onNodeWithTag(TestTag.Settings.AddProfileDialog.ProfileNameTextField)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(name)
    }

    fun tapNewProfileConfirmButton() {
        onNodeWithTag(TestTag.Settings.AddProfileDialog.ConfirmButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapReturnToPrescriptionScreen() {
        onNodeWithTag(TestTag.BottomNavigation.PrescriptionButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapOrderEgk() {
        onNodeWithTag(TestTag.Settings.OrderNewCardButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapProfileWithName(profileName: String) {
        onNodeWithText(profileName)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapProfileDetailsButton(profileNumber: Int) {
        onAllNodesWithTag(TestTag.Settings.ProfileButton)[profileNumber]
            .assertIsDisplayed()
            .performClick()
    }

    fun checkAmountOfProfilesWithNames(numberOfProfiles: Int, profileNames: String) {
        assert(onAllNodesWithText(profileNames).fetchSemanticsNodes().size == numberOfProfiles)
    }
}
