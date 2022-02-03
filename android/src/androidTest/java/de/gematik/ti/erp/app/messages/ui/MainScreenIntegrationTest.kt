/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.messages.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.gematik.ti.erp.app.common.OnboardingHandler
import org.junit.Test

@ExperimentalTestApi
class MainScreenIntegrationTest : OnboardingHandler() {

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
