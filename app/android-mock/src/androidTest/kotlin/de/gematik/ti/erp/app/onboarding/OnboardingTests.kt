/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.UiTest
import de.gematik.ti.erp.app.components.injectConfig
import de.gematik.ti.erp.app.config.TestScenario
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OnboardingTests : UiTest() {

    @Test
    fun onboardingNotDoneTest() {
        runTest {
            composeRule.injectConfig(
                applicationContext = context,
                config = TestScenario(isOnboardingDone = false)
            )
            composeRule.apply {
                onNodeWithTag(TestTag.Onboarding.WelcomeScreen)
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun onboardingDoneTest() {
        runTest {
            composeRule.injectConfig(
                applicationContext = context,
                config = TestScenario(isOnboardingDone = true)
            )
            composeRule.apply {
                onNodeWithTag("password_prompt_password_field")
                    .assertIsDisplayed()
                    .performTextInput("password")
                onNodeWithTag("password_prompt")
                    .assertIsDisplayed()
                    .performClick()
            }
        }
    }
}
