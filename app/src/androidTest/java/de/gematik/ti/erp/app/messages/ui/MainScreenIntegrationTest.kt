package de.gematik.ti.erp.app.messages.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.gematik.ti.erp.app.common.OnboardingHandler
import org.junit.Before
import org.junit.Test

@ExperimentalTestApi
class MainScreenIntegrationTest : OnboardingHandler() {

    @Before
    fun runOnBoarding() {
        handleOnBoarding()
    }

    @Test
    fun testBottomBar_navigationOptions() {
        composeTestRule.onNodeWithTag("erx_btn_prescriptions").assertIsDisplayed()
        composeTestRule.onNodeWithTag("erx_btn_messages").assertIsDisplayed()
        composeTestRule.onNodeWithTag("erx_btn_search_pharmacies").assertIsDisplayed()
    }

    @Test
    fun testClickBottomBar_clicksMessages() {
        composeTestRule.onNodeWithTag("erx_btn_messages")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag("message_screen").assertIsDisplayed()
    }

    @Test
    fun testClickBottomBar_clicksPrescriptions() {
        composeTestRule.onNodeWithTag("erx_btn_prescriptions")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag("main_screen").assertIsDisplayed()
    }

    @Test
    fun testClickBottomBar_clicksPharmacy() {
        composeTestRule.onNodeWithTag("erx_btn_search_pharmacies")
            .assertIsDisplayed()
            .performClick()
        // not working yet
//        composeTestRule.onNodeWithTag("pharmacy_search_screen").assertIsDisplayed()
    }

    @Test
    fun testClickSettings() {
        composeTestRule.onNodeWithTag("erx_btn_show_settings")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }

//    @Test
//    fun testLoadingPrescriptions() {
//        composeTestRule.onNodeWithTag("pull2refresh")
//            .assertIsDisplayed()
//            .performGesture {
//                swipeDown(endY = centerY)
//            }
//    }
//
//    @After
//    fun tearDown() {
//        mockWebServer.shutdown()
//    }
}
