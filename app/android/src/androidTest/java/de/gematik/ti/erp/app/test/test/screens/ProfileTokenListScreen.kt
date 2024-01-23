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
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.assertHasText
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class ProfileTokenListScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesProfileTokenListScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Profile.TokenList.TokenScreen)
    }

    fun checkTokensDoNotExist() {
        onNodeWithTag(TestTag.Profile.TokenList.AccessToken)
            .assertDoesNotExist()
        onNodeWithTag(TestTag.Profile.TokenList.SSOToken)
            .assertDoesNotExist()
    }

    fun checkAccessTokenPresent() {
        onNodeWithTag(TestTag.Profile.TokenList.AccessToken)
            .assertIsDisplayed()
            .assertHasText()
    }

    fun checkSSOTokenPresent() {
        onNodeWithTag(TestTag.Profile.TokenList.SSOToken)
            .assertIsDisplayed()
            .assertHasText()
    }

    fun assertHeaderTextNotEmpty() {
        onNodeWithTag(TestTag.Profile.TokenList.NoTokenHeader)
            .assertIsDisplayed()
            .assertHasText()
    }

    fun assertInfoTextNotEmpty() {
        onNodeWithTag(TestTag.Profile.TokenList.NoTokenInfo)
            .assertIsDisplayed()
            .assertHasText()
    }

    fun assertInfoTextPresent(text: String) {
        onNodeWithTag(TestTag.Profile.TokenList.NoTokenInfo)
            .assertIsDisplayed()
            .assertTextContains(text, substring = true)
    }

    fun closeTokenList() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .performClick()
    }
}
