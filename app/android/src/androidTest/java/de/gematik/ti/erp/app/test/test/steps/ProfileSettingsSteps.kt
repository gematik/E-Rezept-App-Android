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
import de.gematik.ti.erp.app.test.test.screens.AuditEventsScreen
import de.gematik.ti.erp.app.test.test.screens.CardWallScreen
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.ProfileSettingsScreen
import de.gematik.ti.erp.app.test.test.screens.ProfileTokenListScreen
import de.gematik.ti.erp.app.test.test.screens.SettingsScreen

class ProfileSettingsSteps(
    private val composeRule: ComposeTestRule
) : SemanticsNodeInteractionsProvider by composeRule {

    private val mainScreen by lazy { MainScreen(composeRule) }
    private val profileSettingsScreen by lazy { ProfileSettingsScreen(composeRule) }
    private val profileTokenListScreen by lazy { ProfileTokenListScreen(composeRule) }
    private val cardWallScreen by lazy { CardWallScreen(composeRule) }
    private val settingsScreen by lazy { SettingsScreen(composeRule) }
    private val auditEventsScreen by lazy { AuditEventsScreen(composeRule) }

    fun openProfileSettings() {
        mainScreen.tapSettingsButton()
        settingsScreen.goToFirstProfileDetails()
        profileSettingsScreen.userSeesProfileSettingsScreen()
    }

    fun openProfileSettingsForCertainProfile(profileIndex: Int) {
        mainScreen.userSeesMainScreen()
        // to keep scenarios human-readable, we'll hide the fact, that the index actually starts at zero ¯\_(ツ)_/¯
        mainScreen.tapSettingsButton()
        settingsScreen.goToProfileDetails(profileIndex - 1)
    }

    fun checkNoTokenPresent() {
        profileSettingsScreen.tapDisplayTokensButton()
        profileTokenListScreen.userSeesProfileTokenListScreen()
        profileTokenListScreen.checkTokensDoNotExist()
    }

    fun checkTokenHintPresent() {
        profileTokenListScreen.assertHeaderTextNotEmpty()
        profileTokenListScreen.assertInfoTextNotEmpty()
    }

    fun checkTokenHintPresent(text: String) {
        profileSettingsScreen.tapDisplayTokensButton()
        profileTokenListScreen.assertInfoTextPresent(text)
    }

    fun checkNoKVNRIsVisible() {
        profileSettingsScreen.checkKVNRNotVisible()
    }

    fun checkKVNRIsVisible() {
        profileSettingsScreen.checkKVNRIsVisible()
    }

    fun tapLoginButton() {
        profileSettingsScreen.tapThreeDotsMenuButton()
        profileSettingsScreen.tapLoginButton()
    }

    fun userSeesCardwallWelcomeScreen() {
        cardWallScreen.userSeesIntroScreen()
    }

    fun checkTokenPresent() {
        profileSettingsScreen.tapDisplayTokensButton()
        profileTokenListScreen.userSeesProfileTokenListScreen()
        profileTokenListScreen.checkAccessTokenPresent()
        profileTokenListScreen.checkSSOTokenPresent()
        profileTokenListScreen.closeTokenList()
    }

    fun hintTextNotPresent() {
        profileSettingsScreen.tapDisplayTokensButton()
        profileSettingsScreen.assertHintTextNotPresent()
    }

    fun logoutViaProfileSettings() {
        mainScreen.tapSettingsButton()
        settingsScreen.goToFirstProfileDetails()
        profileSettingsScreen.tapThreeDotsMenuButton()
        profileSettingsScreen.tapLogoutButton()
        profileSettingsScreen.closeProfileSettings()
    }

    fun noTokenPresentInProfileSettings() {
        mainScreen.tapSettingsButton()
        settingsScreen.goToFirstProfileDetails()
        profileSettingsScreen.tapDisplayTokensButton()
        profileTokenListScreen.checkTokensDoNotExist()
        profileTokenListScreen.closeTokenList()
    }

    fun userSeesAuditEventsScreen(profileIndex: Int) {
        mainScreen.userSeesMainScreen()
        mainScreen.tapSettingsButton()
        // -1 durch Differenzen zum normalen Sprachgebrauch
        settingsScreen.goToProfileDetails(profileIndex - 1)
        profileSettingsScreen.tapAuditEventsButton()
        auditEventsScreen.userSeesAuditEventsScreen()
    }

    fun userSeesEmptyStateForCertainProfile() {
        auditEventsScreen.userSeesAuditEventsScreen()
        auditEventsScreen.checkAuditEventsDoNotExist()
    }

    fun userLogsOutOffProfile(index: Int) {
        mainScreen.tapSettingsButton()
        settingsScreen.goToProfileDetails(index - 1)
        profileSettingsScreen.tapThreeDotsMenuButton()
        profileSettingsScreen.tapLogoutButton()
        profileSettingsScreen.closeProfileSettings()
        mainScreen.userSeesMainScreen()
    }

    fun userDoesNotSeesEmptyStateForCertainProfile() {
        auditEventsScreen.userSeesAuditEventsScreen()
        auditEventsScreen.checkNoAuditEventsHeaderAndInfoDoesNotExist()
    }

    fun userHasNumberOfProfiles(numberOfProfiles: Int) {
        mainScreen.tapSettingsButton()
        settingsScreen.assertAmountOfProfiles(numberOfProfiles)
        mainScreen.userClicksBottomBarPrescriptions()
        mainScreen.userSeesMainScreen()
    }

    fun userDeletesProfile(profileName: String) {
        mainScreen.tapSettingsButton()
        settingsScreen.tapProfileWithName(profileName)
        profileSettingsScreen.userSeesProfileSettingsScreen()
        profileSettingsScreen.tapThreeDotsMenuButton()
        profileSettingsScreen.tapDeleteProfile()
        profileSettingsScreen.tapConfirmDeleteProfile()
    }

    fun userHasProfilesWithName(numberOfProfiles: Int, profileNames: String) {
        mainScreen.tapSettingsButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.checkAmountOfProfilesWithNames(numberOfProfiles, profileNames)
    }

    fun createProfileAfterLastOneWasDeleted() {
        settingsScreen.userSeesCreateNewProfileAlert()
        settingsScreen.enterProfileName("Profil 1")
        settingsScreen.tapNewProfileConfirmButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.tapReturnToPrescriptionScreen()
        mainScreen.userSeesMainScreen()
    }

    fun userInterruptsDeletingProfile(profileName: String) {
        navigateToProfileWithName(profileName)
        profileSettingsScreen.userSeesProfileSettingsScreen()
        profileSettingsScreen.tapThreeDotsMenuButton()
        profileSettingsScreen.tapDeleteProfile()
        profileSettingsScreen.tapAbortDeleteButton()
        profileSettingsScreen.tapBackToSettingsButton()
    }

    fun editProfileName(profileName: String, newProfileName: String) {
        navigateToProfileWithName(profileName)
        profileSettingsScreen.tapEditProfileNameButton()
        profileSettingsScreen.enterNewProfileName(newProfileName)
    }

    fun assertErrorMessageEmptyProfileName(errorMessage: String) {
        profileSettingsScreen.assertErrorMessageEmptyProfileName(errorMessage)
    }

    fun userChangesProfilePictureOfProfileFromColorTo(profileName: String, color: String) {
        navigateToProfileWithName(profileName)
        profileSettingsScreen.tapEditProfileIconButton()
        profileSettingsScreen.changeProfilePictureColor(color)
        profileSettingsScreen.tapBackToSettingsButton()
    }

    private fun navigateToProfileWithName(profileName: String) {
        mainScreen.tapSettingsButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.tapProfileWithName(profileName)
    }
}
