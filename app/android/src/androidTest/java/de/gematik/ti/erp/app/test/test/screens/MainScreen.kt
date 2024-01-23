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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class MainScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesMainScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Main.MainScreen)
    }

    fun userSeesBottomSheet(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Main.MainScreenBottomSheet.Modal)
        onNodeWithTag(TestTag.Main.MainScreenBottomSheet.Modal).performTouchInput { swipeUp() }
    }

    fun checkProfileHasState(profileName: String, profileState: String) {
        onNodeWithText(profileName, substring = true)
            .assertIsDisplayed()
        onNodeWithText(profileState, substring = true)
            .assertIsDisplayed()
    }

    fun refreshMainScreenBySwipe() {
        onNodeWithTag(TestTag.Main.MainScreen)
            .assertIsDisplayed()
            .performTouchInput { swipeDown() }
    }

    fun tapLoginButton() {
        onNodeWithTag(TestTag.Main.LoginButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun tapConnectLater() {
        userSeesBottomSheet(5000)
        onNodeWithTag(TestTag.Main.MainScreenBottomSheet.ConnectLaterButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapTooltips() {
        onRoot().performClick()
        onRoot().performClick()
        onRoot().performClick()
        onRoot().performClick()
        onRoot().performClick()
        onRoot().performClick()
        onRoot().performClick()
    }

    fun tapSettingsButton() {
        onNodeWithTag(TestTag.BottomNavigation.SettingsButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun userClicksBottomBarPrescriptions() {
        onNodeWithTag(TestTag.BottomNavigation.PrescriptionButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun userClicksBottomBarPharmacy() {
        onNodeWithTag(TestTag.BottomNavigation.PharmaciesButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun userClicksPharmacySearchBar() {
        onNodeWithTag(TestTag.PharmacySearch.TextSearchButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        TestConfig.ScreenChangeTimeout
    }

    fun userClicksBottomBarOrders() {
        onNodeWithTag(TestTag.BottomNavigation.OrdersButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapAddProfileButton() {
        onNodeWithTag(TestTag.Main.AddProfileButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun enterProfileName(name: String) {
        onNodeWithTag(TestTag.Main.MainScreenBottomSheet.ProfileNameField)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(name)
    }

    fun tapNewProfileConfirmButton() {
        userSeesBottomSheet(5000L)
        onNodeWithTag(TestTag.Main.MainScreenBottomSheet.SaveProfileNameButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapCancelAddProfileButton() {
        userSeesBottomSheet()
        onNodeWithTag(TestTag.Main.LoginButton) // BottomSheet has no CancelButton
            .performClick()
    }

    fun assertConfirmationCanNotBeClicked() {
        onNodeWithTag(TestTag.Main.MainScreenBottomSheet.SaveProfileNameButton)
            .assertIsNotEnabled()
    }

    /**
     * cancel user login
     * click-though all tool-tips
     * click on pharmacies bottom button
     */
    fun openPharmaciesFromBottomBarFromStart() {
        tapConnectLater()
        tapTooltips()
        userClicksBottomBarPharmacy()
    }
}
