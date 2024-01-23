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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class CardWallScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesIntroScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.CardWall.Intro.IntroScreen)
    }
    fun userSeesPinScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.CardWall.PIN.PinScreen)
    }
    fun userSeesCANScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.CardWall.CAN.CANScreen)
    }

    // ****** LoginScreen ******
    fun continueWithEGK() {
        onNodeWithTag(TestTag.CardWall.ContinueButton)
            .assertIsDisplayed()
            .performClick()
    }

    // ****** CAN and PIN Screen ******
    fun enterCAN() {
        // CAN
        onNodeWithTag(TestTag.CardWall.CAN.CANField)
            .assertIsDisplayed()
            .performTextInput(TestConfig.DefaultEGKCAN)
        onNodeWithTag(TestTag.CardWall.ContinueButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun enterPin() {
        // PIN
        onNodeWithTag(TestTag.CardWall.PIN.PINField)
            .assertIsDisplayed()
            .performTextInput(TestConfig.DefaultEGKPassword)
        onNodeWithTag(TestTag.CardWall.ContinueButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun saveCredentials() {
        onNodeWithTag(TestTag.CardWall.StoreCredentials.Save)
            .assertIsDisplayed()
            .performClick()
        onNodeWithTag(TestTag.CardWall.SecurityAcceptance.AcceptButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun dontSaveCredentials() {
        onNodeWithTag(TestTag.CardWall.StoreCredentials.DontSave)
            .assertIsDisplayed()
            .performClick()
        onNodeWithTag(TestTag.CardWall.ContinueButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun userSeesNfcScreen() {
        onNodeWithTag(TestTag.CardWall.Nfc.NfcScreen)
            .assertIsDisplayed()
    }

    fun tapOrderEgkFromIntroScreen() {
        onNodeWithTag(TestTag.CardWall.Intro.OrderEgkButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun tapOrderEgkFromCANScreen() {
        onNodeWithTag(TestTag.CardWall.CAN.OrderEgkButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun tapOrderEgkFromPinScreen() {
        onNodeWithTag(TestTag.CardWall.PIN.OrderEgkButton)
            .assertIsDisplayed()
            .performClick()
    }
}
