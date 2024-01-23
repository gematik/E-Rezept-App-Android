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

package de.gematik.ti.erp.app.test.test.core.prescription

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.core.sleep
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.PrescriptionsScreen

class PrescriptionUtils(
    private val composeRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
    private val mainScreen: MainScreen,
    private val prescriptionsScreen: PrescriptionsScreen
) {
    fun loginWithVirtualHealthCardFromMainScreen() {
        mainScreen.userSeesMainScreen()
        composeRule.activity.testWrapper.loginWithVirtualHealthCard()
        composeRule.sleep(1_000L)
        prescriptionsScreen.refreshPrescriptions()
    }

    fun deleteAllPrescriptions() {
        composeRule.activity.testWrapper.deleteAllTasksSafe()
    }
}
