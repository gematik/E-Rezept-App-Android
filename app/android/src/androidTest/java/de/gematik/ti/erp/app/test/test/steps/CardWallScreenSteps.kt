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
import androidx.test.platform.app.InstrumentationRegistry
import de.gematik.ti.erp.app.test.test.VirtualEgk1
import de.gematik.ti.erp.app.test.test.VirtualEgkWithPrescription
import de.gematik.ti.erp.app.test.test.screens.CardWallScreen
import de.gematik.ti.erp.app.test.test.screens.DebugMenuScreen
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.SettingsScreen

class CardWallScreenSteps(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    private val mainScreen by lazy { MainScreen(composeRule) }
    private val cardWallScreen by lazy { CardWallScreen(composeRule) }
    private val settingsScreen by lazy { SettingsScreen(composeRule) }
    private val debugMenuScreen by lazy { DebugMenuScreen(composeRule) }

    fun userStartsAndFinishsTheCardwallSuccessfully() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc nfc enable")

        mainScreen.userSeesMainScreen()
        mainScreen.refreshMainScreenBySwipe()
        mainScreen.tapLoginButton()

        cardWallScreen.continueWithEGK()
        cardWallScreen.enterCAN()
        cardWallScreen.enterPin()
        cardWallScreen.dontSaveCredentials()
        cardWallScreen.userSeesNfcScreen()

        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc nfc disable")
        Thread.sleep(2000)
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc nfc enable")

        mainScreen.userSeesMainScreen(15000)
    }

    fun userStartsAndFinishsTheCardwallWithVirtualCardSuccessfully() {
        mainScreen.userSeesMainScreen()
        mainScreen.tapSettingsButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.tapDebugMenuButton()
        debugMenuScreen.userSeesDebugMenuScreen()
        debugMenuScreen.tapSetVirtualCard()
        debugMenuScreen.closeDebugMenu()
        settingsScreen.tapReturnToPrescriptionScreen()
        mainScreen.userSeesMainScreen()
    }

    fun fakeNFCCapabilities() {
        mainScreen.userSeesMainScreen()
        mainScreen.tapSettingsButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.tapDebugMenuButton()
        debugMenuScreen.userSeesDebugMenuScreen()
        debugMenuScreen.tapFakeNFCCapabilitiesSwitch()
        debugMenuScreen.closeDebugMenu()
        settingsScreen.tapReturnToPrescriptionScreen()
        mainScreen.userSeesMainScreen()
    }

    fun setCustomEgk() {
        // only for demonstration purposes
        mainScreen.userSeesMainScreen()
        mainScreen.tapSettingsButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.tapDebugMenuButton()
        debugMenuScreen.userSeesDebugMenuScreen()
        debugMenuScreen.fillCustomCertificateAndPrivateKey(VirtualEgk1)
        debugMenuScreen.tapSetVirtualCard()
        debugMenuScreen.closeDebugMenu()
        settingsScreen.tapReturnToPrescriptionScreen()
        mainScreen.userSeesMainScreen()
    }

    fun setVirtualEGKWithPrescriptions() {
        // only for demonstration purposes
        mainScreen.userSeesMainScreen()
        mainScreen.tapSettingsButton()
        settingsScreen.userSeesSettingsScreen()
        settingsScreen.tapDebugMenuButton()
        debugMenuScreen.userSeesDebugMenuScreen()
        debugMenuScreen.fillCustomCertificateAndPrivateKey(VirtualEgkWithPrescription)
        debugMenuScreen.tapSetVirtualCard()
        debugMenuScreen.closeDebugMenu()
        settingsScreen.tapReturnToPrescriptionScreen()
        mainScreen.userSeesMainScreen()
    }

    fun userClicksOrderHealthCardFromCardWallIntroScreen() {
        cardWallScreen.userSeesIntroScreen()
        cardWallScreen.tapOrderEgkFromIntroScreen()
    }

    fun userClicksOrderHealthCardFromCardWallCANScreen() {
        cardWallScreen.userSeesIntroScreen()
        cardWallScreen.continueWithEGK()
        cardWallScreen.userSeesCANScreen()
        cardWallScreen.tapOrderEgkFromCANScreen()
    }

    fun userClicksOrderHealthCardFromCardWallPinScreen() {
        cardWallScreen.userSeesIntroScreen()
        cardWallScreen.continueWithEGK()
        cardWallScreen.userSeesCANScreen()
        cardWallScreen.enterCAN()
        cardWallScreen.userSeesPinScreen()
        cardWallScreen.tapOrderEgkFromPinScreen()
    }
}
