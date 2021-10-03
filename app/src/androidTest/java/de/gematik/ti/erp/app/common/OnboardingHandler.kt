package de.gematik.ti.erp.app.common

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import de.gematik.ti.erp.app.MainActivity
import org.junit.Before
import org.junit.Rule

/**
 * BaseIntegrationTest handles OnBoarding in case it is needed
 */
open class OnboardingHandler {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun handleOnBoarding() {

        if (awaitDisplay(
                5000L,
                "onboarding/welcome",
                "erx_btn_messages"
            ) == "onboarding/welcome"
        ) {
            onBoarding()
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

    fun performClickOnOnboardingNextButton() {
        composeTestRule.onNodeWithTag("onboarding/next")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    private fun awaitDisplay(timeout: Long, vararg tags: String): String {
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
}
