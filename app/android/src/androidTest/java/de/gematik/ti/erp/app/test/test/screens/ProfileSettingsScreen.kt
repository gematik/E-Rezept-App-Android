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
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class ProfileSettingsScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesProfileSettingsScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Profile.ProfileScreen)
    }
    fun userSeesDeleteProfileAlert(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.AlertDialog.Modal)
    }

    fun tapDisplayTokensButton() {
        onNodeWithTag(TestTag.Profile.ProfileScreenContent)
            .performScrollToNode(hasTestTag(TestTag.Profile.OpenTokensScreenButton))
        onNodeWithTag(TestTag.Profile.OpenTokensScreenButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun checkKVNRNotVisible() {
        onNodeWithTag(TestTag.Profile.InsuranceId)
            .assertDoesNotExist()
    }

    fun checkKVNRIsVisible() {
        onNodeWithTag(TestTag.Profile.InsuranceId)
            .assertIsDisplayed()
            .assert(!hasText(""))
    }

    fun tapLoginButton() {
        onNodeWithTag(TestTag.Profile.LoginButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun assertHintTextNotPresent() {
        onNodeWithTag(TestTag.Profile.TokenList.NoTokenInfo)
            .assertDoesNotExist()
    }

    fun tapThreeDotsMenuButton() {
        onNodeWithTag(TestTag.Profile.ThreeDotMenuButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapAuditEventsButton() {
        onNodeWithTag(TestTag.Profile.ProfileScreenContent)
            .performScrollToNode(hasTestTag(TestTag.Profile.OpenAuditEventsScreenButton))
        onNodeWithTag(TestTag.Profile.OpenAuditEventsScreenButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapLogoutButton() {
        onNodeWithTag(TestTag.Profile.LogoutButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun closeProfileSettings() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun tapDeleteProfile() {
        onNodeWithTag(TestTag.Profile.DeleteProfileButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapConfirmDeleteProfile() {
        onNodeWithTag(TestTag.AlertDialog.ConfirmButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapAbortDeleteButton() {
        onNodeWithTag(TestTag.AlertDialog.CancelButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun tapEditProfileNameButton() {
        onNodeWithTag(TestTag.Profile.EditProfileNameButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun enterNewProfileName(newProfileName: String) {
        onNodeWithTag(TestTag.Profile.NewProfileNameField)
            .assertIsDisplayed()
            .performClick()
            .performTextReplacement(newProfileName)
    }

    fun assertErrorMessageEmptyProfileName(errorMessage: String) {
        onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }

    fun tapEditProfileIconButton() {
        onNodeWithTag(TestTag.Profile.EditProfileImageButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun changeProfilePictureColor(color: String) {
        val colorToTestTag = mapOf(
            "grau" to TestTag.Profile.EditProfileIcon.ColorSelectorSpringGrayButton,
            "gelb" to TestTag.Profile.EditProfileIcon.ColorSelectorSunDewButton,
            "rosa" to TestTag.Profile.EditProfileIcon.ColorSelectorPinkButton,
            "grün" to TestTag.Profile.EditProfileIcon.ColorSelectorTreeButton,
            "blau" to TestTag.Profile.EditProfileIcon.ColorSelectorBlueMoonButton
        )

        val testTag = colorToTestTag[color.lowercase()]

        testTag?.let {
            onNodeWithTag(testTag)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
        }
    }

    fun tapBackToSettingsButton() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
}
