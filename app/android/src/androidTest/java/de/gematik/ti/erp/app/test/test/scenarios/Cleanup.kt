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

package de.gematik.ti.erp.app.test.test.scenarios

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.core.prescription.PrescriptionUtils
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.OnboardingScreen
import de.gematik.ti.erp.app.test.test.screens.PrescriptionsScreen
import org.junit.Assume
import org.junit.Rule
import org.junit.Test

class Cleanup {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val onboardingScreen by lazy { OnboardingScreen(composeRule) }
    private val mainScreen by lazy { MainScreen(composeRule) }
    private val prescriptionsScreen by lazy { PrescriptionsScreen(composeRule) }
    private val prescriptionUtils by lazy { PrescriptionUtils(composeRule, mainScreen, prescriptionsScreen) }

    @Test
    fun not_a_test_cleanup() {
        onboardingScreen.tapSkipOnboardingButton()
        mainScreen.tapConnectLater()
        mainScreen.tapTooltips()
        prescriptionUtils.loginWithVirtualHealthCardFromMainScreen()
        prescriptionsScreen.awaitPrescriptions()
        prescriptionUtils.deleteAllPrescriptions()
        Assume.assumeTrue(false)
    }
}
