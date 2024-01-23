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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.VirtualEgk
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class DebugMenuScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesDebugMenuScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.DebugMenu.DebugMenuScreen)
    }

    fun waitTillVirtualHealthCardIsSet() {
        composeRule.awaitDisplay(10000) {
            onNodeWithTag(TestTag.DebugMenu.SetVirtualHealthCardButton)
                .assertIsNotEnabled()
        }
        composeRule.awaitDisplay(10000) {
            onNodeWithTag(TestTag.DebugMenu.SetVirtualHealthCardButton)
                .assertIsEnabled()
        }
    }

    fun tapSetVirtualCard() {
        onNodeWithTag(TestTag.DebugMenu.DebugMenuContent)
            .performScrollToNode(hasTestTag(TestTag.DebugMenu.SetVirtualHealthCardButton))
        onNodeWithTag(TestTag.DebugMenu.SetVirtualHealthCardButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        waitTillVirtualHealthCardIsSet()
    }

    fun closeDebugMenu() {
        onNodeWithTag(TestTag.TopNavigation.CloseButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun fillCustomCertificateAndPrivateKey(virtualEgk: VirtualEgk) {
        fillCertificateFieldWith(virtualEgk.certificate)
        fillPrivateKeyFieldWith(virtualEgk.privateKey)
    }

    fun fillCertificateFieldWith(certificateString: String) {
        onNodeWithTag(TestTag.DebugMenu.DebugMenuContent)
            .performScrollToNode(hasTestTag(TestTag.DebugMenu.CertificateField))
        onNodeWithTag(TestTag.DebugMenu.CertificateField)
            .assertIsDisplayed()
            .performTextReplacement(certificateString)
    }

    fun fillPrivateKeyFieldWith(privateKeyString: String) {
        onNodeWithTag(TestTag.DebugMenu.DebugMenuContent)
            .performScrollToNode(hasTestTag(TestTag.DebugMenu.PrivateKeyField))
        onNodeWithTag(TestTag.DebugMenu.PrivateKeyField)
            .assertIsDisplayed()
            .performTextReplacement(privateKeyString)
    }

    fun tapFakeNFCCapabilitiesSwitch() {
        onNodeWithTag(TestTag.DebugMenu.DebugMenuContent)
            .performScrollToNode(hasTestTag(TestTag.DebugMenu.FakeNFCCapabilities))

        onNodeWithTag(TestTag.DebugMenu.FakeNFCCapabilities)
            .assertIsDisplayed()
            .assertIsToggleable()
            .performClick()
    }
}
