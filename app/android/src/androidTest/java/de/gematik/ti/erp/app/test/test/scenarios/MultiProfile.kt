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
import androidx.test.filters.MediumTest
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.steps.MainScreenSteps
import de.gematik.ti.erp.app.test.test.steps.OnboardingSteps
import de.gematik.ti.erp.app.test.test.steps.ProfileSettingsSteps
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
class MultiProfile(fontScale: String) : WithFontScale(fontScale) {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingSteps by lazy { OnboardingSteps(composeRule) }
    private val mainScreenSteps by lazy { MainScreenSteps(composeRule) }

    private val profileSettingSteps by lazy { ProfileSettingsSteps(composeRule) }

    @Before
    fun skipsOnboarding() {
        onboardingSteps.userSkipsOnboarding()
    }

    @Test
    fun create_second_profile_and_delete_first_one() {
        mainScreenSteps.createNewProfile("Herbert Hepatitis B")
        profileSettingSteps.userHasNumberOfProfiles(2)
        profileSettingSteps.userDeletesProfile("Profil 1")
        profileSettingSteps.userHasProfilesWithName(1, "Herbert Hepatitis B")
    }

    @Test
    fun delete_last_profile() {
        mainScreenSteps.userTapsSettingsMenuButton()
        profileSettingSteps.userDeletesProfile("Profil 1")
        profileSettingSteps.createProfileAfterLastOneWasDeleted()
        profileSettingSteps.userHasProfilesWithName(1, "Profil 1")
    }

    @Test
    fun abort_delete_last_profile() {
        profileSettingSteps.userInterruptsDeletingProfile("Profil 1")
        profileSettingSteps.userHasProfilesWithName(1, "Profil 1")
    }

    @Test
    fun abort_create_new_profile() {
        mainScreenSteps.userTapsAddProfileButton()
        mainScreenSteps.userCantConfirmCreation()
        mainScreenSteps.userTapsAbort()
    }

    @Test
    fun create_profile_without_name() {
        mainScreenSteps.userTapsAddProfileButton()
        mainScreenSteps.userCantConfirmCreation()
        mainScreenSteps.userTapsAbort()
        profileSettingSteps.userHasNumberOfProfiles(1)
    }

    @Test
    fun delete_name_of_a_profile() {
        profileSettingSteps.editProfileName("Profil 1", "")
        profileSettingSteps.assertErrorMessageEmptyProfileName("Das Namensfeld darf nicht leer sein.")
    }
}
