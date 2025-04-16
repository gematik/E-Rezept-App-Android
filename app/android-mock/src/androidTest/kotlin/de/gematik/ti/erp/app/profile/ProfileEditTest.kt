/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.profile

import de.gematik.ti.erp.app.UiTest
import de.gematik.ti.erp.app.testactions.actions.profile.ProfileMainScreenAction
import org.junit.Before
import org.junit.Test

class ProfileEditTest : UiTest() {

    private val actions = ProfileMainScreenAction(composeRule)

    @Before
    fun setup() {
        actions.skipOnboarding()
    }

    /**
     * change profile image as Symbols
     * */
    @Test
    fun symbolsAsProfileImage() {
        actions.symbolsAsProfileImage()
    }

    /**
     * change profile image
     * */
    @Test
    fun changeProfileImageTest() {
        actions.changeProfileImageTest()
    }

    /**
     * check avatar images
     * */
    @Test
    fun checkAvatarImageTest() {
        actions.checkAvatarImageTest()
    }

    /**
     * Delete profile picture
     * */
    @Test
    fun deleteAvatarTest() {
        actions.deleteAvatarTest()
    }

    /**
     * profile dialog text check
     * */
    @Test
    fun profileDialogTextTest() {
        actions.profileDialogTextTest()
    }
}
