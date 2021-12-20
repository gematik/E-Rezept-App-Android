/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.centerY
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.IdlingPolicies
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class FullIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        IdlingPolicies.setIdlingResourceTimeout(50, TimeUnit.SECONDS)
        IdlingPolicies.setMasterPolicyTimeout(50, TimeUnit.SECONDS)
    }

    fun performClickOnOnboardingNextButton() {
        composeTestRule.onNodeWithTag("onboarding/next")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun performClickOnCardWallNextButton() {
        composeTestRule.onNodeWithTag("cardWall/next")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun awaitDisplay(timeout: Long, node: () -> SemanticsNodeInteraction) {
        val t0 = System.currentTimeMillis()
        do {
            try {
                node().assertIsDisplayed()
                return
            } catch (_: AssertionError) {
            }
            composeTestRule.mainClock.advanceTimeByFrame()
            Thread.sleep(100)
        } while (System.currentTimeMillis() - t0 < timeout)
        throw AssertionError(
            "Node was not displayed after $timeout milliseconds. Root node was:\n${
            composeTestRule.onRoot().printToString(Int.MAX_VALUE)
            }"
        )
    }

    fun awaitDisplay(timeout: Long, vararg tags: String): String {
        val t0 = System.currentTimeMillis()
        do {
            tags.forEach { tag ->
                try {
                    composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
                    return tag
                } catch (_: AssertionError) {
                }
            }
            composeTestRule.mainClock.advanceTimeByFrame()
            Thread.sleep(100)
        } while (System.currentTimeMillis() - t0 < timeout)
        throw AssertionError(
            "Node was not displayed after $timeout milliseconds. Root node was:\n${
            composeTestRule.onRoot().printToString(Int.MAX_VALUE)
            }"
        )
    }

    @Test
    fun testEmptyMessages_showsEmptyScreen() {

        composeTestRule.mainClock.autoAdvance = true

        val foundStartupTag = awaitDisplay(
            5000L,
            "onboarding/welcome",
            "pull2refresh",
        )

        when (foundStartupTag) {
            "onboarding/welcome" -> {
                onBoarding()
                prescriptionsRefresh()
                cardWall()
            }
            "pull2refresh" -> {
                prescriptionsRefresh()
                cardWall()
            }
        }
    }

    private fun onBoarding() {
        composeTestRule.onNodeWithTag("onboarding/welcome")
            .assertIsDisplayed()

        performClickOnOnboardingNextButton()

        composeTestRule.onNodeWithTag("onboarding/features").assertIsDisplayed()

        performClickOnOnboardingNextButton()

        composeTestRule.onNodeWithTag("onboarding/secureAppPage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding/secure_text_input_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding/secure_text_input_1").performTextInput("a")
        composeTestRule.onNodeWithTag("onboarding/secure_text_input_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding/secure_text_input_2").performTextInput("a")

        performClickOnOnboardingNextButton()

        composeTestRule.onNodeWithTag("onboarding/analytics").assertIsDisplayed()

        performClickOnOnboardingNextButton()

        composeTestRule.onNodeWithTag("onboarding/terms").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding/next")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertIsNotEnabled()

        composeTestRule.onNodeWithTag("onb_btn_accept_privacy").assertIsDisplayed()
            .assertHasClickAction()
            .assertIsToggleable().performClick()

        composeTestRule.onNodeWithTag("onb_btn_accept_terms_of_use").assertIsDisplayed()
            .assertHasClickAction()
            .assertIsToggleable().performClick()

        performClickOnOnboardingNextButton()
    }

    @OptIn(ExperimentalTestApi::class)
    fun prescriptionsRefresh() {
        composeTestRule.onNodeWithTag("pull2refresh")
            .assertIsDisplayed()
            .performGesture {
                swipeDown(endY = centerY)
            }
    }

    fun cardWall() {
        val foundCardWallTag = awaitDisplay(
            5000L,
            "cardWall/intro",
            "cardWall/cardAccessNumber",
            "cardWall/personalIdentificationNumber"
        )

        when (foundCardWallTag) {
            "cardWall/intro" -> {
                performClickOnCardWallNextButton()
                cardAccessNumber()
                personalIdentificationNumber()
                authenticationSelection()
                authentication()
            }
            "cardWall/cardAccessNumber" -> {
                cardAccessNumber()
                personalIdentificationNumber()
                authenticationSelection()
                authentication()
            }
            "cardWall/personalIdentificationNumber" -> {
                personalIdentificationNumber()
                authentication()
            }
        }
    }

    fun cardAccessNumber() {
        composeTestRule.onNodeWithTag("cardWall/next")
            .assertIsNotEnabled()

        composeTestRule.onNodeWithTag("cardWall/cardAccessNumberInputField")
            .assertIsDisplayed()
            .performClick()
            .performTextInput("123123")

        performClickOnCardWallNextButton()
    }

    fun personalIdentificationNumber() {
        composeTestRule.onNodeWithTag("cardWall/next")
            .assertIsNotEnabled()

        composeTestRule.onNodeWithTag("cardWall/personalIdentificationNumberInputField")
            .assertIsDisplayed()
            .performClick()
            .performTextInput("123456")

        performClickOnCardWallNextButton()
    }

    fun authenticationSelection() {

        val tag = awaitDisplay(
            5000L,
            "cardWall/authenticationSelection",
            "cardWall/authentication",
        )

        if (tag == "cardWall/authenticationSelection") {
            composeTestRule.onNodeWithTag("cardWall/authenticationSelection").assertIsDisplayed()

            composeTestRule.onNodeWithTag("cardWall/next")
                .assertIsNotEnabled()

            composeTestRule.onNodeWithTag("cardWall/authenticationSelection/healthCard")
                .assertIsDisplayed()
                .performClick()

            performClickOnCardWallNextButton()
        }
    }

    fun authentication() {
        composeTestRule.onNodeWithTag("cardWall/authentication").assertIsDisplayed()

        awaitDisplay(20_000L) {
            composeTestRule.onNodeWithTag("cardWall/outro")
        }

        performClickOnCardWallNextButton()
    }
}
